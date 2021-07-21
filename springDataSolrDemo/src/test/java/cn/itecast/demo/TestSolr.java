package cn.itecast.demo;

import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-solr.xml")
public class TestSolr {

    @Autowired
    private SolrTemplate solrTemplate;


    @Test
    public void add(){

        TbItem item = new TbItem();
        item.setId(1L);
        item.setTitle("华为手机");
        item.setPrice(new BigDecimal(3000));
        item.setBrand("华为");
        item.setSeller("华为旗舰店西安分店");
        item.setCategory("手机");

        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

    @Test
    public void testFindOne(){
        TbItem item = solrTemplate.getById(1L, TbItem.class);
        System.out.println(item.getTitle()+"  "+item.getPrice());
    }

    @Test
    public void testDelete(){
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    @Test
    public void addList(){

        List<TbItem> items=new ArrayList<TbItem>();
        for (int i = 0; i < 100; i++) {
            TbItem item = new TbItem();
            item.setId(i+1L);
            item.setTitle("华为Mate"+i);
            item.setPrice(new BigDecimal(5000+i));
            item.setBrand("华为");
            item.setSeller("华为旗舰店西安分店");
            item.setCategory("手机");

            items.add(item);
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Test
    public void testPageQuery(){
        Query query = new SimpleQuery("*:*");

        Criteria criteria = new Criteria("item_title").contains("2");
        criteria=criteria.and("item_title").contains("5");


        query.addCriteria(criteria);
        query.setOffset(0);
        query.setRows(20);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        List<TbItem> content = page.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle()+"   "+item.getPrice());
        }
        System.out.println("总记录数："+page.getTotalElements());
    }

    @Test
    public void deleteAll(){
        Query query = new SimpleQuery("*:*");

        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
