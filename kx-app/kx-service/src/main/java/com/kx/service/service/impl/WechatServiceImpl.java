package com.kx.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kx.common.utils.HttpUtils;
import com.kx.service.data.model.WechatConnectSettingItem;
import com.kx.service.service.WechatService;
import org.springframework.stereotype.Service;

@Service
public class WechatServiceImpl implements WechatService {

    public static final String APP_ID = "wx030fb8b41c7574ce";
    public static final String APP_SECRET = "9964e1bff9e1403ffb7f5d3d385e050b";

    @Override
    public JSONObject getUnionIdInfo(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + APP_ID + "&" +
                "secret=" + APP_SECRET + "&" +
                "js_code=" + code + "&" +
                "grant_type=authorization_code";
        String content = HttpUtils.doGet(url, "UTF-8", 100, 1000);
        return JSON.parseObject(content);
    }
}
