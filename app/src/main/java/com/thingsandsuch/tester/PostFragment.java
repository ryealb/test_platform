package com.thingsandsuch.tester;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PostFragment extends Fragment {
    Boolean card_visible = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View post_view = inflater.inflate(R.layout.post_fragment, container, false);
        Bundle bundle = this.getArguments();

        Log.d("POST","create");

        LinearLayout toolbar_layout = (LinearLayout) (getActivity()).findViewById(R.id.toolbar_layout);
        toolbar_layout.setVisibility(View.INVISIBLE);


        if (bundle != null) {
            String title = bundle.getString("title");
            String author = bundle.getString("author");
            String score = bundle.getString("score");
            String preview_url = bundle.getString("preview_url");
            final String hd_url = bundle.getString("hd_url");


            TextView lbl_title = (TextView) post_view.findViewById(R.id.lbl_list_item_title2);
            String t_title = title;
            if (title.length() > 32){
                t_title = title.substring(0,32);
            }

            lbl_title.setText(t_title);

            TextView lbl_author = (TextView) post_view.findViewById(R.id.lbl_list_item_author2);
            lbl_author.setText(author.toUpperCase());

            TextView txt_score = (TextView) post_view.findViewById(R.id.txt_post_score);
            txt_score.setText(score);

            Button btn_set_wall  = (Button) post_view.findViewById(R.id.btn_set_wall);
            btn_set_wall.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                 new download_wallpaper().execute(hd_url);
                }
            });

            PhotoView photoView = (PhotoView) post_view.findViewById(R.id.photo_view);
            Glide.with(post_view.getContext())
                    .load(preview_url)
                    .into(photoView);


            photoView.setOnClickListener(new PhotoView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout lyt_card = (LinearLayout) post_view.findViewById(R.id.post_title_card);
                    if (card_visible){
                        lyt_card.setVisibility(View.INVISIBLE);
                    }else{
                        lyt_card.setVisibility(View.VISIBLE);
                    }

                    card_visible = lyt_card.getVisibility() == View.VISIBLE;

                }
            });
        }

        return post_view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }


    public File save_temp_bitmap(Bitmap bmp) {
//        String root=getApplicationContext().getDir("my_sub_dir", Context.MODE_PRIVATE).getAbsolutePath();
//        File myDir = new File(root + "/Img");
//        if(!myDir.exists()){
//            myDir.mkdirs();
//        }

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


    private class download_wallpaper extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            String image_url = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(image_url).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            // user desktop resolution
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            int width = displayMetrics.widthPixels;
//            int height = displayMetrics.heightPixels;

//            int ratio = width / height;
//            Log.d("","");
//            Log.d("WIDTH", Integer.toString(width)); // 1440
//            Log.d("HEIGHT", Integer.toString(height)); // 2392
//            Log.d("","");

            File temp_file = save_temp_bitmap(result);
            Uri sourceUri = Uri.fromFile(temp_file);
            Uri destinationUri = Uri.fromFile(new File(getContext().getCacheDir(), "IMG_" + System.currentTimeMillis()));

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(100);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setToolbarTitle("Crop Wallpaper");
            options.setToolbarColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimary));
            options.setLogoColor(ContextCompat.getColor(MainActivity.instance, R.color.colorAccent));
            options.setActiveWidgetColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimaryDark));
            options.setStatusBarColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimaryDark));
            options.setFreeStyleCropEnabled(true);
            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
//                    .withAspectRatio(7,8)
                    .start(MainActivity.instance);

        }
    }

}