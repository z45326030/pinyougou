package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 37269 on 2018/8/6.
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfig freemarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Value("${pagedir}")
    private String pagedir; //生成路径

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            Configuration configuration = freemarkerConfig.getConfiguration();
            //1.创建配置类
            //Configuration configuration=new Configuration(Configuration.getVersion());
            //2.设置模板路径
            //configuration.setDirectoryForTemplateLoading(new File("E:\\shanghai34\\pinyougouparent\\pinyougoupageservice\\src\\main\\webapp\\WEB-INF\\ftl\\"));
            //3.设置字符集
            //configuration.setDefaultEncoding("UTF-8");
            Template template = configuration.getTemplate("item.ftl");//获取模板
            Map dataModel=  new HashMap();//构建数据模型
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);//查询商品对象
            dataModel.put("goods",goods);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);//查询商品扩展对象
            dataModel.put("goodsDesc",goodsDesc);


            String itemCat1= itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();//一级分类名称
            String itemCat2= itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();//二级分类名称
            String itemCat3= itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();//三级分类名称

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");//按照是否默认倒序排序,为了让默认SKU排在第一位

            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);

            //Writer out=new FileWriter(new File(pagedir+ goodsId+".html"));
            OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(pagedir+goodsId+".html"),"UTF-8");
            template.process(dataModel,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
