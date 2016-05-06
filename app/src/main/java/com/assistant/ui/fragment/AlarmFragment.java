package com.assistant.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.assistant.R;
import com.assistant.adapter.AlarmAdapter;
import com.assistant.bean.Alarm;
import com.assistant.controll.AlarmClock;
import com.assistant.ui.activity.AddAlarmActivity;
import com.assistant.utils.ConstUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.orhanobut.logger.Logger;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/2
 * <p>
 * 功能描述 :
 */
public class AlarmFragment extends BaseFragment {

    @Bind(R.id.list_view_alarms)
    ListView lv_alarm;
    @Bind(R.id.add_Alarm)
    FloatingActionButton addAlarm;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;
    @Bind(R.id.no_alarm_tip)
    TextView noAlarmTip;

    private List<Alarm> mAlarmList;
    private AlarmClock mAlarmClock;
    private AlarmAdapter mAlarmAdapter;
    private Alarm mAlarm;

    private View onMenu;
    public static boolean isMenuOn;
    private TranslateAnimation animation;
    private float width;

    public static AlarmFragment newInstance() {
        AlarmFragment alarmFragment = new AlarmFragment();
        return alarmFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 点击添加一个新的闹钟
     */
    @OnClick(R.id.add_Alarm)
    public void onClick() {
        mAlarm = new Alarm();
        startAlarmActivity(ConstUtils.ADD_ALARM, mAlarm);
    }

    public void onEventMainThread(AlarmEvent event) {
        switch (event) {
            case UPDATE_ALARM:
                updateAlarms();
                break;
            case CLOSE_MENU:
                Logger.d("CLOSE_MENU is executed");
                toggleMenu(onMenu);
                break;
            case ALARM_ONCE:
                Logger.d("取消一次闹钟");
                updateAlarms();
                break;
        }

    }

    public enum AlarmEvent {
        UPDATE_ALARM,
        CLOSE_MENU,
        ALARM_ONCE
    }

/*******************************************华丽的分割线*********************************************/

    /**
     * 加载闹钟的信息，显示到界面上
     */
    private void initData() {
        onMenu = null;
        isMenuOn = false;
        mAlarmClock = new AlarmClock();
        // 查询数据库中Alarm表的所有记录
        mAlarmList = initItemData();

        // 开启一个空闲的线程来初始化所有的闹钟开关
        if (mAlarmList.size() > 0) {
            threadPoolUtils.execute(() -> {
                for (Alarm alarm : mAlarmList) {
                    mAlarmClock.turnAlarm(alarm);
                }
            });
        }
        initAdapter();
        initListener();

    }

    /**
     * 更新列表的内容
     */
    private void updateAlarms() {
        showProgressWheel(true);

        // 查询数据库中Alarm表的所有记录
        mAlarmList = initItemData();

        mAlarmAdapter.setAlarmList(mAlarmList);
        mAlarmAdapter.notifyDataSetChanged();
        mAlarmClock = new AlarmClock();
        // 开启一个空闲的线程来初始化所有的闹钟开关
        if (mAlarmList.size() > 0) {
            threadPoolUtils.execute(() -> {
                for (Alarm alarm : mAlarmList) {
                    mAlarmClock.turnAlarm(alarm);
                }
            });
        }
        showProgressWheel(false);
    }

    /**
     * 根据 sort 列来进行排序
     *
     * @return
     */
    private List<Alarm> initItemData() {
        List<Alarm> items;
        String strWhere = "userId = " + "\'" + userId + "\'";

        items = finalDb.findAllByWhere(Alarm.class, strWhere, "sort", false);

        if (items.isEmpty()) {
            noAlarmTip.setVisibility(View.VISIBLE);
        } else {
            noAlarmTip.setVisibility(View.GONE);
        }
        return items;
    }

    private void initListener() {
        lv_alarm.setOnItemClickListener((parent, view, position, id) -> {
            if (onMenu != null && onMenu == view) {
                toggleMenu(view);
            } else if (onMenu != null & onMenu != view) {
                toggleMenu(onMenu);
                toggleMenu(view);
            } else {
                toggleMenu(view);
            }
            Button bt_delete = (Button) view.findViewById(R.id.bt_delete_item);
            Button bt_update = (Button) view.findViewById(R.id.bt_update_item);
            bt_delete.setOnClickListener(v -> {
                toggleMenu(view);
                mAlarm = mAlarmList.get(position);
                // 关闭现有闹钟
                mAlarm.setActivate(false);
                mAlarmClock.turnAlarm(mAlarm);
                // 删除该闹钟在数据库的信息
                finalDb.delete(mAlarm);
                // 从当前列表里面移除
                mAlarmList.remove(position);
                updateAlarms();
            });

            bt_update.setOnClickListener(v -> {
                toggleMenu(view);
                mAlarm = mAlarmList.get(position);
                // 以更新Alarm的方式启动activity
                startAlarmActivity(ConstUtils.UPDATE_ALARM, mAlarm);
            });
        });
        lv_alarm.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isMenuOn) {
                    // 如果menu打开了，就将其关闭
                    toggleMenu(onMenu);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void startAlarmActivity(int oprType, Alarm mAlarm) {
        toggleMenu(onMenu);

        Intent intent = new Intent(mActivity, AddAlarmActivity.class);
        intent.putExtra(ConstUtils.OPERATE_ALARM_TYPE_KEY, oprType);
        EventBus.getDefault().postSticky(mAlarm);
        startActivity(intent);
    }

    private void initAdapter() {
        mAlarmAdapter = new AlarmAdapter(mActivity, mAlarmList);
        lv_alarm.setAdapter(mAlarmAdapter);
    }


    private void toggleMenu(View view) {
        if (view == null) {
            return;
        }
        RelativeLayout rl_main = (RelativeLayout) view.findViewById(R.id.rl_main_item);
        LinearLayout ll_button = (LinearLayout) view.findViewById(R.id.ll_button_item);

        Button bt_delete = (Button) view.findViewById(R.id.bt_delete_item);
        Button bt_update = (Button) view.findViewById(R.id.bt_update_item);
        SwitchButton sb_turn = (SwitchButton) view.findViewById(R.id.bt_turn_item);

        float back = ll_button.getWidth();
        float front = rl_main.getWidth();
        width = back / front;
        ll_button.setMinimumHeight(rl_main.getHeight());

        if (isMenuOn) {
            Logger.d("close menu");
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, width, Animation.RELATIVE_TO_SELF,
                    0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
            isMenuOn = false;
            onMenu = null;
        } else {
            Logger.d("open menu");
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                    width, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
            isMenuOn = true;
            onMenu = view;
        }
        animation.setDuration(200);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isMenuOn) {
                    bt_delete.setClickable(true);
                    bt_update.setClickable(true);
                    sb_turn.setClickable(false);
                } else {
                    bt_delete.setClickable(false);
                    bt_update.setClickable(false);
                    sb_turn.setClickable(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        rl_main.startAnimation(animation);
    }


    /**
     * 进度条开关设置
     *
     * @param visible 为true则显示进度条
     */
    private void showProgressWheel(boolean visible) {
        progressWheel.setBarColor(getColorPrimary());
        if (visible) {
            if (!progressWheel.isSpinning()) {
                // 进度条开始旋转
                progressWheel.spin();
            }
        } else {
            progressWheel.postDelayed(() -> {

                if (progressWheel.isSpinning()) {
                    progressWheel.stopSpinning();
                }
            }, 300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMenu(onMenu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
