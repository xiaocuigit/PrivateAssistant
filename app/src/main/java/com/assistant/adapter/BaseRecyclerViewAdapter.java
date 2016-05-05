package com.assistant.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.assistant.utils.ViewHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/28
 * <p>
 * 功能描述 :
 */
public abstract class BaseRecyclerViewAdapter<E>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context mContext;

    private int mDuration = 300;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mLastPosition = -1;
    private boolean isFirstOnly = true;

    protected List<E> list;

    private Map<Integer, onInternalClickListener<E>> canClickItem;

    public BaseRecyclerViewAdapter(List<E> list) {
        this(list, null);
    }

    public BaseRecyclerViewAdapter(List<E> list, Context context) {
        this.list = list;
        this.mContext = context;
    }

    public interface onInternalClickListener<T> {
        void OnClickListener(View parentV, View v, Integer position,
                             T values);

        void OnLongClickListener(View parentV, View v, Integer position,
                                 T values);
    }

    public void add(E e) {
        // 总是将新的元素添加到第一个位置
        this.list.add(0, e);
        notifyDataSetChanged();
    }

    public void remove(E e) {
        int position = list.indexOf(e);
        remove(position);
    }

    public void remove(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }

    public void setList(List<E> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    public List<E> getList() {
        return list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * 由子类去覆写
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null) {
            addInternalClickListener(holder.itemView, position, list.get(position));
        }
    }

    private void addInternalClickListener(final View itemView, final int position, final E values) {
        if (canClickItem != null) {
            for (Integer key : canClickItem.keySet()) {
                // key 在这里表示的是控件的ID值，通过该ID 来绑定控件。
                final View inView = itemView.findViewById(key);
                // 要加 <E> ?????
                final onInternalClickListener<E> listener = canClickItem.get(key);
                if (inView != null && listener != null) {
                    inView.setOnClickListener((view) ->
                                    listener.OnClickListener(itemView, view, position, values)
                    );
                    inView.setOnLongClickListener((view) -> {
                        listener.OnLongClickListener(itemView, view, position, values);
                        return true;
                    });
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }


    /**
     * 给笔记添加监听器，根据ID 的不同添加不同的监听器
     *
     * @param key      表示布局文件的ID
     * @param listener
     */
    public void setOnInViewClickListener(Integer key, onInternalClickListener<E> listener) {
        if (canClickItem == null) {
            canClickItem = new HashMap<>();
        }
        canClickItem.put(key, listener);
    }

    public static class onInternalClickListenerImpl<T> implements onInternalClickListener<T> {
        @Override
        public void OnClickListener(View parentV, View v, Integer position, T values) {

        }

        @Override
        public void OnLongClickListener(View parentV, View v, Integer position, T values) {

        }
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * 设置插补器
     *
     * @param interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setStartPosition(int start) {
        mLastPosition = start;
    }

    /**
     * 作用？？？？？
     *
     * @param firstOnly
     */
    public void setFirstOnly(boolean firstOnly) {
        isFirstOnly = firstOnly;
    }

    protected void animate(RecyclerView.ViewHolder holder, int position) {
        if (!isFirstOnly || position > mLastPosition) {
            for (Animator anim : getAnimators(holder.itemView)) {
                anim.setDuration(mDuration).start();
                anim.setInterpolator(mInterpolator);
            }
            mLastPosition = position;
        } else {
            ViewHelper.clear(holder.itemView);
        }
    }

    protected abstract Animator[] getAnimators(View view);

}
