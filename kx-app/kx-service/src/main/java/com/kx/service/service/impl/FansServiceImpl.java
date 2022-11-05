package com.kx.service.service.impl;

import com.github.pagehelper.PageHelper;

import com.kx.common.enums.MessageEnum;
import com.kx.common.enums.YesOrNo;
import com.kx.common.utils.JsonUtils;
import com.kx.common.utils.PagedGridResult;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.base.RabbitMQConfig;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.data.pojo.Fans;
import com.kx.service.data.vo.FansVO;
import com.kx.service.data.vo.VlogerVO;
import com.kx.service.mapper.FansMapper;
import com.kx.service.mapper.FansMapperCustom;
import com.kx.service.service.FansService;
import com.kx.service.service.MsgService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;
    @Autowired
    private MsgService msgService;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    /**
     * 关注:
     *
     * @param myId
     * @param vlogerId
     */
    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {

        Fans fans = new Fans();
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);

        //1.判断对方是否关注我，如果关注我，那么双方都要互为朋友关系
        Fans vloger = queryFansRelationship(vlogerId, myId);//把对方当做粉丝作为查询条件,是否关注我
        if (vloger != null) {
            //1.1给新增的我是粉丝的关系中标记双方是互粉关系
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            //1.2修改对方关注我的粉丝关系中为互粉关系
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        } else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);

        //2. 系统消息：关注  (消息->mongodb 变成 消息->生产者->rabbitmq->消费者->mongodb)
        //2.1关注对方后让系统给对方发送一条消息
        //当前接口不再直接与mongodb交互
        //msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);//关注,没有消息内容

        //2.1此时的消息不再是消息内容,而是包含发送者+接收者+消息内容 ,最后打包成json发送
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        // 优化：使用mq异步解耦
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,//路由+业务消息类型 组成一个完成的路由
                JsonUtils.objectToJson(messageMO));
    }

    /**
     * 查询粉丝关系
     *
     * @param fanId
     * @param vlogerId
     * @return
     */
    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);

        List list = fansMapper.selectByExample(example);

        Fans fan = null;
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            fan = (Fans) list.get(0);
        }

        return fan;
    }

    /**
     * 取消关注
     * @param myId
     * @param vlogerId
     */
    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {

        //1 判断我们是否朋友关系，如果是，则需要取消双方的关系
        Fans fan = queryFansRelationship(myId, vlogerId);
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            //2.修改对方的关注数据-不是互粉关系     抹除双方的朋友关系，自己的关系删除即可
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }

        //3. 删除自己的关注关联表记录
        fansMapper.delete(fan);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        Fans vloger = queryFansRelationship(myId, vlogerId);
        return vloger != null;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId,
                                          Integer page,
                                          Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId,
                                       Integer page,
                                       Integer pageSize) {

        /**
         * <判断粉丝是否是我的朋友（互粉互关）>
         * 普通做法：
         * 多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下回出现性能问题
         *
         * 常规做法：
         * 1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
         * 2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList，获得每一个粉丝，再去数据库查询我是否关注他
         * 3. 如果我也关注他（粉丝），说明，我俩互为朋友关系（互关互粉），则标记flag为true，否则false
         *
         * 高端做法：
         * 1. 关注/取关的时候，关联关系保存在redis中，不要依赖数据库
         * 2. 数据库查询后，直接循环查询redis，避免第二次循环查询数据库的尴尬局面
         */


        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<FansVO> list = fansMapperCustom.queryMyFans(map);

        for (FansVO f : list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list, page);
    }
}
