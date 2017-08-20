package com.thingsandsuch.tester;

import com.yalantis.ucrop.UCrop;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;

import android.util.Log;
import android.os.Bundle;
import android.util.Base64;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.InputStream;
import java.io.IOException;

import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;


import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.OkHttpClient;




public class MainActivity extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener{

    // path variables to connect to internets
    // verification code
    private static final String STATE = "WULZ_APP_LOGIN";
    // registered reddit app id
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


    public static final String PREFS_NAME = "LoginPrefs";


    String current_user_name;
    Boolean logged_in = Boolean.FALSE;

    List<String> sub_titles;
    ArrayList<Bitmap> sub_banners = new ArrayList<Bitmap>();
    List<List<String>> sub_data = new ArrayList<List<String>>();

    public PostFragment frag_post;
    public RecyclerFragment frag_recycler;
    private ArrayAdapter<String> subs_adapter;

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

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


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


        // setup main fragment
        frag_recycler = new RecyclerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, frag_recycler);
        fragmentTransaction.commit();



        // default data
        init_default_data();


        // last login
        SharedPreferences settings = getSharedPreferences("last_login", 0);
//        String last_login_sett = settings.getString("last_login", null);
        if (settings != null){
            current_user_name = settings.getString("user_name", "");
            REFRESH_TOKEN = settings.getString("refresh_token", "");
            if (!Objects.equals(REFRESH_TOKEN, "")){
                Log.d("TOKENS", current_user_name + REFRESH_TOKEN);
                get_reddit_token_from_refresh();
            }
        }



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
                frag_recycler.to_fragment_sub_title(spinner.getSelectedItem().toString());
                frag_recycler.to_fragment_get_posts_from_sub_action();
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
//                load_more_posts_action();
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




    // RESUME
    @Override // pickup after login intent
    protected void onResume() {
        // pickup on intent passed from android - tell android to run it in manifest

        super.onResume();

        Log.d("LOGIN", "on resume");

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

    @Override // pickup after crop action
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
        sub_titles = new ArrayList<String>();

        List<String> default_subs = new ArrayList<>();
        default_subs.add("All");
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




    // LOGIN
    public void start_sign_in() {
        // build intent and pass out to android to do the thing
        // gets picked up by default web action
        // reddit login page

        Log.d("LOGIN", "start_sign_in");


        if (!Objects.equals(REFRESH_TOKEN, "")){
            Log.d("LOGIN", "have refresh token" + REFRESH_TOKEN);
            get_reddit_token_from_refresh();

        } else {

            String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

        }




    }

    private void get_reddit_tokens(String code) {
        // get login token from reddit api thingy
        // token valid for 1 hr

        Log.d("LOGIN", "get_reddit_tokens");

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
                Log.e("LOGIN", "get_reddit_tokens-ERROR: " + e);
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

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("refresh_token", REFRESH_TOKEN);
                    editor.apply();


                    Log.d("LOGIN", "refresh token" + refreshToken);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("get_reddit_tokens", "FAILED");
                }

                get_user_name(ACCESS_TOKEN);
                get_user_subs_data(ACCESS_TOKEN);
                logged_in = true;



                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
//                    editor.putString("refresh_token", REFRESH_TOKEN);
                Set<String> s = new android.support.v4.util.ArraySet<String>();
                s.add(current_user_name + " " + REFRESH_TOKEN);
                editor.putStringSet("logins",s);
                editor.apply();


            }
        });


    }

    private void get_reddit_token_from_refresh() {
        // get login token from reddit api thingy
        // token valid for 1 hr

        Log.d("LOGIN", "get_reddit_tokens");

        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        // grant_type=refresh_token&refresh_token=TOKEN

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Wulz App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=refresh_token&refresh_token=" + REFRESH_TOKEN +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();


        // put internet request in android thread queue
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("get_reddit_tokens", "ERROR: " + e);
                Log.e("LOGIN", "get_reddit_tokens-ERROR: " + e);
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

                    Log.d("LOGIN", "refresh token" + refreshToken);

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
                if (txt_title != null){ //TODO: fix for real
                    txt_title.setText(title);
                }

                update_sub_banner();

            }else {
                Log.e("TITLE_SET","sub_data > 1");
            }
        }
    }

    public void update_sub_banner(){
        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
        Integer idx_spinner = spinner.getSelectedItemPosition();
//        android.support.design.widget.AppBarLayout lyt_banner = (android.support.design.widget.AppBarLayout) findViewById(R.id.banner_layout);
//        android.support.v7.widget.Toolbar lyt_banner = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        LinearLayout lyt_banner = (LinearLayout) findViewById(R.id.banner_layout);

        Log.d("BANNER_SET",sub_banners.toString());

        if (sub_banners.size() > 0){
            Bitmap bmp_banner = sub_banners.get(idx_spinner);

            if (bmp_banner != null){
                Drawable draw = new BitmapDrawable(getResources(), bmp_banner);
                lyt_banner.setBackground(draw);
            } else {
//                lyt_banner
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
                    current_user_name = data.getString("name");

                } catch (JSONException e) {
                    Log.e("get_user_name", "FAIL");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView txt_user_name = (TextView) findViewById(R.id.txt_user_name);
                        txt_user_name.setText(current_user_name);
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

                        set_sub_title();
                        Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
                        frag_recycler.get_posts_from_sub_action(spinner.getSelectedItem().toString());

                    }
                });


            }
        });
    }




    // FRAGMENT
    public void run_post_fragment(String title, String author, String hd_url, String score, String preview_path){

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("author", author);
        bundle.putString("hd_url", hd_url);
        bundle.putString("score", score);
        bundle.putString("preview_path", preview_path);

        frag_post = new PostFragment();
        frag_post.setArguments(bundle);




        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, frag_post);
        fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();

    }

    // DOWNLOAD
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


    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences("last_login", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user_name", current_user_name);
        editor.putString("refresh_token", REFRESH_TOKEN);
        editor.apply();

    }
}





//Iterator<String> keys = child_data.keys();
//while(keys.hasNext()){
//    String key = keys.next();
//    String value = child_data.getString(key);
//    Log.d("KEY ",key);
//    Log.d("VALUE ",value);
//}