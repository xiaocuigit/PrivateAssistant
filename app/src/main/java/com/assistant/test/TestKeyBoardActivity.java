package com.assistant.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;

import com.assistant.R;
import com.assistant.utils.KeyBoardUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestKeyBoardActivity extends AppCompatActivity {

    @Bind(R.id.btn_display)
    Button btnDisplay;
    @Bind(R.id.btn_dismiss)
    Button btnDismiss;
    @Bind(R.id.text)
    AppCompatEditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_key_board);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_display, R.id.btn_dismiss})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_display:
                KeyBoardUtils.showKeyBoard(this);
                break;
            case R.id.btn_dismiss:
                KeyBoardUtils.hideKeyBoard(this, text);
                break;
        }
    }
}
