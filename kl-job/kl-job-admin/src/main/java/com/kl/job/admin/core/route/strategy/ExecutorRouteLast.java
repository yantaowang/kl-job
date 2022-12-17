package com.kl.job.admin.core.route.strategy;

import com.kl.job.admin.core.route.ExecutorRouter;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ExecutorRouteLast extends ExecutorRouter {


    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        return new ReturnT<>(addressList.get(addressList.size() - 1));
    }
}
