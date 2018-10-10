package com.pinyougou.page.service.impl;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;


    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {

        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据模型
            Map dataModel =  new HashMap<>();

            //根据主键来进行查询

            //商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);


            //商品扩展数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);

            //商品分类
            String itemCat1= itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2= itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3= itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //商品的sku列表
            TbItemExample example = new TbItemExample();

            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//有效

            criteria.andGoodsIdEqualTo(goodsId);//指定SPU的ID；

            example.setOrderByClause("is_default desc");//降序排列，保证第一个sku是默认

            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);

            Writer out =  new FileWriter(pagedir+goodsId+".html");
            template.process(dataModel,out);//输出

            out.close();//关闭流

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {

        try {
            for (Long goodsId:goodsIds){

                new File(pagedir+goodsId+".html").delete();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();


            return false;
        }


    }
}
