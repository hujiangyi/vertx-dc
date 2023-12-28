package com.tdj.datacenter.domain;

import lombok.Data;

/**
 * Oauth2获取Token返回信息封装
 * Created by macro on 2020/7/17.
 */
@Data
public class Oauth2Token {
    //访问令牌
    private String token;
    //刷令牌
    private String refreshToken;
    //访问令牌头前缀
    private String tokenHead;
    //有效时间（秒）
    private int expiresIn;
    //所属企业
    private String entname;
    //用户ID
    private Long memberId;
    //用户角色
    private Integer role;
    //用户名称
    private String username;
    //昵称
    private String nickName;
    //手机号
    private String phonenum;
    //企业ID
    private Integer enterpriseId;
    //uuid
    private String uuid;
    //openId
    private String openId;
    //unionId
    private String unionId;
    //userId
    private String userId;
    //profileUrl
    private String profileUrl;
}
