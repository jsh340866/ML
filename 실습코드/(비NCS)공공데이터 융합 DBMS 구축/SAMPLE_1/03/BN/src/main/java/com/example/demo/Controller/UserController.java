//package com.example.demo.Controller;
//
//import com.example.demo.Config.auth.PrincipalDetails;
//import com.example.demo.Domain.Common.Dtos.UserDTO;
//import com.example.demo.Domain.Common.Entity.User;
//import com.example.demo.Domain.Common.Repository.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@Controller
//@Slf4j
//public class UserController {
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @GetMapping("/login")
//    public void login(){
//        log.info("GET /login...");
//    }
//    @GetMapping("/join")
//    public void join_get(){
//        log.info("GET /join");
//    }
//    @PostMapping("/join")
//    public String join_post(UserDTO userDTO){
//        log.info("POST /join...{}",userDTO);
//        User user = User.builder()
//                .username(userDTO.getUsername())
//                .password(passwordEncoder.encode(userDTO.getPassword()))
//                .role("ROLE_USER")
//                .build();
//        userRepository.save(user);
//        return "redirect:/login";
//    }
//
//    //인증정보확인 - 1
////    @GetMapping("/user")
////    public void user(@AuthenticationPrincipal PrincipalDetails principalDetails , Model model){
////
////        log.info("GET /user...." + principalDetails);
////        model.addAttribute("principalDetails",principalDetails);
////    }
//    //인증정보확인 - 2
//    @GetMapping("/user")
//    public void user(Model model){
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        PrincipalDetails principalDetails =(PrincipalDetails) authentication.getPrincipal();
//
//        model.addAttribute("principalDetails",principalDetails);
//
//        log.info("GET /user");
//    }
////
//
////    @GetMapping("/user")
////    public void user(){
////
////        log.info("GET /user");
////    }
//
//    @GetMapping("/manager")
//    public void manager(){
//        log.info("GET /manager");
//    }
//    //인증정보확인 - 3 (Thymeleaf-security)
//    @GetMapping("/admin")
//    public void admin(){
//        log.info("GET /admin");
//    }
//}
