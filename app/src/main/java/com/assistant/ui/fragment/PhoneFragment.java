package com.assistant.ui.fragment;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.assistant.App;
import com.assistant.R;
import com.assistant.adapter.TimeItemAdapter;
import com.assistant.bean.TimeItem;
import com.assistant.bean.UnLockTime;
import com.assistant.controll.PhoneControl;
import com.assistant.receiver.AdminReceiver;
import com.assistant.service.LockPhoneService;
import com.assistant.ui.activity.LockPhoneActivity;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.DialogUtils;
import com.assistant.view.BetterFab;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.kyleduo.switchbutton.SwitchButton;
import com.orhanobut.logger.Logger;

import java.util.Calendar;
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
 * 创建日期 : 2016/4/24
 * <p>
 * 功能描述 :
 */
public class PhoneFragment extends BaseFragment implements RadialTimePickerDialogFragment.OnTimeSetListener {

    @Bind(R.id.list_view_times)
    ListView lvTimes;
    @Bind(R.id.no_time_text)
    TextView noTimeText;
    @Bind(R.id.add_Time)
    BetterFab addTime;

    private List<TimeItem> mTimeItems;
    private TimeItemAdapter mTimeItemAdapter;
    private TimeItem mTimeItem;

    private View onMenu;
    public static boolean isMenuOn;
    private TranslateAnimation animation;
    private float width;

    private int currentType;

    private RadialTimePickerDialogFragment timePicker;
    private DevicePolicyManager deviceManager;
    private ComponentName componentName;
    private List<UnLockTime> list;
    private Intent service;

    public static PhoneFragment newInstance() {
        PhoneFragment fragment = new PhoneFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().registerSticky(this);
        currentType = ConstUtils.NULL_TIME_ITEM;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDeviceManager();
        initUI();
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMenu(onMenu);
    }

    /**
     * 从时间控件上获取值
     *
     * @param dialog
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
        List<TimeItem> times;
        times = initItemData();
        if (times.size() != 0) {
            for (TimeItem item : times) {
                if (item.getHour() == hourOfDay && item.getMinute() == minute) {
                    Toast.makeText(mActivity, "该时间已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        if (currentType == ConstUtils.EDIT_TIME_ITEM) {
            mTimeItem.setHour(hourOfDay);
            mTimeItem.setMinute(minute);
            mTimeItem.setSort(hourOfDay * 100 + minute);
            finalDb.update(mTimeItem);
        } else if (currentType == ConstUtils.ADD_TIME_ITEM) {
            mTimeItem = new TimeItem();
            mTimeItem.setHour(hourOfDay);
            mTimeItem.setMinute(minute);
            mTimeItem.setSort(hourOfDay * 100 + minute);
            // 设置所属的用户ID
            mTimeItem.setUserId(user.getUserId());
            // 保存到数据库中
            finalDb.saveBindId(mTimeItem);
        } else {
            return;
        }
        // 更新列表视图
        updateTimes();
    }

    /**
     * 添加一个时间
     */
    @OnClick(R.id.add_Time)
    public void onClick() {
        currentType = ConstUtils.ADD_TIME_ITEM;
        if (mTimeItems.size() == 0) {
            noTimeText.setVisibility(View.GONE);
        }
        timePicker.setStartTime(0, 0);
        timePicker.show(getFragmentManager(), ConstUtils.FLAG_TAG_TIME_PICKER);
    }

    /**
     * 接收来自 TimeItemAdapter 发送过来的消息
     *
     * @param position
     */
    public void onEventMainThread(Integer position) {
        if (position >= 0 && position < mTimeItems.size()) {
            // 先检测是否激活了设备管理器
            if (deviceManager.isAdminActive(componentName)) {
                showTipDialog(position);
            } else {
                mTimeItemAdapter.notifyDataSetChanged();
                activateDevicePermission();
            }
        }
    }

    public void onEventMainThread(PhoneFragmentEvent event) {
        switch (event) {
            case STOP_SERVICE:
                mTimeItemAdapter.notifyDataSetChanged();
                stopLockPhoneService();
                EventBus.getDefault().post(LockPhoneActivity.LockPhoneActivityEvent.DESTROY_ACTIVITY);
                break;
            case RESTART_SERVICE:
                Logger.d("重启服务");
                startLockPhoneService();
                break;
        }
    }

    public enum PhoneFragmentEvent {
        STOP_SERVICE,
        RESTART_SERVICE
    }

/*******************************************华丽的分割线*********************************************/

    /**
     * 初始化显示的数据
     */
    private void initData() {
        mTimeItems = initItemData();
        if (mTimeItems.isEmpty()) {
            noTimeText.setVisibility(View.VISIBLE);
        } else {
            noTimeText.setVisibility(View.GONE);
        }
        initAdapter();
        initListener();
    }

    /**
     * 初始化界面显示的控件
     */
    private void initUI() {
        onMenu = null;
        isMenuOn = false;

        timePicker = new RadialTimePickerDialogFragment();
        timePicker.setOnTimeSetListener(PhoneFragment.this);
        timePicker.setThemeLight();
        timePicker.setForced24hFormat();
    }

    /**
     * 初始化设备管理器
     */
    private void initDeviceManager() {
        deviceManager = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(mActivity, AdminReceiver.class);
    }

