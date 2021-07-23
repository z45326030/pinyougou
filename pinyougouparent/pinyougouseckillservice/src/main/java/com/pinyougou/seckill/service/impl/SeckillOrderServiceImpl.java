package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		//1.从缓存中查询商品（判断是否存在，库存是否为0）
		TbSeckillGoods seckillGoods= (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if(seckillGoods==null){
			throw new RuntimeException("商品不存在或已经秒光");
		}
		if(seckillGoods.getStockCount()<=0){
			throw new RuntimeException("商品已经秒光");
		}
		if(seckillGoods.getEndTime().getTime() <= new Date().getTime()){
			throw new RuntimeException("商品秒杀活动已经结束");
		}
		//2.扣减库存（当库存被扣减为0的时候，同步到数据库中并清除缓存）
		seckillGoods.setStockCount( seckillGoods.getStockCount()-1  );
		redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);

		if(seckillGoods.getStockCount()==0){//秒光 同步到数据库中
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//保存
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//移除缓存
		}

		//3.创建订单
		TbSeckillOrder seckillOrder=new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillGoods.getCostPrice());
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setStatus("0");
		seckillOrder.setUserId(userId);
		//缓存里存订单
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder );

	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return  (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {

		//从redis中查询订单
		TbSeckillOrder seckillOrder= (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder==null){
			throw new  RuntimeException("没有此订单");
		}
		if(seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new  RuntimeException("订单号不配备");
		}
		//设置属性值
		seckillOrder.setPayTime(new Date());
		seckillOrder.setStatus("1");//已支付
		seckillOrder.setTransactionId(transactionId);
		//保存到数据库中
		seckillOrderMapper.insert(seckillOrder);
		//从缓存中清除预定单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);

	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		TbSeckillOrder seckillOrder=(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder!=null  && seckillOrder.getId().longValue()== orderId.longValue()){
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//恢复库存
			TbSeckillGoods seckillGoods= (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if(seckillGoods!=null){
				seckillGoods.setStockCount( seckillGoods.getStockCount()+1 );// 库存回收
			}else{
				//从数据库中再次查询，放入缓存，库存数置为1
				seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
				//是否超过当前活动时间
				if( seckillGoods.getEndTime().getTime()> new Date().getTime()  ){
					seckillGoods.setStockCount(1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
				}
			}
		}
	}

}
