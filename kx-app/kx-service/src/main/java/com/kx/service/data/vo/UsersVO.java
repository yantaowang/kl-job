package com.kx.service.data.vo;

import com.kx.service.util.token.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersVO {
    private Long id;
    private String mobile;
    private String unionId;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    private Integer myFollowsCounts;
    private Integer myFansCounts;
//    private Integer myLikedVlogCounts;
    private Integer totalLikeMeCounts;

    private Token token;
}