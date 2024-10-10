package com.yc.task;

import com.yc.api.CommentClient;
import com.yc.bean.Comment;
import com.yc.bean.Item;
import com.yc.mapper.ItemMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class StarTask {
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private CommentClient commentClient;

    /**
        评分处理
     */
    @Scheduled(cron = "0 0 0 * * ? ")
    public void task(){
        try {
            //这个是用来算评分的
            List<Item> select = itemMapper.selectList(null);//查所有的商品
            for (Item item : select) {
                //遍历所有商品，及其评论
                List<Comment> select1 = commentClient.getById(item.getId());
                int total=0;
                Integer o = select1.size();
                if (o.equals(0)){
                    return;
                }
                for (Comment comment : select1) {
                    total += comment.getRating();
                }
                int rating = total/o;
                //平均分弄进去
                itemMapper.updateById(item.builder().rating(rating).id(item.getId()).build());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
