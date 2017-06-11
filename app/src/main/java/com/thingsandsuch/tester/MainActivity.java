package com.thingsandsuch.tester;


import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.yalantis.ucrop.UCrop;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



//TODO: scroll to top before resetting data




public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    // path variables to connect to internets
    // verification code
    private static final String STATE = "WULZ_APP_LOGIN";
    // registerd reddit app id
    private static final String CLIENT_ID = "_8KdmArtAKAhrA";
    // fake url that ?intent goes to after ?completed - url set as intent catcher thing in manifest
    private static final String REDIRECT_URI = "http://www.wulz.com/my_redirect";
    // location of login token
    private static final String ACCESS_TOKEN_URL = "https://www.reddit.com/api/v1/access_token";
    // login page url
    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
                    "&response_type=code&state=%s&redirect_uri=%s&" +
                    "duration=permanent&scope=identity mysubreddits";

    private String ACCESS_TOKEN = "";
    private String REFRESH_TOKEN = "";
    Integer REQUEST_CODE_CROP_PHOTO = 1001;
    Integer MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1002;


    Boolean logged_in = Boolean.FALSE;
    String user_name;
    String sort_by = "new";

    JSONArray posts_json = new JSONArray();
    ArrayList<Bitmap> post_previews = new ArrayList<Bitmap>();
    ArrayList<Bitmap> sub_banners = new ArrayList<Bitmap>();
    ArrayList<List<String>> list_post_data = new ArrayList<List<String>>();

    List<String> sub_titles;
    List<List<String>> sub_data = new ArrayList<List<String>>();

    private SwipeRefreshLayout lyt_refresh_swipe;
    private ArrayAdapter<String> subs_adapter;
    private RecyclerView.Adapter rec_adapter_posts;

    public static Activity instance = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        instance = this; // TODO: this is bad -- definitely memory leak

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // drawer actions
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle); // TODO whats goin on here
        toggle.syncState();


        // roll out menu action listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // default data
        init_default_data();


        // list setup
        RecyclerView rec_view_posts = (RecyclerView) findViewById(R.id.rec_view_posts );
        LinearLayoutManager lin_lyt_manager = new LinearLayoutManager(this);
        rec_view_posts.setLayoutManager(lin_lyt_manager);


        // list adapter setup
        rec_adapter_posts = new PostsRecyclerAdapter(post_previews, list_post_data);
        rec_view_posts.setAdapter(rec_adapter_posts);
        setup_rec_list_listeners();


        // swipe refresh setup
        lyt_refresh_swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_posts);
        lyt_refresh_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_current_sub_posts_action();
                }
            });



        // SUB_TITLE populate sub name spinner adapter
        subs_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.subs_spinner_item, sub_titles);
        subs_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        populate_subs();



        // SUB_TITLE spinner setup
        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
        spinner.setAdapter(subs_adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
                set_sub_title();
                get_posts_from_sub_action(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //
            }

        });


        // FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_more_posts_action();
            }
        });



        // search button listener setup
//        Button btn_search = (Button) findViewById(R.id.btn_search);
//        btn_search.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                search_for_sub();
//            }
//        });


        // search edit text
