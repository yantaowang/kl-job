package com.kx.service.service;


import com.kx.common.utils.PagedGridResult;

public interface FansService {

    /**
     * 关注
     */
    public void doFollow(Long myId, Long vlogerId);

    /**
     * 取关
     */
    public void doCancel(Long myId, Long vlogerId);

    /**
     * 查询用户是否关注博主
     */
    public boolean queryDoIFollowVloger(Long myId, Long vlogerId);

    /**
     * 查询我关注的博主列表
     */
    public PagedGridResult queryMyFollows(Long myId,
                                          Integer page,
                                          Integer pageSize);

    /**
     * 查询我的粉丝列表
     */
    public PagedGridResult queryMyFans(Long myId,
                                       Integer page,
                                       Integer pageSize);
}
