package com.assistant;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.assistant.bean.User;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.PreferenceUtils;
import com.assistant.utils.ThreadPoolUtils;
import com.orhanobut.logger.Logger;

import net.tsz.afinal.FinalDb;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/2
 * <p>
 * 功能描述 :
 */
public class App extends Application {
    private static Context context;
    private static FinalDb finalDb;
    private static FinalDb.DaoConfig config;
    private static ThreadPoolUtils threadPoolUtils;
    private static PreferenceUtils preferenceUtils;
    private static String userId;
    private static String userEmail;
    private static User user;
    private static int phoneState = ConstUtils.NORMAL_STATE;
    private static int userState = ConstUtils.LOGIN;              // 默认是一旦登录成功后就不需要再登录了
    private static boolean noteType = ConstUtils.NORMAL_NOTE;     // 设置默认值为普通笔记
    private static int ringState = ConstUtils.OFF_RING;           // 默认设置是挂断电话的

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init();
        context = getApplicationContext();
        configDB();
    }


    public static int getUserState() {
        return userState;
    }

    public static void setUserState(int userState) {
        App.userState = userState;
    }

    public static int getPhoneState() {
        return phoneState;
    }

    public static void setPhoneState(int phoneState) {
        App.phoneState = phoneState;
    }

    public static boolean isNormalNote() {
        return noteType;
    }

    public static void setNoteType(boolean mNoteType) {
        noteType = mNoteType;
    }

    public static int getRingState() {
        return ringState;
    }

    public static void setRingState(int ringState) {
        App.ringState = ringState;
    }

    public static void setUser(User mUser) {
        user = mUser;
    }

    public static User getUser() {
        if (user != null) {
            return user;
        }
        return null;
    }

    /**
     * 单例模式
     * 返回数据库操作对象
     *
     * @return
     */
    public static FinalDb getFinalDb() {
        if (finalDb == null) {
            finalDb = FinalDb.create(config);
        }
        return finalDb;
    }

    public static void setUserId(String id) {
        userId = id;
    }

    public static String getUserId() {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        return userId;
    }

    public static String getUserEmail() {
        if (TextUtils.isEmpty(userEmail)) {
            return null;
        }
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        App.userEmail = userEmail;
    }

    /**
     * 返回一个线程池操作对象
     *
     * @return
     */
    public static ThreadPoolUtils getThreadPoolUtils() {
        if (threadPoolUtils == null) {
            threadPoolUtils = new ThreadPoolUtils();
        }
        return threadPoolUtils;
    }

    private FinalDb.DaoConfig configDB() {
        config = new FinalDb.DaoConfig();
        config.setDbName("notes.db");
        config.setDbVersion(1);
        config.setDebug(Boolean.parseBoolean("true"));
        config.setContext(context);
        return config;
    }

    public static PreferenceUtils getPreferenceUtils() {
        if (preferenceUtils == null) {
            preferenceUtils = PreferenceUtils.getInstance(context);
        }
        return preferenceUtils;
    }

    public static Context getContext() {
        return context;
    }
}
