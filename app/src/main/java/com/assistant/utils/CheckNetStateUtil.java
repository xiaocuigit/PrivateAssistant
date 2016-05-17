package com.assistant.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

import com.assistant.R;
import com.orhanobut.logger.Logger;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/21
 * <p>
 * 功能描述 :
 */
public class CheckNetStateUtil {
    /**
     * 检测网络是否可用
     *
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

    /**
     * 显示网络连接不可用对话框
     */
    public static void showNetUnAvailableDialog(Context context) {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(context);
        builder.setMessage("网络不可用，请连接网络后再使用");
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            // 打开设置界面让用户设置网络
            dialog.dismiss();
        });
        builder.show();
    }
}
