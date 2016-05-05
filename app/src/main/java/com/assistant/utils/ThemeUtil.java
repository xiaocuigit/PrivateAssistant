package com.assistant.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.GridView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.adapter.ColorsListAdapter;
import com.assistant.ui.activity.HomeActivity;
import com.assistant.ui.fragment.SettingFragment;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/25
 * <p>
 * 功能描述 :
 */
public class ThemeUtil {

    public enum Theme {
        RED(0x00),
        BROWN(0x01),
        BLUE(0x02),
        BLUE_GREY(0x03),
        YELLOW(0x04),
        DEEP_PURPLE(0x05),
        PINK(0x06),
        GREEN(0x07);

        private int mValues;

        Theme(int value) {
            this.mValues = value;
        }

        public static Theme mapValueToTheme(int value) {
            for (Theme theme : Theme.values()) {
                if (theme.getIntValue() == value) {
                    return theme;
                }
            }
            return BLUE;
        }

        static Theme getDefault() {
            return BLUE;
        }

        public int getIntValue() {
            return mValues;
        }
    }

    public static void changeTheme(Activity activity, Theme theme) {
        if (activity == null) {
            return;
        }
        // 默认主题为蓝色
        int style = R.style.BlueTheme;

        switch (theme) {
            case RED:
                style = R.style.RedTheme;
                break;
            case BROWN:
                style = R.style.BrownTheme;
                break;
            case BLUE:
                style = R.style.BlueTheme;
                break;
            case BLUE_GREY:
                style = R.style.BlueGreyTheme;
                break;
            case YELLOW:
                style = R.style.YellowTheme;
                break;
            case DEEP_PURPLE:
                style = R.style.DeepPurpleTheme;
                break;
            case PINK:
                style = R.style.PinkTheme;
                break;
            case GREEN:
                style = R.style.GreenTheme;
                break;
            default:
                break;
        }
        // 设置选中的主题
        activity.setTheme(style);
    }

    public static Theme getCurrentTheme(Context context) {
        int style = PreferenceUtils.getInstance(context)
                .getIntParam(ConstUtils.CHANGE_THEME, 0);

        return ThemeUtil.Theme.mapValueToTheme(style);
    }

    /**
     * 显示更换主题的对话框
     *
     * @param context
     */
    public static void showThemeChooseDialog(Context context) {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(context);

        builder.setTitle(R.string.change_theme);

        Integer[] res = new Integer[]{R.drawable.red_round, R.drawable.brown_round, R.drawable.blue_round,
                R.drawable.blue_grey_round, R.drawable.yellow_round, R.drawable.deep_purple_round,
                R.drawable.pink_round, R.drawable.green_round};
        List<Integer> list = Arrays.asList(res);
        ColorsListAdapter adapter = new ColorsListAdapter(list, context);
        adapter.setCheckItem(ThemeUtil.getCurrentTheme(context).getIntValue());

        GridView gridView = (GridView) LayoutInflater.from(context)
                .inflate(R.layout.colors_panel_layout, null);

        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);

        builder.setView(gridView);
        AlertDialog dialog = builder.create();
        dialog.show();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            int value = ThemeUtil.getCurrentTheme(context).getIntValue();
            if (value != position) {
                App.getPreferenceUtils().saveParam(ConstUtils.CHANGE_THEME, position);
                EventBus.getDefault().post(HomeActivity.MainEvent.CHANGE_THEME);
                EventBus.getDefault().post(SettingFragment.MainEvent.CHANGE_THEME);
            }
        });
    }
}
