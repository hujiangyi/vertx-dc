package com.tdj.datacenter;

import com.tdj.datacenter.annotation.Scheduled;
import com.tdj.datacenter.scheduling.TriggerContext;
import com.tdj.datacenter.scheduling.support.CronTask;
import com.tdj.datacenter.scheduling.support.CronTrigger;
import com.tdj.datacenter.scheduling.support.SimpleTriggerContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ScheduleVerticle extends AbstractVerticle {
    private List<CronTask> schedules = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            List<Method> methods = findScheduledMethods("com.tdj");
            for (Method method : methods) {
                if (method.isAnnotationPresent(Scheduled.class)) {
                    Scheduled scheduleAnnotation = method.getAnnotation(Scheduled.class);
                    String cronExpression = scheduleAnnotation.cron();
                    SimpleTriggerContext triggerContext = new SimpleTriggerContext();
                    schedules.add(new CronTask(scheduleAnnotation, method, new CronTrigger(cronExpression), triggerContext));
                }
            }
        } catch (Exception e) {
            log.error("" , e);
        }
        vertx.eventBus().consumer("schedule_test", message -> {
            // 处理收到的消息
            String body = (String) message.body();
            for (CronTask cronTask : schedules) {
                cronTask.run();
            }
        });
        // 在这里添加执行定时任务的逻辑
        vertx.setPeriodic(1000, timerId -> {
            for (CronTask cronTask : schedules) {
                try {
                    if (cronTask.getNextExecutionTime().before(new Date())) {
                        SimpleTriggerContext simpleTriggerContext = cronTask.getTriggerContext();
                        simpleTriggerContext.update(new Date(),simpleTriggerContext.lastActualExecutionTime(),simpleTriggerContext.lastCompletionTime());
                        vertx.executeBlocking((Promise<CronTask> future) -> {
                            cronTask.run();
                            future.complete(cronTask);
                        }, result -> {
                            if (result.succeeded()) {
                                CronTask ct = result.result();
                                log.info("{}任务被在{}执行", ct.getScheduled().name(), sdf.format(ct.getTriggerContext().lastActualExecutionTime()));
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
    }

    public List<Method> findScheduledMethods(String basePackage) throws IOException, ClassNotFoundException {
        List<Method> scheduledMethods = new ArrayList<>();
        findAndAddScheduledMethods(basePackage, scheduledMethods);
        return scheduledMethods;
    }

    private void findAndAddScheduledMethods(String basePackage, List<Method> scheduledMethods) throws IOException, ClassNotFoundException {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                // 处理普通文件系统中的类
                String packagePath = resource.getPath();
                scheduledMethods.addAll(findScheduledMethodsInPackage(basePackage, packagePath, classLoader));
            }
            // 可以添加其他处理不同协议的逻辑，例如处理 jar 文件中的类
        }
    }

    private List<Method> findScheduledMethodsInPackage(String basePackage, String packagePath, ClassLoader classLoader) throws ClassNotFoundException, IOException {
        List<Method> scheduledMethods = new ArrayList<>();
        File packageDir = new File(packagePath);

        if (!packageDir.exists() || !packageDir.isDirectory()) {
            return scheduledMethods;
        }

        File[] classFiles = packageDir.listFiles(file -> file.isFile() && file.getName().endsWith(".class"));
        for (File classFile : classFiles) {
            String className = basePackage + "." + classFile.getName().substring(0, classFile.getName().length() - 6);
            // 使用反射加载类
            Class<?> clazz = classLoader.loadClass(className);
            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                // 查找带有 @Scheduled 注解的方法
                if (method.isAnnotationPresent(Scheduled.class)) {
                    scheduledMethods.add(method);
                }
            }
        }

        // 递归查找子包下的类
        File[] subDirectories = packageDir.listFiles(File::isDirectory);
        for (File subDirectory : subDirectories) {
            String subPackageName = basePackage + "." + subDirectory.getName();
            String subPackagePath = packagePath + "/" + subDirectory.getName();
            findAndAddScheduledMethods(subPackageName, scheduledMethods);
        }

        return scheduledMethods;
    }
}