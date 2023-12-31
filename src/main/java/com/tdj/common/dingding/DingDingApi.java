package com.tdj.common.dingding;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiV2UserGetbymobileRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiV2UserGetbymobileResponse;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhany
 * @since 2022/3/7
 */
@Slf4j
public class DingDingApi {

    /**
     * 获取钉钉的token
     *
     * @return
     */
    public String getToken() {
        String token = "";
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            //天源
            OapiGettokenRequest request = new OapiGettokenRequest();
            request.setAppkey("dingegfzvmiz3netissh");
            request.setAppsecret("kQkBv8DHnK7LEqGH1i3qEPoDYqjoPQiJe71PrFtUBC7mi-_rpmsGWWllvgn4t9GJ");
            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);
            token = response.getAccessToken();
            log.info("---- token: " + token);
        } catch (Exception e) {
            log.error("",e);
        }
        return token;
    }

    /**
     * 根据手机号查询用户userId
     * 这里是单个的手机号码
     *
     * @param phone 手机号码
     * @return
     */
    public String toGetDingDingPeople(String phone) {
        String userId = "";
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getbymobile");
            OapiV2UserGetbymobileRequest req = new OapiV2UserGetbymobileRequest();
            req.setMobile(phone);
            //是否支持通过手机号搜索专属帐号(不含其他组织创建的专属帐号)。
            req.setSupportExclusiveAccountSearch(true);
            OapiV2UserGetbymobileResponse rsp = client.execute(req, getToken());
            if (rsp.getResult() != null){
                userId = rsp.getResult().getUserid();
            }
        } catch (Exception e) {
            log.error("",e);
        }
        return userId;
    }

    /**
     * 钉钉通讯录中对应的userId,可以传多个
     * 例子：user123,user456(多个userId字符串拼接)
     *
     * @param userId         钉钉通讯录中对应的userId,可以传多个
     * @param messageContent 消息内容
     */
    public void toSendDingDingMessage(String userId, String messageContent) {
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
            OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
            request.setAgentId(1488603170L);
            request.setUseridList(userId);
            request.setToAllUser(false);

            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setMsgtype("text");
            msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(date);
            msg.getText().setContent(time+"  "+messageContent);
            request.setMsg(msg);
            OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(request, getToken());
            log.info("rsp:" + rsp.getBody());
        } catch (Exception e) {
            log.error("",e);
        }
    }

    /**
     * 依据手机号发送消息
     * @param phone
     * @param messageContent
     */
    public void toSendDingDingMessageByPhone(Vertx vertx,String phone, String messageContent) {
        vertx.executeBlocking(obj->{
            try {
                String userId = toGetDingDingPeople(phone);
                toSendDingDingMessage(userId, messageContent);
            } catch (Exception e) {
                log.error("",e);
            }
        },err ->{
            log.error("发送钉钉消息失败！");
        });
    }


}
