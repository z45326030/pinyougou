package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//审核状态  为未审核
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);
	}

	//设置sku字段信息
	private void setItemValues(TbItem item,Goods goods){
		item.setCategoryid(goods.getGoods().getCategory3Id());//分类ID
		item.setCreateTime(  new Date());
		item.setUpdateTime( new Date());
		item.setGoodsId( goods.getGoods().getId() );//商品ID
		item.setSellerId( goods.getGoods().getSellerId());//商家ID

		//商品分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//商家
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//商品图片
		List<Map> imageList=  JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class) ;
		if(imageList.size()>0){
			item.setImage( (String)imageList.get(0).get("url")  );
		}


	}

	/**
	 * 保存sku列表信息
	 * @param goods
	 */
	private void saveItemList(Goods goods){
		if( "1".equals(goods.getGoods().getIsEnableSpec()  )){
			//保存SKUL列表  循环SKU  插入到item表中
			for(TbItem item:  goods.getItemList()){
				//标题
				String title= goods.getGoods().getGoodsName();
				Map<String,Object> map = JSON.parseObject(item.getSpec());
				for(String key:map.keySet()){
					title+=" "+  map.get(key);
				}
				item.setTitle(title);

				setItemValues(item,goods);

				itemMapper.insert( item );
			}
		}else{//不启用规格
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//标题
			item.setPrice(goods.getGoods().getPrice()  );//价格
			item.setNum( 99999 );//库存数量
			item.setStatus("1");// 状态
			item.setIsDefault("1");//是否默认
			item.setSpec("{}");//规格字符串
			setItemValues(item,goods);
			itemMapper.insert(item);
		}

	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

		goodsMapper.updateByPrimaryKey(goods.getGoods());//商品主表
		goodsDescMapper.updateByPrimaryKey(  goods.getGoodsDesc());//商品扩展表

		//先删除原来的SKU列表，再重新添加

		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		saveItemList(goods);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods=new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));//商品主表
		goods.setGoodsDesc( goodsDescMapper.selectByPrimaryKey(id) );//商品扩展表

		//SKU 表 item
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//商品ID  where goods_id=?
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}
	}


	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//where isDelete =null
		if(goods!=null){
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());//必须改成精确查询
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}

		}

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	@Override
	public void updateMarketabel(Long[] ids, String marketabel,String sellerId) {
		//sellerId 如果为null，表示运营商后台调用此方法  ,不验证商家ID
		for(Long id:ids){
			TbGoods tbGoods=goodsMapper.selectByPrimaryKey(id);
			if(!"1".equals(tbGoods.getAuditStatus()) &&  "1".equals(marketabel)){  //如果当前的操作是上架  并且当前商品未审核
				continue;
			}
			if(!tbGoods.getSellerId().equals(sellerId) && sellerId!=null){
				continue;
			}
			tbGoods.setIsMarketable(marketabel);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIds(Long[] goodsIds) {
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn( Arrays.asList(goodsIds) );//  Long[] goodsIds  List<Long>
		criteria.andStatusEqualTo("1");//状态为1
		List<TbItem> items = itemMapper.selectByExample(example);
		for(TbItem item:items){
			System.out.println(item.getTitle()+"  "+item.getPrice());
			Map specMap= JSON.parseObject(item.getSpec()) ;
			item.setSpecMap(specMap);
		}
		return items;
	}

}
