package com.kx.service.data.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
public class Users {
    @Id
    private Long id;

    /**
     * 手机号
     */
    private String mobile;

    private String unionId;
    private String openId;

    /**
     * 昵称，媒体号
     */
    private String nickname;

    /**
     * 慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     */
    @Column(name = "imooc_num")
    private String imoocNum;

    /**
     * 头像
     */
    private String face;

    /**
     * 性别 1:男  0:女  2:保密
     */
    private Integer sex;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 简介
     */
    private String description;

    private String company;
    private String job;

    /**
     * 个人介绍的背景图
     */
    @Column(name = "bg_img")
    private String bgImg;

    /**
     * 慕课号能否被修改，1：默认，可以修改；0，无法修改
     */
    @Column(name = "can_imooc_num_be_updated")
    private Integer canImoocNumBeUpdated;

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

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}