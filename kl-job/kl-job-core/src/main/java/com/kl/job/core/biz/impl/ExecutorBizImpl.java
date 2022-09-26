package com.kl.job.core.biz.impl;

import com.kl.job.core.biz.AdminBiz;
import com.kl.job.core.biz.ExecutorBiz;
import com.kl.job.core.biz.model.*;

import java.util.List;

public class ExecutorBizImpl implements ExecutorBiz {

    @Override
    public ReturnT<String> beat() {
        return null;
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return null;
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return null;
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return null;
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return null;
    }
}
