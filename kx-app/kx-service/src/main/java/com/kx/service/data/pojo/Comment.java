package com.kx.service.data.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Data
public class Comment {
    @Id
    private Long id;

    /**
     * 评论的视频是哪个作者（vloger）的关联id
     */
    @Column(name = "vloger_id")
    private Long vlogerId;

    /**
     * 如果是回复留言，则本条为子留言，需要关联查询
     */
    @Column(name = "father_comment_id")
    private Long fatherCommentId;

    /**
     * 回复的那个视频id
     */
    @Column(name = "vlog_id")
    private Long vlogId;

    /**
     * 发布留言的用户id
     */
    @Column(name = "comment_user_id")
    private Long commentUserId;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 留言的点赞总数
     */
    @Column(name = "like_counts")
    private Integer likeCounts;

    /**
     * 留言时间
     */
    @Column(name = "create_time")
    private Date createTime;
}