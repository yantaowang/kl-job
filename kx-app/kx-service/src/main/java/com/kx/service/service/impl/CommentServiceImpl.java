package com.kx.service.service.impl;

import com.github.pagehelper.PageHelper;
import com.kx.common.enums.MessageEnum;
import com.kx.common.enums.YesOrNo;
import com.kx.common.utils.JsonUtils;
import com.kx.common.utils.PagedGridResult;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.base.RabbitMQConfig;
import com.kx.service.data.bo.CommentBO;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.data.pojo.Comment;
import com.kx.service.data.pojo.Vlog;
import com.kx.service.data.vo.CommentVO;
import com.kx.service.mapper.CommentMapper;
import com.kx.service.mapper.CommentMapperCustom;
import com.kx.service.service.CommentService;
import com.kx.service.service.MsgService;
import com.kx.service.service.VlogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private MsgService msgService;
    @Autowired
    private VlogService vlogService;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Override
    public CommentVO createComment(CommentBO commentBO) {
        //1.评论入库

        Comment comment = new Comment();
        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        //2 redis操作放在service中，评论总数的累加
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        //3 留言后的最新评论需要返回给前端进行展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        //评论/回复后让系统给对方发送一条消息 系统消息：评论/回复
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", comment.getId());
        msgContent.put("commentContent", commentBO.getContent());//消息为评论/回复的具体内容
        //Integer type = MessageEnum.COMMENT_VLOG.type;//默认消息类型:评论视频
        String receiverUserId= commentBO.getVlogerId();
        String routeType = MessageEnum.COMMENT_VLOG.enValue;//既是消息类型又是路由
        //当前评论有父评论,则消息类型是回复评论
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
                !commentBO.getFatherCommentId().equalsIgnoreCase("0") ) {
            //查询被回复的这条评论的主人是谁,把系统消息发给他
            Comment fatherComment = getComment(commentBO.getFatherCommentId());
             receiverUserId = fatherComment.getCommentUserId();
            routeType = MessageEnum.REPLY_YOU.enValue;
        }

//        msgService.createMsg(commentBO.getCommentUserId(),
//                receiverUserId,//收消息的人
//                type,
//                msgContent);
        // MQ异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId());
        messageMO.setToUserId(receiverUserId);
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + routeType,
                JsonUtils.objectToJson(messageMO));
        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize) {
        //1.查询评论数据
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        PageHelper.startPage(page, pageSize);

        List<CommentVO> list = commentMapperCustom.getCommentList(map);
        //2.遍历视频所有评论,查询单个评论的详情数据
        for (CommentVO cv:list) {
            String commentId = cv.getCommentId();

            // 当前短视频的某个评论的点赞总数
            String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);

            // 判断当前用户是否点赞过该评论
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId) {

        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);

        commentMapper.delete(pendingDelete);

        // 评论总数的累减
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    /**
     * 查询某个评论信息
     * @param id
     * @return
     */
    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
}