//        EditText edit_text_sub_search = (EditText) findViewById(R.id.edit_text_sub_search);
//        edit_text_sub_search.setVisibility(View.INVISIBLE);


        // check & get permissions to WRITE_EXTERNAL_STORAGE
        setup_storage_permissions();


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_refresh) {
//            populate_posts_list(posts_json);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            // do the login
            start_sign_in();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    // PICKUP AFTER LOGIN INTENT
    @Override
    protected void onResume() {
        // pickup on intent passed from android - tell android to run it in manifest

        super.onResume();

        if (logged_in){
            return;
        }

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            // pick up returned data from the intent
            Uri uri = getIntent().getData();

            // check internal data for
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e("onResume","FAIL An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");

                // verify return data against my verification string
                if(state.equals(STATE)) {
                    // returned data contains reddit authorization code
                    // get logged in user access/refresh token data from reddit api
                    // get user info
                    String code = uri.getQueryParameter("code");

                    get_reddit_tokens(code);

                    Toast.makeText(getApplicationContext(), "login be donned", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    // PICKUP AFTER CROP ACTION
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);

            Bitmap bmp_edited = null;
            try {
                bmp_edited = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);

            } catch (IOException e) {
                Log.e("BMP DECODE", e.getMessage());
                }

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                wallpaperManager.setBitmap(bmp_edited);
            } catch (IOException ex) {
                ex.printStackTrace();
                }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            }
    }



    // SETUP
    private void init_default_data() {
        list_post_data.clear();

        List<String> p_data = new ArrayList<>();
        p_data.add("title");
        p_data.add("author");
        p_data.add("hd_url");
        list_post_data.add(0,p_data);

        sub_titles = new ArrayList<String>();

        List<String> default_subs = new ArrayList<>();
        default_subs.add("EarthPorn");
        default_subs.add("AbandonedPorn");
        default_subs.add("ImaginaryTechnology");
        default_subs.add("art");
        default_subs.add("ImaginaryColorscapes");
        default_subs.add("Futurology");
        default_subs.add("aww");
        default_subs.add("pics");

        Bitmap banner = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.logo_wbg);
        for (int i = 0; i < default_subs.size(); i++) {
            String name = default_subs.get(i);
            List<String> dt = new ArrayList<String>();
            dt.add(name);
            dt.add("Title");
            sub_data.add(i,dt);
            sub_banners.add(i,banner);
            get_sub_data_for_title(i,name);
        }

    }

    private void setup_rec_list_listeners(){
        final RecyclerView rec_view_posts = (RecyclerView)findViewById(R.id.rec_view_posts);

        ItemTouchHelper.SimpleCallback swipe_callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                list_post_data.remove(position);
                post_previews.remove(position);
                rec_view_posts.getAdapter().notifyItemRemoved(position);
            }
        };
        ItemTouchHelper swipe_helper = new ItemTouchHelper(swipe_callback);

        swipe_helper.attachToRecyclerView(rec_view_posts);



    }

    private void setup_storage_permissions(){

        // check for permission WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }



    }



    // SEARCH
    private void search_for_sub(){
        Log.d("SEARCH", "search");

//        runOnUiThread(new Runnable() {
//            Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
////            EditText edit_text_sub_search = (EditText) findViewById(R.id.edit_text_sub_search);
//
//            @Override
//            public void run() {
//                spinner.setVisibility(View.INVISIBLE);
////                edit_text_sub_search.setVisibility(View.VISIBLE);
//            }
//        });
    }



    // POSTS
    public void refresh_current_sub_posts_action(){
        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
        get_posts_from_sub_action(spinner.getSelectedItem().toString());
        lyt_refresh_swipe.setRefreshing(false);
    }

    public void get_posts_from_sub_action(final String sub_name){
        get_posts_from_sub(sub_name, false);
    }

    public void load_more_posts_action(){
        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
        String sub_name = spinner.getSelectedItem().toString();
        get_posts_from_sub(sub_name, true);
    }


    public void get_posts_from_sub(final String sub_name, final Boolean add_to_list){
        // basic sub data
        Request request = new Request.Builder()
                .url("https://www.reddit.com/r/" + sub_name + "/hot.json?limit=20&raw_json=1&sort=" + sort_by)
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
                    Log.e("FAIL - get_data", "object");
                }

                try{
                    Log.d("POST",data.toString());
                    posts_json_obj = data.getJSONObject("data").getJSONArray("children");
                }catch (Exception e) {
                    Log.e("FAIL - get_data", "children22");
                }

                populate_posts_list(posts_json_obj, add_to_list);

            }
        });
    }

    public void populate_posts_list(JSONArray subs_obj, Boolean add_to_list){

        if (!add_to_list){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RecyclerView rec_view_posts = (RecyclerView)findViewById(R.id.rec_view_posts);
                    rec_view_posts.smoothScrollToPosition(0);
                }
            });

            list_post_data.clear();
            post_previews.clear();

        }


        Integer posts_count = subs_obj.length();
        Log.d("POPULATE_SUBS_obj_cnt", Integer.toString(posts_count));





        Integer num = 0;
        for (int i = 0; i < subs_obj.length(); i++)
        {
            try {
                JSONObject preview = subs_obj.getJSONObject(i).getJSONObject("data").getJSONObject("preview");
                String author = subs_obj.getJSONObject(i).getJSONObject("data").getString("author");
                String title = subs_obj.getJSONObject(i).getJSONObject("data").getString("title");

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

                list_post_data.add(data_list);

                new download_thumbnail(num).execute(source_url);

                num += 1;


            }catch (JSONException e) {
                Log.e("POSITION_PUT", Integer.toString(num));
                Log.e("POSITION_PUT", e.toString());
                Log.d("FAIL", subs_obj.toString());
            }
        }

        Log.d("POPULATE_SUBS", subs_obj.toString());
        Log.d("POPULATE_SUBS", Integer.toString(list_post_data.size()));


    }





    // LOGIN
    public void start_sign_in() {
        // build intent and pass out to android to do the thing
        // gets picked up by default web action
        // reddit login page

        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void get_reddit_tokens(String code) {
        // get login token from reddit api thingy
        // token valid for 1 hr

        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Wulz App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();


        // put internet request in android thread queue
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("get_reddit_tokens", "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data;

                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");
                    ACCESS_TOKEN = accessToken;
                    REFRESH_TOKEN = refreshToken;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("get_reddit_tokens", "FAILED");
                }

                get_user_name(ACCESS_TOKEN);
                get_user_subs_data(ACCESS_TOKEN);
                logged_in = true;
            }
        });


    }



    // SUB_TITLE SPINNER BAR
    public void set_sub_title(){
        if (sub_data == null){
            Log.e("TITLE_SET","sub_data == null");
        }else {
            if (sub_data.size() >= 1){
                Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
                TextView txt_title = (TextView) findViewById(R.id.txt_sub_title);

                Integer idx_spinner = spinner.getSelectedItemPosition();

                String title = sub_data.get(idx_spinner).get(1);
                txt_title.setText(title);

                update_sub_banner();

            }else {
                Log.e("TITLE_SET","sub_data > 1");
            }
        }
    }

    public void update_sub_banner(){
        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
        Integer idx_spinner = spinner.getSelectedItemPosition();
        android.support.design.widget.AppBarLayout lyt_banner = (android.support.design.widget.AppBarLayout) findViewById(R.id.banner_layout);
//        android.support.v7.widget.Toolbar lyt_banner = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
//        LinearLayout lyt_banner = (LinearLayout) findViewById(R.id.banner_layout);

        Log.d("BANNER_SET",sub_banners.toString());

        if (sub_banners.size() > 0){
            Bitmap bmp_banner = sub_banners.get(idx_spinner);

            if (bmp_banner != null){
                Drawable draw = new BitmapDrawable(getResources(), bmp_banner);
                lyt_banner.setBackground(draw);
            }
        }

    }

    public void get_sub_data_for_title(Integer index, String sub_title){

        final Integer idx = index;
        Request request = new Request.Builder()
                .url("https://www.reddit.com/r/" + sub_title + "/about.json?raw_json=1")
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
                try {
                    data = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e("FAIL - get_data", "object");
                }

                try{
                    JSONObject child_data = data.getJSONObject("data");
                    String sub_name = child_data.getString("display_name");
                    String sub_title = child_data.getString("title");
                    String sub_banner_url = child_data.getString("banner_img");

                    List<String> dt = new ArrayList<String>();
                    dt.add(sub_name);
                    dt.add(sub_title);
                    sub_data.set(idx, dt);

                    if (sub_banner_url.equals("null")){
                        Log.e("BANNER_GET_init", sub_name + "  "+ sub_banner_url);
                    }else if (sub_banner_url.equals(null)){
                        Log.e("BANNER_GET_init", sub_name + "  "+ sub_banner_url);
                    }else if (sub_banner_url.equals(" ")){
                        Log.e("BANNER_GET_init", sub_name + "  "+ sub_banner_url);
                    }else if (sub_banner_url.isEmpty()){
                        Log.e("BANNER_GET_init", sub_name + "  "+ sub_banner_url);
                    }else {
                        Log.d("BANNER_GET_init", sub_name + "  "+ sub_banner_url);
                    }



                    new download_banner(idx).execute(sub_banner_url);

                }catch (Exception e) {
                    Log.e("BANNER_GET_init", e.toString());
                }




//                populate_posts_list(posts_json);

            }
        });


    }

    public void populate_subs(){
        for (int i = 0; i < sub_data.size(); i++) {
            String name = sub_data.get(i).get(0);
            subs_adapter.add(name);}
    }






    // USER DATA PULL
    public void get_user_name(String access_token) throws IOException {
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Wulz App")
                .addHeader("Authorization", "bearer "+access_token+"")
                .url("https://oauth.reddit.com/api/v1/me")
                .build();

        // put internet request in android thread queue
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //     Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;

                try {
                    data = new JSONObject(json);
                    user_name = data.getString("name");

                } catch (JSONException e) {
                    Log.e("get_user_name", "FAIL");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView txt_user_name = (TextView) findViewById(R.id.txt_user_name);
                        txt_user_name.setText(user_name);
                    }
                });

            }
        });
    }

    public void get_user_subs_data(String access_token) throws IOException {
        //TODO : separate method to get titles/banner images from sub_names array

        sub_data.clear();
        sub_banners.clear();
//        post_previews.clear();

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Wulz App")
                .addHeader("Authorization", "bearer "+access_token+"")
                .url("https://oauth.reddit.com/subreddits/mine/subscriber")
                .build();

        ///subreddits/mine/subscriber
        // put internet request in android thread queue
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //     Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONArray data_json_array = null;

                try {
                    JSONObject data_json_obj = new JSONObject(json).getJSONObject("data");
                    data_json_array = data_json_obj.getJSONArray("children");

                    for (int i = 0; i < data_json_array.length(); i++) {
                        JSONObject child_data = data_json_array.getJSONObject(i).getJSONObject("data");
                        String sub_name = child_data.getString("display_name");
                        String sub_title = child_data.getString("title");
                        String sub_banner_url = child_data.getString("banner_img");


                        List<String> dt = new ArrayList<String>();
                        dt.add(sub_name);
                        dt.add(sub_title);
                        sub_data.add(i,dt);
//                        post_previews.add(null);

                        if (sub_banner_url != null){
                            Log.d("BANNER_GET", sub_banner_url);
                            new download_banner(i).execute(sub_banner_url);
                        }else{
                            Log.e("BANNER_GET", child_data.toString());
                        }

                    }

                } catch (JSONException e) {
                    Log.e("FAIL", "get subs");
                }

//                set_subs_data();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        subs_adapter.clear();
                        for (int i = 0; i < sub_data.size(); i++) {
                            String name = sub_data.get(i).get(0);
                            subs_adapter.add(name);
                        }

                        subs_adapter.notifyDataSetChanged();

                        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
                        get_posts_from_sub_action(spinner.getSelectedItem().toString());
                        set_sub_title();

                    }
                });


            }
        });
    }



    // DOWNLOAD IMAGES
    private class download_banner extends AsyncTask<String, Void, Bitmap> {
        Integer banner_index;

        public download_banner (Integer banner_index) {
            this.banner_index = banner_index;
        }

        protected Bitmap doInBackground(String... urls) {
            String image_url = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(image_url).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("BANNER_DOWNLOAD", image_url);
//                Log.e("BANNER_DOWNLOAD", e.getMessage());
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            try{
                Log.d("BANNER_DWNL_IDX", this.banner_index.toString()+sub_banners.toString());
                if (result != null){
                    sub_banners.set(this.banner_index, result);
                }
            }catch (Exception e){
                Log.e("BANNER_DWNL_IDX", this.banner_index.toString() + sub_banners.toString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (banner_index == 0){
                        update_sub_banner();
                    }

                }
            });


        }
    }

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rec_adapter_posts.notifyDataSetChanged();

                }
            });


        }
    }




    // moved this to other activity
    public File save_temp_bitmap(Bitmap bmp) {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
//        String imageFileName = "PNG_" + timeStamp + "_.png";
//        File mFileTemp = null;


        String root=getApplicationContext().getDir("my_sub_dir", Context.MODE_PRIVATE).getAbsolutePath();
        File myDir = new File(root + "/Img");
        if(!myDir.exists()){
            myDir.mkdirs();
        }

//        Bitmap bbicon;
//        bbicon=BitmapFactory.decodeResource(getResources(),R.drawable.logo);
        //ByteArrayOutputStream baosicon = new ByteArrayOutputStream();
        //bbicon.compress(Bitmap.CompressFormat.PNG,0, baosicon);
        //bicon=baosicon.toByteArray();

        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        Log.d("FILE",extStorageDirectory);
        OutputStream outStream = null;
        File file = new File(extStorageDirectory, "er.PNG");
        try {
            outStream = new FileOutputStream(file);
//            bbicon.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch(Exception e) {
            Log.e("WRITE","FAIL");
            e.printStackTrace();
        }

//        try {
//            mFileTemp=File.createTempFile(imageFileName,".png",myDir.getAbsoluteFile());
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }

