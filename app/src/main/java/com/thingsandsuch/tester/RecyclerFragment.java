package com.thingsandsuch.tester;

import android.util.Log;
import android.app.Fragment;

import android.os.Bundle;
import android.os.AsyncTask;

import android.support.v7.widget.RecyclerView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.OkHttpClient;



public class RecyclerFragment extends Fragment  implements FragmentCommunicator{

    String sort_by = "hot";
    String sub_name = "All";
    String last_post_id;

    private RecyclerView rec_view_posts;
    private SwipeRefreshLayout lyt_refresh_swipe;
    private PostsRecyclerAdapter rec_adapter_posts;

    private Switch swc_nsfw;

    ArrayList<List<String>> list_post_data = new ArrayList<List<String>>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rec_view = inflater.inflate(R.layout.recycler_fragment, container, false);

        // list setup
        rec_view_posts = (RecyclerView) rec_view.findViewById(R.id.rec_view_posts );
        LinearLayoutManager lin_lyt_manager = new LinearLayoutManager(rec_view.getContext());
        rec_view_posts.setLayoutManager(lin_lyt_manager);



        // list adapter setup
        rec_adapter_posts = new PostsRecyclerAdapter(list_post_data);
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


        // nsfw switch
        View sett_view = inflater.inflate(R.layout.side_menu, container, false);
        swc_nsfw = (Switch) sett_view.findViewById(R.id.swc_nsfw);
        //TODO: add listener to refresh on change

        return rec_view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RECYCLER","create");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("RESUME", "recycler");

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

                rec_adapter_posts.notifyDataSetChanged();

                Log.d("SWIPE", "swiped");
//                rec_adapter_posts.notifyDataSetChanged();
//                int position = viewHolder.getAdapterPosition();
//                list_post_data.remove(position);
//                rec_adapter_posts.notifyItemRemoved(position); //TODO: this might cause issues
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof PostsRecyclerAdapter.PreviewHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                } else {
                    return 0;
                }
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
        String get_url = "https://www.reddit.com/r/" + sub_name + "/" + sort_by + ".json?limit=25&raw_json=1";
//        String get_url = "https://www.reddit.com/r/" + sub_name + "/" + sort_by + ".json?limit=100&raw_json=1";

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
        get_posts_from_sub(sub_name, true);
        rec_adapter_posts.showLoading(false);
    }

    public void populate_posts_list(JSONArray subs_obj, Boolean add_to_list){

        if (!add_to_list){
            rec_view_posts.smoothScrollToPosition(0);
            list_post_data.clear();
        }

        Log.d("PREVIEW_rec_view width", Integer.toString(rec_view_posts.getWidth()));

        Integer posts_count = subs_obj.length();
        Log.d("POPULATE_SUBS_obj_cnt", Integer.toString(posts_count));

        Boolean show_nsfw = swc_nsfw.isChecked();

        Integer num = 0;
        if (add_to_list){
            num = list_post_data.size();
        }
        for (int i = 0; i < subs_obj.length(); i++)
        {
            try {
                if (!show_nsfw){
                    String nsfw = subs_obj.getJSONObject(i).getJSONObject("data").getString("over_18");
                    if(Objects.equals(nsfw, "true")) {
                        continue;
                    }
                }


                JSONObject preview = subs_obj.getJSONObject(i).getJSONObject("data").getJSONObject("preview");
                String author = subs_obj.getJSONObject(i).getJSONObject("data").getString("author");
                String title = subs_obj.getJSONObject(i).getJSONObject("data").getString("title");

                String score = subs_obj.getJSONObject(i).getJSONObject("data").getString("score");
                String up_votes = subs_obj.getJSONObject(i).getJSONObject("data").getString("ups");
//                String down_votes = subs_obj.getJSONObject(i).getJSONObject("data").getString("downs");

                String post_id = subs_obj.getJSONObject(i).getJSONObject("data").getString("id");

                Log.d("POST_DATA", subs_obj.getJSONObject(i).getJSONObject("data").toString());

                JSONObject images = preview.getJSONArray("images").getJSONObject(0);
                String source_url = images.getJSONObject("source").getString("url");
                JSONArray resolutions = images.getJSONArray("resolutions");

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
                data_list.add(source_url);
                data_list.add(up_votes);
                data_list.add(sub_name);
                data_list.add(post_id);

                list_post_data.add(data_list);


                num += 1;


            }catch (JSONException e) {
                Log.e("POSITION_PUT", Integer.toString(num));
                Log.e("POSITION_PUT", e.toString());
                Log.d("FAIL", "populate_posts_list"+ subs_obj.toString());
            }
        }

        new RecyclerFragment.update_recycler_view(num).execute("");

    }

    public void refresh_current_sub_posts_action(){
        get_posts_from_sub_action(sub_name);
        lyt_refresh_swipe.setRefreshing(false);
    }



    // NOT SURE NOW
    private class update_recycler_view extends AsyncTask<String, Void, String> {

        public update_recycler_view(Integer i) {
        }

        protected String doInBackground(String... ss) {
            return "";
        }

        protected void onPostExecute(String result) {
            rec_adapter_posts.notifyDataSetChanged();
        }
    }



    // COMMS
    @Override
    public void to_fragment_get_posts_from_sub_action(String subname) {
        sub_name = subname;
        get_posts_from_sub_action(sub_name);
    }


}