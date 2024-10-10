package com.yc.service;

import com.yc.bean.DataModel;

import java.util.List;
import java.util.Map;

public interface AdminCommentService {
    public int selfCommentReplay(String idsStr, String comments);

    public int keepSelf(String comments);

    public int allCommentReplay(String idsStr, String selectedOption);

    public List<Map<String, Object>> getBackCommentTemplate();

    public DataModel fuzzyQueryComment(String shenhe, String huifu, String dengji, String page1, String limit1);

    public int shopComment(String id, String shopBackcomment);

    public int AllCommentOperate(String operate);

    public int batchCommentEnabled(String operate, String idStr);

    public int notReplyComment(String userId, String itemId, String id);

    public int replyComment(String userId, String itemId, String id);

    public DataModel allCommentData(DataModel ud, String page1, String limit1);
}
