package com.kx.controller;

import com.kx.common.enums.YesOrNo;
import com.kx.common.result.GraceJSONResult;
import com.kx.common.utils.PagedGridResult;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.data.bo.VlogBO;
import com.kx.service.service.VlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO) {
        // FIXME 作业，校验VlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    /**
     * 查询首页/搜索的vlog视频列表:包含分页功能
     * @param userId
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") Long userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        //未传分页数据,则使用默认的分页变量
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getIndexVlogList(userId, search, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 查询视频详情
     * @param userId
     * @param vlogId
     * @return
     */
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") Long userId,
                                  @RequestParam Long vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }

    /**
     * 将视频设为私密
     * @param userId
     * @param vlogId
     * @return
     */
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam Long userId,
                                           @RequestParam Long vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    /**
     * 设为公开
     * @param userId
     * @param vlogId
     * @return
     */
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam Long userId,
                                          @RequestParam Long vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "查询我发布的公开视频")//接口名
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam Long userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        //有传分页则用前端的 没有在用后端默认的
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.NO.type);
        return GraceJSONResult.ok(gridResult);
    }

    @ApiOperation(value = "查询我的私密视频集合")//接口名
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam Long userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.YES.type);
        return GraceJSONResult.ok(gridResult);
    }

    @ApiOperation(value = "查询我赞过的视频集合")//用户主页点赞视频列表查询
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam Long userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyLikedVlogList(userId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
//    @Value("${nacos.counts:1}")
    private static Integer nacosCounts=3;
    @ApiOperation(value = "点赞")//接口名
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam Long userId,
                                @RequestParam Long vlogerId,
                                @RequestParam Long vlogId) {

        // 我点赞的视频，关联关系保存到数据库
        vlogService.userLikeVlog(userId, vlogId);

        // 点赞后，视频和视频发布者的获赞都会 +1
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        // 我点赞的视频，需要在redis中保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        // 点赞完毕，获得当前在redis中的总数
        // 比如获得总计数为 1k/1w/10w，假定阈值（配置）为2000
        // 此时1k满足2000，则触发入库
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("======" + REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "======");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {
                vlogService.flushCounts(vlogId, counts);
            }
        }
        return GraceJSONResult.ok();
    }
    @ApiOperation(value = "取消点赞")//接口名
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam Long userId,
                                  @RequestParam Long vlogerId,
                                  @RequestParam Long vlogId) {

        //1. 我取消点赞的视频，关联关系删除
        vlogService.userUnLikeVlog(userId, vlogId);
        //2.redis操作
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "视频获赞总数查询")//用于点赞过后的刷新页面时更新点赞数量时调用
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam Long vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }

    @ApiOperation(value = "关注的作者的视频列表查询")
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam Long myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @ApiOperation(value = "互粉的朋友的视频列表查询")
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam Long myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
}
