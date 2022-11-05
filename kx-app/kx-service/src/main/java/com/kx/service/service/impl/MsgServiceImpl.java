package com.kx.service.service.impl;

import com.kx.common.enums.MessageEnum;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.data.pojo.Users;
import com.kx.service.mapper.repository.MessageRepository;
import com.kx.service.service.MsgService;
import com.kx.service.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Override
    public void createMsg(Long fromUserId,
                          Long toUserId,
                          Integer type,
                          Map msgContent) {
        //1.从数据库拿到发消息的用户数据 再加收消息的人的数据 再加消息内容 存到mongodb
        Users fromUser = userService.getUser(fromUserId);

        MessageMO messageMO = new MessageMO();
        //1.1 发消息的人的数据
        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());
        //1.2收消息的人的数据
        messageMO.setToUserId(toUserId);
        //1.3消息类型与消息内容
        messageMO.setMsgType(type);
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    /**
     * 查询某用户的消息 集合
     * @param toUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public List<MessageMO> queryList(Long toUserId,
                                     Integer page,
                                     Integer pageSize) {

        Pageable pageable = PageRequest.of(page,
                                            pageSize,
                                            Sort.Direction.DESC,
                                            "createTime");
        //1.mongodb的查询也可以分页
        List<MessageMO> list =  messageRepository
                        .findAllByToUserIdEqualsOrderByCreateTimeDesc(toUserId,
                                                                pageable);
        //2.遍历消息,查询添加互粉的信息
        for (MessageMO msg : list) {
            // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }

                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
             }
        }
        return list;
    }
}
