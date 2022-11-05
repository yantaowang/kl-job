package com.kx.app.controller;

import com.kx.common.enums.MessageEnum;
import com.kx.common.result.GraceJSONResult;
import com.kx.common.utils.JsonUtils;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.base.RabbitMQConfig;
import com.kx.service.data.bo.CommentBO;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.data.pojo.Comment;
import com.kx.service.data.pojo.Vlog;
import com.kx.service.data.vo.CommentVO;
import com.kx.service.service.CommentService;
import com.kx.service.service.MsgService;
import com.kx.service.service.VlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "CommentController 评论模块的接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;
    @Autowired
    private MsgService msgService;
    @Autowired
    private VlogService vlogService;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @ApiOperation(value = "新增评论")//接口名
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO)
            throws Exception {

        CommentVO commentVO = commentService.createComment(commentBO);
        return GraceJSONResult.ok(commentVO);
    }
    @ApiOperation(value = "统计评论数")//接口名
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {

        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }

        return GraceJSONResult.ok(Integer.valueOf(countsStr));
    }
    @ApiOperation(value = "视频所有评论查询")//接口名
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {

        return GraceJSONResult.ok(
                commentService.queryVlogComments(
                        vlogId,
                        userId,
                        page,
                        pageSize));
    }

    @ApiOperation(value = "删除评论")//接口名
    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                @RequestParam String commentId,
                                @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId,
                                    commentId,
                                    vlogId);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "点赞评论")//接口名
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId) {

        // 故意犯错，bigkey
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);//累加评论被喜欢的数量
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");//建立用户与某评论的喜欢关系
//        redis.hset(REDIS_USER_LIKE_COMMENT, userId, "1");

        //对别人的评论点赞后让系统发送一条消息 系统消息：点赞评论
        Comment comment = commentService.getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent = new HashMap();
        //封装消息内容
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);

//        msgService.createMsg(userId,
//                comment.getCommentUserId(),
//                MessageEnum.LIKE_COMMENT.type,
//                msgContent);

        // MQ异步解耦
        //与消息相关的信息都封装成一个消息
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getCommentUserId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_COMMENT.enValue,
                JsonUtils.objectToJson(messageMO));
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "取消点赞评论")//接口名
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId,
                                  @RequestParam String userId) {

        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);//评论被喜欢的数量减1
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);//删掉用户与某评论的喜欢关系

        return GraceJSONResult.ok();
    }
}
