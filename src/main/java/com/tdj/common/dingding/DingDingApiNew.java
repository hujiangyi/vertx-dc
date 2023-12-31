package com.tdj.common.dingding;

import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 *  新版钉钉sdk
 * </p>
 *
 * @author jay
 * @since 2023/12/27
 */
@Slf4j
public class DingDingApiNew {
    public static com.aliyun.dingtalkoauth2_1_0.Client createAuth_2_1_0_Client() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }
    public static com.aliyun.dingtalkrobot_1_0.Client createRobot_1_0_Client() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkrobot_1_0.Client(config);
    }

    /**
     * 获取钉钉的token
     *
     * @return
     */
    public String getToken() {
        String token = "";
        try {

            com.aliyun.dingtalkoauth2_1_0.Client client = createAuth_2_1_0_Client();
            com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest getAccessTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest()
                    .setAppKey("dingyjca6hmhmtzopjfb")
                    .setAppSecret("5Q4eeeyj-id31sYVT6_y3lTmfi1WDWjEep1P3GFnnoHj9HYaCM9qMiTm5twy8xDL");
            try {
                token = client.getAccessToken(getAccessTokenRequest).getBody().getAccessToken();
            } catch (TeaException err) {
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    log.error("",err);
                }

            } catch (Exception _err) {
                TeaException err = new TeaException(_err.getMessage(), _err);
                if (!Common.empty(err.code) && !Common.empty(err.message)) {
                    log.error("",err);
                }

            }
            log.info("---- token: " + token);
        } catch (Exception e) {
            log.error("",e);
        }
        return token;
    }

    public void batchSendTo(List<String> userIds,String msgKey, String message) throws Exception {
        com.aliyun.dingtalkrobot_1_0.Client client = createRobot_1_0_Client();
        com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders batchSendOTOHeaders = new com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getToken();
        com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest batchSendOTORequest = new com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest()
                .setRobotCode("dingyjca6hmhmtzopjfb")
                .setUserIds(userIds)
                .setMsgKey(msgKey)
                .setMsgParam(message);
        try {
            client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new com.aliyun.teautil.models.RuntimeOptions());
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                log.error("",err);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                log.error("",err);
            }

        }
    }
    /**
     * 批量发消息给userId
     *
     * @param vertx
     * @param userIds
     * @param messageContent
     */
    public void batchSendTo(Vertx vertx, List<String> userIds,String msgKey, String messageContent) {
        vertx.executeBlocking(obj->{
            try {
                batchSendTo(userIds,msgKey,messageContent);
            } catch (Exception e) {
                log.error("",e);
            }
        },err ->{
            log.error("发送钉钉消息失败！");
        });
    }
}
