package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		//设置sellerID  商家ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);

		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		//确保商家提交修改的商品一定是此商家商品

		//查询当前登录的商家
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		if(!goods.getGoods().getSellerId().equals(sellerId)){
			return new Result(false, "不是当前商家商品");
		}
		Goods goods2 = goodsService.findOne(goods.getGoods().getId());//查询修改前商品对象
		if(!goods2.getGoods().getSellerId().equals(sellerId)){
			return new Result(false, "不是当前商家商品");
		}
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		//得到当前商家的ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);		
	}

	@Reference
	private ItemSearchService itemSearchService;

	/**
	 * 上下架
	 * @param ids
	 * @param marketabel
	 * @return
	 */
	@RequestMapping("/updateMarketabel")
	public Result updateMarketabel(Long[] ids, String marketabel){
		try {
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			goodsService.updateMarketabel(ids,marketabel,sellerId);

			if(marketabel.equals("1")){//上架
				List<TbItem> itemList = goodsService.findItemListByGoodsIds(ids);//查询上架的SKU列表
				if(itemList.size()>0){
					itemSearchService.importList(itemList);//导入
					//静态页生成
					for(Long goodsId:ids){
						itemPageService.genItemHtml(goodsId);
					}

				}
			}else{//下架
				itemSearchService.deleteByGoodsIds(ids);//删除索引数据
			}

			return new Result(true,"操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"操作失败");
		}
	}

	@Reference(timeout=40000)
	private ItemPageService itemPageService;
	/**
	 * 生成静态页（测试）
	 * @param goodsId
	 */
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}


}
