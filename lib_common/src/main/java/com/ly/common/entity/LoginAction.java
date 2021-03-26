package com.ly.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author ly
 * @date 2018/5/19 16:05
 */
public class LoginAction implements Parcelable {
    public static final int ACTION_TO_WEB = 1;

    private int action;
    /**
     * 登录完成后需要用到的参数，自由发挥。。
     * Created by ly on 2018/5/19 16:11
     */
    private String[] params;

    public LoginAction(int action) {
        this(action, "");
    }

    public LoginAction(int action, String... params) {
        this.action = action;
        this.params = params;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getParam(int index) {
        if (params != null && params.length > index)
            return params[index];
        return "";
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.action);
        dest.writeStringArray(this.params);
    }

    protected LoginAction(Parcel in) {
        this.action = in.readInt();
        this.params = in.createStringArray();
    }

    public static final Creator<LoginAction> CREATOR = new Creator<LoginAction>() {
        @Override
        public LoginAction createFromParcel(Parcel source) {
            return new LoginAction(source);
        }

        @Override
        public LoginAction[] newArray(int size) {
            return new LoginAction[size];
        }
    };
}
