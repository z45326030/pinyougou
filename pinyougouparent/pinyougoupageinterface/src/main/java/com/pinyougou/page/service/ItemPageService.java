package com.pinyougou.page.service;

import java.io.IOException;

/**
 * Created by 37269 on 2018/8/6.
 */
public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId) ;

}
