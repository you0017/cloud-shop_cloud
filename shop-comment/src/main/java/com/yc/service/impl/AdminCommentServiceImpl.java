package com.yc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yc.api.FrontEditClient;
import com.yc.bean.AdminComment;
import com.yc.bean.Comment;
import com.yc.bean.DataModel;
import com.yc.bean.DataRecord;
import com.yc.mapper.AdminCommentMapper;
import com.yc.mapper.CommentMapper;
import com.yc.service.AdminCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminCommentServiceImpl implements AdminCommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FrontEditClient frontEditClient;
    @Autowired
    private AdminCommentMapper adminCommentMapper;

    @Override
    public int selfCommentReplay(String idsStr, String comments) {
        idsStr = idsStr.substring(0, idsStr.length() - 1);

        LambdaUpdateWrapper<Comment> lu = Wrappers.lambdaUpdate();
        lu.set(Comment::getShop_backcomment_status, 1)
                .set(Comment::getShop_backcomment, comments)
                .in(Comment::getId, idsStr);
        return commentMapper.update(null, lu);
    }

    @Override
    public int keepSelf(String comments) {
        DataRecord dataRecord = DataRecord.builder().recorde_name("shop_reply_template").recorde_value(comments).recorde_status(1).build();
        return frontEditClient.add(dataRecord);
    }

    @Override
    public int allCommentReplay(String idsStr, String selectedOption) {
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");

//        String sql = "update comments set shop_reply=? where id in (" + idsStr + ")";
        LambdaUpdateWrapper<Comment> db = Wrappers.lambdaUpdate();
        db.set(Comment::getShop_backcomment_status, 1).set(Comment::getShop_backcomment, selectedOption).in(Comment::getId, strArray);
        return commentMapper.update(null, db);
    }

    @Override
    public List<Map<String, Object>> getBackCommentTemplate() {
        return frontEditClient.get();
    }

    @Override
    public DataModel fuzzyQueryComment(String shenhe, String huifu, String dengji, String page1, String limit1) {
        DataModel ud = new DataModel();

        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        List<AdminComment> select = adminCommentMapper.fuzzyQueryComment(shenhe, huifu, dengji, limit, skip);

        int total = adminCommentMapper.fuzzyQueryComment2(shenhe, huifu, dengji);

        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setData(select);
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("暂无数据");
        }
        return ud;
    }

    @Override
    public int shopComment(String id, String shopBackcomment) {
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(Comment::getShop_reply).eq(Comment::getId, id);
        Comment comment = commentMapper.selectOne(lambdaQueryWrapper);
        int reply_status = comment.getShop_reply();

        if (reply_status != 2) {  // 未审核
            return -1;
        }

        LambdaUpdateWrapper<Comment> db = new LambdaUpdateWrapper<>();
        db.set(Comment::getShop_backcomment, shopBackcomment)
                .set(Comment::getShop_reply, 2)
                .set(Comment::getShop_backcomment_status, 1)
                .eq(Comment::getId, id);
        return commentMapper.update(null, db);
    }

    @Override
    public int AllCommentOperate(String operate) {
        LambdaUpdateWrapper<Comment> lambdaUpdateWrapper = new LambdaUpdateWrapper();
        if ("allOk".equals(operate)) {
            lambdaUpdateWrapper.set(Comment::getShop_reply, 2).eq(Comment::getShop_reply, 0);
        } else if ("allNo".equals(operate)) {
            lambdaUpdateWrapper.set(Comment::getShop_reply, 1).eq(Comment::getShop_reply, 0);
        }
        return commentMapper.update(null, lambdaUpdateWrapper);
    }

    @Override
    public int batchCommentEnabled(String operate, String idStr) {
        // 0待审核  1不通过  2通过
        idStr = idStr.substring(0, idStr.length() - 1);
        String[] strArray = idStr.split(",");

        LambdaUpdateWrapper<Comment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if ("ok".equals(operate)) {
            lambdaUpdateWrapper.set(Comment::getShop_reply, 2).in(Comment::getId, strArray);
        } else if ("no".equals(operate)) {
            lambdaUpdateWrapper.set(Comment::getShop_reply, 1).in(Comment::getId, strArray);
        }
        return commentMapper.update(null, lambdaUpdateWrapper);
    }

    @Override
    public int notReplyComment(String userId, String itemId, String id) {
        LambdaUpdateWrapper<Comment> db = Wrappers.lambdaUpdate();
        db.set(Comment::getShop_reply, 1).eq(Comment::getId, id);
        return commentMapper.update(null, db);
    }

    @Override
    public int replyComment(String userId, String itemId, String id) {
        LambdaUpdateWrapper<Comment> db = Wrappers.lambdaUpdate();
        db.set(Comment::getShop_reply, 2).eq(Comment::getId, id);
        return commentMapper.update(null, db);
    }

    @Override
    public DataModel allCommentData(DataModel ud, String page1, String limit1) {
        // 获取所有用户评论数据的方法
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 获取总条数
        Long total = commentMapper.selectCount(null);

        // user_id!= 0 代表是用户评论
//        String limitSql = "select * from comments limit ? offset ?";
        List<AdminComment> limitMaps = commentMapper.allCommentData(limit, skip);

//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;

    }
}
