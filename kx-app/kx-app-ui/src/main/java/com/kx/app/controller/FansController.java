package com.kx.app.controller;

import com.kx.common.result.GraceJSONResult;
import com.kx.common.result.ResponseStatusEnum;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.data.pojo.Users;
import com.kx.service.service.FansService;
import com.kx.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "FansController 粉丝相关业务功能的接口")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {

    @Autowired
    private UserService userService;
    @Autowired
    private FansService fansService;

    @ApiOperation(value = "关注")//接口名
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam Long myId,
                                  @RequestParam Long vlogerId) {

        //1 判断两个id不能为空
        if (myId == null || vlogerId == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        //2 判断当前用户，自己不能关注自己
        if (myId.equals(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        //3 判断两个id对应的用户是否存在
        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);

        // fixme: 两个用户id的数据库查询后的判断，是分开好？还是合并判断好？
        if (myInfo == null || vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        //4 保存粉丝关系到数据库
        fansService.doFollow(myId, vlogerId);

        //5 博主的粉丝+1，我的关注+1
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //6 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "取消关注")//接口名
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam Long myId,
                                  @RequestParam Long vlogerId) {

        //1 删除业务的执行
        fansService.doCancel(myId, vlogerId);

        //2 博主的粉丝-1，我的关注-1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //3 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "查询是否已关注作者")//接口名
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam Long myId,
                                                @RequestParam Long vlogerId) {
        //这里最好是去redis去查
        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId, vlogerId));
    }

    @ApiOperation(value = "查询我的关注")//接口名
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam Long myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fansService.queryMyFollows(
                        myId,
                        page,
                        pageSize));
    }
    @ApiOperation(value = "查询我的粉丝")//接口名
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam Long myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fansService.queryMyFans(
                        myId,
                        page,
                        pageSize));
    }
}
