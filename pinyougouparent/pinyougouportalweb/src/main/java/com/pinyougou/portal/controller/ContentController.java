package com.pinyougou.portal.controller;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/content")
public class ContentController {

	@Reference
	private ContentService contentService;

	/**
	 *  根据ID查询广告列表
	 * @param categoryId
	 * @return
	 */
	@RequestMapping("/findByCategoryId")
	public List<TbContent> findByCategoryId(Long categoryId){
		return contentService.findByCategoryId(categoryId);
	}

}
