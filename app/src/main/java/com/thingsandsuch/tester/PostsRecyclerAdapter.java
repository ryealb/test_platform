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
import android.content.Intent;

/**
 * Created by ryan on 5/20/2017.
 */



public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.PreviewHolder> {
    private ArrayList<Bitmap> post_previews;
    private ArrayList<List<String>> post_data;

    public static class PreviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView img_view_preview;
        private Bitmap p_preview;
        private List p_data;

//        private static final String PHOTO_KEY = "PHOTO";

        public PreviewHolder(View v) {
            super(v);
            img_view_preview = (ImageView) v.findViewById(R.id.img_view_preview);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {



//            Context context = itemView.getContext();
//            Intent showPhotoIntent = new Intent(context, PhotoActivity.class);
//            showPhotoIntent.putExtra(PHOTO_KEY, mPhoto);
//            context.startActivity(showPhotoIntent);
            try{
                String title = p_data.get(0).toString();
                String author = p_data.get(1).toString();
                String hd_url = p_data.get(2).toString();
                Bitmap bitmap = p_preview;

                Context context = img_view_preview.getContext();

                File temp_file = save_temp_bitmap(context, bitmap);
                Uri sourceUri = Uri.fromFile(temp_file);
//
                Intent selected_item_intent = new Intent(context, PostActivity.class);
//                Intent selected_item_intent = new Intent(context, PostActivity_two.class);
                selected_item_intent.putExtra("title", title);
                selected_item_intent.putExtra("author", author);
                selected_item_intent.putExtra("hd_url", hd_url);
                selected_item_intent.putExtra("preview_path", sourceUri.getPath());
                context.startActivity(selected_item_intent);

            }catch (NullPointerException e){
                Log.d("CLICK","BROKE"+e.toString());
                Log.d("CLICK", p_data.toString());

            }



        }

        public File save_temp_bitmap(Context ctx, Bitmap bmp) {
            String root=ctx.getDir("my_sub_dir", Context.MODE_PRIVATE).getAbsolutePath();
            File myDir = new File(root + "/Img");
            if(!myDir.exists()){
                myDir.mkdirs();
            }
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            Log.d("FILE",extStorageDirectory);
            OutputStream outStream = null;
            File file = new File(extStorageDirectory, "er.PNG");
            try {
                outStream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch(Exception e) {
                Log.e("WRITE","FAIL");
                e.printStackTrace();
            }
            return file;
        }




        public void bind_data(Bitmap in_preview, List<String> data) {
            p_preview = in_preview;
            p_data = data;
            try{
                img_view_preview.setImageBitmap(in_preview);
            }catch (Exception e) {
                Log.e("set_preview","FAILED");
            }

        }
    }

    public PostsRecyclerAdapter(ArrayList<Bitmap> previews, ArrayList<List<String>> data) {
        post_data = data;
        post_previews = previews;
    }

    @Override
    public PostsRecyclerAdapter.PreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.posts_recycler_item, parent, false);
        return new PreviewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(PostsRecyclerAdapter.PreviewHolder holder, int position) {

        Bitmap bmp_preview = post_previews.get(position);
        List<String> lst_data = post_data.get(position);
        holder.bind_data(bmp_preview, lst_data);
    }

    @Override
    public int getItemCount() {
        return post_previews.size();
    }
}
