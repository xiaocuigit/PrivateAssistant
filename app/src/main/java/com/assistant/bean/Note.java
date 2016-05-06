package com.assistant.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/26
 * <p>
 * 功能描述 :
 */
// 表名为 note
@Table(name = "note")
public class Note {
    @Id(column = "id")
    private Integer id;         // id作为自增长属性
    private String title;       // 笔记标题
    private String content;     // 笔记内容
    private Long createTime;    // 创建时间
    private Long lastOprTime;   // 最后一次修改时间

    private String userId;      // 用来区分不同的用户
    private String isLock;      // 是否加锁

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLastOprTime() {
        return lastOprTime;
    }

    public void setLastOprTime(Long lastOprTime) {
        this.lastOprTime = lastOprTime;
    }

    public String getIsLock() {
        return isLock;
    }

    public void setIsLock(String isLock) {
        this.isLock = isLock;
    }
}
