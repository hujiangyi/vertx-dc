package com.tdj.datacenter.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业用户表
 */
@Data
public class EntUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private String uuid;
    private int State;
    private int enterpriseid;
    private String phonenumber;
    private String name;
    private String pwd;
    private String truename;
    private int sex;
    private int usertype;
    private int isenable;
    private String cardno;
    private String memo;
    private Date dateofcreate;
    private Date dateofoperator;
    private int operatorid;
    private String operatorname;

    private String headUrl;

    private String enterprisename;

    private int isenablePbms;// 用户中心0:启用1:禁用  需要转换为  物业系统  启用是0 禁用是2

}
