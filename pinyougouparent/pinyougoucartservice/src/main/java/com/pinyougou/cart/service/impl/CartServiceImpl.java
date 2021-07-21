package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 37269 on 2018/8/11.
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据SKU id查询商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不合法");
        }

        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.查询购物车列表中是否存在该商家
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if(cart==null){  //4.如果不存在该商家
            //4.1 构建购物车对象, 购物车对象中的明细列表添加明细
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());//商家名称

            List<TbOrderItem> orderItemList=new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);//添加购物车明细对象到明细列表
            cart.setOrderItemList(orderItemList);

            //4.2 将购物车对象添加到购物车列表中
            cartList.add(cart);
        }else{ //5.如果存在该商家

            //  判断商品是否在购物车明细列表中存在

            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){//5.1 不存在  在购物车明细列表中添加记录
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);

            }else{//5.2 存在  修改购物车明细列表中记录的数量、金额

                orderItem.setNum( orderItem.getNum()+num );
                orderItem.setTotalFee(new BigDecimal( orderItem.getNum()*orderItem.getPrice().doubleValue() ));

                //如果操作后数量为0，则移除该明细
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果操作后明细记录数为0，则删除购物车对象
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }

        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * redis查询购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车"+username);
        List<Cart> cartList=(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向redis中存入购物车
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入购物车"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for( Cart cart :cartList2 ){
            for( TbOrderItem orderItem:cart.getOrderItemList() ){
                cartList1=addGoodsToCartList(cartList1, orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家ID在购物车列表中查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for( Cart cart:cartList ){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }


    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for(TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 构建购物车明细对象
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice() );
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee( new BigDecimal(orderItem.getPrice().doubleValue()* orderItem.getNum())  );
        return orderItem;
    }

}
