package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 37269 on 2018/7/25.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName",name);
        return map;
    }

    public static void main(String[] args) {
        /*BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        boolean b = encoder.matches("mimimi", "$2a$10$O4y8qlA/QgBWo9cA5pfWKe.YuvqZemlXsavBBr.YnG6H09I67WRIi");
        System.out.println(b);*/

        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        for(int i=0;i<100;i++){
            String newpwd = encoder.encode("mimimi");
            System.out.println(newpwd);
        }



    }

}
