package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yc.api.FrontEditClient;
import com.yc.bean.AdminComment;
import com.yc.bean.Comment;
import com.yc.bean.DataModel;
import com.yc.bean.DataRecord;
import com.yc.mapper.AdminCommentMapper;
import com.yc.mapper.CommentMapper;
import com.yc.model.JsonModel;
import com.yc.service.AdminCommentService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@WebServlet("/admin/comment.action")
@RestController
@RequestMapping("/comment/admin")
public class AdminCommentController {
    @Autowired
    private AdminCommentService adminCommentService;

    /**
     * 自定义回复
     * @param idsStr 要快速回复的评论id
     * @param comments 快速回复的选项
     */

    @RequestMapping("/selfCommentReplay")
    public JsonModel selfCommentReplay(@RequestParam("idsStr")String idsStr,
                                       @RequestParam("comments")String comments) {
        JsonModel jm = new JsonModel();

        int result = adminCommentService.selfCommentReplay(idsStr,comments);

        if (result > 0) {
            jm.setCode(1);
            jm.setObj("操作成功");
        } else {
            jm.setCode(0);
            jm.setObj("操作失败");
        }
        return jm;

    }


    // 我的自定义语句保存到数据字典
    @RequestMapping("/keepSelf")
    public JsonModel keepSelf(@RequestParam("comments")String comments) {
        JsonModel jm = new JsonModel();

        int result = adminCommentService.keepSelf(comments);

        if (result > 0) {
            jm.setCode(1);
            jm.setObj("保存成功");
        } else {
            jm.setCode(0);
            jm.setObj("保存失败");
        }
        return jm;
    }

    /**
     * 商家快速回复商品评价
     * @param idsStr 要快速回复的评论id
     * @param selectedOption 快速回复的选项
     * @return
     */
    @RequestMapping("/allCommentReplay")
    public JsonModel allCommentReplay(@RequestParam("idsStr")String idsStr,
                                      @RequestParam("selectedOption")String selectedOption) {
        JsonModel jm = new JsonModel();

        int result = adminCommentService.allCommentReplay(idsStr,selectedOption);

        if (result > 0) {
            jm.setCode(1);
            jm.setObj("操作成功");
        } else {
            jm.setCode(0);
            jm.setObj("操作失败");
        }
        return jm;
    }


    /**
     * 商家回复的评论模板
     */
    @RequestMapping("/getBackCommentTemplate")
    public JsonModel getBackCommentTemplate() {

        List<Map<String, Object>> maps = adminCommentService.getBackCommentTemplate();

        JsonModel jm = new JsonModel();
        jm.setObj(maps);
        jm.setCode(1);
        return jm;
    }


    /**
     * 评论的模糊查询
     */
    @RequestMapping("/fuzzyQueryComment")
    public DataModel fuzzyQueryComment(@RequestParam("shenhe")String shenhe,// 审核状态  0待审核  1 不通过  2 通过
                                       @RequestParam("huifu")String huifu,// 回复状态  0未回复  1已回复
                                       @RequestParam("dengji")String dengji,// 星级
                                       @RequestParam("page")String page1,
                                       @RequestParam("limit")String limit1) {
        DataModel ud = adminCommentService.fuzzyQueryComment(shenhe, huifu, dengji, page1, limit1);
        return ud;
    }

    // 商家回复单个评论
    @RequestMapping("/shopComment")
    public DataModel shopComment(@RequestParam("id")String id, // 商家回复的id
                                 @RequestParam("editor1")String shop_backcomment // 商家回复内容
    ) {
        DataModel ud = new DataModel();

        int result = adminCommentService.shopComment(id, shop_backcomment);

        if (result == -1){
            ud.setCode(1);
            ud.setMsg("评论未审核或没通过");
            return ud;
        }else if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        return ud;
    }


    // 评论的一键操作
    @RequestMapping("/AllCommentOperate")
    public DataModel AllCommentOperate(@RequestParam("operate")String operate) {
        DataModel ud = new DataModel();
        int result = adminCommentService.AllCommentOperate(operate);
        if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        return ud;
    }

    // 评论批量操作
    @RequestMapping("/batchCommentEnabled")
    public DataModel batchCommentEnabled(@RequestParam("operate")String operate,
                                         @RequestParam("idsStr")String idStr) {
        DataModel ud = new DataModel();
        int result = adminCommentService.batchCommentEnabled(operate, idStr);
        if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        return ud;


    }

    // 审核不通过
    @RequestMapping("/notReplyComment")
    public DataModel notReplyComment(@RequestParam("user_id")String user_id,// 用户id
                                     @RequestParam("item_id")String item_id,// 商品id
                                     @RequestParam("id")String  id // 评论id
    ) {
        DataModel ud = new DataModel();

        int result = adminCommentService.notReplyComment(user_id, item_id, id);

        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("审核成功");
        } else {
            ud.setCode(1);
            ud.setMsg("审核失败");
        }
        return ud;
    }

    // 审核评论通过
    public DataModel replyComment(@RequestParam("user_id")String user_id,// 用户id
                                  @RequestParam("item_id")String item_id,// 商品id
                                  @RequestParam("id")String  id // 评论id
    ) {
        DataModel ud = new DataModel();
        int result2 = adminCommentService.replyComment(user_id, item_id, id);
        if (result2 > 0) {
            ud.setCode(0);
            ud.setMsg("审核成功");
        } else {
            ud.setCode(1);
            ud.setMsg("审核失败");
        }
        return ud;
    }


    @RequestMapping("/getAllComment")
    public DataModel getAllComment(@RequestParam("page")String page1,
                                   @RequestParam("limit")String limit1) {
        DataModel ud = new DataModel();
        ud = adminCommentService.allCommentData(ud, page1, limit1);
        System.out.println("我来要评论来了");
        return ud;
    }




}
