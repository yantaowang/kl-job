package com.kx.service.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentVO {
    private Long id;
    private Long commentId;
    private Long vlogerId;
    private String fatherCommentId;
    private Long vlogId;
    //评论者id,名称和头像
    private Long commentUserId;
    private String commentUserNickname;
    private String commentUserFace;
    private String content;
    //评论点赞
    private Integer likeCounts;
    private String replyedUserNickname;//上一级评论的评论者的昵称
    private Date createTime;
    private Integer isLike = 0;
}