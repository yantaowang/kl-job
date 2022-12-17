package com.kx.service.service;

import com.kx.service.data.bo.RegistLoginBO;
import com.kx.service.data.bo.UpdatedUserBO;
import com.kx.service.data.pojo.Users;

public interface UserService {

    /**
     * 判断用户是否存在，如果存在则返回用户信息
     */
    public Users queryMobileIsExist(String mobile);

    Users queryByUnionId(String unionId);

    /**
     * 创建用户信息，并且返回用户对象
     */
    public Users createUser(String mobile);

    /**
     * 创建用户信息，并且返回用户对象
     */
    public Users createUser(RegistLoginBO registLoginBO);

    /**
     * 根据用户主键查询用户信息
     */
    public Users getUser(Long userId);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);
}
