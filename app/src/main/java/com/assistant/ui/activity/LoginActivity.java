package com.assistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.MyBmobUser;
import com.assistant.bean.User;
import com.assistant.utils.CheckNetStateUtil;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.KeyBoardUtils;
import com.orhanobut.logger.Logger;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.tsz.afinal.FinalDb;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.sms.BmobSMS;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity {

    private static final String APPLICATION_ID = "3cf1b80eef7d353945fce65542f77344";

    @Bind(R.id.input_userName)
    EditText etUserName;

    @Bind(R.id.input_password)
    EditText etPassword;

    @Bind(R.id.btn_login)
    Button btnLogin;

    @Bind(R.id.tv_signup_account)
    TextView tvSignUp;

    @Bind((R.id.tv_forget_password))
    TextView tvForgetPassword;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    private String username;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (App.getUserState() == ConstUtils.LOGIN) {
            // 检测是否已经登录成功过
            checkIsLogined();
        }

        initBmob();

        if (!CheckNetStateUtil.netIsAvailable(this)) {
            CheckNetStateUtil.showNetUnAvailableDialog(this);
        }

        btnLogin.setOnClickListener(v -> {
            if (!CheckNetStateUtil.netIsAvailable(this)) {
                CheckNetStateUtil.showNetUnAvailableDialog(this);
                return;
            }
            readyLogin();
        });

        tvSignUp.setOnClickListener(v -> {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            hideKeyBoard();
            finish();
        });

        tvForgetPassword.setOnClickListener(v -> {
            username = etUserName.getText().toString();

            if (!TextUtils.isEmpty(username)) {
                userNameIsExist(username);
            } else {
                etUserName.setError("用户名不能为空");
            }
        });
    }

    private void userNameIsExist(String username) {
        BmobQuery<MyBmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.findObjects(this, new FindListener<MyBmobUser>() {
            @Override
            public void onSuccess(List<MyBmobUser> list) {
                boolean flag = false;

                for (MyBmobUser user : list) {
                    if (user.getUsername().equals(username)) {
                        Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        hideKeyBoard();
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    etUserName.setError("该用户还没有注册");
                }
            }

            @Override
            public void onError(int i, String s) {
            }
        });

    }

    private void checkIsLogined() {
        BmobUser bmobUser = BmobUser.getCurrentUser(this);
        if (bmobUser != null) {
            Logger.d("username" + bmobUser.getUsername());
            FinalDb finalDb = App.getFinalDb();
            // 从数据库总读取所有的用户信息
            List<User> list = finalDb.findAll(User.class);
            for (User user : list) {
                if (user.getUserId().equals(bmobUser.getObjectId())) {
                    Logger.d("password = " + user.getSavePassword());
                    Logger.d("免除登录");
                    App.setUser(user);
                    // 进入软件主页
                    startHomeActivity();
                    return;
                }
            }
        }
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
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void hideKeyBoard() {
        KeyBoardUtils.hideKeyBoard(this, etUserName);
        KeyBoardUtils.hideKeyBoard(this, etPassword);
    }

    /**
     * 初始化后端云
     */
    private void initBmob() {
        Bmob.initialize(this, APPLICATION_ID);
        BmobSMS.initialize(this, APPLICATION_ID);
    }

    /**
     * 进行登录之前的检测
     */
    public void readyLogin() {
        // 显示正在加载
        showProgressWheel(true);

        username = etUserName.getText().toString();
        password = etPassword.getText().toString();

        // 检测网络是否连接
        if (!CheckNetStateUtil.netIsAvailable(this)) {
            showToast("网络连接不可用");
            showProgressWheel(false);
            return;
        }
        // 判断输入是否合法
        if (!validate()) {
            showProgressWheel(false);
            return;
        }
        checkUserName(username);

    }


    /**
     * 登录
     */
    public void login() {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        // 初始化用户信息
        initUserInfo();
        user.login(this, new SaveListener() {
            @Override
            public void onSuccess() {
                showProgressWheel(false);
                App.setUserState(ConstUtils.LOGIN);
                startHomeActivity();
            }

            @Override
            public void onFailure(int i, String s) {
                showProgressWheel(false);
                etPassword.setError("密码错误");
            }
        });
    }

    private void startHomeActivity() {
        // 将登陆的用户传递给user变量，供全局使用
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        hideKeyBoard();
        finish();
    }

    /**
     * 获取当前登录用户的信息
     */
    private void initUserInfo() {
        final MyBmobUser[] bmobUser = new MyBmobUser[1];
        BmobQuery<MyBmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.findObjects(this, new FindListener<MyBmobUser>() {
            @Override
            public void onSuccess(List<MyBmobUser> list) {
                bmobUser[0] = list.get(0);
                if (bmobUser[0] != null) {
                    FinalDb finalDb = App.getFinalDb();
                    // 返回所有的User信息
                    List<User> userList = finalDb.findAll(User.class);
                    boolean flag = false;
                    for (User user : userList) {
                        // 访问数据库，查看该用户信息是否已经在数据库中
                        if (user.getUserId().equals(bmobUser[0].getObjectId())) {
                            // 将当前登录的用户信息赋值给整个应用程序
                            App.setUser(user);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        // 用户首次登录，数据库没有保存该用户的信息
                        // 将用户的信息保存起来。
                        User user = new User();
                        user.setUserId(bmobUser[0].getObjectId());
                        user.setUserPhone(bmobUser[0].getUserPhone());
                        user.setUserName(username);
                        user.setSavePassword(password);

                        finalDb.save(user);
                        // 重新查找数据库，将当前登录的用户赋值给整个应用程序
                        userList = finalDb.findAll(User.class);
                        for (User u : userList) {
                            // 访问数据库，查看该用户信息是否已经在数据库中
                            if (u.getUserId().equals(bmobUser[0].getObjectId())) {
                                // 将当前登录的用户信息赋值给整个应用程序
                                App.setUser(u);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Logger.d(s);
            }
        });
    }

    /**
     * 判断用户输入是否合法
     *
     * @return
     */
    public boolean validate() {
        boolean value = true;

        if (TextUtils.isEmpty(username)) {
            etUserName.setError("用户名不能为空");
            value = false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            value = false;
        }
        return value;
    }

    /**
     * 登录之前先检查该用户是否已经注册
     *
     * @param name
     */
    public void checkUserName(final String name) {

        BmobQuery<MyBmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", name);
        query.findObjects(this, new FindListener<MyBmobUser>() {
            @Override
            public void onSuccess(List<MyBmobUser> list) {
                boolean isExist = false;
                for (MyBmobUser user : list) {
                    if (user.getUsername().equals(name)) {
                        isExist = true;
                        login();
                        break;
                    }
                }
                if (!isExist) {
                    showProgressWheel(false);
                    etUserName.setError("请先注册再登录");
                }
            }

            @Override
            public void onError(int i, String s) {
                showProgressWheel(false);
                showToast("查询失败");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
