package com.assistant.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.adapter.BaseRecyclerViewAdapter;
import com.assistant.adapter.NotesAdapter;
import com.assistant.bean.Note;
import com.assistant.ui.activity.NoteActivity;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.DialogUtils;
import com.assistant.utils.PasswordUtils;
import com.assistant.view.BetterFab;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/2
 * <p>
 * 功能描述 :
 */
public class NoteFragment extends BaseFragment {

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.fab)
    BetterFab addNote;

    @Bind(R.id.no_note_tip)
    TextView noNoteTip;

    private NotesAdapter recyclerAdapter;

    private boolean cardLayout = true;   // 笔记默认是以卡片风格显示

    public static NoteFragment newInstance() {
        return new NoteFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册事件接收器
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        ButterKnife.bind(this, view);

        initRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 当界面重新呈现给用户时检测当前笔记显示的风格
        if (cardLayout != preferenceUtils.getBooleanParam(ConstUtils.CARD_LAYOUT, true)) {
            cardLayout = !cardLayout;
            changeItemLayout(cardLayout);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private void showFab(boolean visible) {
        addNote.setForceHide(!visible);
    }


    /**
     * 添加笔记的监听按钮
     */
    @OnClick(R.id.fab)
    public void onClick() {
        Note note = new Note();
        startNoteActivity(NoteActivity.CREATE_NOTE_TYPE, note);
    }


/****************************************华丽的分割线**********************************************/


    /**
     * 设置笔记的显示风格
     *
     * @param flag
     */
    private void changeItemLayout(boolean flag) {
        cardLayout = flag;
        if (flag) {
            // 卡片式
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        }
    }

    /**
     * 初始化显示的数据
     */
    private void initRecyclerView() {
        // 加载笔记的时候显示进度条
        showProgressWheel(true);
        // 初始化笔记的布局
        initItemLayout();
        // 方法用来使RecyclerView保持固定的大小，该信息被用于自身的优化。
        recyclerView.setHasFixedSize(true);
        // 为adapter初始化数据
        recyclerAdapter = new NotesAdapter(initItemData(), mActivity);
        // 给笔记整体添加监听器
        recyclerAdapter.setOnInViewClickListener(R.id.notes_item_root,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<Note>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Note values) {
                        super.OnClickListener(parentV, v, position, values);
                        // 以查看的模式进入笔记
                        startNoteActivity(NoteActivity.VIEW_NOTE_TYPE, values);
                    }
                });
        // 给笔记的设置按钮添加监听器
        recyclerAdapter.setOnInViewClickListener(R.id.note_more,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<Note>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Note values) {
                        super.OnClickListener(parentV, v, position, values);
                        // 点击每个笔记的属性按钮，弹出对话框
                        if (App.isNormalNote()) {
                            // 普通笔记
                            showPopupMenu(v, values);
                        } else {
                            // 加密笔记
                            showPrivatePopupMenu(v, values);
                        }
                    }
                });
        recyclerAdapter.setFirstOnly(false);
        recyclerAdapter.setDuration(300);
        recyclerView.setAdapter(recyclerAdapter);
        showProgressWheel(false);
    }

    private void showPrivatePopupMenu(View view, Note note) {
        PopupMenu popup = new PopupMenu(mActivity, view);

        popup.getMenuInflater().inflate(R.menu.menu_private_notes_more, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.edit:
                    startNoteActivity(NoteActivity.EDIT_NOTE_TYPE, note);
                    break;
                case R.id.unlock_note:
                    // 将当前笔记放到加密笔记本里面
                    note.setIsLock("false");
                    finalDb.update(note);
                    // 在这里刷新界面为什么不成功
                    updateNotes();
                    break;
                case R.id.delete:
                    showDeleteForeverDialog(note);
                    break;
                default:
                    break;
            }
            return true;
        });
        popup.show();
    }

    /**
     * 针对不同模式的menu来设置popup动画，并设置点击监听器
     *
     * @param view
     * @param note
     */
    private void showPopupMenu(View view, Note note) {
        PopupMenu popup = new PopupMenu(mActivity, view);

        popup.getMenuInflater().inflate(R.menu.menu_notes_more, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.share:
                    shareNote(note);
                    break;
                case R.id.edit:
                    startNoteActivity(NoteActivity.EDIT_NOTE_TYPE, note);
                    break;
                case R.id.lock_note:
                    // 先检查用户是否设置了密码
                    if (TextUtils.isEmpty(user.getNotePassword())) {
                        PasswordUtils passwordUtils = new PasswordUtils(mActivity);
                        passwordUtils.showSetPrivatePWDialog(getString(R.string.set_password_title));
                    }
                    // 将当前笔记放到加密笔记本里面
                    note.setIsLock("true");
                    // 更新数据库的note属性
                    finalDb.update(note);
                    updateNotes();
                    break;
                case R.id.delete:
                    showDeleteForeverDialog(note);
                    break;
                default:
                    break;
            }
            return true;
        });
        popup.show();
    }


    /**
     * 分享笔记
     *
     * @param note
     */
    private void shareNote(Note note) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText(note));
        shareIntent = Intent.createChooser(shareIntent, "分享到");
        startActivity(shareIntent);
    }

    /**
     * 获取分享的内容
     *
     * @param note
     * @return
     */
    private String getShareText(Note note) {
        String noteTitle = note.getTitle();
        String noteContent = note.getContent();
        String text = "标题: " + noteTitle + "\n" + "内容: " + noteContent;
        return text;
    }

    /**
     * 删除笔记提醒用户的对话框
     *
     * @param note
     */
    private void showDeleteForeverDialog(Note note) {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(mActivity);
        builder.setTitle(R.string.delete_tip);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    finalDb.delete(note);
                    // 更新视图
                    updateNotes();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
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
     * 根据type来判断当前处于什么页面，然后返回该页面上应该显示的笔记
     *
     * @param type
     * @return
     */
    private List<Note> initItemData() {
        List<Note> items;
        List<Note> notes = new ArrayList<>();
        String strWhere = "userId = " + "\'" + userId + "\'";
        // 将查询结果根据最后编辑的时间来排序，true表示默认降序排列
        // 在这里降序排列插入list后，取出的结果是按从小到大排列的。
        // 根据userID来获取该用户所有的笔记
        items = finalDb.findAllByWhere(Note.class, strWhere, "lastOprTime", true);
        if (App.isNormalNote()) {
            // 普通笔记
            for (Note note : items) {
                if (note.getIsLock().equals("false")) {
                    // 如果当前笔记没有加密
                    notes.add(note);
                }
            }
        } else {
            // 加密笔记
            for (Note note : items) {
                if (note.getIsLock().equals("true")) {
                    // 显示已经加密的笔记
                    notes.add(note);
                }
            }
        }
        if (notes.isEmpty()) {
            noNoteTip.setVisibility(View.VISIBLE);
        } else {
            noNoteTip.setVisibility(View.GONE);
        }
        return notes;
    }

    /**
     * 初始化笔记的显示风格
     */
    private void initItemLayout() {
        if (preferenceUtils.getBooleanParam(ConstUtils.CARD_LAYOUT, true)) {
            cardLayout = true;
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        } else {
            cardLayout = false;
            recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        }
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    /**
     * 以某一种特定的模式进入编辑笔记页面
     *
     * @param oprType
     * @param values
     */
    private void startNoteActivity(int oprType, Note values) {
        Intent intent = new Intent(mActivity, NoteActivity.class);
        intent.putExtra(NoteActivity.OPERATE_NOTE_TYPE_KEY, oprType);
        EventBus.getDefault().postSticky(values);
        startActivity(intent);
    }

    private void updateNotes() {
        showProgressWheel(true);

        // 返回当前笔记的状态下数据库里面剩余的笔记，还是按照最后操作的时间来排序
        List<Note> list = initItemData();

        // 当前模式下的所有笔记
        recyclerAdapter.setList(list);
        mActivity.runOnUiThread(recyclerAdapter::notifyDataSetChanged);

        showProgressWheel(false);
    }

    /**
     * 进度条开关设置
     *
     * @param visible 为true则显示进度条
     */
    private void showProgressWheel(boolean visible) {
        progressWheel.setBarColor(getColorPrimary());
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

    /**
     * 接收来自 搜索框 发送的事件
     *
     * @param text
     */
    public void onEventMainThread(String text) {
        // 显示搜索的信息
        recyclerAdapter.getFilter().filter(text);
    }

    /**
     * 接收异步事件的处理函数
     *
     * @param event
     */

    public void onEventMainThread(NoteEvent event) {
        switch (event) {
            // 更新当前笔记内容
            case UPDATE_NOTE:
                updateNotes();
                break;
            case HIDE_FAB:
                showFab(false);
                break;
            case DISPLAY_FAB:
                showFab(true);
                break;
        }
    }

    public enum NoteEvent {
        UPDATE_NOTE,             // 更新正常的笔记
        HIDE_FAB,                // 隐藏悬浮按钮
        DISPLAY_FAB              // 显示悬浮按钮
    }
}