    /**
     * 当用户点击switch开关时，通过对话框提醒用户手机将被强制锁定。
     */
    private void showTipDialog(Integer position) {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(mActivity);
        builder.setTitle(getString(R.string.careful));
        String message1 = "一旦点击确定，该手机将在";
        String message2 = "以内处于锁定状态，您将不能再玩手机了。";
        TimeItem item = mTimeItems.get(position);
        if (item.getHour() == 0) {
            builder.setMessage(message1 + getString(R.string.display_seted_time_2, item.getMinute()) + message2);
        } else {
            if (item.getMinute() == 0) {
                builder.setMessage(message1 + getString(R.string.display_seted_time_3, item.getHour()) + message2);
            } else
                builder.setMessage(message1 + getString(R.string.display_seted_time_1, item.getHour(), item.getMinute()) + message2);
        }
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            // 获取解锁的时间
            UnLockTime unLockTime = getUnLockTime(item);
            if (unLockTime != null) {
                // 将手机状态设置为锁定状态
                App.setPhoneState(ConstUtils.LOCK_STATE);
                // 设置手机状态为锁定状态
                unLockTime.setLockState(ConstUtils.LOCK_STATE);
                // 保证数据库中只有一条记录，不能再插入第二天记录
                if (list.size() == 0) {
                    finalDb.saveBindId(unLockTime);
                    // 保存到数据库后，重新从数据库中取出，因为保存后会给它的ID赋值。
                    list = finalDb.findAll(UnLockTime.class);
                    unLockTime = list.get(0);
                } else {
                    finalDb.update(unLockTime);
                }
                // 设置锁定的时间，时间到达后对手机解锁。
                new PhoneControl().forceLockPhone(unLockTime);
                // 跳转到锁屏界面
                startLockPhoneService();
            }

        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            mTimeItemAdapter.notifyDataSetChanged();
        });

        builder.show();
    }

    /**
     * 启动后台服务，用来监听电源按钮
     */
    private void startLockPhoneService() {
        service = new Intent(mActivity, LockPhoneService.class);
        mActivity.startService(service);
    }

    /**
     * 销毁后台服务
     */
    private void stopLockPhoneService() {
        if (service != null) {
            mActivity.stopService(service);
        } else {
            Logger.d("service is null");
        }
    }


    /**
     * 得到一个手机解锁的时间
     *
     * @param item
     * @return
     */
    private UnLockTime getUnLockTime(TimeItem item) {
        UnLockTime unLockTime;

        list = finalDb.findAll(UnLockTime.class);
        // 保证数据库中只有一条记录，不能再插入第二天记录
        if (list.size() == 0) {
            unLockTime = new UnLockTime();
        } else {
            unLockTime = list.get(0);
        }

        Calendar calendar = Calendar.getInstance();
        // 获取当前系统的时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int lockHour = item.getHour();
        int lockMinute = item.getMinute();
        // 将锁定的时间转换成分钟数
        int lockTime = lockHour * 60 + lockMinute;
        int minuteNum = minute + lockTime;

        if (minuteNum == 60) {
            if (hour == 23) {
                // 正好是晚上11点，设置的锁定时间为1个小时，则解锁的时间为 0:0
                unLockTime.setHour(0);
                unLockTime.setMinute(0);
            } else {
                unLockTime.setHour(hour + 1);
                unLockTime.setMinute(0);
            }
        } else if (minuteNum < 60) {
            unLockTime.setHour(hour);
            unLockTime.setMinute(minuteNum);
        } else {
            int nowHour = (minuteNum / 60) + hour;
            if (nowHour == 24) {
                unLockTime.setHour(0);
            } else if (nowHour > 24) {
                unLockTime.setHour(nowHour - 24);
            } else {
                unLockTime.setHour(nowHour);
            }
            unLockTime.setMinute(minuteNum % 60);
        }
        return unLockTime;
    }

    /**
     * 检测是否激活了设备管理权限
     */
    private void activateDevicePermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活了设备管理权限");
        mActivity.startActivity(intent);
        Logger.d("激活设备管理权限");
    }


    private void initListener() {
        lvTimes.setOnItemClickListener((parent, view, position, id) -> {
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
                if (mTimeItems.size() == 1) {
                    noTimeText.setVisibility(View.VISIBLE);
                }
                mTimeItem = mTimeItems.get(position);
                threadPoolUtils.execute(() -> finalDb.delete(mTimeItem));
                mTimeItems.remove(position);
                mTimeItemAdapter.notifyDataSetChanged();
            });
            bt_update.setOnClickListener(v -> {
                currentType = ConstUtils.EDIT_TIME_ITEM;

                mTimeItem = mTimeItems.get(position);
                timePicker.setStartTime(mTimeItem.getHour(), mTimeItem.getMinute());
                timePicker.show(getFragmentManager(), ConstUtils.FLAG_TAG_TIME_PICKER);
            });
        });
        lvTimes.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        lvTimes.setVerticalScrollBarEnabled(false);
    }

    private void initAdapter() {
        mTimeItemAdapter = new TimeItemAdapter(mActivity, mTimeItems);
        lvTimes.setAdapter(mTimeItemAdapter);
    }

    /**
     * 更新列表内容
     */
    private void updateTimes() {
        List<TimeItem> list = initItemData();
        mTimeItemAdapter.setTimeItems(list);
        mTimeItemAdapter.notifyDataSetChanged();
        initData();
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
     * 根据 sort 列来进行排序
     *
     * @return 返回数据库里面的信息
     */
    private List<TimeItem> initItemData() {
        List<TimeItem> items;
        String strWhere = "userId = " + "\'" + userId + "\'";

        items = finalDb.findAllByWhere(TimeItem.class, strWhere, "sort", false);
        return items;
    }

}
