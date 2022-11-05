package com.kx.service.data.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistLoginBO {
    //在请求参数的pojo里面校验参数信息,基于hibernate
    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机长度不正确")//限制手机号长度为11
    private String mobile;
    @NotBlank(message = "验证码不能为空")
    private String smsCode;

}
