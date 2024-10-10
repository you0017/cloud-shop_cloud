package com.yc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yc.bean.AdminComment;
import com.yc.bean.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface AdminCommentMapper extends BaseMapper<AdminComment> {

    public List<AdminComment> fuzzyQueryComment(@Param("shenhe") String shenhe,
                                                @Param("huifu") String huifu,
                                                @Param("dengji") String dengji,
                                                @Param("limit") int limit,
                                                @Param("skip") int skip);

    public int  fuzzyQueryComment2(@Param("shenhe") String shenhe,
                                                        @Param("huifu") String huifu,
                                                        @Param("dengji") String dengji);
}
