package com.assistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.R;
import com.assistant.utils.CheckNetStateUtil;
import com.orhanobut.logger.Logger;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.input_userName)
    EditText _emailText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.input_password_again)
    EditText _passwordAgainText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        if (!CheckNetStateUtil.netIsAvailable(this)) {
            Toast.makeText(SignupActivity.this, "网络连接不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        _signupButton.setOnClickListener(v -> {
            if (!CheckNetStateUtil.netIsAvailable(this)) {
                Toast.makeText(SignupActivity.this, "网络连接不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            signUp();
        });

        _loginLink.setOnClickListener(v -> {
            // Finish the registration screen and return to the Login activity
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    public void signUp() {
        showProgressWheel(true);
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String passwordAgain = _passwordAgainText.getText().toString();

        // 检测网络连接
        if (!CheckNetStateUtil.netIsAvailable(this)) {
            showProgressWheel(false);
            showToast("网络连接不可用");
            return;
        }

        // 检测用户是否存在服务器中
        checkUserName(name);

        // 检测输入是否合法
        if (!validate()) {
            showProgressWheel(false);
            showToast("输入不合法");
            return;
        }
        // 检测两次输入的密码是否一致
        if (!password.equals(passwordAgain)) {
            showProgressWheel(false);
            _passwordAgainText.setError("两次输入密码不一致");
            return;
        }
        BmobUser user = new BmobUser();
        user.setUsername(name);
        user.setPassword(password);
        user.setEmail(email);

        user.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                showProgressWheel(false);
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                showToast("注册成功");
                finish();
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

    /**
     * 检查该用户是否注册过
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
                        _nameText.setError("用户名已经存在，请直接登录");
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

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("最少3个字符");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("邮箱不合法");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            _passwordText.setError("密码不能少于4位");
            valid = false;
        } else {
            _passwordText.setError(null);
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
}