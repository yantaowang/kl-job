package com.kl.job.core.thread;

import com.kl.job.core.biz.handler.IJobHandler;
import com.kl.job.core.biz.model.ReturnT;
import com.kl.job.core.biz.model.TriggerParam;
import com.kl.job.core.context.XxlJobContext;
import com.kl.job.core.context.XxlJobHelper;
import com.kl.job.core.executor.XxlJobExecutor;
import com.kl.job.core.log.XxlJobFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.font.TextHitInfo;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class JobThread extends Thread{
    private static Logger logger = LoggerFactory.getLogger(JobThread.class);

    private int jobId;
    private IJobHandler handler;
    private LinkedBlockingQueue<TriggerParam> triggerQueue;
    private Set<Long> triggerLogIdSet;

    private volatile boolean toStop = false;
    private String stopReason;

    private boolean runinng = false;
    private int idleTimes = 0;

    public JobThread(int jobId, IJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingQueue<TriggerParam>();
        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());

        this.setName("xxl-job,JobThread-" + jobId + "-" + System.currentTimeMillis());
    }

    public IJobHandler getHandler() {
        return handler;
    }

    public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
        if (triggerLogIdSet.contains(triggerParam.getLogId())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "id:" + triggerParam.getLogId());
        }

        triggerLogIdSet.add(triggerParam.getLogId());
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }

    public void toStop(String stopReason) {
        this.toStop = true;
        this.stopReason = stopReason;
    }

    public boolean isRunningOrHasQueue() {
        return runinng || triggerQueue.size() > 0;
    }

    @Override
    public void run() {
        try {
            handler.init();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        while (!toStop) {
            runinng = false;
            idleTimes++;

            TriggerParam triggerParam = null;
            try {
                triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (triggerParam != null) {
                    runinng = true;
                    idleTimes = 0;
                    triggerLogIdSet.remove(triggerParam.getLogId());

                    // log filename, like "logPath/yyyy-MM-dd/9999.log"
                    String logFileName = XxlJobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());
                    XxlJobContext xxlJobContext = new XxlJobContext(
                            triggerParam.getJobId(),
                            triggerParam.getExecutorParams(),
                            logFileName,
                            triggerParam.getBroadcastIndex(),
                            triggerParam.getBroadcastTotal());

                    // init job context
                    XxlJobContext.setXxlJobContext(xxlJobContext);

                    // execute
                    XxlJobHelper.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + xxlJobContext.getJobParam());

                    if (triggerParam.getExecutorTimeout()>0) {
                        Thread futureThread = null;
                        try {
                            FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    XxlJobContext.setXxlJobContext(xxlJobContext);
                                    handler.execute();
                                    return true;
                                }
                            });
                            futureThread = new Thread(futureTask);
                            futureThread.start();

                            futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                        } catch (Exception e) {
                            XxlJobHelper.log("<br>----------- xxl-job job execute timeout");
                            XxlJobHelper.log(e);

                            // handle result
                            XxlJobHelper.handleTimeout("job execute timeout ");
                        } finally {
                            futureThread.interrupt();
                        }
                    } else {
                        handler.execute();
                    }

                    if (XxlJobContext.getXxlJobContext().getHandleCode() <= 0) {
                        XxlJobHelper.handleFail("job handle result lost.");
                    } else {
                        String tempHandleMsg = XxlJobContext.getXxlJobContext().getHandleMsg();
                        tempHandleMsg = (tempHandleMsg!=null&&tempHandleMsg.length()>50000)
                                ?tempHandleMsg.substring(0, 50000).concat("...")
                                :tempHandleMsg;
                        XxlJobContext.getXxlJobContext().setHandleMsg(tempHandleMsg);
                    }

                    XxlJobHelper.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- Result: handleCode="
                            + XxlJobContext.getXxlJobContext().getHandleCode()
                            + ", handleMsg = "
                            + XxlJobContext.getXxlJobContext().getHandleMsg());
                } else {
                    if (idleTimes > 30) {
                        if(triggerQueue.size() == 0) {	// avoid concurrent trigger causes jobId-lost
                            XxlJobExecutor.removeJobThread(jobId, "excutor idel times over limit.");
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}

