package com.kl.job.admin.core.route.strategy;

import com.kl.job.admin.core.route.ExecutorRouter;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;

import java.util.List;
import java.util.Random;

public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return new ReturnT<>(address);
    }
}
