package com.example.demo.Controller;

import com.example.demo.Config.auth.jwt.JWTProperties;
import com.example.demo.Config.auth.jwt.JWTTokenProvider;
import com.example.demo.Config.auth.jwt.TokenInfo;
import com.example.demo.Config.auth.redis.RedisUtil;
import com.example.demo.Domain.Common.Dtos.UserDTO;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Domain.Common.Repository.JwtTokenRepository;
import com.example.demo.Domain.Common.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping(value = "/join",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> join_post(@RequestBody UserDTO userDto){
        log.info("POST /join..."+userDto);

        //dto->entity
        User user = User.builder()
                .username(userDto.getUsername())
                .password( passwordEncoder.encode(userDto.getPassword())  )
                .role("ROLE_USER")
                .build();

        // save entity to DB
        userRepository.save(user);

        //
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }
    //Header 방식 (Authorization: Bearer <token>)
    // - XXS 공격에 매우취약 - LocalStorage / SessionStorage에 저장시 문제 발생
    // - 쿠키방식이 비교적 안전
    @PostMapping(value = "/login" , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> login(@RequestBody UserDTO userDto, HttpServletResponse resp) throws IOException {
        log.info("POST /login..." + userDto);
        Map<String, Object> response = new HashMap<>(); // 데이터 주기 위한 작업

        try{
            //사용자 인증 시도(ID/PW 일치여부 확인)
            //확인하고 나온 결과가 authentication으로 반환됨
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(userDto.getUsername(),userDto.getPassword()) //Token 객체로 받아서 PW 인증
                    );
            System.out.println("인증성공 : " + authentication);

            //Token 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
            System.out.println("JWT TOKEN : " + tokenInfo);

            //REDIS 에 REFRESH 저장
            redisUtil.save("RT:"+authentication.getName() , tokenInfo.getRefreshToken());
            //
            response.put("state","success");
            response.put("message","인증성공!");

            //---------------------------------------------
            //access token은 cookie화해서 보안 적용 - JS에서 함부로 접근하지 못하도록 처리
            Cookie accessCookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken());
            accessCookie.setHttpOnly(true); //쿠키보안관련 처리 - FN 수준에서의 쿠키 전달 시 접근 제한 (필수!)
            accessCookie.setSecure(false); // Only for HTTPS - 쿠키를 전달할 때 인증서 기반 접근 허용
            accessCookie.setPath("/"); // Define valid paths
            accessCookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 1 hour expiration

            // Set refresh-token as HTTP-only cookie
//            Cookie refreshCookie = new Cookie(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, tokenInfo.getRefreshToken());
//            refreshCookie.setHttpOnly(true);
//            accessCookie.setSecure(false); // Only for HTTPS
//            refreshCookie.setPath("/");
//            refreshCookie.setMaxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME); // 7 days expiration

            Cookie userCookie = new Cookie("username", authentication.getName());
            userCookie.setHttpOnly(true);
            accessCookie.setSecure(false); // Only for HTTPS
            userCookie.setPath("/");
            userCookie.setMaxAge(JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME); // 7 days expiration

            resp.addCookie(accessCookie);
//            resp.addCookie(refreshCookie);
            resp.addCookie(userCookie);
            //---------------------------------------------
        }catch(AuthenticationException e){
            System.out.println("인증실패 : " + e.getMessage());
            response.put("state","fail");
            response.put("message",e.getMessage());
            return new ResponseEntity(response,HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity< Map<String,Object> > user(HttpServletRequest request, Authentication authentication) {
        log.info("GET /user..." + authentication);
        log.info("name..." + authentication.getName());

        Optional<User> userOptional =  userRepository.findById(authentication.getName());
        Map<String, Object> response = new HashMap<>();

        if(userOptional.isPresent()){
            User user = userOptional.get();
            response.put("username",user.getUsername());
            response.put("role",user.getRole());

            return new ResponseEntity<>(response , HttpStatus.OK);
        }
        return new ResponseEntity<>(null , HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("GET /validate ... authentication : " + authentication);
        Collection<? extends GrantedAuthority> auth =  authentication.getAuthorities();
        auth.forEach(System.out::println);
        boolean hasRoleAnon = auth.stream()
                .anyMatch(authority -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));

        if (authentication.isAuthenticated() && !hasRoleAnon) {
            System.out.println("인증된 상태입니다.");
            return new ResponseEntity<>("",HttpStatus.OK);
        }

        System.out.println("미인증된 상태입니다.");
        return new ResponseEntity<>("",HttpStatus.UNAUTHORIZED);
    }
}
