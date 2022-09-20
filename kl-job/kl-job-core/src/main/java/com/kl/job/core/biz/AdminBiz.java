package com.kl.job.core.biz;

import com.kl.job.core.biz.model.HandleCallbackParam;
import com.kl.job.core.biz.model.RegistryParam;
import com.kl.job.core.biz.model.ReturnT;

import java.util.List;

public interface AdminBiz {

    ReturnT<String> callbcak(List<HandleCallbackParam> callbackParamList);

    ReturnT<String> register(RegistryParam registryParam);

    ReturnT<String> registerRemove(RegistryParam registryParam);
}
