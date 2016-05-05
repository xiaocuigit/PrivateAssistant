package com.assistant.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.User;
import com.assistant.ui.activity.HomeActivity;

import net.tsz.afinal.FinalDb;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/9
 * <p>
 * 功能描述 :
 */
public class PasswordUtils {

    private Context context;
    private User user;
    private FinalDb finalDb;

    public PasswordUtils(Context context) {
        this.context = context;
        finalDb = App.getFinalDb();
        user = App.getUser();
    }


    /**
     * 检测用户是否设置了私密笔记密码
     */
    public void checkSetting() {

        if (TextUtils.isEmpty(user.getNotePassword())) {
            // 如果用户还没有设置密码
            showSetPrivatePWDialog(context.getString(R.string.set_password_title));
        } else {
            // 已经设置了密码
            showEnterPrivateDialog();
        }
    }

    /**
     * 提示用户设置加密笔记的密码
     */
    public void showSetPrivatePWDialog(String title) {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(context);
        View view = View.inflate(context, R.layout.dialog_set_private, null);
        AppCompatEditText etPassword = (AppCompatEditText) view.findViewById(R.id.et_password);
        AppCompatEditText etPasswordAgain = (AppCompatEditText) view.findViewById(R.id.et_ensure_password);
        Button btnSure = (Button) view.findViewById(R.id.btn_sure);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        builder.setView(view);
        builder.setTitle(title);

        AlertDialog dialog = builder.create();

        btnSure.setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            String ensurePassword = etPasswordAgain.getText().toString();
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("密码不能为空");
                return;
            }
            if (TextUtils.isEmpty(ensurePassword)) {
                etPasswordAgain.setError("密码不能为空");
                return;
            }
            if (!password.equals(ensurePassword)) {
                etPasswordAgain.setError("两次输入密码不一致");
                return;
            }
            // 将密码保存起来,通过MD5算法加密
            user.setNotePassword(MD5Utils.enCode(password));
            finalDb.update(user);
            dialog.dismiss();
            Toast.makeText(context, "密码设置成功", Toast.LENGTH_SHORT).show();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

    }

    /**
     * 进入加密笔记的验证操作
     * 先弹出一个对话框，提示用户输入密码后才能进入，如果忘记密码，就进行重置，用登录密码来验证用户。
     */
    public void showEnterPrivateDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(context);

        View view = View.inflate(context, R.layout.dialog_enter_private, null);
        AppCompatEditText etPassword = (AppCompatEditText) view.findViewById(R.id.et_password);
        TextView tvForgetPassword = (TextView) view.findViewById(R.id.tv_forget_password);
        Button btnSure = (Button) view.findViewById(R.id.btn_sure);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        builder.setTitle(R.string.password_title);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        btnSure.setOnClickListener(v1 -> {
            String mPassword = etPassword.getText().toString();
            if (TextUtils.isEmpty(mPassword)) {
                etPassword.setError("密码不能为空");
                return;
            }
            if (user.getNotePassword().equals(MD5Utils.enCode(mPassword))) {
                EventBus.getDefault().post(HomeActivity.MainEvent.ENTER_PRIVATE_NOTE);
                dialog.dismiss();
            } else {
                etPassword.setError("密码错误");
                etPassword.setText("");
            }
        });

        btnCancel.setOnClickListener(v1 -> dialog.dismiss());

        tvForgetPassword.setOnClickListener(v -> {
            dialog.dismiss();
            showForgetPasswordDialog();
        });

        dialog.show();
    }

    /**
     * 显示忘记密码对话框，提示重新设置密码
     */
    public void showForgetPasswordDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(context);

        View view = View.inflate(context, R.layout.dialog_enter_private, null);
        AppCompatEditText etPassword = (AppCompatEditText) view.findViewById(R.id.et_password);
        TextView tvForgetPassword = (TextView) view.findViewById(R.id.tv_forget_password);
        Button btnSure = (Button) view.findViewById(R.id.btn_sure);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        tvForgetPassword.setVisibility(View.GONE);
        etPassword.setHint("请输入登录密码");

        builder.setView(view);
        builder.setTitle(context.getString(R.string.find_password_title));
        AlertDialog dialog = builder.create();

        btnSure.setOnClickListener(v -> {
            String inputPassword = etPassword.getText().toString();
            if (!TextUtils.isEmpty(inputPassword)) {
                String userPassword = user.getSavePassword();
                if (userPassword.equals(inputPassword)) {
                    // 这里为什么不消失？？
                    dialog.dismiss();
                    showSetPrivatePWDialog(context.getString(R.string.reset_password_title));
                } else {
                    etPassword.setError("密码错误");
                    etPassword.setText("");

                }
            } else {
                etPassword.setError("密码不能为空");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


}
