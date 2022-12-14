package com.kx;

import com.kx.intercepter.PassportInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");//拦截的路由
//        //注册需要拦截的路由与拦截器
//        registry.addInterceptor(userTokenInterceptor())
//                .addPathPatterns("/userInfo/modifyUserInfo")
//                .addPathPatterns("/userInfo/modifyImage");
    }
}
