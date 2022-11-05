package com.kx.service.data.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentBO {

    @NotBlank(message = "留言信息不完整")
    private Long vlogerId;

    @NotBlank(message = "留言信息不完整")
    private Long fatherCommentId;

    @NotBlank(message = "留言信息不完整")
    private Long vlogId;

    @NotBlank(message = "当前用户信息不正确，请尝试重新登录")
    private Long commentUserId;

    @NotBlank(message = "评论内容不能为空")
    @Length(max = 50, message = "评论内容长度不能超过50")
    private String content;
}