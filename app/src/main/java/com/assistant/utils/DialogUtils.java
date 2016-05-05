package com.assistant.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.assistant.R;

/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/28
 * <p/>
 * 功能描述 :
 */
public class DialogUtils {
    /**
     * 标准的对话框
     * @param context
     * @return
     */
    public static AlertDialog.Builder makeDialogBuilderByTheme(Context context){

        ThemeUtil.Theme theme = ThemeUtil.getCurrentTheme(context);
        AlertDialog.Builder builder;
        int style = R.style.BlueTheme;
        switch (theme){
            case BROWN:
                style = R.style.BrownDialogTheme;
                break;
            case BLUE:
                style = R.style.BlueDialogTheme;
                break;
            case BLUE_GREY:
                style = R.style.BlueGreyDialogTheme;
                break;
            case YELLOW:
                style = R.style.YellowDialogTheme;
                break;
            case DEEP_PURPLE:
                style = R.style.DeepPurpleDialogTheme;
                break;
            case PINK:
                style = R.style.PinkDialogTheme;
                break;
            case GREEN:
                style = R.style.GreenDialogTheme;
                break;
            default:
                break;
        }
        builder = new AlertDialog.Builder(context, style);
        return builder;
    }

}
