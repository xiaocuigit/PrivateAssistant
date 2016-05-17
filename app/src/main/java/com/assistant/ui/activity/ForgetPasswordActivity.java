package com.assistant.ui.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.MyBmobUser;
import com.assistant.bean.User;
import com.assistant.utils.CheckNetStateUtil;
import com.assistant.utils.DialogUtils;
import com.orhanobut.logger.Logger;

import net.tsz.afinal.FinalDb;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.sms.listener.VerifySMSCodeListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class ForgetPasswordActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.et_phone_number)
    EditText etPhoneNumber;
    @Bind(R.id.et_check_code)
    EditText etCheckCode;
    @Bind(R.id.btn_check_code)
    Button btnCheckCode;
    @Bind(R.id.et_reset_password)
    EditText etResetPassword;
    @Bind(R.id.et_reset_password_again)
    EditText etResetPasswordAgain;
    @Bind(R.id.btn_ok)
    AppCompatButton btnOk;

    private String mPhoneNumber;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        ButterKnife.bind(this);

        initToolbar();

        if (!CheckNetStateUtil.netIsAvailable(this)) {
            CheckNetStateUtil.showNetUnAvailableDialog(this);
        }

        parseIntent();

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String number = s.toString();
                if (number.matches("^1[3-8]\\d{9}$")) {
                    btnCheckCode.setClickable(true);
                    btnCheckCode.setTextColor(Color.BLACK);
                } else {
                    btnCheckCode.setClickable(false);
                    btnCheckCode.setTextColor(Color.GRAY);
                    etPhoneNumber.setError("手机号码不正确");
                }
            }
        });

    }

    private void initToolbar() {
        toolbar.setTitle(R.string.forget_login_password);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.collapseActionView();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        Logger.d("username is " + username);

        BmobQuery<MyBmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.findObjects(this, new FindListener<MyBmobUser>() {
            @Override
            public void onSuccess(List<MyBmobUser> list) {
                for (MyBmobUser user : list) {
                    if (user.getUsername().equals(username)) {
                        mPhoneNumber = user.getUserPhone();
                        mUserId = user.getObjectId();
                        if (!TextUtils.isEmpty(mPhoneNumber)) {
                            etPhoneNumber.setText(mPhoneNumber);
                            btnCheckCode.setClickable(true);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Logger.d("无法获取手机号");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btn_check_code, R.id.btn_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_check_code:
                String number = etPhoneNumber.getText().toString();
                getCheckCode(number);
                break;
            case R.id.btn_ok:
                if (!CheckNetStateUtil.netIsAvailable(this)) {
                    CheckNetStateUtil.showNetUnAvailableDialog(this);
                } else {
                    resetPassword();
                }
                break;
        }
    }

    /**
     * 重启设置密码
     */
    private void resetPassword() {

        if (!validate()) {
            return;
        }

        verifySmsCode();
    }

    private void updateBmobData() {
        String password = etResetPassword.getText().toString();

        MyBmobUser user = new MyBmobUser();
        user.setUserPhone(mPhoneNumber);
        user.setPassword(password);
        user.update(this, mUserId, new UpdateListener() {
            @Override
            public void onSuccess() {
                Logger.d("更新成功");
                updateDataBase();
            }

            @Override
            public void onFailure(int i, String s) {
                Logger.d("更新失败");
            }
        });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setMessage("密码重置成功, 重新登录");
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
            finish();
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * 更新本地数据库的用户信息
     */
    private void updateDataBase() {
        String password = etResetPassword.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();
        FinalDb finalDb = App.getFinalDb();

        List<User> userList = finalDb.findAll(User.class);
        for (User user : userList) {
            if (user.getUserId().equals(mUserId)) {
                user.setUserPhone(phoneNumber);
                user.setSavePassword(password);
                finalDb.update(user);
                showSuccessDialog();
                break;
            }
        }
    }

    private void verifySmsCode() {

        String phoneNumber = etPhoneNumber.getText().toString();
        String smsCode = etCheckCode.getText().toString();
        BmobSMS.verifySmsCode(this, phoneNumber, smsCode, new VerifySMSCodeListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    updateBmobData();
                } else {
                    etCheckCode.setError("验证码错误");
                }
            }
        });
    }

    /**
     * 验证输入是否合法
     *
     * @return
     */
    private boolean validate() {

        String phoneNumber = etPhoneNumber.getText().toString();
        String password = etResetPassword.getText().toString();
        String passwordAgain = etResetPasswordAgain.getText().toString();
        String smsCode = etCheckCode.getText().toString();

        boolean valid = true;

        // 手机号码不能为空
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("手机号码不能为空");
            valid = false;
        }
        // 验证码不能为空
        if (TextUtils.isEmpty(smsCode)) {
            etCheckCode.setError("验证码不能为空");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            etResetPassword.setError("密码不能为空");
            valid = false;
        }
        if (TextUtils.isEmpty(passwordAgain)) {
            etResetPasswordAgain.setError("确认密码不能为空");
            valid = false;
        }
        // 检测两次输入的密码是否一致
        if (!password.equals(passwordAgain)) {
            etResetPasswordAgain.setError("两次输入密码不一致");
            valid = false;
        }
        return valid;
    }

    /**
     * 获取验证码
     */
    private void getCheckCode(String userPhone) {
        BmobSMS.requestSMSCode(this, userPhone, "短信验证", new RequestSMSCodeListener() {
            @Override
            public void done(Integer code, BmobException e) {
                if (e == null) {
                    Logger.d("SmsCode is " + code);
                    TimeCount timeCount = new TimeCount(60000, 1000);
                    timeCount.start();
                } else {
                    Logger.d("获取验证码失败：" + e.getMessage());
                }
            }
        });
    }

    private void showNetUnAvailableDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setMessage("网络不可用，请连接网络后再使用");
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            // 打开设置界面让用户设置网络
            dialog.dismiss();
        });
        builder.show();
    }

    private void showToast(String text) {
        Toast.makeText(ForgetPasswordActivity.this, text, Toast.LENGTH_LONG).show();
    }


    class TimeCount extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnCheckCode.setClickable(false);
            btnCheckCode.setTextColor(Color.GRAY);
            btnCheckCode.setText(millisUntilFinished / 1000 + "秒后重发");
        }

        @Override
        public void onFinish() {
            btnCheckCode.setText("重新验证");
            btnCheckCode.setTextColor(Color.BLACK);
            btnCheckCode.setClickable(true);
        }
    }
}
