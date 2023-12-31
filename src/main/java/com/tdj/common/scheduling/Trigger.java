package com.tdj.common.scheduling;
import java.util.Date;

public interface Trigger {
    Date nextExecutionTime(TriggerContext var1);
}
