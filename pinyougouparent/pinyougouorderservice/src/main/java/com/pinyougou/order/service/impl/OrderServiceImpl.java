package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//orderMapper.insert(order);
		//提取购物车中的数据 ，循环生成订单、订单明细

		List<Cart> cartList =(List<Cart>)redisTemplate.boundHashOps("cartList").get(order.getUserId());
		if(cartList!=null){

			String out_trade_no= idWorker.nextId()+"";// 支付订单号
			double total_money= 0;//支付总金额！！！！！！！（元）
			for(Cart cart :cartList ){
				TbOrder tbOrder=new TbOrder();
				long orderId = idWorker.nextId();//订单编号
				tbOrder.setOrderId(orderId);//订单编号
				tbOrder.setCreateTime(new Date());
				tbOrder.setSellerId(cart.getSellerId());//商家
				tbOrder.setUserId(order.getUserId());//用户
				tbOrder.setPaymentType(order.getPaymentType());
				tbOrder.setReceiverAreaName(order.getReceiverAreaName()  );//地址
				tbOrder.setReceiverMobile(order.getReceiverMobile());//手机
				tbOrder.setReceiver(  order.getReceiver());//联系人
				tbOrder.setStatus("1");//未支付
				tbOrder.setSourceType(order.getSourceType());//来源
				tbOrder.setOutTradeNo(out_trade_no);//支付订单号！！！！！！
				double money=0;//金额
				//订单明细
				for(TbOrderItem orderItem :cart.getOrderItemList()){
					long id = idWorker.nextId();
					orderItem.setId(id);
					orderItem.setOrderId( orderId );//订单ID
					orderItemMapper.insert(orderItem);
					money+=orderItem.getTotalFee().doubleValue();
				}
				tbOrder.setPayment( new BigDecimal(money) );//总金额
				orderMapper.insert(tbOrder);

				total_money+=money;//支付总金额累加！！！！！
			}

			if("1".equals(order.getPaymentType())){
				TbPayLog payLog=new TbPayLog();
				payLog.setOutTradeNo(out_trade_no);//支付订单号
				payLog.setCreateTime(new Date());
				payLog.setPayType("1");
				payLog.setTotalFee( (long)(total_money*100) );
				payLog.setTradeState("0");//未支付
				payLog.setUserId(order.getUserId());//用户
				payLogMapper.insert(payLog);
				redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);//保存到缓存
			}

			redisTemplate.boundHashOps("cartList").delete(order.getUserId());//从购物车中移除
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return  (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transactionId) {
		//1.修改payLog 的状态为1
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());//支付时间
		payLog.setTradeState("1");//状态
		payLog.setTransactionId(transactionId);//交易流水
		payLogMapper.updateByPrimaryKey(payLog);

		//2.修改订单order 的状态为2
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		criteria.andOutTradeNoEqualTo(out_trade_no);
		List<TbOrder> orderList = orderMapper.selectByExample(example);

		for(TbOrder order:orderList){
			order.setStatus("2");
			order.setPaymentTime(new Date());//支付时间
			orderMapper.updateByPrimaryKey(order);
		}
		//3.清除缓存
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

	}

}
