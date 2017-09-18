package com.thingsandsuch.tester;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 5/20/2017.
 */



public class CommentsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected boolean showLoader = true;
    private ArrayList<List<String>> comment_data;
    private static final int VIEWTYPE_ITEM = 1;
    private static final int VIEWTYPE_LOADER = 2;


    public static class CommentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txt_comment;
        private TextView txt_author;
        private List c_data;


        public CommentHolder(View v) {
            super(v);
            txt_comment = (TextView) v.findViewById(R.id.txt_comment);
            txt_author = (TextView) v.findViewById(R.id.txt_author);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }

        public void bind_data(List<String> data) {
            c_data = data;
            try{
                String comment = c_data.get(0).toString();

                String author = "  - ";
                author += c_data.get(1).toString();

                txt_comment.setText(comment);
                txt_author.setText(author);

            }catch (Exception e) {
                Log.d("COMMENT","setText");
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


    public CommentsRecyclerAdapter(ArrayList<List<String>> data) {
        comment_data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_LOADER) {
            View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.loader_recycler_item, parent, false);
            return new LoaderHolder(inflatedView);
        }
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_recycler_item, parent, false);
        return new CommentHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoaderHolder) {
            LoaderHolder loaderViewHolder = (LoaderHolder)holder;
            if (showLoader) {
                Log.d("LOADER", "show");
                //  loaderViewHolder.prog_bar.setVisibility(View.VISIBLE);
            } else {
                Log.d("LOADER", "show");
                // loaderViewHolder.prog_bar.setVisibility(View.GONE);
            }
        }

        if (holder instanceof  CommentHolder) {
            CommentHolder comment_holder = (CommentHolder)holder;
            List<String> lst_data = comment_data.get(position);
            comment_holder.bind_data(lst_data);
        }
    }

    @Override
    public int getItemCount() {
        return comment_data.size();
    }

    @Override
    public int getItemViewType(int position) {
        // loader can't be at position 0
        // loader can only be at the last position
        if (position != 0 && position == getItemCount() - 1) {
            return VIEWTYPE_LOADER;
        }

        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

}
