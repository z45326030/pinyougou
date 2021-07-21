package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 37269 on 2018/8/2.
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        Map resultMap=new HashMap();//返回结果
        //0.去掉空格
        String keywords=(String)searchMap.get("keywords");
        keywords=keywords.replace(" ","");
        searchMap.put("keywords",keywords);

        //1.查询列表数据
        resultMap.putAll(searchList(searchMap)  );

        //2.查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);

        //3.查询品牌和规格列表
        if(!"".equals(searchMap.get("category"))){
            resultMap.putAll(searchBrandAndSpecList( (String)searchMap.get("category")));
        }else{
            if(categoryList.size()>0 ){
                resultMap.putAll(searchBrandAndSpecList( categoryList.get(0) ));
            }
        }

        return resultMap;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIds) {
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
    }


    /**
     * 列表查询
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map resultMap=new HashMap();//返回结果

        //1. 关键字搜索
        //Query query =new SimpleQuery();
        HighlightQuery query=new SimpleHighlightQuery();//高亮查询对象
        String keywords=(String)searchMap.get("keywords");
        Criteria criteria=new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);

        //2.商品分类筛选
        if( !"".equals(searchMap.get("category")) ){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            //FilterQuery 过滤查询对象
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //3.品牌筛选
        if( !"".equals(searchMap.get("brand")) ){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            //FilterQuery 过滤查询对象
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //4.规格筛选
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap=(Map<String,String>)searchMap.get("spec");
            for(String key:specMap.keySet()){
                Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key) );
                //FilterQuery 过滤查询对象
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }


        //5.价格筛选
        if(!"".equals(searchMap.get("price"))){
            String[] price = ((String)searchMap.get("price")).split("-");
            if(!price[0].equals("0")){//最低价格不等于0
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual( price[0] );
                //FilterQuery 过滤查询对象
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            if(!price[1].equals("*")){//如果价格有上限
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
                //FilterQuery 过滤查询对象
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }


        }

        //6.分页查询
        Integer pageNo=   (Integer)searchMap.get("pageNo");//页码
        if(pageNo==null){
            pageNo=1;
        }
        Integer pageSize=   (Integer)searchMap.get("pageSize");//页大小
        if(pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);


        //7.排序
        String sortType=  (String) searchMap.get("sort");//ASC  DESC
        String sortField=  (String) searchMap.get("sortField");//排序字段  price

        if(sortType!=null && !"".equals(sortType)){//有排序

            if(sortType.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC ,"item_"+ sortField);
                query.addSort(sort);
            }
            if(sortType.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC ,"item_"+ sortField);
                query.addSort(sort);
            }
        }

        //   <em style="color:'red'">三星</em>note7劲爆款
        //8.高亮
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮列
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);//设置高亮选项

        //查询
        //ScoredPage<TbItem> itemPage = solrTemplate.queryForPage(query, TbItem.class);
        //得到高亮页
        HighlightPage<TbItem> itemPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //得到高亮入口集合
        List<HighlightEntry<TbItem>> highlighted = itemPage.getHighlighted();
        for(HighlightEntry<TbItem> highlightEntry:highlighted ){ //遍历高亮入口对象
            TbItem item  = highlightEntry.getEntity(); //得到实体类，注意：title字段没有高亮效果

            List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
            if(highlights.size()>0 &&  highlights.get(0).getSnipplets().size()>0 ) {
                item.setTitle(highlights.get(0).getSnipplets().get(0));//设置高亮片段
            }
        }


        resultMap.put("rows",itemPage.getContent()) ;
        resultMap.put("total",itemPage.getTotalElements());//总条数
        resultMap.put("totalPages",itemPage.getTotalPages());//总页数

        return resultMap;
    }


    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    public List<String> searchCategoryList(Map searchMap){
        List<String> list=new ArrayList<>();

        //spring Data solr分组查询  相当于sql 中的group by

        Query query =new SimpleQuery("*:*");
        String keywords=(String)searchMap.get("keywords");
        Criteria criteria=new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);  //where item_keywords=....

        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");//group by item_category
        query.setGroupOptions(groupOptions);//设置分组选项

        //得到分组页对象
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);  //from TbItem
        //得到分组结果对象
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");  //select item_category
        //得到分组入口页对象
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合对象
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        //循环分组入口集合，得到分组值 ，加到list集合中
        for( GroupEntry<TbItem> groupEntry: content){
            list.add(groupEntry.getGroupValue());
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据商品分类名称查询品牌和规格列表
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //1.根据商品分类名称得到模板Id
        Long typeId=  (Long)redisTemplate.boundHashOps("itemCat").get(category);
        if(typeId!=null){
            //2.查询品牌列表
            List<Map> brandList=  (List<Map>)redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //3.查询规格列表
            List<Map> specList = (List<Map>)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

}
