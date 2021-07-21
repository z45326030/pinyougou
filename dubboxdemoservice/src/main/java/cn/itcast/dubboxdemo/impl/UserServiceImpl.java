package cn.itcast.dubboxdemo.impl;

import cn.itcast.dubboxdemo.UserService;
import com.alibaba.dubbo.config.annotation.Service;

/**
 * 业务实现
 */
@Service
public class UserServiceImpl implements UserService{
    @Override
    public String getName() {
        return "itcast";
    }
}
