package com.kl.job.core.biz;

import com.kl.job.core.biz.model.*;

public interface ExecutorBiz {
    ReturnT<String> beat();

    ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);

    ReturnT<String> run(TriggerParam triggerParam);

    ReturnT<String> kill(KillParam killParam);

    ReturnT<LogResult> log(LogParam logParam);
}
