package com.thingsandsuch.tester;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

/**
 * Created by ryan on 5/20/2017.
 */



public class PostsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<List<String>> post_data;

    protected boolean showLoader = true;
    protected boolean showHeader = true;

    private static final int VIEWTYPE_ITEM = 1;
    private static final int VIEWTYPE_LOADER = 2;
    private static final int VIEWTYPE_HEADER = 3;


    public static class PreviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView img_view_preview;
        private List p_data;


        public PreviewHolder(View v) {
            super(v);
            img_view_preview = (ImageView) v.findViewById(R.id.img_view_preview);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try{
                String title = p_data.get(0).toString();
                String author = p_data.get(1).toString();
                String hd_url = p_data.get(2).toString();
                String score = p_data.get(3).toString();
                String preview_url = p_data.get(4).toString();

                ((MainActivity) v.getContext()).run_post_fragment(title, author, hd_url, score, preview_url);

            }catch (NullPointerException e){
                Log.d("CLICK","BROKE"+e.toString());
                Log.d("CLICK", p_data.toString());

            }
        }


        public void bind_data(List<String> data) {
            p_data = data;
            try{
                Context context = itemView.getContext();
                String url = p_data.get(4).toString();
                Glide.with(context)
                        .load(url)
                        .into(img_view_preview);

            }catch (Exception e) {
                Log.e("set_preview","FAILED");
            }

        }
    }



    public static class LoaderHolder extends RecyclerView.ViewHolder {
        private ProgressBar prog_bar;

        public LoaderHolder(View v) {
            super(v);
            prog_bar = (ProgressBar) v.findViewById(R.id.prog_loader);
            prog_bar.setVisibility(View.VISIBLE);
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View v) {
            super(v);
        }
    }






    public PostsRecyclerAdapter(ArrayList<List<String>> data) {
        post_data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_LOADER) {
            Log.d("LOADER", "load");
            View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.loader_recycler_item, parent, false);
            return new LoaderHolder(inflatedView);
        }

        if (viewType == VIEWTYPE_HEADER) {
            Log.d("HEADER", "show");
            View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_recycler_item, parent, false);
            return new HeaderHolder(inflatedView);
        }


        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_recycler_item, parent, false);
        return new PreviewHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoaderHolder) {
            LoaderHolder loaderViewHolder = (LoaderHolder)holder;
            if (showLoader) {
                Log.d("LOADER", "show");
//                loaderViewHolder.prog_bar.setVisibility(View.VISIBLE);
            } else {
                Log.d("LOADER", "show");
//                loaderViewHolder.prog_bar.setVisibility(View.GONE);
            }
            return;
        }

        if (holder instanceof HeaderHolder) {
            HeaderHolder headerViewHolder = (HeaderHolder)holder;
            if (showHeader) {
                Log.d("LOADER", "show");
//                loaderViewHolder.prog_bar.setVisibility(View.VISIBLE);
            } else {
                Log.d("LOADER", "show");
//                loaderViewHolder.prog_bar.setVisibility(View.GONE);
            }
            return;
        }


        try {
            PreviewHolder preview_holder = (PreviewHolder)holder;
            List<String> lst_data = post_data.get(position);
            preview_holder.bind_data(lst_data);

        }catch (Exception e){
            Log.e("PREVIEw0", "TURD");
        }



    }

    @Override
    public int getItemCount() {
        return post_data.size()+1;
    }

    @Override
    public int getItemViewType(int position) {

        // loader can't be at position 0
        // loader can only be at the last position
        if (position != 0 && position == getItemCount() - 1) {
            return VIEWTYPE_LOADER;
        }

        if (position == 0) {
            return VIEWTYPE_HEADER;
        }


        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    public void showHeader(boolean status) {
        showHeader = status;
    }



}
