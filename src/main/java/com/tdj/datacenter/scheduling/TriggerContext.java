package com.tdj.datacenter.scheduling;

import java.util.Date;

public interface TriggerContext {
    Date lastScheduledExecutionTime();

    Date lastActualExecutionTime();

    Date lastCompletionTime();
}
