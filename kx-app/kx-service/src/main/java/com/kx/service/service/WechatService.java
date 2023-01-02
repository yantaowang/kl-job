package com.kx.service.service;

import com.alibaba.fastjson.JSONObject;

public interface WechatService {
    JSONObject getUnionIdInfo(String code);
}
