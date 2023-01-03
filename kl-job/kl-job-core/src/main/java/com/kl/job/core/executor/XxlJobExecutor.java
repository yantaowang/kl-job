package com.kl.job.core.executor;

import com.kl.job.core.biz.AdminBiz;
import com.kl.job.core.biz.client.AdminBizClient;
import com.kl.job.core.biz.handler.IJobHandler;
import com.kl.job.core.biz.handler.annotation.XxlJob;
import com.kl.job.core.biz.handler.impl.MethodJobHandler;
import com.kl.job.core.log.XxlJobFileAppender;
import com.kl.job.core.server.EmbedServer;
import com.kl.job.core.thread.JobLogFileCleanThread;
import com.kl.job.core.thread.JobThread;
import com.kl.job.core.util.IpUtil;
import com.kl.job.core.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import sun.awt.EmbeddedFrame;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class XxlJobExecutor {
    public static final Logger logger = LoggerFactory.getLogger(XxlJobExecutor.class);

    private String adminAddress;
    private String accessToken;
    private String appname;
    private String address;
    private String ip;
    private String logPath;
    private int port;
    private int logRetentionDays;

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public void start() throws Exception {
        XxlJobFileAppender.initLogPath(logPath);

        initAdminBizList(adminAddress, accessToken);

        JobLogFileCleanThread.getInstance().start(logRetentionDays);

        // TODO: 2022-09-27

        initEmbedServer(address,ip,port,appname,accessToken);
    }

    private EmbedServer embedServer = null;

    private void initEmbedServer(String address, String ip, int port, String appname, String accessToken) {
        port = port > 0?port : NetUtil.findAvailablePort(9999);
        ip = (ip != null && ip.trim().length()>0)? ip : IpUtil.getIp();

        if (address == null || address.trim().length() == 0) {
            String ip_port_address = IpUtil.getIpPort(ip, port);
            address = "http://{ip_port}/".replace("{ip_port}", ip_port_address);
        }

        if (StringUtils.isEmpty(accessToken)) {
            logger.warn("accesstoken is null");
        }

        embedServer = new EmbedServer();
        embedServer.start(address,port,appname,accessToken);
    }

    private static List<AdminBiz> adminBizList;
    private void initAdminBizList(String adminAddress, String accessToken) {
        if (!StringUtils.isEmpty(adminAddress)) {
            for (String address : adminAddress.trim().split(",")) {
                if (!StringUtils.isEmpty(address)) {
                    AdminBiz adminBiz = new AdminBizClient(address.trim(), accessToken);
                    if (adminBizList == null) {
                        adminBizList = new ArrayList<>();
                    }
                    adminBizList.add(adminBiz);
                }
            }
        }
    }

    public static List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }

    private static ConcurrentMap<String, IJobHandler> jobHandlerRepository = new ConcurrentHashMap<>();
    public static IJobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }
    public static IJobHandler registJobHandler(String name, IJobHandler jobHandler){
        logger.info(">>>>>>>>>>> xxl-job register jobhandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }
    public void registJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        if (xxlJob == null) {
            return;
        }
        String name = xxlJob.value();
        Class<?> clazz = bean.getClass();
        String methodName = executeMethod.getName();
        if (name.trim().length() == 0) {
            throw new RuntimeException("xxl-job method");
        }

        if (loadJobHandler(name) != null) {
            throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
        }
        executeMethod.setAccessible(true);

        Method initMethod = null;
        Method destroyMethod = null;
        if (xxlJob.init().trim().length() > 0) {
            try {
                initMethod = clazz.getDeclaredMethod(xxlJob.init());
                initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-jobhandler initMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }
        if (xxlJob.destroy().trim().length() > 0) {
            try {
                clazz.getDeclaredMethod(xxlJob.destroy());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-jobhandler destroyMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }

        // registry jobhandler
        registJobHandler(name, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));
    }

    private static ConcurrentMap<Integer, JobThread> jobThreadRepository = new ConcurrentHashMap<Integer, JobThread>();
    public static JobThread registJobThread(int jobId, IJobHandler handler, String removeOldReason){
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        logger.info(">>>>>>>>>>> xxl-job regist JobThread success, jobId:{}, handler:{}", new Object[]{jobId, handler});

        JobThread oldJobThread = jobThreadRepository.put(jobId, newJobThread);	// putIfAbsent | oh my god, map's put method return the old value!!!
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
    }

    public static JobThread removeJobThread(int jobId, String removeOldReason){
        JobThread oldJobThread = jobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();

            return oldJobThread;
        }
        return null;
    }

    public static JobThread loadJobThread(int jobId){
        return jobThreadRepository.get(jobId);
    }
}
