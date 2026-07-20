package com.example.demo.Config.auth;


import com.example.demo.Config.auth.provider.GoogleUserInfo;
import com.example.demo.Config.auth.provider.KakaoUserInfo;
import com.example.demo.Config.auth.provider.NaverUserInfo;
import com.example.demo.Config.auth.provider.OAuth2UserInfo;
import com.example.demo.Domain.Common.Dtos.UserDTO;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Domain.Common.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
@Slf4j
public class PrincipalDetailsOauth2Service extends DefaultOAuth2UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        System.out.println("userRequest : " + userRequest);
//        System.out.println("userRequest.getClientRegistration() :"+ userRequest.getClientRegistration());
//        System.out.println("userRequest.getAccessToken() : "+ userRequest.getAccessToken());
//        System.out.println("userRequest.getAdditionalParameters() : "+ userRequest.getAdditionalParameters());
//        System.out.println("userRequest.getAccessToken().getTokenValue() : "+ userRequest.getAccessToken().getTokenValue());
//        System.out.println("userRequest.getAccessToken().getTokenType().getValue() : "+ userRequest.getAccessToken().getTokenType().getValue());
//        System.out.println("userRequest.getAccessToken().getScopes() : "+ userRequest.getAccessToken().getScopes());
        OAuth2User oAuth2User = super.loadUser(userRequest);
//        System.out.println("oAuth2User : " + oAuth2User);
        System.out.println("oAuth2User.getAttributes() : " + oAuth2User.getAttributes() );
//        System.out.println("Provider Name : " + userRequest.getClientRegistration().getClientName());

        OAuth2UserInfo oAuth2UserInfo = null;
        String provider = userRequest.getClientRegistration().getClientName();
        Map<String,Object> attributes = oAuth2User.getAttributes();
        String username = null;

        if(provider.startsWith("Kakao")){
            System.out.println("OAUTH2 PROVIDER : KAKAO");

            Long id = (Long)attributes.get("id");
            LocalDateTime connected_at = OffsetDateTime.parse( attributes.get("connected_at").toString() ).toLocalDateTime();
            Map<String,Object> properties = (Map<String,Object>)attributes.get("properties");
            Map<String,Object> kakao_account = (Map<String,Object>)attributes.get("kakao_account");
//            System.out.println("id : " + id );
//            System.out.println("connected_at : " + connected_at );
//            System.out.println("properties : " + properties );
//            System.out.println("kakao_account : " + kakao_account );

            oAuth2UserInfo = new KakaoUserInfo(id,connected_at,properties,kakao_account);
            username = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId();

        }else if(provider.startsWith("Naver")){
            String resultcode = (String)attributes.get("resultcode");
            String message = (String)attributes.get("message");
            Map<String,Object> response = (Map<String,Object>)attributes.get("response");

            oAuth2UserInfo = new NaverUserInfo(resultcode,message,response);

            System.out.println("OAUTH2 PROVIDER : NAVER");
            username = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId();

        }else if(provider.startsWith("Google")){
            System.out.println("OAUTH2 PROVIDER : GOOGLE");
            oAuth2UserInfo = new GoogleUserInfo(attributes);
            username = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId();
        }

        System.out.println("oAuth2UserInfo" + oAuth2UserInfo);

        // OAuth2계정정보를 바탕으로 로컬계정 생성(접속 최초 1회만)
        String password = passwordEncoder.encode("1234");
        UserDTO userDTO = null;

        if(!userRepository.existsById(username)){
            User user = User    .builder()
                                .username(username)
                                .password(password)
                                .role("ROLE_USER")
                                .build();
            userRepository.save(user);
            userDTO = UserDTO   .builder()
                                .username(username)
                                .password(password)
                                .role("ROLE_USER")
                                .build();
        }else{
            User user = userRepository.findById(username).get();
            userDTO = UserDTO   .builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .role(user.getRole())
                                .build();
        }



        // Principal로 변환 -> SecurityContextHolder에 Authentication 보관
        PrincipalDetails principalDetails = PrincipalDetails.builder()
                                            .userDTO(userDTO)
                                            .attributes(attributes)
                                            .build();


        return principalDetails;
    }
}