//        File mFileTemp = new File(imageFileName);
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(mFileTemp);
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
//            // PNG is a lossless format, the compression factor (100) is ignored
//        } catch (Exception e) {
//            Log.e("WRITE","FAIL");
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }



        return file;
    }

    public class download_wallpaper extends AsyncTask<String, Void, Bitmap> {
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
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            int ratio = width / height;
            Log.d("","");
            Log.d("WIDTH", Integer.toString(width)); // 1440
            Log.d("HEIGHT", Integer.toString(height)); // 2392
            Log.d("","");


            File temp_file = save_temp_bitmap(result);
            Uri sourceUri = Uri.fromFile(temp_file);
            Uri destinationUri = Uri.fromFile(new File(getApplicationContext().getCacheDir(), "IMG_" + System.currentTimeMillis()));

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(100);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setToolbarTitle("Crop Wallpaper");
            options.setToolbarColor(ContextCompat.getColor(instance, R.color.colorPrimary));
            options.setLogoColor(ContextCompat.getColor(instance, R.color.colorAccent));
            options.setActiveWidgetColor(ContextCompat.getColor(instance, R.color.colorPrimaryDark));
            options.setStatusBarColor(ContextCompat.getColor(instance, R.color.colorPrimaryDark));
            options.setFreeStyleCropEnabled(true);
            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .withAspectRatio(7,8) // 7x8 fits 3 screens
                    .start(instance);


        }
    }

}




//                        Iterator<String> keys = child_data.keys();
//                        while(keys.hasNext()){
//                            String key = keys.next();
//                            String value = child_data.getString(key);
//                            Log.d("KEY ",key);
//                            Log.d("VALUE ",value);
//                        }