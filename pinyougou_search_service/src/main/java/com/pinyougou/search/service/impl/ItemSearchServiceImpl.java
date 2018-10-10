package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService{

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        Map map = new HashMap();

        map.putAll(searchList(searchMap));

        List<String> categoryList = searchCategoryList(searchMap);

        map.put("categoryList",categoryList);

        //查询品牌和规格列表

        if(categoryList.size()>0){
            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }

        //3.查询品牌和规格策划
        String categoryName = (String) searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if(categoryList.size() >0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }



        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);

        Query query = new SimpleQuery();

        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);

        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    //查询列表
    private Map searchList(Map searchMap){

        Map map = new HashMap();

        SimpleHighlightQuery query = new SimpleHighlightQuery();

        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置进行高亮的域

        highlightOptions.setSimplePrefix("<em style='color:red'>");

        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);//设置高亮选项

        //1.1 按关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 分类查询

        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }



        //1.3 品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }

        //1.4 规格过滤

        if(searchMap.get("spec")!= null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(searchMap.get(key));
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }
        //1.5 价格过滤

        if(!"".equals(searchMap.get("price"))){
                //把价格区间看为字符串
           String[] price = ((String) searchMap.get("price")).split("-");
           if(!price[0].equals("0")){
               //如果区间起点不等于0 那么就按照起点的价格去数据库查询
               Criteria filterQuery = new Criteria("item_price").greaterThanEqual(price[0]);
               SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterQuery);
               query.addFilterQuery(simpleFilterQuery);

           }
           if(!price[1].equals("*")){

               Criteria filterQuery = new Criteria("item_price").lessThanEqual(price[1]);
               SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterQuery);
               query.addFilterQuery(simpleFilterQuery);
           }
        }


        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;//设置默认页面
        }

        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize == null){
            pageSize=20;
        }

        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);//每页显示数


        //排序

        String sortValue = (String) searchMap.get("sort");//ASC DESC
        String sortField = (String) searchMap.get("sortField");//排序字段

        if(sortValue!=null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }

            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }
        //********************************************************
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);

        for (HighlightEntry<TbItem> h : page.getHighlighted()) {
            TbItem entity = h.getEntity();
            if (h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size() >0){
                entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows",page.getContent());

        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数
        return map;
    }



    private List<String> searchCategoryList(Map searchMap){


        List list = new ArrayList();

        Query query = new SimpleQuery("*:*");

        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口的集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }

        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //取出模板id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(templateId!=null){
            //根据模板id取出品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList",brandList);

            //取出规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);

            map.put("specList",specList);
        }



        return map;
    }


}
