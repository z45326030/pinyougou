package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.swing.*;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/applicationContext-redis.xml")
public class TestList {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void setValue1(){
        redisTemplate.boundListOps("namelist1").rightPush("刘备");
        redisTemplate.boundListOps("namelist1").rightPush("关于");
        redisTemplate.boundListOps("namelist1").rightPush("张飞");
    }

    @Test
    public void getValue1(){
        List list = redisTemplate.boundListOps("namelist1").range(0, 10);
        System.out.println(list);
    }

    @Test
    public void setValue2(){
        redisTemplate.boundListOps("namelist3").leftPush("刘备");
        redisTemplate.boundListOps("namelist3").leftPush("关于");
        redisTemplate.boundListOps("namelist3").leftPush("张飞");
    }

    @Test
    public void getValue2(){
        List list = redisTemplate.boundListOps("namelist2").range(0, 10);
        System.out.println(list);
    }

    @Test
    public void removeValue(){
        redisTemplate.boundListOps("namelist2").remove(1,"关于");
    }

    @Test
    public void searchByIndex(){
        String o =(String) redisTemplate.boundListOps("namelist3").index(1);
        System.out.println(o);
    }

    @Test
    public void deleteValue(){
        redisTemplate.delete("namelist2");
    }
}
