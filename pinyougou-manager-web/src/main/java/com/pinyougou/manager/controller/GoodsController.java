package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
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

	//@Reference
	//private ItemSearchService itemSearchService;
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
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
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
	@Autowired
	private Destination queueSolrDeleteDestination;

	@Autowired
    private Destination topicPageDeleteDestination;//用于删除静态网页消息

    @RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            //删除页面
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
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
		return goodsService.findPage(goods, page, rows);		
	}


    @Autowired
    private Destination queueSolrDestination;

	@Autowired
	private Destination topicPageDestination;

    @Autowired
    private JmsTemplate jmsTemplate;

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids, status);
			//按照spu id查询sku列表（状态为1）
			if(status.equals("1")){//审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if(itemList.size()>0){
					//itemSearchService.importList(itemList);

					final String jsonString = JSON.toJSONString(itemList);

                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(jsonString);
                        }
                    });

                    //静态页生成
                    for (final Long goodsId:ids){
                        //itemPageServcie.genItemHtml(goodsId);

                        jmsTemplate.send(topicPageDestination, new MessageCreator() {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                return session.createTextMessage(goodsId+"");
                            }
                        });
                    }

				}else {
					System.out.println("没有明细数据");
				}


			}
			return new Result(true,"修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();

			return new Result(false,"修改状态失败");
		}
	}

	//@Reference
	//private ItemPageService itemPageServcie;

	@RequestMapping("/genHtml")
	public void genItemHtml(Long goodsId){
		//itemPageServcie.genItemHtml(goodsId);
	}



}
