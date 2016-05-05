package com.assistant.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.assistant.R;
import com.assistant.bean.Note;
import com.assistant.utils.TimeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/28
 * <p/>
 * 功能描述 :
 */
public class NotesAdapter extends BaseRecyclerViewAdapter<Note> implements Filterable{

    private final List<Note> originalList;

    public NotesAdapter(List<Note> list) {
        super(list);
        originalList = new ArrayList<>(list);
    }

    public NotesAdapter(List<Note> list, Context context){
        super(list, context);
        originalList = new ArrayList<>(list);
    }

    /**
     * 实现父类的方法，获得动画数组
     * @param view
     * @return
     */
    @Override
    protected Animator[] getAnimators(View view) {
        if (view.getMeasuredHeight() <=0){
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1.0f);
            return new ObjectAnimator[]{scaleX, scaleY};
        }
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1.0f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1.0f),
        };
    }

    @Override
    public void setList(List<Note> list) {
        super.setList(list);
        this.originalList.clear();
        originalList.addAll(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        // 这里也出过问题。
        View view = LayoutInflater.from(context).inflate(R.layout.notes_item_layout, parent, false);
        return new NotesItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        NotesItemViewHolder holder = (NotesItemViewHolder) viewHolder;

        Note note = list.get(position);

        if (note == null)
            return;
        holder.setLabelText(note.getTitle());
        holder.setContentText(note.getContent());
        holder.setTimeText(TimeUtils.getConciseTime(note.getLastOprTime(), mContext));
        // 为该位置的view绑定动画
        animate(holder, position);

    }

    @Override
    public Filter getFilter() {
        return new NoteFilter(this, originalList);
    }

    /**
     * 搜索过滤器
     */
    private static class NoteFilter extends Filter {

        private final NotesAdapter adapter;
        // 原始的数据
        private final List<Note> originalList;
        // 过滤后的数据
        private final List<Note> filteredList;

        private NoteFilter(NotesAdapter adapter, List<Note> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                for ( Note note : originalList) {
                    if (note.getContent().contains(constraint) || note.getTitle().contains(constraint)) {
                        filteredList.add(note);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.list.clear();
            adapter.list.addAll((ArrayList<Note>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
