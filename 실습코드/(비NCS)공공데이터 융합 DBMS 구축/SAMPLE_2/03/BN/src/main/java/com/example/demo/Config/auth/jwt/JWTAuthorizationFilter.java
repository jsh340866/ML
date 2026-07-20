package com.example.demo.Config.auth.jwt;

import com.example.demo.Config.auth.redis.RedisUtil;
import com.example.demo.Domain.Common.Entity.JwtToken;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Domain.Common.Repository.JwtTokenRepository;
import com.example.demo.Domain.Common.Repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    JWTTokenProvider jwtTokenProvider;

    @Autowired
    JwtTokenRepository jwtTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JWTAuthorizationFilter's doFilterInternal invoke...!");
        //access-token 쿠키 받기
        String token = null;      //access-token 쿠키 받아 token=null;
        String username = null;
        Cookie[] cookies = request.getCookies();
        if(cookies!=null)
        {
            token = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);

            username = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals("username");})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);
        }
        System.out.println("[Token] : " + token);
        System.out.println("[Username] : " + username);
        if(token!=null){
            // access-token is not null
            try {
                //-access-token is Expired ?
                if (jwtTokenProvider.validateToken(token)) {
                    //= no -> access-token -> Authentication -> SecurityContextholder 전달
                   Authentication authentication  = jwtTokenProvider.getAuthentication(token);
                   if(authentication!=null)
                       SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                else{

                }
            }catch(ExpiredJwtException e1){
                System.out.println("ExpiredJwtException ...AccessToken Expired.."  + e1.getMessage());

                //- yes -> refresh-token -> refresh-token is Expired?
//                //------------------------------------------------
//                // REFRESH  - MYSQLDB
//                //------------------------------------------------
//                JwtToken entity =  jwtTokenRepository.findByAccessToken(token);
//                System.out.println("entity : " + entity);
//
//                if (entity != null) {
//                        try {
//                            if (jwtTokenProvider.validateToken(entity.getRefreshToken())) {
//                                //access-token 재발급
//                                long now = (new Date()).getTime();  //현재시간
//                                String accessToken = Jwts.builder()
//                                        .setSubject(entity.getUsername()) //본문 TITLE
//                                        .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME )) //만료날짜(밀리초단위)
//                                        .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) // 서명값
//                                        .claim("username",entity.getUsername()) // 본문 내용
//                                        .claim("auth",entity.getAuth()) // 본문 내용
//                                        .compact();
//                                System.out.println("!! NEW ACCESS_TOKEN : " + accessToken);
//                                Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);
//
//                                // [수정] Cookie.setMaxAge()는 '초' 단위이나 상수는 '밀리초'이므로 1000으로 나눠 전달 (5분)
//                                //        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME/1000);    //accesstoken 유지시간
//                                cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
//                                cookie.setPath("/");    //쿠키 적용경로(/ : 모든경로)
//                                // [수정] JS에서 토큰 접근 차단(XSS 탈취 방지). HTTPS 환경에서는 cookie.setSecure(true)도 함께 권장
//                                cookie.setHttpOnly(true);
//                                response.addCookie(cookie); //응답정보에 쿠키 포함
//
//                                //db 에 갱신된 access-token 저장
//                                entity.setAccessToken(accessToken);
//                                jwtTokenRepository.save(entity);
//
//                                // 갱신된 accesstoken 으로 authentication
//                                Authentication authentication  = jwtTokenProvider.getAuthentication(accessToken);
//                                if(authentication!=null)
//                                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                                System.out.println("리프래시 갱신완료!!!");
//                            }
//                        }catch(ExpiredJwtException e2){
//                            System.out.println("ExpiredJwtException ...RefreshToken Expired.."  + e2.getMessage());
//                            //access 만료, refresh 만료
//                            // - > 쿠키만료
//                            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,null);
//                            cookie.setMaxAge(0);
//                            response.addCookie(cookie);
//                            // - > db 삭제
//                            jwtTokenRepository.deleteById(entity.getId());
//
//                        }catch(Exception e3){
//
//                        }
//
//                }
//                else
//                {
//                        ;
//                }
                //------------------------------------------------
                // REFRESH  - REDIS
                //------------------------------------------------
                String refreshToken = (username != null) ? redisUtil.getRefreshToken("RT:"+username) : null;
//                JwtToken entity =  jwtTokenRepository.findByAccessToken(token);
//                System.out.println("entity : " + entity);
                if (refreshToken != null) {
                    try {
                        if (jwtTokenProvider.validateToken(refreshToken)) {
                            //access-token 재발급
                            long now = (new Date()).getTime();  //현재시간
                            //
                            User user = userRepository.findById(username).orElse(null);

                            String accessToken = Jwts.builder()
                                    .setSubject(username) //본문 TITLE
                                    .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME )) //만료날짜(밀리초단위)
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) // 서명값
                                    .claim("username",username) // 본문 내용
                                    .claim("auth",user.getRole()) // 본문 내용
                                    .compact();
                            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);

                            // [수정] Cookie.setMaxAge()는 '초' 단위이나 상수는 '밀리초'이므로 1000으로 나눠 전달 (5분)
                            cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME/1000);    //accesstoken 유지시간
//                          cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
                            cookie.setPath("/");    //쿠키 적용경로(/ : 모든경로)
                            // [수정] JS에서 토큰 접근 차단(XSS 탈취 방지). HTTPS 환경에서는 cookie.setSecure(true)도 함께 권장
                            cookie.setHttpOnly(true);
                            response.addCookie(cookie); //응답정보에 쿠키 포함

                            // 갱신된 accesstoken 으로 authentication
                            Authentication authentication  = jwtTokenProvider.getAuthentication(accessToken);
                            if(authentication!=null)
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            System.out.println("리프래시 갱신완료!!!");
                        }
                    }catch(ExpiredJwtException e2){
                        System.out.println("ExpiredJwtException ...RefreshToken Expired.."  + e2.getMessage());
                        //access 만료, refresh 만료
                        // - > 쿠키만료
                        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,null);
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);

                        Cookie cookie2 = new Cookie("username",null);
                        cookie2.setMaxAge(0);
                        cookie2.setPath("/");
                        response.addCookie(cookie2);

                        // - > REDIS 에서 삭제
                        redisUtil.delete("RT:" + username);

                    }catch(Exception e3){

                    }

                }
            }catch(Exception e4){
                System.out.println("Exception ...."  + e4.getMessage());
            }
            //------------------------------------------------
        }else {
            //Token(accesstoken) == null && refreshToken != null

            String refreshToken = (username != null) ? redisUtil.getRefreshToken("RT:"+username) : null;
            if (refreshToken != null) {
                try {
                    if (jwtTokenProvider.validateToken(refreshToken)) {
                        //access-token 재발급
                        long now = (new Date()).getTime();  //현재시간
                        //
                        User user = userRepository.findById(username).orElse(null);

                        String accessToken = Jwts.builder()
                                .setSubject(username) //본문 TITLE
                                .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME )) //만료날짜(밀리초단위)
                                .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) // 서명값
                                .claim("username",username) // 본문 내용
                                .claim("auth",user.getRole()) // 본문 내용
                                .compact();
                        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);

                        // [수정] Cookie.setMaxAge()는 '초' 단위이나 상수는 '밀리초'이므로 1000으로 나눠 전달 (5분)
                        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME/1000);    //accesstoken 유지시간
//                      cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
                        cookie.setPath("/");    //쿠키 적용경로(/ : 모든경로)
                        // [수정] JS에서 토큰 접근 차단(XSS 탈취 방지). HTTPS 환경에서는 cookie.setSecure(true)도 함께 권장
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie); //응답정보에 쿠키 포함

                        // 갱신된 accesstoken 으로 authentication
                        Authentication authentication  = jwtTokenProvider.getAuthentication(accessToken);
                        if(authentication!=null)
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("리프래시 갱신완료!!!");
                    }
                }catch(ExpiredJwtException e2){
                    System.out.println("ExpiredJwtException ...RefreshToken Expired.."  + e2.getMessage());
                    //access 만료, refresh 만료
                    // - > 쿠키만료
                    Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,null);
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);

                    Cookie cookie2 = new Cookie("username",null);
                    cookie2.setMaxAge(0);
                    cookie2.setPath("/");
                    response.addCookie(cookie2);

                    // - > REDIS 에서 삭제
                    redisUtil.delete("RT:" + username);

                }catch(Exception e3){

                }
            }
            else{
                //Token(accesstoken) == null && refreshToken == null
            }
        }
        filterChain.doFilter(request,response);
    }
}
