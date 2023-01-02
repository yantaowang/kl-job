package com.kl.job.admin.core.route.strategy;

import com.kl.job.admin.core.route.ExecutorRouter;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorRouteLRU extends ExecutorRouter {

    private static ConcurrentHashMap<Integer, LinkedHashMap<String, String>> jobLRUMap = new ConcurrentHashMap<>();
    private static long CACHE_VALID_TIME = 0;

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = route(triggerParam.getJobId(), addressList);
        return new ReturnT<>(address);
    }

    public String route(int jobId, List<String> addressList) {
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;;
        }
        LinkedHashMap<String, String> lruItem = jobLRUMap.get(jobId);
        if (lruItem == null) {
            lruItem = new LinkedHashMap<>(16, 0.75f, true);
            jobLRUMap.putIfAbsent(jobId, lruItem);
        }
        for (String address : addressList) {
            if (!lruItem.containsKey(address)) {
                lruItem.put(address, address);
            }
        }
        List<String> delKeys = new ArrayList<>();
        for(String existKey : lruItem.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if(delKeys.size() > 0) {
            for (String delKey : delKeys) {
                lruItem.remove(delKey);
            }
        }

        String eldestKey = lruItem.entrySet().iterator().next().getKey();
        String eldestValue = lruItem.get(eldestKey);
        return eldestValue;
    }
}
