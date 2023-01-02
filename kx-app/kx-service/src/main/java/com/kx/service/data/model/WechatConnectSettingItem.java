package com.kx.service.data.model;

import lombok.Data;

/**
 * 微信设置
 *
 * @author Chopper
 * @since 2020/11/17 8:00 下午
 */
@Data
public class WechatConnectSettingItem {


    /**
     * @See ClientType
     */
    private String clientType;

    private String appId;

    private String appSecret;
}
