package com.kx.service.service;

import com.kx.common.utils.PagedGridResult;
import com.kx.service.data.bo.CommentBO;
import com.kx.service.data.pojo.Comment;
import com.kx.service.data.vo.CommentVO;

public interface CommentService {

    /**
     * 发表评论
     */
    public CommentVO createComment(CommentBO commentBO);

    /**
     * 查询评论的列表
     */
    public PagedGridResult queryVlogComments(Long vlogId,
                                             Long userId,
                                             Integer page,
                                             Integer pageSize);

    /**
     * 删除评论
     */
    public void deleteComment(Long commentUserId,
                              Long commentId,
                              Long vlogId);

    /**
     * 根据主键查询comment
     */
    public Comment getComment(Long id);
}
