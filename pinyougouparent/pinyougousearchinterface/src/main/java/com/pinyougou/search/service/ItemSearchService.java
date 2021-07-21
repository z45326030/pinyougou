package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * 搜索业务接口
 */
public interface ItemSearchService {


    public Map search(Map searchMap);

    /**
     *  导入数据
     * @param list
     */
    public void importList(List list);

    /**
     * 根据商品ID集合删除索引数据
     * @param goodsIds
     */
    public void deleteByGoodsIds(Long[] goodsIds);

}
