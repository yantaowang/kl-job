package com.kx.service.service;

import com.kx.service.data.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {

    /**
     * 创建消息
     */
    public void createMsg(Long fromUserId,
                          Long toUserId,
                          Integer type,
                          Map msgContent);

    /**
     * 查询消息列表
     */
    public List<MessageMO> queryList(Long toUserId,
                                     Integer page,
                                     Integer pageSize);

}
