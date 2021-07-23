package com.pinyougou.pay.service;

import java.util.Map;

/**
 * Created by 37269 on 2018/8/14.
 */
public interface WeixinPayService {


    /**
     * 生成微信支付二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);


    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 查询支付状态(轮询)
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatusWhile(String out_trade_no);

}
