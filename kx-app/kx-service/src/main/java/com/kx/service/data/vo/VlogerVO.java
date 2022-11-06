package com.kx.service.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogerVO {
    private Long vlogerId;
    private String nickname;
    private String face;
    private boolean isFollowed = true;
}