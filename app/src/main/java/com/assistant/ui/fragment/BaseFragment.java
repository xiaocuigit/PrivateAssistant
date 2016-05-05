package com.assistant.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.User;
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
public class BaseFragment extends Fragment {
    protected Activity mActivity;

    protected PreferenceUtils preferenceUtils;
    protected ThreadPoolUtils threadPoolUtils;
    protected FinalDb finalDb;
    protected String userId;
    protected User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        threadPoolUtils = App.getThreadPoolUtils();
        preferenceUtils = App.getPreferenceUtils();
        finalDb = App.getFinalDb();
        user = App.getUser();
        if (user != null) {
            userId = user.getUserId();
        } else {
            Logger.e("user is null");
        }
    }

    @Override
    public void onDestroy() {
        mActivity = null;
        super.onDestroy();
    }

    public int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        mActivity.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }


}
