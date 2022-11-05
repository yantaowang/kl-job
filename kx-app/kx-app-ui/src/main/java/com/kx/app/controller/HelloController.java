//package com.kx.app.controller;
//
//import com.kx.base.RabbitMQConfig;
//import com.kx.grace.result.GraceJSONResult;
//import com.kx.model.Stu;
//import com.kx.utils.SMSUtils;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Api(tags = "Hello 测试的接口")//controller分组名
//@Slf4j//lombok的日志注解
//@RestController
//@RefreshScope//spring cloud提供的一种特殊的scope实现，用来实现配置、实例热加载。
//public class HelloController {
//    @Autowired
//    private SMSUtils smsUtils;
//    @Autowired
//    public RabbitTemplate rabbitTemplate;
//
//    @Value("${nacos.counts}")
//    private Integer nacosCounts;
//    @GetMapping("nacosCounts")
//    public Object nacosCounts() {
//        return GraceJSONResult.ok("nacosCounts的数值为：" + nacosCounts);
//    }
//
//    @ApiOperation(value = "hello - 这是一个hello的测试路由")//接口名
//    @GetMapping("hello")
//    public Object hello() {
//        Stu stu = new Stu("imooc", 18);
//        //以日志的形式去打印
//        log.debug(stu.toString());
//        log.info(stu.toString());
//        log.warn(stu.toString());
//        log.error(stu.toString());
//        return GraceJSONResult.ok(stu);
////        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_GLOBAL);
////        return GraceJSONResult.ok("Hello SpringBoot~");
//    }
//
//    @GetMapping("sms")
//    public Object sms() throws Exception {
//        String code = "123456";
//        smsUtils.sendSMS("15347268843", code);
//        return GraceJSONResult.ok();
//    }
//
//    @GetMapping("produce")
//    public Object produce() throws Exception {
//
//
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.EXCHANGE_MSG,
//                "sys.msg.send",
//                "我发了一个消息~~");
//
//
//        /**
//         * 路由规则
//         * route-key
//         * display.
//         *      display.a.b
//         *      display.public.msg
//         *      display.a.b.c
//         *      * 代表一个占位符
//         *
//         *  display.#
//         *      display.a.b
//         *      display.a.b.c.d
//         *      display.public.msg
//         *      display.delete.msg.do
//         *      # 代表多个占位符
//         *
//         */
//
//        return GraceJSONResult.ok();
//    }
//
//    @GetMapping("produce2")
//    public Object produce2() throws Exception {
//
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.EXCHANGE_MSG,
//                "sys.msg.delete",
//                "我删除了一个消息~~");
//
//        return GraceJSONResult.ok();
//    }
//}
