package com.tdj.datacenter.scheduling.support;


import com.tdj.datacenter.scheduling.TriggerContext;

import java.util.Date;

public class SimpleTriggerContext implements TriggerContext {
    private volatile Date lastScheduledExecutionTime;
    private volatile Date lastActualExecutionTime;
    private volatile Date lastCompletionTime;

    public SimpleTriggerContext() {
        lastScheduledExecutionTime = new Date();
        lastActualExecutionTime = new Date();
        lastCompletionTime = new Date();
    }

    public SimpleTriggerContext(Date lastScheduledExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
        this.lastScheduledExecutionTime = lastScheduledExecutionTime;
        this.lastActualExecutionTime = lastActualExecutionTime;
        this.lastCompletionTime = lastCompletionTime;
    }

    public void update(Date lastScheduledExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
        this.lastScheduledExecutionTime = lastScheduledExecutionTime;
        this.lastActualExecutionTime = lastActualExecutionTime;
        this.lastCompletionTime = lastCompletionTime;
    }

    public Date lastScheduledExecutionTime() {
        return this.lastScheduledExecutionTime;
    }

    public Date lastActualExecutionTime() {
        return this.lastActualExecutionTime;
    }

    public Date lastCompletionTime() {
        return this.lastCompletionTime;
    }
}
