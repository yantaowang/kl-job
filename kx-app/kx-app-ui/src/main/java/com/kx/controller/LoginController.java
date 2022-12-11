package com.kx.controller;


import com.kx.common.result.GraceJSONResult;
import com.kx.common.result.ResponseStatusEnum;
import com.kx.common.utils.IPUtil;
import com.kx.common.utils.SMSUtils;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.data.bo.RegistLoginBO;
import com.kx.service.data.pojo.Users;
import com.kx.service.data.vo.UsersVO;
import com.kx.service.service.FansService;
import com.kx.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@Api(tags = "PassportController 通信证接口模块")
@RequestMapping("login")
@RestController
public class LoginController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;
    @Autowired
    private UserService userService;
    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,
                                      HttpServletRequest request) throws Exception {
        //校验手机号
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }
        // 获得用户ip，
        String userIp = IPUtil.getRequestIp(request);
        // 根据用户ip进行限制，限制用户在60秒之内只能获得一次验证码,然后存到redis,一个ip只能存一个验证码(60s的生命周期)
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);//记录当前ip是否存验证码到redis
        //生成验证码
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        //发送短信
        smsUtils.sendSMS(mobile, code);
        log.info(code);
        // 把验证码放入到redis中，用于后续的验证
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        return GraceJSONResult.ok();
    }

    @PostMapping("login")//@Valid开启请求参数校验,校验出的错误存到BindingResult
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO,
//                                 BindingResult result,    // 对代码有侵入性
                                 HttpServletRequest request) throws Exception {

        // 0. 判断BindingResult中是否保存了错误的验证信息，如果有，则需要返回到前端
//        if( result.hasErrors() ) {
//            Map<String, String> map = getErrors(result);
//            return GraceJSONResult.errorMap(map);
//        }

        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();

        // 1. 校验验证码:从redis中获得验证码进行校验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 查询数据库，判断用户是否存在
        Users user = userService.queryMobileIsExist(mobile);
        if (user == null) {
            // 2.1 如果用户为空，表示没有注册过，则为null，需要注册信息入库
            user = userService.createUser(mobile);
        }

        // 3. 如果不为空，可以继续下方业务，可以保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);//前后端分离情况下是不能获取httpsession,只能借助redis存储会话

        // 4. 用户登录注册成功以后，删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 5. 返回用户信息，包含token令牌
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);
    }
    @ApiOperation(value = "用户注销接口")
    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam Long userId,
                                  HttpServletRequest request) throws Exception {

        // 后端只需要清除用户的token信息即可，前端也需要清除，清除本地app中的用户信息和token会话信息
        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceJSONResult.ok();
    }
}
