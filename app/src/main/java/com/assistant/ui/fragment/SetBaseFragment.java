package com.assistant.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.assistant.ui.activity.BaseActivity;
import com.assistant.utils.PreferenceUtils;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/29
 * <p>
 * 功能描述 :
 */
public class SetBaseFragment extends PreferenceFragment {

    protected PreferenceUtils preferenceUtils;
    protected BaseActivity baseActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            this.baseActivity = (BaseActivity) getActivity();
        }
        preferenceUtils = PreferenceUtils.getInstance(baseActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onDetach() {
        baseActivity = null;
        super.onDetach();
    }
}
