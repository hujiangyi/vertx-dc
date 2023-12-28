package com.tdj.datacenter.scheduling.support;

import com.tdj.datacenter.annotation.Scheduled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

@Data
@Slf4j
public class CronTask implements Runnable{
    private Scheduled scheduled;
    private Method method;
    private CronTrigger cronTrigger;
    private SimpleTriggerContext triggerContext;
    private Date nextExecutionTime;

    public CronTask(Scheduled scheduled, Method method, CronTrigger cronTrigger, SimpleTriggerContext triggerContext) {
        this.scheduled = scheduled;
        this.method = method;
        this.cronTrigger = cronTrigger;
        this.triggerContext = triggerContext;
        this.nextExecutionTime = cronTrigger.nextExecutionTime(triggerContext);
    }

    @Override
    public void run() {
        triggerContext.update(triggerContext.lastScheduledExecutionTime(),new Date(),triggerContext.lastCompletionTime());
        try {
            Object obj = method.getDeclaringClass().newInstance();
            method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("",e);
        }
        triggerContext.update(triggerContext.lastScheduledExecutionTime(),triggerContext.lastActualExecutionTime(),new Date());
        nextExecutionTime = cronTrigger.nextExecutionTime(triggerContext);
    }
}
