package com.assistant.ui.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/2
 * <p>
 * 功能描述 :
 */
public class MyFragmentPageAdapter extends FragmentPagerAdapter {

    List<String> titleList = new ArrayList<>();

    public MyFragmentPageAdapter(FragmentManager fm) {
        super(fm);
        titleList.add("笔记");
        titleList.add("闹钟");
        titleList.add("手机控");
    }

    @Override
    public BaseFragment getItem(int position) {
        switch (position) {
            case 0:
                // 创建一个NoteFragment
                return NoteFragment.newInstance();
            case 1:
                // 创建一个AlarmFragment
                return AlarmFragment.newInstance();
            case 2:
                // 创建一个PhoneFragment
                return PhoneFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // 返回当前页面的标题
        return titleList.get(position);
    }

    /**
     * 总共存在两个导航页面，均用Fragment来显示
     *
     * @return
     */
    @Override
    public int getCount() {
        return 3;
    }
}
