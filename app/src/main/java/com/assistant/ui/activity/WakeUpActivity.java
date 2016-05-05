package com.assistant.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.R;
import com.assistant.bean.Alarm;
import com.assistant.service.AlarmRingService;
import com.assistant.utils.DialogUtils;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 根据用户设置的赖床等级来显示不同难度的计算题。
 */
public class WakeUpActivity extends BaseActivity {

    @Bind(R.id.tv_display_time)
    TextView tvDisplayTime;
    @Bind(R.id.tv_display_tag)
    TextView tvDisplayTag;
    @Bind(R.id.tv_tip)
    TextView tvTip;
    @Bind(R.id.tv_question)
    TextView tvQuestion;
    @Bind(R.id.et_result)
    AppCompatEditText etResult;
    @Bind(R.id.btn_ok)
    Button btnOk;
    @Bind(R.id.display_lazy_question)
    LinearLayout displayLazyQuestion;

    private Alarm alarm;
    private DateFormat format;
    private int lazyLevel;
    private Intent service;

    private int numA;
    private int numB;
    private int numC;
    private int result;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        alarm = (Alarm) intent.getSerializableExtra("alarm");

        if (alarm != null) {
            initService();
            initView();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: // 使返回键失效
                Logger.d("KEYCODE_BACK");
                return true;
            case KeyEvent.KEYCODE_SYM:
                Logger.d("KEYCODE_SYM");
                return true;
            case KeyEvent.KEYCODE_MENU: // 使菜单键失效
                Logger.d("KEYCODE_MENU");
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initService() {
        service = new Intent(this, AlarmRingService.class);
        service.putExtra("ringId", alarm.getRingResId());
        startService(service);
    }

    @SuppressLint("SimpleDateFormat")
    private void initView() {
        // 在一个线程里面进行时间的更新，使用RxJava来做
        format = new SimpleDateFormat("HH : mm");
        // 每隔1分钟更新一次界面时间
        Observable.interval(1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(aLong -> refreshTime());

        tvDisplayTag.setText(alarm.getTag());
        refreshTime();
        lazyLevel = alarm.getLazyLevel();

        if (lazyLevel == 0) {
            displayLazyQuestion.setVisibility(View.GONE);
            showHelloDialog();
        } else {
            displayLazyQuestion.setVisibility(View.VISIBLE);
            tvTip.setText("来，懒虫。先做个题");
            initQuestion();
        }
    }

    @OnClick(R.id.btn_ok)
    public void onClick() {
        String input = etResult.getText().toString();
        if (TextUtils.isEmpty(input)) {
            showToast("还想交白卷？？？");
        } else {
            int userValue = Integer.parseInt(input);
            if (userValue == result) {
                // 如果计算正确，关闭闹钟。
                count = 0;
                stopService(service);
                showToast("心情愉快哦~~");
                finish();
            } else {
                showToast("算错了");
                count++;
                etResult.setText("");
                if (count < 2) {
                    tvTip.setText("亲，清醒点，再算一次");
                } else if (count <= 4) {
                    tvTip.setText("死鬼，还不快快起床");
                } else if (count < 7) {
                    tvTip.setText("就没见过像你这样既笨又懒的人");
                }
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void initQuestion() {
        btnOk.setEnabled(false);
        result = getRandomQuestion();
        if (result == 0){
            initQuestion();
        }
        switch (lazyLevel){
            case 1:
                tvQuestion.setText(getResources().getString(R.string.show_question_1, numA, numB, numC));
                break;
            case 2:
                tvQuestion.setText(getResources().getString(R.string.show_question_2, numA, numB, numC));
                break;
            case 3:
                tvQuestion.setText(getResources().getString(R.string.show_question_3, numA, numB, numC));
                break;
            case 4:
                tvQuestion.setText(getResources().getString(R.string.show_question_4, numA, numB, numC));
                break;
        }
        btnOk.setEnabled(true);
    }

    private int getRandomQuestion() {
        Random random = new Random();
        switch (lazyLevel){
            case 1:
                numA = random.nextInt(90) + 10;
                numB = random.nextInt(80) + 20;
                numC = random.nextInt(70) + 30;
                return numA + numB + numC;
            case 2:
                numA = random.nextInt(18) + 2;
                while (numA <= 10){
                    numA += 2;
                }
                numB = random.nextInt(18) + 2;
                while (numB <= 10){
                    numB += 3;
                }
                numC = random.nextInt(200) + 100;
                return numA * numB + numC;
            case 3:
                numA = random.nextInt(18) + 2;
                while (numA <= 10){
                    numA += 2;
                }
                numB = random.nextInt(38) + 1;
                while (numB <= 20){
                    numB += 5;
                }
                numC = random.nextInt(2000) + 1000;
                return numA * numB + numC;
            case 4:
                numA = random.nextInt(5) + 5;
                numB = random.nextInt(10) + 10;
                while (numB <= 10){
                    numB = numB + 2;
                }
                numC = random.nextInt(20) + 1;
                while (numC <= 10){
                    numC = numC + 2;
                }
                if (numB == numC){
                    numC += 5;
                }
                return numA * numB * numC;
            default:
                return 0;
        }
    }

    private void showHelloDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle("提示");
        builder.setMessage("关闭闹钟");
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            stopService(service);
            dialog.dismiss();
            finish();
            Logger.d("关闭闹钟");
        });

        AlertDialog dialog = builder.create();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(params);
        dialog.show();

        showToast("心情愉快哦~");
    }

    /**
     * 每分钟刷新一次界面的时间
     */
    private void refreshTime() {
        Date date = new Date();
        tvDisplayTime.setText(format.format(date));
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_wake_up;
    }

}
