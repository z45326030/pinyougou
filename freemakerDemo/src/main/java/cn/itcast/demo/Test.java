package cn.itcast.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Test {
    public static void main(String[] args) throws Exception {

        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setDirectoryForTemplateLoading(new File("D:\\shanghai34\\freemakerDemo\\src\\main\\resources"));
        configuration.setDefaultEncoding("UTF-8");
        Template template = configuration.getTemplate("text.ftl");
        Map map = new HashMap();
        map.put("name","张三");
        map.put("message","傻逼");
        map.put("success",false);


        List goodsList = new ArrayList();
        Map goods1 = new HashMap();
        goods1.put("name","苹果");
        goods1.put("price",2.8);
        goodsList.add(goods1);


        Map goods2 = new HashMap();
        goods2.put("name","香蕉");
        goods2.put("price",5.8);
        goodsList.add(goods2);

        Map goods3 = new HashMap();
        goods3.put("name","橘子");
        goods3.put("price",3.8);
        goodsList.add(goods3);

        map.put("goodsList",goodsList);

        map.put("today",new Date());

        map.put("point",111132312);

        Writer writer = new FileWriter("d:\\test.html");
        template.process(map,writer);
        writer.close();
    }
}
