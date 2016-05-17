package com.assistant.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/9
 * <p>
 * 功能描述 : 根据用户的ID 来查看该用户的密码
 */
@Table(name = "users")
public class User {
    // id作为自增长属性
    @Id(column = "id")
    private Integer id;
    // 用户的ID
    private String userId;
    // 用户的邮箱
    private String userPhone;
    // 用户名
    private String userName;
    // 保存用户的密码
    private String savePassword;
    // 保存加密笔记的密码，保存到是经过MD5加密的密文
    private String notePassword;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSavePassword() {
        return savePassword;
    }

    public void setSavePassword(String savePassword) {
        this.savePassword = savePassword;
    }

    public String getNotePassword() {
        return notePassword;
    }

    public void setNotePassword(String notePassword) {
        this.notePassword = notePassword;
    }
}
