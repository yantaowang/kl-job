package com.kl.job.core.biz;

import com.kl.job.core.biz.model.HandleCallbackParam;
import com.kl.job.core.biz.model.RegistryParam;
import com.kl.job.core.biz.model.ReturnT;

import java.util.List;

public interface AdminBiz {

    // ---------------------- callback ----------------------

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    // ---------------------- registry ----------------------

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registryRemove(RegistryParam registryParam);

}
