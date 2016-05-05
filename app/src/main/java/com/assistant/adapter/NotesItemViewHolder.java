package com.assistant.adapter;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.assistant.R;


/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/28
 * <p/>
 * 功能描述 :
 */
public class NotesItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView mNoteLabelTextView;
    private final TextView mNoteContentTextView;
    private final TextView mNoteTimeTextView;

    public NotesItemViewHolder(View parent) {
        super(parent);
        mNoteLabelTextView = (TextView) parent.findViewById(R.id.note_label_text);
        mNoteContentTextView = (TextView) parent.findViewById(R.id.note_content_text);
        mNoteTimeTextView = (TextView) parent.findViewById(R.id.note_last_edit_text);
    }

    public void setLabelText(CharSequence text) {
        setTextView(mNoteLabelTextView, text);
    }

    public void setLabelText(int text) {
        setTextView(mNoteLabelTextView, text);
    }

    public void setContentText(CharSequence text) {
        setTextView(mNoteContentTextView, text);
    }

    public void setContentText(int text) {
        setTextView(mNoteContentTextView, text);
    }

    public void setTimeText(CharSequence text) {
        setTextView(mNoteTimeTextView, text);
    }

    public void setTimeText(int text) {
        setTextView(mNoteTimeTextView, text);
    }

    private void setTextView(TextView view, CharSequence text) {
        // 判断view是否存在 以及 输入的内容是否为空
        if (view == null || TextUtils.isEmpty(text))
            return;
        view.setText(text);
    }

    private void setTextView(TextView view, @StringRes int text) {
        if (view == null || text <= 0)
            return;
        view.setText(text);
    }
}
