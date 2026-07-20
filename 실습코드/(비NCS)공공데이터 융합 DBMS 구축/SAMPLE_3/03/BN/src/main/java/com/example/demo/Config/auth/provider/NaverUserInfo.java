package com.example.demo.Config.auth.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverUserInfo implements OAuth2UserInfo{

    private String resultcode;
    private String message;
    private Map<String,Object> response;


    @Override
    public String getName() {
        return (String)response.get("name");
    }

    @Override
    public String getEmail() {
        return (String)response.get("email");
    }

    @Override
    public String getProvider() {
        return "Naver";
    }

    @Override
    public String getProviderId() {
        return (String)response.get("id");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return response;
    }
}
