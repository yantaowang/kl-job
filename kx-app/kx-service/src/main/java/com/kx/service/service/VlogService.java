package com.kx.service.service;

import com.kx.common.utils.PagedGridResult;
import com.kx.service.data.bo.VlogBO;
import com.kx.service.data.pojo.Vlog;
import com.kx.service.data.vo.IndexVlogVO;

public interface VlogService {

    /**
     * 新增vlog视频
     */
    public void createVlog(VlogBO vlogBO);

    /**
     * 查询首页/搜索的vlog列表
     */
    public PagedGridResult getIndexVlogList(Long userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize);
    /**
     * 根据视频主键查询vlog
     */
    public IndexVlogVO getVlogDetailById(Long userId, Long vlogId);

    /**
     * 用户把视频改为公开/私密的视频
     */
    public void changeToPrivateOrPublic(Long userId,
                                        Long vlogId,
                                        Integer yesOrNo);

    /**
     * 查询用的公开/私密的视频列表
     */
    public PagedGridResult queryMyVlogList(Long userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo);

    /**
     * 查询用户点赞过的短视频 用户主页点赞视频列表查询
     */
    public PagedGridResult getMyLikedVlogList(Long userId,
                                              Integer page,
                                              Integer pageSize);

    /**
     * 点赞/喜欢某视频
     */
    public void userLikeVlog(Long userId, Long vlogId);

    /**
     * 用户取消点赞/喜欢视频
     */
    public void userUnLikeVlog(Long userId, Long vlogId);

    /**
     * 某视频获得用户点赞视频的总数
     */
    public Integer getVlogBeLikedCounts(Long vlogId);

    /**
     * 查询用户关注的博主发布的短视频列表
     */
    public PagedGridResult getMyFollowVlogList(Long myId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 查询朋友发布的短视频列表
     */
    public PagedGridResult getMyFriendVlogList(Long myId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 根据主键查询vlog
     */
    public Vlog getVlog(Long id);

    /**
     * 把counts输入数据库
     */
    public void flushCounts(Long vlogId, Integer counts);
}
