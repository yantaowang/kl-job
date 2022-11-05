package com.kx.service.data.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "my_liked_vlog")
public class MyLikedVlog {
    @Id
    private Long id;

    /**
     * 用户id
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 喜欢的短视频id
     */
    @Column(name = "vlog_id")
    private Long vlogId;

}