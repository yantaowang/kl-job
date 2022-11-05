package com.kx.app.intercepter;

import com.kx.common.exceptions.GraceException;
import com.kx.common.result.ResponseStatusEnum;
import com.kx.common.utils.IPUtil;
import com.kx.service.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    //访问controller之前,请求被这个方法拦截
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        // 获得用户的ip
        String userIp = IPUtil.getRequestIp(request);

        //判断用户ip是否存验证码到redis 得到是否存在的判断
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);

        if (keyIsExist) {
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率太大！");
            return false;
        }

        /**
         * true: 请求放行
         * false: 请求拦截
         */
        return true;
    }
    //访问controller之后,在渲染数据之前,会被该方法拦截
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    //走完controller和渲染完视图之后,被该方法拦截
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
