package com.kl.job.admin.core.route.strategy;

import com.kl.job.admin.core.route.ExecutorRouter;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;

import java.util.List;

public class ExecutorRouteFirst extends ExecutorRouter {
    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        return new ReturnT<>(addressList.get(0));
    }
}
