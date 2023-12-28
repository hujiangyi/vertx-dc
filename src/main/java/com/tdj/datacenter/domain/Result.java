package com.tdj.datacenter.domain;

import lombok.Data;

@Data
public class Result<T> {

    private int err;

        private T data;
//    private Object data;

    private String error;

    private String msg;

    private String errorCode;
    //20210519新加,后面项目可以删除
    private int status;  //1成功  -1失败  -10状态，账号不存在,用于管控那边登陆后，调用其他系统快速度登陆
    private String info; //错误提示信息

    public static final int  failcode = -1;
    public static final int  needLogin = 1;
    public static final int  failcode_notExsists = -10;

    public Result() {
        this.err = 0;
        this.msg = "Success";
        this.status=1;
        this.info="Success";
    }

    public Result(T data) {
        this();
        this.setData(data);
    }

    protected Result(int err, String msg, T data) {
        this.err = err;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    public static Result failed(String msg) {
        return new Result().seterrValue(Result.failcode).setmsgValue(msg);
    }
    public static Result failed(IErrorCode errorCode) {
        return new Result().seterrValue(errorCode.getCode()).setmsgValue(errorCode.getMessage());
    }

    public static Result validateFailed(String message) {
        return failed(ResultCode.VALIDATE_FAILED);
    }

    public static <T> Result<T> unauthorized(T data) {
        return new Result<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }
    public static <T> Result<T> forbidden(T data) {
        return new Result<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }


    /**
     * 快速修改返回结果
     * @param err
     * @return
     */
    public  Result  seterrValue(int err) {
        this.err = err;
        if(err==1){
            this.status=-1;
        }else {
            this.status = err;
        }
        return this;
    }


    /**
     * 快速修改返回结果
     * @return
     */
    public  Result  setmsgValue(String msg) {
        this.msg = msg;
        this.info=msg;
        return this;
    }
    /**
     * 快速修改返回结果
     * @param err
     * @return
     */
    public  Result<T>  setDataValue(T data) {
        this.data = data;
        return this;
    }
}
