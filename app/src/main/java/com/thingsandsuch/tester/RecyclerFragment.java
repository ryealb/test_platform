package com.thingsandsuch.tester;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.app.Fragment;

import android.os.Bundle;
import android.os.AsyncTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.support.v7.widget.RecyclerView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.OkHttpClient;



public class RecyclerFragment extends Fragment  implements FragmentCommunicator{

    String sort_by = "hot";
    String sub_title = "All";
    String last_post_id;

    private RecyclerView rec_view_posts;
    private SwipeRefreshLayout lyt_refresh_swipe;
    private PostsRecyclerAdapter rec_adapter_posts;

    ArrayList<Bitmap> post_previews = new ArrayList<Bitmap>();
    ArrayList<List<String>> list_post_data = new ArrayList<List<String>>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rec_view = inflater.inflate(R.layout.recycler_fragment, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();


        // list setup
        rec_view_posts = (RecyclerView) rec_view.findViewById(R.id.rec_view_posts );
        LinearLayoutManager lin_lyt_manager = new LinearLayoutManager(rec_view.getContext());
        rec_view_posts.setLayoutManager(lin_lyt_manager);


        // list adapter setup
        rec_adapter_posts = new PostsRecyclerAdapter(post_previews, list_post_data);
        rec_view_posts.setAdapter(rec_adapter_posts);
        setup_rec_list_listeners(rec_view_posts);


        // swipe refresh setup
        lyt_refresh_swipe = (SwipeRefreshLayout) rec_view.findViewById(R.id.swipe_refresh_layout_posts);
        lyt_refresh_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_current_sub_posts_action();
                }
            });

        return rec_view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }




    // LISTENERS
    private void setup_rec_list_listeners(final RecyclerView rec_view_posts){

        ItemTouchHelper.SimpleCallback swipe_callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
//                list_post_data.remove(position);
//                post_previews.remove(position);
//                rec_adapter_posts.notifyItemRemoved(position); //TODO: this might cause issues
            }
        };
        ItemTouchHelper swipe_helper = new ItemTouchHelper(swipe_callback);

        swipe_helper.attachToRecyclerView(rec_view_posts);


        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rec_view_posts.getLayoutManager();
        rec_view_posts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int ydy = 0;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int offset = dy - ydy;
                ydy = dy;

                Integer child_count = list_post_data.size();

                boolean at_top = (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);

                if (at_top) {
                    Log.d("SCROLL","show header");
                    //hide show main header.
                    return;
                }

                boolean at_bottom = linearLayoutManager.findLastCompletelyVisibleItemPosition() == child_count - 1;

                if (at_bottom) {
                    Log.d("SCROLL","load more");
                    rec_adapter_posts.showLoading(true);

                    load_more_posts_action();
                }
            }
        });


    }



    // POSTS
    public void get_posts_from_sub(final String sub_name, final Boolean add_to_list){

        String get_url = "https://www.reddit.com/r/" + sub_name + "/" + sort_by + ".json?limit=20&raw_json=1";

        if (add_to_list){
            get_url += "&after=" + last_post_id;
            Log.d("URL",get_url);
        }


        // basic sub data
        Request request = new Request.Builder()
                .url(get_url)
                .build();

        // put internet request in android thread queue
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAIL", "request fail");
            }



            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;
                JSONArray posts_json_obj = null;
                try {
                    data = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e("POST_get_data", "object");
                }

                try{
                    posts_json_obj = data.getJSONObject("data").getJSONArray("children");
                }catch (Exception e) {
                    Log.e("POST", "children22");
                }

                try{
                    last_post_id = posts_json_obj.getJSONObject(posts_json_obj.length()-1).getJSONObject("data").getString("name");
                }catch (Exception e){
                    Log.e("POST_ID", e.toString());
                }

                populate_posts_list(posts_json_obj, add_to_list);

            }
        });
    }

    public void get_posts_from_sub_action(final String sub_name){
        get_posts_from_sub(sub_name, false);
    }

    public void load_more_posts_action(){
        Log.d("LOAD", "load more");
        get_posts_from_sub(sub_title, true);
        rec_adapter_posts.showLoading(false);
    }

    public void populate_posts_list(JSONArray subs_obj, Boolean add_to_list){

        if (!add_to_list){
            rec_view_posts.smoothScrollToPosition(0);
            list_post_data.clear();
            post_previews.clear();

        }


        Integer posts_count = subs_obj.length();
        Log.d("POPULATE_SUBS_obj_cnt", Integer.toString(posts_count));



        Integer num = 0;
        if (add_to_list){
            num = post_previews.size();
        }
        for (int i = 0; i < subs_obj.length(); i++)
        {
            try {
                JSONObject preview = subs_obj.getJSONObject(i).getJSONObject("data").getJSONObject("preview");
                String author = subs_obj.getJSONObject(i).getJSONObject("data").getString("author");
                String title = subs_obj.getJSONObject(i).getJSONObject("data").getString("title");

                String score = subs_obj.getJSONObject(i).getJSONObject("data").getString("score");
                String up_votes = subs_obj.getJSONObject(i).getJSONObject("data").getString("ups");
                String down_votes = subs_obj.getJSONObject(i).getJSONObject("data").getString("downs");



                JSONObject images = preview.getJSONArray("images").getJSONObject(0);
                String source_url = images.getJSONObject("source").getString("url");
                JSONArray resolutions = images.getJSONArray("resolutions");

                post_previews.add(null);



                try{
                    source_url = resolutions.getJSONObject(3).getString("url");
                }catch (Exception e)
                {
                    Log.e("NO IMAGE", "soooory");
                }



                String hd_url;
                try{
//                    Integer res_count = resolutions.length();
                    hd_url = resolutions.getJSONObject(5).getString("url");

                }catch (Exception e){
                    hd_url = source_url;
                    Log.e("FAIL HD RES", "i dunno");
                }

                List<String> data_list = new ArrayList<>();
                data_list.add(title);
                data_list.add(author);
                data_list.add(hd_url);
                data_list.add(score);

                list_post_data.add(data_list);

                new RecyclerFragment.download_thumbnail(num).execute(source_url);

                num += 1;


            }catch (JSONException e) {
                Log.e("POSITION_PUT", Integer.toString(num));
                Log.e("POSITION_PUT", e.toString());
                Log.d("FAIL", subs_obj.toString());
            }
        }


    }

    public void refresh_current_sub_posts_action(){
        get_posts_from_sub_action(sub_title);
        lyt_refresh_swipe.setRefreshing(false);
    }



    // DOWNLOAD
    private class download_thumbnail extends AsyncTask<String, Void, Bitmap> {
        Integer preview_index;

        public download_thumbnail(Integer preview_index) {
            this.preview_index = preview_index;
        }

        protected Bitmap doInBackground(String... urls) {
            String image_url = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(image_url).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DOWNLOAD THUMB", e.getMessage());
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            Log.d("PREVIEW_IDX", this.preview_index.toString()+post_previews.toString());
            post_previews.set(this.preview_index, result);
            rec_adapter_posts.notifyDataSetChanged();
        }
    }




    // COMMS
    @Override
    public void to_fragment_sub_title(String subtitle) {
        sub_title = subtitle;
    }

    @Override
    public void to_fragment_get_posts_from_sub_action() {
        get_posts_from_sub_action(sub_title);
    }








}