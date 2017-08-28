package com.thingsandsuch.tester;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class PostFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View post_view = inflater.inflate(R.layout.post_fragment, container, false);
        Bundle bundle = this.getArguments();

        CollapsingToolbarLayout toolbar_layout = (CollapsingToolbarLayout) (getActivity()).findViewById(R.id.main_toolbar_layout);
        toolbar_layout.setVisibility(View.GONE);

        if (bundle != null) {
            String title = bundle.getString("title", "");
            String author = bundle.getString("author");
            String score = bundle.getString("score");
            String preview_url = bundle.getString("preview_url");
            final String hd_url = bundle.getString("hd_url");

            // TITLE
            TextView txt_title = (TextView) post_view.findViewById(R.id.post_info_title);
            TextView txt_title_2 = (TextView) post_view.findViewById(R.id.post_info_title_2);

            Boolean in_top_line = true;
            if (title.length() > 24){
                String[] split_title = title.split("\\s+");
                String title_1 = "";
                String title_2 = "";

                for (String aSplit_title : split_title) {
                    if (in_top_line && title_1.length() < 24 && title_1.length() + aSplit_title.length() + 1 <= 24) {
                        title_1 += aSplit_title + " ";
                    } else {
                        title_2 += aSplit_title + " ";
                        in_top_line = false;
                    }
                }
                txt_title.setText(title_1);
                if (!Objects.equals(title_2, "")) {
                    Log.d("TITLE", "show" + title_2);
                    txt_title_2.setVisibility(View.VISIBLE);
                    txt_title_2.setText(title_2);
                } else {
                    txt_title_2.setVisibility(View.GONE);
                }


            } else {
                txt_title.setText(title);
                txt_title_2.setVisibility(View.INVISIBLE);
            }

            // AUTHOR
            TextView lbl_author = (TextView) post_view.findViewById(R.id.post_info_author);
            String disp_author = "- " + author.toUpperCase();
            lbl_author.setText(disp_author);

            // VOTES
            TextView txt_score = (TextView) post_view.findViewById(R.id.post_info_upvote);
            txt_score.setText(score);

            // SET WALL BUTTON
            FloatingActionButton btn_set_wall  = (FloatingActionButton) post_view.findViewById(R.id.btn_set_wall);
            btn_set_wall.setOnClickListener(new FloatingActionButton.OnClickListener() {
                public void onClick(View v) {
                 new download_wallpaper().execute(hd_url);
                }
            });

            // POST IMAGE
            PhotoView photoView = (PhotoView) post_view.findViewById(R.id.photo_view);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(post_view.getContext())
                    .load(preview_url)
                    .into(photoView);



//            final GestureDetector gesture = new GestureDetector(getActivity(),
//                    new GestureDetector.SimpleOnGestureListener() {
//
//                        @Override
//                        public boolean onDown(MotionEvent e) {
//                            return true;
//                        }
//
//                        @Override
//                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                                               float velocityY) {
//                            Log.d("SWIPE_GESTURE", "onFling has been called!");
//                            final int SWIPE_MIN_DISTANCE = 120;
//                            final int SWIPE_MAX_OFF_PATH = 250;
//                            final int SWIPE_THRESHOLD_VELOCITY = 200;
//                            try {
//                                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
//                                    return false;
//                                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
//                                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                                    Log.d("SWIPE_GESTURE", "Right to Left");
//                                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
//                                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                                    Log.d("SWIPE_GESTURE", "Left to Right");
//
//                                    getActivity().getFragmentManager().popBackStack();
//
//                                }
//                            } catch (Exception e) {
//                                // nothing
//                            }
//                            return super.onFling(e1, e2, velocityX, velocityY);
//                        }
//                    });
//
//            photoView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    return gesture.onTouchEvent(event);
//                }
//            });

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