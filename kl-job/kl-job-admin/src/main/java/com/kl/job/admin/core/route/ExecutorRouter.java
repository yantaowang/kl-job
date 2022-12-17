package com.kl.job.admin.core.route;

import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    public abstract ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);
}
