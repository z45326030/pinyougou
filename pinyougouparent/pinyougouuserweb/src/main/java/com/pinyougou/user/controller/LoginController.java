package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 37269 on 2018/8/9.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 获取当前登录的用户
     * @return
     */
    @RequestMapping("/name")
    public Map showUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName",name);
        return map;
    }

}
