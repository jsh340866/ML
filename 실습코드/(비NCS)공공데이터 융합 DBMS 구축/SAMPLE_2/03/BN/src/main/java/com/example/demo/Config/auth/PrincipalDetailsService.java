package com.example.demo.Config.auth;

import com.example.demo.Domain.Common.Dtos.UserDTO;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Domain.Common.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PrincipalDetailsService  implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("PrincipalDetailsService's loadUserByUsername...{}" ,username);
        Optional<User> userOptional = userRepository.findById(username);
        PrincipalDetails principalDetails = null;
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            UserDTO userDTO = UserDTO.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .build();
            principalDetails = PrincipalDetails.builder().userDTO(userDTO).build();
        }
        else
            throw new UsernameNotFoundException(username+"이 존재하지않습니다");
        return principalDetails;
    }
}
