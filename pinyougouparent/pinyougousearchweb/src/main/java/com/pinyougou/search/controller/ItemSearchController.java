package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by 37269 on 2018/8/2.
 */
@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {


    @Reference(timeout = 5000)
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map searchMap){
        Map resultMap = itemSearchService.search(searchMap);
        return resultMap;
    }


}
