package com.assistant.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.Note;
import com.assistant.ui.fragment.NoteFragment;
import com.assistant.utils.DialogUtils;
import com.assistant.utils.KeyBoardUtils;
import com.assistant.utils.TimeUtils;
import com.orhanobut.logger.Logger;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class NoteActivity extends BaseActivity implements View.OnFocusChangeListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.label_edit_text)
    MaterialEditText titleEditText;

    @Bind(R.id.content_edit_text)
    MaterialEditText contentEditText;

    @Bind(R.id.opr_time_line_text)
    TextView oprTimeLineText;

    // 从主页面进入笔记详情页面的操作模式
    public final static int VIEW_NOTE_TYPE = 0x00;
    public final static int EDIT_NOTE_TYPE = 0x01;
    public final static int CREATE_NOTE_TYPE = 0x02;

    public final static String OPERATE_NOTE_TYPE_KEY = "OPERATE_NOTE_TYPE_KEY";

    private MenuItem okItem;
    private MenuItem shareItem;
    private int operatorType = 0;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseIntent(getIntent());
        EventBus.getDefault().registerSticky(this);
        // activity的进入动画
        //showActivityInAnimal();
    }

    /**
     * 接收来自HomeActivity 发送的异步消息
     *
     * @param note
     */
    public void onEventMainThread(Note note) {
        this.note = note;
        initToolbar();
        initEditText();
        initTextView();
    }

    @Override
    protected void onStop() {
        KeyBoardUtils.hideKeyBoard(this, titleEditText);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        okItem = menu.findItem(R.id.done);
        shareItem = menu.findItem(R.id.share);

        okItem.setVisible(false);
        if (operatorType == CREATE_NOTE_TYPE) {
            shareItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                shareNote();
                return true;
            case R.id.done:
                saveNote();
                return true;
            case android.R.id.home:   // 注意加 android
//                hideKeyBoard(titleEditText);
                KeyBoardUtils.hideKeyBoard(this, titleEditText);
                KeyBoardUtils.hideKeyBoard(this, contentEditText);
                if (okItem.isVisible()) {
                    showNotSaveNoteDialog();
                    return true;
                }
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 分享笔记到其他应用
     */
    private void shareNote() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText());
        shareIntent = Intent.createChooser(shareIntent, "分享到");
        startActivity(shareIntent);

    }

    private String getShareText() {
        String noteTitle = note.getTitle();
        String noteContent = note.getContent();
        String text = "标题:" + noteTitle + "\n" + "内容:" + noteContent;

        Logger.d(text);

        return text;
    }

    /**
     * 当用户按下返回键后，进行判断
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            hideKeyBoard(titleEditText);
            KeyBoardUtils.hideKeyBoard(this, titleEditText);
            if (okItem != null && okItem.isVisible()) {
                showNotSaveNoteDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
        //showActivityOutAnimal();
    }

    private void showNotSaveNoteDialog() {

        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        DialogInterface.OnClickListener listener = (DialogInterface dialog, int which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    saveNote();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    NoteActivity.this.finish();
                    break;
                default:
                    break;
            }
        };
        builder.setPositiveButton(R.string.sure, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.show();
    }

    /**
     * 保存笔记
     */
    private void saveNote() {
        KeyBoardUtils.hideKeyBoard(this, titleEditText);
        KeyBoardUtils.hideKeyBoard(this, contentEditText);

        note.setTitle(titleEditText.getText().toString());
        note.setContent(contentEditText.getText().toString());
        // 设置最后的编辑时间
        note.setLastOprTime(TimeUtils.getCurrentTimeInLong());

        switch (operatorType) {
            case CREATE_NOTE_TYPE:
                // 设置这个笔记所属的用户
                note.setUserId(user.getUserId());
                // 设置笔记是否为加密状态，如果是从加密的页面进行创建的笔记，则设置为加密状态。
                if (App.isNormalNote()) {
                    note.setIsLock("false");
                } else {
                    note.setIsLock("true");
                }
                note.setCreateTime(TimeUtils.getCurrentTimeInLong());
                finalDb.saveBindId(note);
                break;
            default:
                finalDb.update(note);
                break;
        }
        // 当笔记内容发生变更后发送消息通知NoteFragment更新视图
        EventBus.getDefault().post(NoteFragment.NoteEvent.UPDATE_NOTE);
        finish();

    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.view_note);

        switch (operatorType) {
            case CREATE_NOTE_TYPE:
                toolbar.setTitle(R.string.new_note);
                break;
            case VIEW_NOTE_TYPE:
                toolbar.setTitle(R.string.view_note);
                break;
            case EDIT_NOTE_TYPE:
                toolbar.setTitle(R.string.edit_note);
                break;
            default:
                break;
        }
    }

    private void initTextView() {
        oprTimeLineText.setText(getOprTimeLineText(note));
    }

    /**
     * 返回笔记的编辑时间的信息
     *
     * @param note
     * @return
     */
    private String getOprTimeLineText(Note note) {
        if (note == null || note.getLastOprTime() == 0) {
            return "";
        }

        String create = getString(R.string.create);
        String update = getString(R.string.last_update);
        StringBuilder buffer = new StringBuilder();

        /**
         * 当最后编辑的时间小于创建笔记的时间时，说明该笔记是新创建的。
         */
        if (note.getLastOprTime() <= note.getCreateTime() || note.getCreateTime() == 0) {
            buffer.append(getString(R.string.note_log_text, create, TimeUtils.getTime(note.getLastOprTime())));
            return buffer.toString();
        }
        buffer.append(getString(R.string.note_log_text, update, TimeUtils.getTime(note.getLastOprTime())));
        buffer.append("\n");
        buffer.append(getString(R.string.note_log_text, create, TimeUtils.getTime(note.getCreateTime())));

        return buffer.toString();
    }

    private void initEditText() {
        switch (operatorType) {
            case EDIT_NOTE_TYPE:
//                showKeyBoard();
                KeyBoardUtils.showKeyBoard(this);
                titleEditText.requestFocus();
                titleEditText.setText(note.getTitle());
                contentEditText.setText(note.getContent());
                titleEditText.setSelection(note.getTitle().length());
                contentEditText.setSelection(note.getContent().length());
                break;
            case VIEW_NOTE_TYPE:
//                hideKeyBoard(titleEditText);
                KeyBoardUtils.hideKeyBoard(this, titleEditText);
                titleEditText.setText(note.getTitle());
                contentEditText.setText(note.getContent());
                titleEditText.setOnFocusChangeListener(this);
                contentEditText.setOnFocusChangeListener(this);
                break;
            default:
                // 显示键盘
//                showKeyBoard();
                KeyBoardUtils.showKeyBoard(this);
                // 新建一个笔记，标题获取焦点
                titleEditText.requestFocus();

                break;
        }
        titleEditText.addTextChangedListener(new SimpleTextWatcher());
        contentEditText.addTextChangedListener(new SimpleTextWatcher());
    }


    /**
     * 解析从上个页面传来的INTENT
     *
     * @param intent
     */
    private void parseIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            operatorType = intent.getIntExtra(OPERATE_NOTE_TYPE_KEY, 0);
        }
    }


    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (okItem == null) {
                Log.d("TAG", "okItem is null");
                return;
            }
            String labelSrc = titleEditText.getText().toString();
            String contentSrc = contentEditText.getText().toString();
            // 使用正则表达式替换出现的不合法字符
            // 1."\\s"---匹配任何不可见字符，包括空格，制表，换页符
            // 2."|"-----将两个匹配条件进行逻辑‘或’运算
            // 3."\t"----匹配一个制表符
            // 4."\r"----匹配一个回车符
            // 5."\n"----标识一个八进制转义值或一个向后引用
            String title = labelSrc.replaceAll("\\s*|\t|\r|\n", "");
            String content = contentSrc.replaceAll("\\s*|\t|\r|\n", "");
            // 标题和内容都不为空时，设置 doneMenuItem 为可见
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
                if (TextUtils.equals(labelSrc, note.getTitle()) && TextUtils.equals(contentSrc, note.getContent())) {
                    // 如果用户并没有改变文本内容，则不用保存
                    okItem.setVisible(false);
                    return;
                }
                // 笔记内容发生改变,菜单栏随之发送变化
                shareItem.setVisible(true);
                okItem.setVisible(true);
            } else {
                okItem.setVisible(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void showKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyBoard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromInputMethod(editText.getWindowToken(), 0);
    }

    /**
     * 进入动画
     */
    private void showActivityInAnimal() {
        // 淡入淡出效果
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 退出动画
     */
    private void showActivityOutAnimal() {
        overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
    }

    /**
     * 检测用户是否准备编辑笔记
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && toolbar != null) {
            toolbar.setTitle(R.string.edit_note);
        }
    }
}
