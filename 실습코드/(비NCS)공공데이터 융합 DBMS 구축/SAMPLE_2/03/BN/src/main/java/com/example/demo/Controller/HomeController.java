package com.example.demo.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//https://velog.io/@yuureru/Spring-Security-%EB%8F%99%EC%9E%91%EA%B3%BC%EC%A0%95-%EA%B5%AC%EC%A1%B0

@Controller
@Slf4j
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home(){
        log.info("GET /");
        return "index";
    }
}
