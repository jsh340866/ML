package com.example.demo.Config;

import com.example.demo.Config.auth.exception.CustomAccessDeniedHandler;
import com.example.demo.Config.auth.exception.CustomAuthenticationEntryPoint;
import com.example.demo.Config.auth.handler.*;
import com.example.demo.Config.auth.jwt.JWTAuthorizationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    JWTAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    CustomLoginSuccessHandler customLoginSuccessHandler;
    @Autowired
    CustomLoginFailureHandler customLoginFailureHandler;
    @Autowired
    CustomLogoutHandler customLogoutHandler;
    @Autowired
    CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception
    {
        //CSRF 비활성화(비활성화하지 않으면 logout 요청은 기본적으로 POST방식을 따른다)
        http.csrf((config)->{config.disable();});

        //권한처리
        http.authorizeHttpRequests((auth)->{
            //정적경로 매핑
            auth.requestMatchers("/favicon.ico").permitAll();

            auth.requestMatchers("/","/join","/login","/validate","/actuator/**").permitAll();
            //
            auth.requestMatchers("/user").hasAnyRole("USER","ADMIN"); //
            auth.requestMatchers("/manager").hasAnyRole("MANAGER"); //
            auth.requestMatchers("/admin").hasAnyRole("ADMIN"); //

            auth.anyRequest().authenticated();
        });


        //로그인
        //-----------------------------------------------------
        // [수정] 로그인(직접처리 - UserRestController)
        //-----------------------------------------------------
        http.formLogin((login)->{
            login.disable();
//            login.permitAll();
//            login.loginPage("/login");
//            login.successHandler(customLoginSuccessHandler);//로그인 성공시 동작하는 핸들러
//            login.failureHandler(customLoginFailureHandler); //로그인 실패시(ID 미존재 , PW 불일치)
        });
        //로그아웃
        http.logout((logout)->{
            logout.permitAll();
            logout.addLogoutHandler(customLogoutHandler); //로그아웃 직접처리 핸들러
            logout.logoutSuccessHandler(customLogoutSuccessHandler);//로그아웃 성공시 동작하는 핸들러
        });

        //예외처리
        http.exceptionHandling(exception->{
            exception.authenticationEntryPoint(customAuthenticationEntryPoint); //미인증된 상태+ 권한이 필요한 Endpoint 접근시 예외 처리
            exception.accessDeniedHandler(customAccessDeniedHandler); //인증이후 권한이 부족할때
        });

        //OAuth2-Client 활성
        http.oauth2Login((oauth2)->{
            oauth2.loginPage("/login");
        });

        //SESSION 비활성화
        http.sessionManagement((sessionConfig)->{
            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        //JWTFilter 추가
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);

        //-----------------------------------------------------
        //[추가] CORS - React(3000) 에서 쿠키 포함 요청 허용 (corsConfigurationSource 빈 연결)
        //-----------------------------------------------------
        http.cors((cors)->{
            cors.configurationSource(corsConfigurationSource());
        });

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //-----------------------------------------------------
    //[추가] CORS
    //-----------------------------------------------------
    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(Collections.singletonList("*")); //허용헤더
        config.setAllowedMethods(Collections.singletonList("*")); //허용메서드
        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000"));  //허용도메인
        config.setAllowCredentials(true); // COOKIE TOKEN OPTION
        return new CorsConfigurationSource(){
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                return config;
            }
        };
    }
    //-----------------------------------------------------
    //[추가] ATHENTICATION MANAGER 설정 - 로그인 직접처리를 위한 BEAN
    //-----------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
