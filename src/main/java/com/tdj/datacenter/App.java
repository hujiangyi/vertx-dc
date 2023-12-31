package com.tdj.datacenter;

import com.tdj.common.Contact;
import com.tdj.common.ModuleInit;
import com.tdj.common.annotation.Dao;
import com.tdj.common.annotation.Utils;
import com.tdj.common.utils.RedisUtils;
import com.tdj.common.verticle.NacosVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

@Slf4j
public class App {
    public static void main(String[] args) {
        Contact.setVertxInstance(Vertx.vertx());
        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .addStore(new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(true)
                .setConfig(new JsonObject().put("path", "config_local.properties"))
            );
        ConfigRetriever retriever = ConfigRetriever.create(Contact.getVertxInstance(), options);
        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                try {
                    List<Class> annotations = new ArrayList<>();
                    annotations.add(Dao.class);
                    annotations.add(Utils.class);
                    Map<Class,List<Class>> clazzs = findByAnnotations("com.tdj",annotations);
                    for (Class annotation: clazzs.keySet()) {
                        String name = annotation.getName();
                        List<Class> list = clazzs.get(annotation);
                        for (Class clazz : list) {
                            Map<Class,Object> map = new HashMap<>();
                            if (Contact.beanMap.containsKey(name)) {
                                map = Contact.beanMap.get(name);
                            } else {
                                Contact.beanMap.put(name,map);
                            }
                            map.put(clazz,clazz.newInstance());
                        }
                    }
                } catch (Exception e) {
                    log.error("" , e);
                }
                Vertx.vertx().eventBus().consumer("init success", message -> {
                    log.info("nacos init success----------->");
                    try {
                        Properties nacosConfig = new Properties();
                        nacosConfig.load(new StringReader(message.body().toString()));
                        initModule(Vertx.vertx(),nacosConfig);
                    } catch (IOException e) {
                        log.error("",e);
                    }
                });
                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(ar.result());
                Contact.getVertxInstance().deployVerticle(new NacosVerticle(),deploymentOptions);
                Contact.getVertxInstance().deployVerticle(new RedisVerticle(),deploymentOptions);
//                Contact.getVertxInstance().deployVerticle(new CheckVerticle(),deploymentOptions);
                Contact.getVertxInstance().deployVerticle(new ApiVerticle(),deploymentOptions);
//                Contact.getVertxInstance().deployVerticle(new ScheduleVerticle(),deploymentOptions);
//                Contact.getVertxInstance().deployVerticle(new CheckTestVerticle(),deploymentOptions);
            }
        });
    }

    private static void initModule(Vertx vertx, Properties nacosConfig) {
        Future<Boolean> redis = initModule(vertx,nacosConfig, RedisUtils.class);
        redis.onComplete(handler->{
            log.info("All modules have been initialized and variable injection has begun.");
            vertx.eventBus().send("init doInjection",null);
        });
    }

    private static Future<Boolean> initModule(Vertx vertx, Properties nacosConfig, Class clazz) {
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        for (Annotation annotation:annotations) {
            if (annotation instanceof Dao || annotation instanceof Utils) {
                try{
                    ModuleInit moduleInit = (ModuleInit) Contact.beanMap.get(annotation.annotationType().getName()).get(clazz);
                    return moduleInit.init(vertx,nacosConfig);
                } catch (Exception e) {
                    log.error("",e);
                }
            }
        }
        return Future.succeededFuture(false);
    }


    public static Map<Class,List<Class>> findByAnnotations(String basePackage,List<Class> annotations) throws IOException, ClassNotFoundException {
        Map<Class,List<Class>> clazzs = new HashMap<>();
        findAndAddByAnnotations(basePackage, clazzs,annotations);
        return clazzs;
    }

    private static void findAndAddByAnnotations(String basePackage, Map<Class,List<Class>> clazzs,List<Class> annotations) throws IOException, ClassNotFoundException {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                // 处理普通文件系统中的类
                String packagePath = resource.getPath();
                clazzs.putAll(findScheduledMethodsInPackage(basePackage, packagePath, classLoader,annotations));
            }
            // 可以添加其他处理不同协议的逻辑，例如处理 jar 文件中的类
        }
    }

    private static Map<Class,List<Class>> findScheduledMethodsInPackage(String basePackage, String packagePath, ClassLoader classLoader,List<Class> annotations) throws ClassNotFoundException, IOException {
        Map<Class,List<Class>> clazzs = new HashMap<>();
        File packageDir = new File(packagePath);

        if (!packageDir.exists() || !packageDir.isDirectory()) {
            return clazzs;
        }

        File[] classFiles = packageDir.listFiles(file -> file.isFile() && file.getName().endsWith(".class"));
        for (File classFile : classFiles) {
            String className = basePackage + "." + classFile.getName().substring(0, classFile.getName().length() - 6);
            // 使用反射加载类
            Class<?> clazz = classLoader.loadClass(className);
            List<Class> list = new ArrayList<>();
            for (Class annotation:annotations) {
                if (clazz.isAnnotationPresent(annotation)) {
                    if (clazzs.containsKey(annotation)) {
                        list = clazzs.get(annotation);
                    } else {
                        clazzs.put(annotation,list);
                    }
                } else {
                    continue;
                }
            }
            list.add(clazz);
        }

        // 递归查找子包下的类
        File[] subDirectories = packageDir.listFiles(File::isDirectory);
        for (File subDirectory : subDirectories) {
            String subPackageName = basePackage + "." + subDirectory.getName();
            findAndAddByAnnotations(subPackageName, clazzs,annotations);
        }
        return clazzs;
    }
}
