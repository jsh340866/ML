package com.example.demo.Config.auth.handler;

import com.example.demo.Config.auth.jwt.JWTProperties;
import com.example.demo.Config.auth.redis.RedisUtil;
import com.example.demo.Domain.Common.Repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {


    @Autowired
    JwtTokenRepository jwtTokenRepository;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class,transactionManager = "jpaTransactionManager")
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("CustomLogoutSuccessHandler's onLogoutSuccess invoke...");

        //access-token 쿠키 받아 token=null;
        String token = null;

        Cookie[] cookies = request.getCookies();
        if(cookies!=null)
        {
            token = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);
        }
        if(token!=null){
            //db 삭제
//            jwtTokenRepository.deleteByAccessToken(token);
            redisUtil.delete("RT:"+authentication.getName());

            //access-token 쿠키 제거
            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            Cookie cookie2 = new Cookie("username",null);
            cookie2.setMaxAge(0);
            cookie2.setPath("/");
            response.addCookie(cookie2);

        }


        response.sendRedirect("/login?logout="+ URLEncoder.encode("로그아웃 성공","utf-8"));
    }
}
