package com.ly.common.net;

/**
 * Created by ly on 2017/2/13 14:17.
 */

public class RespCode {
    public static final int OK = 200;//成功
    public static final int UNAUTHORIZED = 401;//未授权
    public static final int TOKEN_EXPIRED = -601;//凭证已过期
    public static final int TOKEN_ERROR = -602;//凭证异常
    public static final int DU_NON_EXISTENT = -603;//设备未注册
    public static final int ERROR_SYS = -200;//系统错误

    public static final int ERROR = -88;//本地错误

}
