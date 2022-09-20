package com.kl.job.core.thread;

import com.kl.job.core.biz.model.HandleCallbackParam;
import com.kl.job.core.executor.XxlJobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class TriggerCallbackThread {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private static TriggerCallbackThread instance  = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<>();
    public static void pushCallBack(HandleCallbackParam handleCallbackParam) {
        getInstance().callBackQueue.add(handleCallbackParam);
    }

    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;

    private volatile boolean toStop = false;
    public void start() {
        if (XxlJobExecutor.getAdminBizList() == null) {
            return;
        }

        triggerCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    try {
                        HandleCallbackParam handleCallbackParam = getInstance().callBackQueue.take();
                        if (handleCallbackParam != null) {
                            List<HandleCallbackParam> callbackParamList = new ArrayList<>();
                            getInstance().callBackQueue.drainTo(callbackParamList);
                            callbackParamList.add(handleCallbackParam);

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
