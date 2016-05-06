package com.assistant.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.Note;
import com.assistant.ui.activity.PayActivity;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.DialogUtils;
import com.assistant.utils.FileUtils;
import com.assistant.utils.PasswordUtils;
import com.assistant.utils.ThemeUtil;
import com.jenzz.materialpreference.CheckBoxPreference;
import com.jenzz.materialpreference.Preference;
import com.jenzz.materialpreference.SwitchPreference;
import com.orhanobut.logger.Logger;
import com.triggertrap.seekarc.SeekArc;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/29
 * <p>
 * 功能描述 :
 */
public class SettingFragment extends SetBaseFragment {
    public static final String PREFERENCE_FILE_NAME = "user.settings";

    private CheckBoxPreference cardLayoutPreference;        // 切换笔记显示模式
    private Preference changeThemePreference;               // 更换主题
    private SwitchPreference vibratePreference;             // 控制振动开启
    private SwitchPreference lockPhoneVibratePreference;    // 控制振动开启

    private Preference adjustVolumePreference;              // 调节铃音音量

    private Preference feedbackPreference;                  // 意见反馈
    private Preference payMePreference;                     // 赞赏

    private boolean cardLayout;
    private boolean isVibrate;
    private boolean isLockPhoneVibrate;

    private static SettingFragment fragment;
    private static Context mContext;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private String ringName = "galebi.mp3";
    private int currentVolume;
    private Preference setPasswordPreference;

    public SettingFragment() {
        super();
    }

