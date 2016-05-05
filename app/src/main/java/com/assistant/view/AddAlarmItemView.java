package com.assistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.assistant.R;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/3
 * <p>
 * 功能描述 : 自定义的view
 */
public class AddAlarmItemView extends RelativeLayout {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private Context mContext;
    private TextView tv_title;
    private TextView tv_desc;

    private String mTitle;
    private String mDesc;

    public AddAlarmItemView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    private void initView() {
        View.inflate(mContext, R.layout.view_add_item, this);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_desc = (TextView) findViewById(R.id.tv_desc);

        setTitle(mTitle);
        setDesc(mDesc);
    }


    public AddAlarmItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTitle = attrs.getAttributeValue(NAMESPACE, "mTitle");
        mDesc = attrs.getAttributeValue(NAMESPACE, "mDesc");
        initView();
    }

    public void setTitle(String title) {
        tv_title.setText(title);
    }

    public void setDesc(String desc) {
        tv_desc.setText(desc);
    }
}
