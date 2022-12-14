package com.kx.service.service.impl;

import com.github.pagehelper.PageHelper;
import com.kx.common.enums.MessageEnum;
import com.kx.common.enums.YesOrNo;
import com.kx.common.utils.JsonUtils;
import com.kx.common.utils.PagedGridResult;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.base.RabbitMQConfig;
import com.kx.service.data.bo.VlogBO;
import com.kx.service.data.mo.MessageMO;
import com.kx.service.data.pojo.MyLikedVlog;
import com.kx.service.data.pojo.Vlog;
import com.kx.service.data.vo.IndexVlogVO;
import com.kx.service.mapper.MyLikedVlogMapper;
import com.kx.service.mapper.VlogMapper;
import com.kx.service.mapper.VlogMapperCustom;
import com.kx.service.service.FansService;
import com.kx.service.service.MsgService;
import com.kx.service.service.VlogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Autowired
    private FansService fansService;
    @Autowired
    private MsgService msgService;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(Long userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
        //????????????,??????????????????`??????`/????????????
        for (IndexVlogVO v : list) {
            Long vlogerId = v.getVlogerId();
            Long vlogId = v.getVlogId();

            if (userId != null) {
                // ???????????????????????????
                boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
                v.setDoIFollowVloger(doIFollowVloger);

                // ???????????????????????????????????????
                v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }

            // ???????????????????????????????????????
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(list, page);
    }

    /**
     * ????????????????????????????????????
     * @param myId
     * @param vlogId
     * @return
     */
    private boolean doILikeVlog(Long myId, Long vlogId) {

        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    @Override
    public IndexVlogVO getVlogDetailById(Long userId, Long vlogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);

        if (list != null && list.size() > 0 && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            return setterVO(vlogVO, userId);//?????????????????????`??????/??????`??????
        }

        return null;
    }

    /**
     * ???????????????`??????`/????????????
     * @param v
     * @param userId
     * @return
     */
    private IndexVlogVO setterVO(IndexVlogVO v, Long userId) {
        Long vlogerId = v.getVlogerId();
        Long vlogId = v.getVlogId();

        if (userId != null) {
            // ???????????????????????????
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            // ???????????????????????????????????????
            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }

        // ???????????????????????????????????????
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));

        return v;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(Long userId,
                                        Long vlogId,
                                        Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);//??????????????????????????????

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    /**
     * ????????????????????????
     * @param userId
     * @param page
     * @param pageSize
     * @param yesOrNo
     * @return
     */
    @Override
    public PagedGridResult queryMyVlogList(Long userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize);
        List<Vlog> list = vlogMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    /**
     * ????????????????????????????????????
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult getMyLikedVlogList(Long userId,
                                              Integer page,
                                              Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    /**
     * ??????
     * @param userId
     * @param vlogId
     */
    @Transactional
    @Override
    public void userLikeVlog(Long userId, Long vlogId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);

        myLikedVlogMapper.insert(likedVlog);

        //?????????????????????????????????????????????   ??????????????????????????????
        Vlog vlog = this.getVlog(vlogId);//????????????????????????
        //??????????????????
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());//?????????
//        msgService.createMsg(userId,
//                vlog.getVlogerId(),
//                MessageEnum.LIKE_VLOG.type,
//                msgContent);

        // MQ????????????
        //?????????+?????????+???????????? ???????????????????????????
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));
    }

    @Override
    public Vlog getVlog(Long id) {
        return vlogMapper.selectByPrimaryKey(id);
    }
    /**
     * ????????????
     * @param userId
     * @param vlogId
     */
    @Transactional
    @Override
    public void userUnLikeVlog(Long userId, Long vlogId) {

        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);

        myLikedVlogMapper.delete(likedVlog);
    }

    /**
     * ??????????????????
     * @param vlogId
     * @return
     */
    @Override
    public Integer getVlogBeLikedCounts(Long vlogId) {
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    /**
     * ???????????????????????????????????????????????????
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult getMyFollowVlogList(Long myId,
                                               Integer page,
                                               Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : list) {
            Long vlogerId = v.getVlogerId();
            Long vlogId = v.getVlogId();

            if (myId != null) {
                // ???????????????????????????
                v.setDoIFollowVloger(true);

                // ???????????????????????????????????????
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // ???????????????????????????????????????
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    /**
     * ????????????????????????????????????
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult getMyFriendVlogList(Long myId,
                                               Integer page,
                                               Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : list) {
            Long vlogerId = v.getVlogerId();
            Long vlogId = v.getVlogId();

            if (myId != null) {
                // ???????????????????????????
                v.setDoIFollowVloger(true);

                // ???????????????????????????????????????
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // ???????????????????????????????????????
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    /**
     * ????????????????????????
     * @param vlogId
     * @param counts
     */
    @Transactional
    @Override
    public void flushCounts(Long vlogId, Integer counts) {

        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);

    }
}
