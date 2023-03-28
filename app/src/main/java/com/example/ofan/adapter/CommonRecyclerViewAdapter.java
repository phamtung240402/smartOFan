package com.example.ofan.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<CommonRecyclerViewAdapter.MyViewholder> {
    protected Context mContext;
    private List<T> mDataList;
    private LayoutInflater mInflater;
    private SparseArray<int[]> mResLayoutAndViewIds;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public CommonRecyclerViewAdapter(Context mContext, List<T> mDataList, SparseArray<int[]> mResLayoutAndViewIds) {
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDataList = mDataList;
        this.mResLayoutAndViewIds = mResLayoutAndViewIds;

        checkResLayoutAndViewIds(mResLayoutAndViewIds);
    }


    @NonNull
    @Override
    public MyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(viewType,parent,false);
        return new MyViewholder(convertView,mResLayoutAndViewIds.get(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewholder holder, final int position) {
        MyViewholder mHolder = (MyViewholder) holder;
        if (mClickListener != null) {
            mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(view,position);
                }
            });
        }
        if (mLongClickListener != null) {
            mHolder.itemView.setLongClickable(true);
            mHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mLongClickListener.onItemLongClick(view, position);
                    return true;
                }
            });
        }
        bindDataToItem(mHolder, mDataList.get(position), position);
    }

    protected abstract void bindDataToItem(MyViewholder mHolder, T t, int position);

    public abstract int getItemResLayoutType(int position);


    @Override
    public int getItemViewType(int position) {
        int type = getItemResLayoutType(position);
        if(mResLayoutAndViewIds.indexOfKey(type) < 0) {
            throw new IllegalStateException("the ResLayoutAndView doesnt containt " + type + " item layout type");
        }
        return type;
    }

    private void checkResLayoutAndViewIds(@NonNull SparseArray<int[]> resLayoutAndViewIds) {
        for(int i = 0; i< resLayoutAndViewIds.size(); i++) {
            int reslayout = resLayoutAndViewIds.keyAt(i);
            int[] viewIds = resLayoutAndViewIds.get(reslayout);
            View itemView = mInflater.inflate(reslayout,null);
            for(int viewId : viewIds) {
                View view = itemView.findViewById(viewId);
                if(view == null) {
                    throw new IllegalStateException("Some viewIds dont be found in corresponding resLayout ");
                }
            }
        }
    }
    public static class MyViewholder extends RecyclerView.ViewHolder{
        public SparseArray<View> mViews;

        public MyViewholder(@NonNull View itemView, @NonNull int[] resViewID) {
            super(itemView);
            mViews = new SparseArray<>();
            for(int viewId: resViewID)
            {
                View view = itemView.findViewById(viewId);
                mViews.put(viewId,view);
            }
        }
    }

    public void setOnItemClickListener(@NonNull OnItemClickListener onItemClickListener) {
        this.mClickListener =  onItemClickListener;
    }

    public void setOnItemLongClickListener(@NonNull OnItemLongClickListener onItemLongClickListener) {
        this.mLongClickListener =  onItemLongClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
