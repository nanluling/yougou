package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

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
		goods.getGoods().setAuditStatus("0");//把状态设置为未申请
		goodsMapper.insert(goods.getGoods());//添加一个goods
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置id
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);//插入商品sku数据
	}

	private void setItemValus(Goods goods, TbItem item) {

		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//商家名称

		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());

		item.setSeller(seller.getNickName());


		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);

		if (imageList.size() > 0) {
			item.setImage((String) imageList.get(0).get("url"));
		}
	}

	private void saveItemList(Goods goods){
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				//标题
				String title = goods.getGoods().getGoodsName();

				Map<String, Object> specMap = JSON.parseObject(item.getSpec());

				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				item.setTitle(title);
				item.setGoodsId(goods.getGoods().getId());//
				item.setSellerId(goods.getGoods().getSellerId());
				item.setCategoryid(goods.getGoods().getCategory3Id());
				item.setCreateTime(new Date());
				item.setUpdateTime(new Date());

				//品牌名称
				TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
				item.setBrand(brand.getName());
				//分类名称
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
				item.setCategory(itemCat.getName());
				//商家名称

				TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());

				item.setSeller(seller.getNickName());


				List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);

				if (imageList.size() > 0) {
					item.setImage((String) imageList.get(0).get("url"));
				}

				itemMapper.insert(item);

			}
		}else {
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品 KPU+规格描述串作为
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods,item);
			itemMapper.insert(item);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新基本数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//删除所有的列表数据
		TbItemExample example = new TbItemExample();

		TbItemExample.Criteria criteria = example.createCriteria();

		criteria.andGoodsIdEqualTo(goods.getGoods().getId());

		itemMapper.deleteByExample(example);

		//再插入新的数据

		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();

		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);

		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		System.out.println();
		goods.setGoodsDesc(tbGoodsDesc);

		//查询sku商品列表
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();

		criteria.andGoodsIdEqualTo(id);

		List<TbItem> itemsList = itemMapper.selectByExample(example);
		goods.setItemList(itemsList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");

			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}



	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		criteria.andIsDeleteIsNull();//非删除状态
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							criteria.andSellerIdLike(goods.getSellerId());
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
	public void updateStatus(Long [] ids, String status) {
		for (Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

			tbGoods.setAuditStatus(status);

			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}


}
