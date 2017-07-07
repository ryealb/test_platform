package com.thingsandsuch.tester;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PostActivity extends Fragment {
    Bitmap bitmap;
    Boolean card_visible = true;
    SwipeRefreshLayout lyt_swipe;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        return inflater.inflate(R.layout.post_activity, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("UHH","");
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);


//        setContentView(R.layout.post_activity);
//        Intent intent = getIntent();



//        LinearLayout lyt_card = (LinearLayout) findViewById(R.id.post_title_card);



//        setDragEdge(SwipeBackLayout.DragEdge.TOP);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_selected);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        // enabling action bar app icon and behaving it as toggle button
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);


//        String title = intent.getStringExtra("title");
//        String author = intent.getStringExtra("author");
//        String score = intent.getStringExtra("score");
//        final String hd_url = intent.getStringExtra("hd_url");
//
//        String preview_path = intent.getStringExtra("preview_path");
//        Log.d("PATH", preview_path);
//        File image_file = new  File(preview_path);
//
//        if(image_file.exists()){
//            Log.d("FILE", "EXISTS");
//            bitmap = BitmapFactory.decodeFile(image_file.getAbsolutePath());
//        }
//
//        TextView lbl_title = (TextView) findViewById(R.id.lbl_list_item_title2);
//        lbl_title.setText(title);
//
//        TextView lbl_author = (TextView) findViewById(R.id.lbl_list_item_author2);
//        lbl_author.setText(author.toUpperCase());
//
//        TextView txt_score = (TextView)findViewById(R.id.txt_post_score);
//        txt_score.setText(score);

//        Log.d("POST_SCORE", score);

//        Button btn_set_wall  = (Button) findViewById(R.id.btn_set_wall);
//        btn_set_wall.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//             new download_wallpaper().execute(hd_url);
//            }
//        });



//        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
//        photoView.setOnClickListener(new PhotoView.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                LinearLayout lyt_card = (LinearLayout) findViewById(R.id.post_title_card);
//                if (card_visible){
//                    lyt_card.setVisibility(View.INVISIBLE);
//                }else{
//                    lyt_card.setVisibility(View.VISIBLE);
//                }
//
//                card_visible = lyt_card.getVisibility() == View.VISIBLE;
//
//            }
//        });

        // swipe dismiss setup
//        lyt_swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_selected);
//        lyt_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                Log.d("PULL", "DO IT");
////                refresh_current_sub_posts_action();
//            }
//        });

//
//         runOnUiThread(new Runnable() {
//             @Override
//             public void run() {
//                 PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
//                 photoView.setImageBitmap(bitmap);
//                 Log.d("BITMAP", bitmap.toString());
//             }
//         });
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
            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            int ratio = width / height;
            Log.d("","");
            Log.d("WIDTH", Integer.toString(width)); // 1440
            Log.d("HEIGHT", Integer.toString(height)); // 2392
            Log.d("","");

            File temp_file = save_temp_bitmap(result);
            Uri sourceUri = Uri.fromFile(temp_file);
//            Uri destinationUri = Uri.fromFile(new File(getApplicationContext().getCacheDir(), "IMG_" + System.currentTimeMillis()));

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(100);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setToolbarTitle("Crop Wallpaper");
            options.setToolbarColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimary));
            options.setLogoColor(ContextCompat.getColor(MainActivity.instance, R.color.colorAccent));
            options.setActiveWidgetColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimaryDark));
            options.setStatusBarColor(ContextCompat.getColor(MainActivity.instance, R.color.colorPrimaryDark));
            options.setFreeStyleCropEnabled(true);
//            UCrop.of(sourceUri, destinationUri)
//                    .withOptions(options)
//                    .withAspectRatio(7,8)
//                    .start(MainActivity.instance);



        }
    }


}