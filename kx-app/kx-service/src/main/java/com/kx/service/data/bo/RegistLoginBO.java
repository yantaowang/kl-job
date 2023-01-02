package com.kx.service.data.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistLoginBO {
//    //在请求参数的pojo里面校验参数信息,基于hibernate
//    @NotBlank(message = "手机号不能为空")
//    @Length(min = 11, max = 11, message = "手机长度不正确")//限制手机号长度为11
//    private String mobile;
//    @NotBlank(message = "验证码不能为空")
//    private String smsCode;
//

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

    /**
     * 个人介绍的背景图
     */
    private String bgImg;

    /**
     * 创建时间 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间 更新时间
     */
    private Date updatedTime;

    private String code;
}
