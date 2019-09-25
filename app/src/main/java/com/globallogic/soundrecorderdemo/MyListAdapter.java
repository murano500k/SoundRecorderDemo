package com.globallogic.soundrecorderdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private final Context context;
    List<File> mItems;
    private int lastCheckedPosition = 0;
    private OnSelectedItemChangedListener onSelectedItemChangedListener;

    public MyListAdapter(List<File> items, Context context) {
        this.mItems = items;
        this.context = context;

    }

    public void setItems(List<File> mItems){
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item, viewGroup, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdapter.ViewHolder viewHolder, final int position) {
        File item = mItems.get(position);
        viewHolder.textView.setText(item.getName());

        if(lastCheckedPosition==position){
            viewHolder.textView.setChecked(true);
        }else {
            viewHolder.textView.setChecked(false);
        }

        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCheckedPosition=position;
                if(onSelectedItemChangedListener!=null){
                    onSelectedItemChangedListener.onSelectedItemChanged(mItems.get(position));
                }
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
    public void setOnSelectedItemChangedListener(OnSelectedItemChangedListener onSelectedItemChangedListener) {
        this.onSelectedItemChangedListener = onSelectedItemChangedListener;
    }

    public int setSelectedItem(String recordedFile) {
        for (int i = 0; i < mItems.size(); i++) {
            if(mItems.get(i).getAbsolutePath().contains(recordedFile)){
                lastCheckedPosition=i;
                notifyDataSetChanged();
                return i;
            }
        }
        return lastCheckedPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MyCheckedTextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (MyCheckedTextView) itemView.findViewById(R.id.textView);
        }
    }
    public File getSelectedItem(){
        return mItems.get(lastCheckedPosition);
    }


    public interface OnSelectedItemChangedListener {
        void onSelectedItemChanged(File file);
    }
}