    public static SettingFragment getInstance(Context context) {
        if (fragment == null) {
            fragment = new SettingFragment();
        }
        mContext = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        addPreferencesFromResource(R.xml.preference);

        getPreferenceManager().setSharedPreferencesName(PREFERENCE_FILE_NAME);

        cardLayout = preferenceUtils.getBooleanParam(ConstUtils.CARD_LAYOUT, true);
        cardLayoutPreference = (CheckBoxPreference) findPreference(getString(R.string.card_note_item_layout_key));
        cardLayoutPreference.setChecked(cardLayout);

        changeThemePreference = (Preference) findPreference(getString(R.string.change_theme_key));

        isVibrate = preferenceUtils.getBooleanParam(ConstUtils.IS_VIBRATE, true);
        vibratePreference = (SwitchPreference) findPreference(getString(R.string.start_vibrate_key));
        vibratePreference.setChecked(isVibrate);

        isLockPhoneVibrate = preferenceUtils.getBooleanParam(ConstUtils.IS_LOCK_PHONE_VIBRATE, true);
        lockPhoneVibratePreference = (SwitchPreference) findPreference(getString(R.string.lock_phone_vibrate_key));
        lockPhoneVibratePreference.setChecked(isLockPhoneVibrate);

        adjustVolumePreference = (Preference) findPreference(getString(R.string.adjust_volume_key));
        setPasswordPreference = (Preference) findPreference(getString(R.string.set_password_key));

        feedbackPreference = (Preference) findPreference(getString(R.string.advice_feedback_key));
        payMePreference = (Preference) findPreference(getString(R.string.pay_for_me_key));

        initFeedbackPreference();
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = preferenceUtils.getIntParam(ConstUtils.ALARM_VOLUME, 50);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    /**
     * 当绘制界面的时候调用此函数，给每个item绘制分割线
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(new ColorDrawable(getResources().getColor(R.color.grey)));
        listView.setDividerHeight((int) getResources().getDimension(R.dimen.preference_divider_height));
        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, android.preference.Preference preference) {
        if (!isResumed() || preference == null) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        String key = preference.getKey();

        if (TextUtils.equals(key, getString(R.string.card_note_item_layout_key))) {
            cardLayout = !cardLayout;
            preferenceUtils.saveParam(ConstUtils.CARD_LAYOUT, cardLayout);
        }
        if (TextUtils.equals(key, getString(R.string.change_theme_key))) {
            ThemeUtil.showThemeChooseDialog(baseActivity);
        }
        if (TextUtils.equals(key, getString(R.string.start_vibrate_key))) {
            isVibrate = !isVibrate;
            preferenceUtils.saveParam(ConstUtils.IS_VIBRATE, isVibrate);
            if (isVibrate) {
                startVibrate();
            }
        }
        if (TextUtils.equals(key, getString(R.string.lock_phone_vibrate_key))) {
            isLockPhoneVibrate = !isLockPhoneVibrate;
            preferenceUtils.saveParam(ConstUtils.IS_LOCK_PHONE_VIBRATE, isLockPhoneVibrate);
            if (isLockPhoneVibrate) {
                startVibrate();
            }
        }
        if (TextUtils.equals(key, getString(R.string.adjust_volume_key))) {
            startTestMusic();
            showAdjustVolumeDialog();
        }
        if (TextUtils.equals(key, getString(R.string.set_password_key))) {
            checkIsSetPassword();
        }

        if (TextUtils.equals(key, getString(R.string.backup_local_key))) {
            backupLocal();
        }

        // 随意打赏
        if (TextUtils.equals(key, getString(R.string.pay_for_me_key))) {
            Intent intent = new Intent(getActivity(), PayActivity.class);
            startActivity(intent);
        }

        if (TextUtils.equals(key, getString(R.string.advice_feedback_key))) {

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * 将笔记本分到本地
     */
    private void backupLocal() {
        FileUtils fileUtils = new FileUtils();

        List<Note> notes;
        String userId = App.getUser().getUserId();
        if (TextUtils.isEmpty(userId)) {
            Logger.d("用户没有登录");
            return;
        }
        String strWhere = "userId = " + "\'" + userId + "\'";

        notes = App.getFinalDb().findAllByWhere(Note.class, strWhere, "lastOprTime", true);
        if (notes.size() > 0) {
            if (fileUtils.backupSNotes(notes)) {
                showSnackbar("笔记备份成功");
            } else {
                showSnackbar("笔记备份失败");
            }
        } else {
            showSnackbar("没有笔记需要备份");
        }

    }

    private void showSnackbar(String message) {
        Snackbar.make(getActivity().getWindow().getDecorView(), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 显示对话框，让用户设置私密笔记的密码。
     * 如果当前用户没有设置过密码，则提示用户设置
     * 如果当前用户已经存在私密笔记密码，则提示用户是否重置密码，若选择重置密码，需要先输入登录的密码进行确认。
     */
    private void checkIsSetPassword() {

        PasswordUtils passwordUtils = new PasswordUtils(mContext);
        if (TextUtils.isEmpty(App.getUser().getNotePassword())) {
            // 如果与用户还没有设置加密笔记的密码
            passwordUtils.showSetPrivatePWDialog(mContext.getString(R.string.set_password_title));
        } else {
            // 已经设置了密码，需要重新设置。
            passwordUtils.showForgetPasswordDialog();
        }
    }


    /**
     * 接收更新主题的事件
     *
     * @param event
     */
    public void onEventMainThread(MainEvent event) {
        switch (event) {
            case CHANGE_THEME:
                // 重新初始化界面
                baseActivity.recreate();
                break;
        }
    }

    public enum MainEvent {
        CHANGE_THEME
    }


    /**
     * 调节音量大小的对话框
     */
    private void showAdjustVolumeDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(mContext);
        // 给对话框里面添加view 怎么写？？
        View view = View.inflate(mContext, R.layout.dialog_seekbar, null);

        SeekArc mSeekArc = (SeekArc) view.findViewById(R.id.seekArc);
        TextView mVolumeNum = (TextView) view.findViewById(R.id.seekArcProgress);
        mSeekArc.setProgress(currentVolume);
        mVolumeNum.setText(String.valueOf(currentVolume));

        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                currentVolume = progress;
                mVolumeNum.setText(String.valueOf(progress));
                setSystemVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }
        });
        builder.setView(view);
        builder.setTitle(R.string.volume_dialog_title);
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            stopTestMusic();
            // 将当前设置的音量值保存起来。
            preferenceUtils.saveParam(ConstUtils.ALARM_VOLUME, currentVolume);
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * 设置系统的音量
     *
     * @param progress
     */
    private void setSystemVolume(int progress) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int setVolume = (maxVolume * progress) / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, setVolume, 0);
    }

    /**
     * 播放测试音乐
     */
    private void startTestMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor assetFileDescriptor;
            mediaPlayer.reset();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                assetFileDescriptor = mContext.getAssets().openFd(ringName);
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                        assetFileDescriptor.getStartOffset(),
                        assetFileDescriptor.getLength());
                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopTestMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer = null;
            }
        }
    }

    /**
     * 如果开启振动，则振动一会儿。
     *
     * @param isVibrate
     */
    private void startVibrate() {
        Vibrator vibrator = (Vibrator) App.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{500, 800}, -1);
    }

    /**
     * 初始化反馈操作
     */
    private void initFeedbackPreference() {

    }
}
