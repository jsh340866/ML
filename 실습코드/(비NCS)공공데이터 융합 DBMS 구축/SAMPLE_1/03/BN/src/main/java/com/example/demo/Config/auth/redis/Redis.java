//package com.example.demo.Config.auth.redis;
//
//
//import com.example.demo.Config.auth.jwt.JWTProperties;
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.data.redis.core.RedisHash;
//
//@RedisHash(value = "JwtToken", timeToLive = JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME)
//@AllArgsConstructor
//@Data
//public class Redis {
//    @Id
//    private String username;
//    private String refreshToken;
//}