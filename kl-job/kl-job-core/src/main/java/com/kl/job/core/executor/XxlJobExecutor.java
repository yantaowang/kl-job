package com.kl.job.core.executor;

import com.kl.job.core.biz.AdminBiz;
import com.kl.job.core.biz.client.AdminBizClient;
import com.kl.job.core.log.XxlJobFileAppender;
import com.kl.job.core.thread.JobLogFileCleanThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

        initEmbedServer(address,ip,port,appname,accessToken);
    }

    private void initEmbedServer(String address, String ip, int port, String appname, String accessToken) {

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
}
