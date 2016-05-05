package com.assistant.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.orhanobut.logger.Logger;

/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/21
 * <p/>
 * 功能描述 :
 */
public class CheckNetStateUtil {
    /**
     * 检测网络是否可用
     * @param context
     * @return
     */
    public static boolean netIsAvailable(Context context) {

        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(context.CONNECTIVITY_SERVICE);

        boolean wifi = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        boolean internet = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

        Logger.d("wifi is " + wifi + " internet is" + internet);

        // 如果WIFI 和 流量 都不可用，返回 false
        if (!wifi && !internet) {
            return false;
        }
        return true;
    }
}
