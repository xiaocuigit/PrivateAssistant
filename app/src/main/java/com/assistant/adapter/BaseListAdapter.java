package com.assistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/28
 * <p/>
 * 功能描述 :
 */
public abstract class BaseListAdapter<E> extends BaseAdapter {
    public List<E> list;
    public Context mContext;
    public LayoutInflater mInflater;
    public Map<Integer, onInternalClickListener<E>> canClickItem;

    public List<E> getList() {
        return list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }

    public void add(E e) {
        this.list.add(e);
        notifyDataSetChanged();
    }

    public void addAll(List<E> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }

    public BaseListAdapter(List<E> list, Context context){
        super();
        this.list = list;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = bindView(position, convertView, parent);
        addInternalClickListener(convertView, position, list.get(position));
        return convertView;
    }

    protected abstract View bindView(int position, View convertView, ViewGroup parent);
    /**
     * 为item为position 的view添加一个监听器
     *
     * @param itemV
     * @param position
     * @param valuesMap
     */
    private void addInternalClickListener(final View itemV, final Integer position, final E valuesMap) {
        if (canClickItem != null) {
            // 遍历所的Integer值
            for (Integer key : canClickItem.keySet()) {
                // 绑定值为key的view
                View inView = itemV.findViewById(key);
                // 获取该view 的监听对象
                final onInternalClickListener<E> listener = canClickItem.get(key);
                if (inView != null && listener != null) {
                    // 为该view设置监听事件
                    inView.setOnClickListener((view) ->
                                    listener.OnClickListener(itemV, view, position,
                                            valuesMap)
                    );
                    // 设置长按不放监听
                    inView.setOnLongClickListener((view) -> {
                        listener.OnLongClickListener(itemV, view, position,
                                valuesMap);
                        return true;
                    });
                }
            }
        }
    }

    public void setOnInViewClickListener(Integer key,  onInternalClickListener<E> onClickListener) {
        if (canClickItem == null)
            canClickItem = new HashMap<>();
        canClickItem.put(key, onClickListener);
    }

    public interface onInternalClickListener<T> {
        void OnClickListener(View parentV, View v, Integer position,
                             T values);

        void OnLongClickListener(View parentV, View v, Integer position,
                                 T values);
    }

    public static class onInternalClickListenerImpl<T> implements onInternalClickListener<T> {
        @Override
        public void OnClickListener(View parentV, View v, Integer position, T values) {

        }

        @Override
        public void OnLongClickListener(View parentV, View v, Integer position, T values) {

        }
    }
}
