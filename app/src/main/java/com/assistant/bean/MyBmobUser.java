package com.assistant.bean;

import cn.bmob.v3.BmobUser;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/5/16
 * <p>
 * 功能描述 :
 */
public class MyBmobUser extends BmobUser {
    String userPhone;

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
