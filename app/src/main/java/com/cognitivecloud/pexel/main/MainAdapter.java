package com.cognitivecloud.pexel.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;

import com.cognitivecloud.pexel.R;
import com.cognitivecloud.pexel.model.ImageListResponse;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ImageListResponse.Images> data;

    private onPressListener listener;

    // flag for footer ProgressBar (i.e. last item of list)
    private boolean isLoadingAdded = false;
    static final int ITEM = 0;
    static final int LOADING = 1;

    public interface onPressListener{
        void onClick(String images);
    }

    MainAdapter(onPressListener listener) {
        data = new ArrayList<>();
        this.listener =listener;
    }

    public List<ImageListResponse.Images> getImages() {
        return data;
    }

    public void setImages(List<ImageListResponse.Images> imagesList) {
        this.data = imagesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (i) {
            case ITEM:
                viewHolder = new ViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_data, viewGroup, false));
                break;

            case LOADING:
                viewHolder = new Loading(DataBindingUtil.inflate(inflater, R.layout.item_progress, viewGroup, false).getRoot());
                break;
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int i) {

        switch (getItemViewType(i)) {
            case ITEM:
                MainAdapter.ViewHolder viewHolder = (MainAdapter.ViewHolder) holder;
                viewHolder.bind(data.get(i),listener);
                break;
            case LOADING:
//                Do nothing
                break;
        }
    }


    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    @Override
    public int getItemViewType(int position) {
        return (position == data.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }


    private class ViewHolder extends RecyclerView.ViewHolder{
        private ViewDataBinding binding;

        private ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }


        private void bind(Object obj,onPressListener listener) {
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onClick(((ImageListResponse.Images)obj).getImageObject().getImage());
                }
            });

            binding.setVariable(BR.obj, obj);
            binding.executePendingBindings();
        }
    }




    protected class Loading extends RecyclerView.ViewHolder {

        Loading(View itemView) {
            super(itemView);
        }
    }

    private void add(ImageListResponse.Images image) {
        data.add(image);
        notifyItemInserted(data.size() - 1);
    }



    void addAll(ImageListResponse mcList) {
        for (ImageListResponse.Images response: mcList.getImages()) {
            add(response);
        }
    }

        private void remove(ImageListResponse.Images images) {
        int position = data.indexOf(images);
        if (position > -1) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }
//
//    public boolean isEmpty() {
//        return getItemCount() == 0;
//    }

    void addLoadingFooter() {
        isLoadingAdded = true;
        add(new ImageListResponse.Images());
    }

    void removeLoadingFooter() {
        if(!data.isEmpty()) {
            isLoadingAdded = false;

            int position = data.size() - 1;
            ImageListResponse.Images item = getItem(position);
            if (item != null) {
                data.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    private ImageListResponse.Images getItem(int position) {
        return data.get(position);
    }
}
