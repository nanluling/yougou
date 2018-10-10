package com.pinyougou.solrUtil;

import java.util.List;
import java.util.Map;


import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;


import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {

	@Autowired
	private SolrTemplate solrTemplate;


	@Autowired
	private TbItemMapper itemMapper;


	/*
	*
	* 导入商品数据
	* */
	public void importItemData(){
		TbItemExample example=new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//已审核
		List<TbItem> itemList = itemMapper.selectByExample(example);
		System.out.println("===商品列表===");
		for(TbItem item:itemList){
			//把json字符串转为map集合
			Map specMap = JSON.parseObject(item.getSpec());

			item.setSpecMap(specMap);
			System.out.println(item.getTitle());
		}
		solrTemplate.saveBeans(itemList);

		solrTemplate.commit();

		System.out.println("结束");
	}


	public static void main(String[] args) {

		ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil=(SolrUtil) context.getBean("solrUtil");
		solrUtil.importItemData();



	}

}
