package com.kx.service.data.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
public class Fans {
    @Id
    private Long id;

    /**
     * 作家用户id
     */
    @Column(name = "vloger_id")
    private Long vlogerId;

    /**
     * 粉丝用户id
     */
    @Column(name = "fan_id")
    private Long fanId;

    /**
     * 粉丝是否是vloger的朋友，如果成为朋友，则本表的双方此字段都需要设置为1，如果有一人取关，则两边都需要设置为0
     */
    @Column(name = "is_fan_friend_of_mine")
    private Integer isFanFriendOfMine;
}