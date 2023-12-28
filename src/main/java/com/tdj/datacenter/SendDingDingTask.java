package com.tdj.datacenter;

import com.tdj.datacenter.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendDingDingTask {
    private SendDingDingTask sendDingDingTask;

    @Scheduled(cron = "0 2 0 * * ?")
    public void sendDingding() {
        sendDingDingTask = this;
        App.getVertxInstance().eventBus().send("check_cmd","");
    }
}
