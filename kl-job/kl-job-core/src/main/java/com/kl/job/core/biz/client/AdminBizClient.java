package com.kl.job.core.biz.client;

import com.kl.job.core.biz.AdminBiz;
import com.kl.job.core.biz.model.HandleCallbackParam;
import com.kl.job.core.biz.model.RegistryParam;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.util.XxlJobRemotingUtil;
import org.springframework.stereotype.Service;

import java.util.List;

public class AdminBizClient implements AdminBiz {

    private String addressUrl;
    private String accessToken;
    private int timeout = 3;

    public AdminBizClient() {
    }

    public AdminBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;

        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/callbcak", accessToken, timeout,
                callbackParamList,String.class);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/register", accessToken, timeout,registryParam,String.class);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/redistryRemove", accessToken, timeout,registryParam,String.class);
    }
}
