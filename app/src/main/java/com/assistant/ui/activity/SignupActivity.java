package com.assistant.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.R;
import com.assistant.bean.MyBmobUser;
import com.assistant.utils.CheckNetStateUtil;
import com.assistant.utils.DialogUtils;
import com.orhanobut.logger.Logger;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.sms.listener.VerifySMSCodeListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class SignupActivity extends AppCompatActivity {

    @Bind(R.id.input_name)
    EditText etUserName;

    @Bind(R.id.input_password)
    EditText etPassword;

    @Bind(R.id.input_password_again)
    EditText etPasswordAgain;

    @Bind(R.id.btn_signup)
    Button btnSignUp;

    @Bind(R.id.link_login)
    TextView loginLink;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.et_phone_number)
    EditText etPhoneNumber;

    @Bind(R.id.et_check_code)
    EditText etCheckCode;

    @Bind(R.id.btn_check_code)
    Button btnCheckCode;
    private String username;
    private String password;
    private String phoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        if (!CheckNetStateUtil.netIsAvailable(this)) {
            CheckNetStateUtil.showNetUnAvailableDialog(this);
        }

        loginLink.setOnClickListener(v -> {
            // Finish the registration screen and return to the Login activity
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        btnCheckCode.setTextColor(Color.GRAY);
        btnCheckCode.setClickable(false);

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
                    btnCheckCode.setTextColor(Color.BLACK);
                    btnCheckCode.setClickable(true);
                } else {
                    btnCheckCode.setClickable(false);
                    btnCheckCode.setTextColor(Color.GRAY);
                    etPhoneNumber.setError("手机号码不正确");
                }
            }
        });

        btnCheckCode.setOnClickListener(v -> {
            String userPhone = etPhoneNumber.getText().toString();

            getCheckCode(userPhone);

        });

        btnSignUp.setOnClickListener(v -> {
            if (!CheckNetStateUtil.netIsAvailable(this)) {
                CheckNetStateUtil.showNetUnAvailableDialog(this);
                return;
            }
            checkSignUp();
        });
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
                    setButtonState();
                } else {
                    Logger.d("获取验证码失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 设置获取验证码按钮在60s内不可点击
     */
    private void setButtonState() {
        TimeCount timeCount = new TimeCount(60000, 1000);
        timeCount.start();
    }

    public void checkSignUp() {
        showProgressWheel(true);

        username = etUserName.getText().toString();
        password = etPassword.getText().toString();
        phoneNumber = etPhoneNumber.getText().toString();

        // 检测用户是否存在服务器中
        checkUserName(username);

        // 检测输入是否合法
        if (!validate()) {
            showProgressWheel(false);
            return;
        }
        verifySmsCode();
    }

    private void signUp() {

        MyBmobUser user = new MyBmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setUserPhone(phoneNumber);

        user.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                showProgressWheel(false);
                showSuccessTipDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                showProgressWheel(false);
                Logger.d(s);

                if (s.contains("email")) {
                    showToast("该邮箱已经被注册过");
                } else {
                    showToast("注册失败");
                }
            }
        });
    }

    private void verifySmsCode() {

        String phoneNumber = etPhoneNumber.getText().toString();
        String smsCode = etCheckCode.getText().toString();
        BmobSMS.verifySmsCode(this, phoneNumber, smsCode, new VerifySMSCodeListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    signUp();
                } else {
                    Logger.d("验证失败:" + e.getLocalizedMessage());
                    showProgressWheel(false);
                    etCheckCode.setError("验证码不正确");
                }
            }
        });

    }

    /**
     * 登录成功提示对话框
     */
    private void showSuccessTipDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle("登录成功");
        builder.setMessage("您已经注册成功，快去登录体验软件吧!");
        builder.setPositiveButton(R.string.sure, (dialog, which) -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            dialog.dismiss();
            finish();
        });
        builder.show();
    }

    /**
     * 检查该用户是否注册过
     *
     * @param name
     */
    public void checkUserName(String name) {

        BmobQuery<BmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", name);
        query.findObjects(this, new FindListener<BmobUser>() {
            @Override
            public void onSuccess(List<BmobUser> list) {
                for (BmobUser user : list) {
                    if (user.getUsername().equals(name)) {
                        showProgressWheel(false);
                        // 如果服务器中存在用户名，则提示用户登录
                        etUserName.setError("用户名已经存在，请直接登录");
                        return;
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                showProgressWheel(false);
            }
        });
    }

    /**
     * 检测输入是否合法
     *
     * @return
     */
    public boolean validate() {
        boolean valid = true;

        String passwordAgain = etPasswordAgain.getText().toString();
        String checkCode = etCheckCode.getText().toString();

        if (TextUtils.isEmpty(username) || username.length() < 3) {
            etUserName.setError("最少3个字符");
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 4) {
            etPassword.setError("密码不能少于4位");
            valid = false;
        }

        // 手机号码不能为空
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("手机号码不能为空");
            valid = false;
        }
        // 验证码不能为空
        if (TextUtils.isEmpty(checkCode)) {
            etCheckCode.setError("验证码不能为空");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            valid = false;
        }
        if (TextUtils.isEmpty(passwordAgain)) {
            etPasswordAgain.setError("确认密码不能为空");
            valid = false;
        }
        // 检测两次输入的密码是否一致
        if (!password.equals(passwordAgain)) {
            etPasswordAgain.setError("两次输入密码不一致");
            valid = false;
        }

        return valid;
    }

    /**
     * 进度条开关设置
     *
     * @param visible 为true则显示进度条
     */
    private void showProgressWheel(boolean visible) {
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

    private void showToast(String text) {
        Toast.makeText(SignupActivity.this, text, Toast.LENGTH_LONG).show();
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