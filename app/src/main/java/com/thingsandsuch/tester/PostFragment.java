package com.thingsandsuch.tester;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostFragment extends Fragment {

    String sort_by = "hot";
    String last_post_id;

    private RecyclerView rec_view_comments;
    private CommentsRecyclerAdapter rec_adapter_comments;
    ArrayList<List<String>> comments_post_data = new ArrayList<List<String>>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View post_view = inflater.inflate(R.layout.post_fragment, container, false);
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String title = bundle.getString("title", "");
            String author = bundle.getString("author");
            String upvote = bundle.getString("upvote");
            String preview_url = bundle.getString("preview_url");

            String sub_name = bundle.getString("sub_name");
            String post_id = bundle.getString("post_id");

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
            TextView txt_upvote = (TextView) post_view.findViewById(R.id.post_info_upvote);
            txt_upvote.setText(upvote);

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


            // COMMENTS
            rec_view_comments = (RecyclerView) post_view.findViewById(R.id.post_info_comments_view);
            LinearLayoutManager lin_lyt_manager = new LinearLayoutManager(post_view.getContext());
            rec_view_comments.setLayoutManager(lin_lyt_manager);

            rec_adapter_comments = new CommentsRecyclerAdapter(comments_post_data);
            rec_view_comments.setAdapter(rec_adapter_comments);

            get_comments_from_post(sub_name, post_id, false);


//            List<String> test_data = new ArrayList<String>();
//            List<String> test_data1 = new ArrayList<String>();
//            List<String> test_data2 = new ArrayList<String>();
//            List<String> test_data3 = new ArrayList<String>();
//            List<String> test_data4 = new ArrayList<String>();
//            List<String> test_data5 = new ArrayList<String>();
//            List<String> test_data6 = new ArrayList<String>();
//            List<String> test_data7 = new ArrayList<String>();
//            List<String> test_data8 = new ArrayList<String>();
//            test_data.add("Comment one");
//            test_data1.add("Comment two");
//            test_data2.add("Comment five");
//            test_data3.add("three, sir");
//            test_data4.add("What?");
//            test_data5.add("Three");
//            test_data6.add("Ah, Right");
//            test_data7.add("Three");
//            test_data8.add("Four");
//
//            comments_post_data.add(test_data);
//            comments_post_data.add(test_data1);
//            comments_post_data.add(test_data2);
//            comments_post_data.add(test_data3);
//            comments_post_data.add(test_data4);
//            comments_post_data.add(test_data5);
//            comments_post_data.add(test_data6);
//            comments_post_data.add(test_data7);
//            comments_post_data.add(test_data8);
//            rec_adapter_comments.notifyDataSetChanged();





//            setup_rec_list_listeners(post_info_comments_view);



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
        AppBarLayout app_bar_layout = (AppBarLayout) (getActivity().findViewById(R.id.main_app_bar));

        if (app_bar_layout != null) {
            app_bar_layout.setExpanded(false, true);
        }

    }


    public List<String> title_split(String full_title) {
        List<String> titles = new ArrayList<String>();

        Boolean in_top_line = true;
        if (full_title.length() > 24){
            String[] split_title = full_title.split("\\s+");
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

            titles.add(title_1);
            titles.add(title_2);
        } else {
            titles.add(full_title);
        }




        return titles;
    }

    public File save_temp_bitmap(Bitmap bmp) {
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


    // COMMENTS
    public void get_comments_from_post(final String sub_name, final String post_id, final Boolean add_to_list){
        String get_url = "https://www.reddit.com/r/" + sub_name + "/comments/" + post_id + "/" + sort_by + ".json?limit=100&raw_json=1";

        if (add_to_list){
            get_url += "&after=" + last_post_id;
            Log.d("URL",get_url);
        }
        Request request = new Request.Builder()
                .url(get_url)
                .build();

        // put internet request in android thread queue
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("COMMENT", "get post comments");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONArray data = null;
                JSONArray comments_json_obj = null;
                try {
                    data = new JSONArray(json);
//                    Log.d("COMMENT", data.toString());
//
                } catch (JSONException e) {
                    Log.e("COMMENT", e.toString());
                }



                try{
                    comments_json_obj = data.getJSONObject(1).getJSONObject("data").getJSONArray("children");
//                    Log.d("COMMENT_comm", comments_json_obj.toString());
                }catch (Exception e) {
                    Log.e("COMMENT", e.toString());
                }


//                for (int i = 0; i < comments_json_obj.length(); i++)
//                {
//                    try{
//
//                        Log.d("COMMENT", comments_json_obj.getJSONObject(i).getJSONObject("data").toString());
//                    } catch (Exception e) {
//                        Log.e("COMMENT", e.toString());
//
//                    }
//
//                }



//                try{
//                    last_post_id = posts_json_obj.getJSONObject(posts_json_obj.length()-1).getJSONObject("data").getString("name");
//                }catch (Exception e){
//                    Log.e("COMMENT_last_id", e.toString());
//                }

                populate_comments_list(comments_json_obj, add_to_list);

            }
        });
    }


    public void populate_comments_list(JSONArray comms_obj, Boolean add_to_list){

//        if (!add_to_list){
//            rec_view_posts.smoothScrollToPosition(0);
//            list_post_data.clear();
//        }

//        Log.d("PREVIEW_rec_view width", Integer.toString(rec_view_comments.getWidth()));

        Integer posts_count = comms_obj.length();
//        Log.d("COMMENTS", "populate"+Integer.toString(posts_count));


        Integer num = 0;
        if (add_to_list){
            num = comments_post_data.size();
        }
        for (int i = 0; i < comms_obj.length(); i++)
        {
            try {
                String author = comms_obj.getJSONObject(i).getJSONObject("data").getString("author");
                String comment = comms_obj.getJSONObject(i).getJSONObject("data").getString("body");

                List<String> data_list = new ArrayList<>();
                data_list.add(comment);
                data_list.add(author.toUpperCase());

                comments_post_data.add(data_list);

                num += 1;


            }catch (JSONException e) {
                Log.e("COMMENT", "populate_comments_list"+ e.toString());
            }
        }

        new PostFragment.update_recycler_view(num).execute("");





    }

    private class update_recycler_view extends AsyncTask<String, Void, String> {

        public update_recycler_view(Integer i) {
        }

        protected String doInBackground(String... ss) {
            return "";
        }

        protected void onPostExecute(String result) {
            rec_adapter_comments.notifyDataSetChanged();
        }
    }





}