package com.yc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.api.OrderClient;
import com.yc.api.UserInformationClient;
import com.yc.bean.*;
import com.yc.context.BaseContext;
import com.yc.mapper.CommentMapper;
import com.yc.model.JsonModel;
import com.yc.service.CommentService;
import com.yc.utils.AliOSSProperties;
import com.yc.utils.AliOSSUtils;
import com.yc.utils.YcConstants;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInformationClient userInformationClient;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @Override
    public void likes(String id, String index) {
        //评论id      //操作类型

        int i;

        String user_id = BaseContext.getCurrentId();
        if (index.equals("1")) {
            //点赞
            if (redisTemplate.boundSetOps(id + YcConstants.LIKES_COMMENT).isMember(user_id + "")) {
                //此用户已经点赞过，再点就是取消
                redisTemplate.boundSetOps(id + YcConstants.LIKES_COMMENT).remove(user_id + "");   //用户 -> [多个评论id]
                //用户编号 此处也要删除
                redisTemplate.boundSetOps(user_id + YcConstants.LIKES_USER).remove(id + "");//评论 -> [多个用户id]

                //更新数据库，方便看，主要是懒得改查询代码了
                LambdaUpdateWrapper<Comment> db = new LambdaUpdateWrapper<>();
                db.eq(Comment::getId, id)
                        .setSql("likes=likes-1");
                commentMapper.update(null, db);
            } else {
                //没有点赞过
                redisTemplate.boundSetOps(id + YcConstants.LIKES_COMMENT).add(user_id + "");//用户 -> [多个评论id]
                redisTemplate.boundSetOps(user_id + YcConstants.LIKES_USER).add(id + "");//评论 -> [多个用户id]

                LambdaUpdateWrapper<Comment> db = new LambdaUpdateWrapper<>();
                db.eq(Comment::getId, id)
                        .setSql("likes=likes+1");
                commentMapper.update(null, db);
            }
        } else {
            //踩
            if (redisTemplate.boundSetOps(id + YcConstants.DISLIKES_COMMENT).isMember(user_id + "")) {
                //此用户已经点踩过，再点就是取消
                redisTemplate.boundSetOps(id + YcConstants.DISLIKES_COMMENT).remove(user_id + "");
                //用户编号 此处也要删除
                redisTemplate.boundSetOps(user_id + YcConstants.DISLIKES_USER).remove(id + "");

                LambdaUpdateWrapper<Comment> db = new LambdaUpdateWrapper<>();
                db.eq(Comment::getId, id)
                        .setSql("dislikes=dislikes-1");
                commentMapper.update(null, db);
            } else {
                //没有点踩过
                redisTemplate.boundSetOps(id + YcConstants.DISLIKES_COMMENT).add(user_id + "");//用户 -> [多个评论id]
                redisTemplate.boundSetOps(user_id + YcConstants.DISLIKES_COMMENT).add(id + "");//评论 -> [多个用户id]

                LambdaUpdateWrapper<Comment> db = new LambdaUpdateWrapper<>();
                db.eq(Comment::getId, id)
                        .setSql("dislikes=dislikes+1");
                commentMapper.update(null, db);
            }


        }

    }

    @Override
    public Comment_max getComments(String id, String pageNo, String pageSize, String sortBy, String sort) {
        JsonModel jm = new JsonModel();

        Comment_max commentMax = new Comment_max(); //最外层
        commentMax.setPageno(Integer.parseInt(pageNo));
        commentMax.setPagesize(Integer.parseInt(pageSize));
        commentMax.setSortby(sortBy);
        commentMax.setSort(sort);

        //所有主评论
        LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLambdaQueryWrapper
                .eq(Comment::getItem_id, id)
                .eq(Comment::getParent_comment_id, 0)
                .eq(Comment::getShop_reply, 2);
        List<Comment> select = commentMapper.selectList(commentLambdaQueryWrapper);
        //查所有，为评分服务，分页评分有问题，不全，只能单独算
        for (Comment comments : select) {
            //顺便看一下打分个数
            if (comments.getRating() == 5) {
                commentMax.setFive(commentMax.getFive() + 1);
            } else if (comments.getRating() == 4) {
                commentMax.setFour(commentMax.getFour() + 1);
            } else if (comments.getRating() == 3) {
                commentMax.setThree(commentMax.getThree() + 1);
            } else if (comments.getRating() == 2) {
                commentMax.setTwo(commentMax.getTwo() + 1);
            } else if (comments.getRating() == 1) {
                commentMax.setOne(commentMax.getOne() + 1);
            }
        }
        commentMax.setComment_count((long) select.size());
        int main_comments = select.size();
        for (Comment comments : select) {
            //副评论
            LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(Comment::getParent_comment_id, comments.getId())
                    .eq(Comment::getShop_reply, 2)
                    .orderByDesc(Comment::getId);
            List<Comment> childs = commentMapper.selectList(lambdaQueryWrapper);

            commentMax.setComment_count(commentMax.getComment_count() + childs.size());//每次遍历都要把副评论数量算进去

            for (Comment child : childs) {
                //顺便看一下打分个数
                if (child.getRating() == 5) {
                    commentMax.setFive(commentMax.getFive() + 1);
                } else if (child.getRating() == 4) {
                    commentMax.setFour(commentMax.getFour() + 1);
                } else if (child.getRating() == 3) {
                    commentMax.setThree(commentMax.getThree() + 1);
                } else if (child.getRating() == 2) {
                    commentMax.setTwo(commentMax.getTwo() + 1);
                } else if (child.getRating() == 1) {
                    commentMax.setOne(commentMax.getOne() + 1);
                }
            }
        }

        /**
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         */


        int i = (commentMax.getPageno() - 1) * commentMax.getPagesize();

        //查询，并且要父评论id为0                                           返回几个    从第几个开始
        //查这个商品所有的评论
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(Comment::getItem_id, id)
                .eq(Comment::getParent_comment_id, 0)
                .eq(Comment::getShop_reply, 2);
        // 动态处理排序
        if ("likes".equals(sortBy)) {
            lambdaQueryWrapper.orderByDesc(Comment::getLikes);
        } else {
            lambdaQueryWrapper.orderByDesc(Comment::getId);
        }
        // 使用 limit 和 offset
        lambdaQueryWrapper.last("limit " + commentMax.getPagesize() + " offset " + i);
        select = commentMapper.selectList(lambdaQueryWrapper);

        if (select == null || select.size() <= 0) {
            return commentMax;
        }
        //(Long.valueOf(select2.get(0).get("count(*)").toString()));//数量  还差回复评论

        //计算页数  只根据主页数算
        commentMax.setTotalpages((int) (main_comments % commentMax.getPagesize() == 0 ? main_comments / commentMax.getPagesize() : main_comments / commentMax.getPagesize() + 1));

        //计算上一页
        if (commentMax.getPageno() == 1 || commentMax.getTotalpages() == 0) {
            commentMax.setPre(1);
        } else {
            commentMax.setPre(commentMax.getPageno() - 1);
        }

        //计算下一页
        if (commentMax.getPageno() == commentMax.getTotalpages() || commentMax.getTotalpages() == 0) {
            //最后一页
            commentMax.setNext(commentMax.getPageno());
        } else {
            commentMax.setNext(commentMax.getPageno() + 1);
        }

        List<CommentList> commentLists = new ArrayList<>();//最外层的属性，需要把所有主评论加进去，然后放入最外层
        CommentList commentList = null;//主评论+副评论集合

        //找名字
        for (Comment comments : select) {

            comments.setCreated_at(comments.getCreated_at().substring(0, comments.getCreated_at().length() - 2));//时间格式化一下


            //根据id查用户
            //List<Map<String, Object>> select1 = db.select("select name,image from userinformation where id = ?", comments.getUser_id());
            UserInformation user = userInformationClient.getById(comments.getUser_id());
            comments.setUser_name(user.getName());
            comments.setImage(user.getImage());//单个主评论已经完整

            //主评论添加到  主评论+副评论集合
            commentList = new CommentList();
            commentList.setComment(comments);//主评论

            //副评论
            lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(Comment::getParent_comment_id, comments.getId())
                    .eq(Comment::getShop_reply, 2)
                    .orderByDesc(Comment::getId);
            List<Comment> childs = commentMapper.selectList(lambdaQueryWrapper);

            for (Comment child : childs) {
                //时间格式化一下
                child.setCreated_at(child.getCreated_at().substring(0, child.getCreated_at().length() - 2));


                user = userInformationClient.getById(comments.getUser_id());
                comments.setUser_name(user.getName());
                comments.setImage(user.getImage());//单个副评论已经完整
            }
            commentList.setComment_2(childs);//该主评论的副评论已经完整，可以追加到  主评论+副评论集合
            commentLists.add(commentList);
        }
        //所有主评论及其对应副评论遍历完
        commentMax.setCommentLists(commentLists);


        return commentMax;
    }

    @Override
    @Transactional
    public void remark(String id1, String topic, String id, String rating, String remark) {

        String user_id = BaseContext.getCurrentId();


        JsonModel jm = new JsonModel();

        /**
         * 要看这个用户有没有购买这个商品
         */

        boolean hasUserPurchasedProduct = orderClient.hasUserPurchasedProduct(id, user_id);

        if (!hasUserPurchasedProduct){
            throw new RuntimeException("您还没有购买过该商品，无法评论");
        }


        //都存商品id，方便后面算分，但是取得时候只取主评论
        Comment comment = new Comment(null, Integer.valueOf(id), null, Integer.valueOf(user_id), topic, remark, Integer.valueOf(rating), null, null, null, null, id1, 0, null, null, null);
        int i = commentMapper.insert(comment);
        if (i<=0){
            throw new RuntimeException("评论失败");
        }
    }

    @Override
    public String pic(Part file) {

        String url;
        try {
            url = aliOSSUtils.upload(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("图片上传失败");
        }
        return url;
    }
}
