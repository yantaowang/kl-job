package com.kx.service.data.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Data
public class Vlog {
    @Id
    private Long id;

    /**
     * 对应用户表id，vlog视频发布者
     */
    @Column(name = "vloger_id")
    private Long vlogerId;

    /**
     * 视频播放地址
     */
    private String url;

    /**
     * 视频封面
     */
    private String cover;

    /**
     * 视频标题，可以为空
     */
    private String title;

    /**
     * 视频width
     */
    private Integer width;

    /**
     * 视频height
     */
    private Integer height;

    /**
     * 点赞总数
     */
    @Column(name = "like_counts")
    private Integer likeCounts;

    /**
     * 评论总数
     */
    @Column(name = "comments_counts")
    private Integer commentsCounts;

    @Column(name = "collect_counts")
    private Integer collectCounts;
    /**
     * 是否私密，用户可以设置私密，如此可以不公开给比人看
     */
    @Column(name = "is_private")
    private Integer isPrivate;

    @Column(name = "content_type")
    private Integer contentType;

    /**
     * 创建时间 创建时间
     */
    @Column(name = "created_time")
    private Date createdTime;

    /**
     * 更新时间 更新时间
     */
    @Column(name = "updated_time")
    private Date updatedTime;
}