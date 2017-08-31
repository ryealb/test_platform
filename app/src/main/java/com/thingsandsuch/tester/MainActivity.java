package com.thingsandsuch.tester;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.os.Bundle;
import android.util.Base64;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

import android.support.v7.widget.Toolbar;
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
    List<List<String>> sub_data = new ArrayList<List<String>>();

    List<String> sub_banner_data = new ArrayList<>();

    public PostFragment frag_post;
    public RecyclerFragment frag_recycler;
    private ArrayAdapter<String> subs_adapter;

    public static Activity instance = null;

    PopupWindow sort_popup;


    private ShareActionProvider mShareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar action_bar = getSupportActionBar();
        action_bar.setDisplayShowTitleEnabled(false);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instance = this; // TODO: this is bad -- definitely memory leak


        // drawer actions
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle); // TODO whats goin on here
        toggle.syncState();

        LinearLayout drawer_main = (LinearLayout) findViewById(R.id.drawer_main);
        drawer_main.setVisibility(View.GONE);

//        // roll out menu action listener
//        NavigationView navigationView = (NavigationView) findViewById(R.id.content_main);
//        navigationView.setNavigationItemSelectedListener(this);


        // setup main fragment
        frag_recycler = new RecyclerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, frag_recycler);
        fragmentTransaction.commit();


        // default data
        init_default_data();

        Log.d("ON_CREATE", "yup");

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
                String sub_name = spinner.getSelectedItem().toString();
                frag_recycler.to_fragment_get_posts_from_sub_action(sub_name);

                get_title_banner_for_sub(sub_name);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //
            }

        });


        // SORT button setup
        Button btn_sort = (Button) findViewById(R.id.btn_sort);
        btn_sort.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                sort_posts_recycler();
            }
        });



        // TOOLBAR hide - show
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.main_app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                TextView main_banner_title = (TextView) findViewById(R.id.main_banner_title);
                if (scrollRange + verticalOffset == 0) {
                    main_banner_title.setVisibility(View.GONE);
                    isShow = true;
                } else if(isShow) {
                    main_banner_title.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });



        // search button listener setup
        ImageButton btn_login = (ImageButton) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
//                search_for_sub();
            }
        });


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

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    // RESUME
    @Override // pickup after login intent
    protected void onResume() {
        // pickup on intent passed from android - tell android to run it in manifest

        super.onResume();

        Log.d("RESUME", "main");

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
        default_subs.add("AbandonedPorn");
        default_subs.add("art");
        default_subs.add("aww");
        default_subs.add("EarthPorn");
        default_subs.add("Futurology");
        default_subs.add("ImaginaryColorscapes");
        default_subs.add("ImaginaryTechnology");
        default_subs.add("pics");

        for (int i = 0; i < default_subs.size(); i++) {
            String name = default_subs.get(i);
            List<String> dt = new ArrayList<String>();
            dt.add(name);
            dt.add("All");
            dt.add("all the things");
            sub_data.add(i,dt);
        }

        get_title_banner_for_sub("all");

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
    public void populate_subs(){
        for (int i = 0; i < sub_data.size(); i++) {
            String name = sub_data.get(i).get(0);
            subs_adapter.add(name);}
    }



    // BANNER TITLE
    public void get_title_banner_for_sub(final String sub_title){
        sub_banner_data.clear();
        sub_banner_data.add("");
        sub_banner_data.add("");

        Log.d("BANNER_GET", sub_title);

        Request request = new Request.Builder()
                .url("https://www.reddit.com/r/" + sub_title + "/about.json?raw_json=1")
                .build();


        // GET DATA
        // put internet request in android thread queue
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BANNER", "request fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;
                JSONObject child_data = null;
                try {
                    data = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e("BANNER", e.toString());
                }

                try {
                    child_data = data.getJSONObject("data");
                }catch (Exception e) {
                    Log.e("BANNER_GET_child_data", e.toString());
                }

                try{
                    String sub_display_title = child_data.getString("title");
                    if (Objects.equals(sub_display_title, "")) {
                        sub_display_title = "All the things.";
                    }
                    sub_banner_data.set(0, sub_display_title);
                }catch (Exception e) {
                    sub_banner_data.set(0, "All the things.");
                    Log.e("BANNER_GET_title", e.toString());
                }

                String sub_banner_url = "";
                try{
                    sub_banner_url = child_data.getString("banner_img");
                }catch (Exception e) {
                    Log.e("BANNER_GET_url", e.toString());
                }

                if (Objects.equals(sub_banner_url, "")) {
                    get_fallback_banner_for_sub(sub_title);
                    return;
                }

                if (Objects.equals(sub_banner_url, null)) {
                    get_fallback_banner_for_sub(sub_title);
                    return;
                }

                sub_banner_data.set(1, sub_banner_url);
                update_title_banner_display();

            }
        });
    }

    public void get_fallback_banner_for_sub(String sub_title){
        String get_url = "https://www.reddit.com/r/" + sub_title + "/" + "top" + ".json?limit=1&raw_json=1";
        Request request = new Request.Builder()
                .url(get_url)
                .build();

        // GET DATA
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BANNER_FALLBACK", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject data = new JSONObject(json);
                    JSONArray posts_json_obj = data.getJSONObject("data").getJSONArray("children");
                    JSONObject preview = posts_json_obj.getJSONObject(0).getJSONObject("data").getJSONObject("preview");
                    JSONObject images = preview.getJSONArray("images").getJSONObject(0);
                    String banner_url = images.getJSONObject("source").getString("url");

                    if (!Objects.equals(banner_url, null)){
                        sub_banner_data.set(1, banner_url);
                        update_title_banner_display();
                    }

                }catch (Exception e) {
                    Log.e("BANNER_FALLBACK", e.toString());
                }
            }
        });
    }

    public void update_title_banner_display() {
        Log.d("BANNER_DATA", sub_banner_data.toString());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView img_view_banner = (ImageView) findViewById(R.id.main_activity_banner);
                Log.d("BANNER_IMG", Boolean.toString(img_view_banner.isEnabled()));
                String banner_url = sub_banner_data.get(1);
                Log.d("BANNER_URL", banner_url);

                Context context = getApplicationContext();

                try {
                    Glide.with(context)
                            .load(banner_url)
                            .placeholder(ContextCompat.getDrawable(context, R.mipmap.base_banner))
                            .centerCrop()
                            .into(img_view_banner);
                    img_view_banner.setColorFilter(ContextCompat.getColor(context, R.color.color_banner_shade), android.graphics.PorterDuff.Mode.MULTIPLY);
                }catch (Exception e) {
                    Log.e("BANNER_BIND_IMG","failed");
                }


                try {
                    TextView banner_title = (TextView) findViewById(R.id.main_banner_title);

                    String title = sub_banner_data.get(0);
                    banner_title.setText(title);
                }catch (Exception e) {
                    Log.e("BANNER_BIND_TITLE","failed");
                }
            }
        });
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

                        Log.d("SUB_NAME", sub_name);
                        Log.d("TITLE", sub_title);
                        List<String> dt = new ArrayList<String>();
                        dt.add(sub_name);
                        dt.add(sub_title);
                        dt.add(sub_banner_url);
                        sub_data.add(i,dt);

                    }

                } catch (JSONException e) {
                    Log.e("TITLE", "get subs" + e.toString());
                    Log.e("FAIL", "get subs" + e.toString());
                }

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
                        frag_recycler.get_posts_from_sub_action(spinner.getSelectedItem().toString());

                    }
                });


            }
        });
    }




    // FRAGMENTS
    public void run_post_fragment(String title, String author, String hd_url, String upvote, String preview_url){

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("author", author);
        bundle.putString("hd_url", hd_url);
        bundle.putString("upvote", upvote);
        bundle.putString("preview_url", preview_url);

        frag_post = new PostFragment();
        frag_post.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("post_fragment");
        fragmentTransaction.replace(R.id.fragment_container, frag_post);
        fragmentTransaction.commit();

    }

    public void sort_posts_recycler() {
//        RelativeLayout content_main = (RelativeLayout) findViewById(R.id.content_main);
        NestedScrollView content_main = (NestedScrollView) findViewById(R.id.content_main);
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.sort_popup,null);

        final RadioGroup rad_sort_group = (RadioGroup) customView.findViewById(R.id.rad_sort_group);
        RadioButton rad_sort_hot = (RadioButton) customView.findViewById(R.id.rad_sort_hot);
        RadioButton rad_sort_new = (RadioButton) customView.findViewById(R.id.rad_sort_new);
        RadioButton rad_sort_top = (RadioButton) customView.findViewById(R.id.rad_sort_top);
        RadioButton rad_sort_controversial = (RadioButton) customView.findViewById(R.id.rad_sort_controversial);

//        final List<String> sort_options = new ArrayList<>();
//        sort_options.add("hot");
//        sort_options.add("new");
//        sort_options.add("top");
//        sort_options.add("controversial");


        if (Objects.equals(frag_recycler.sort_by, "hot")) {
            rad_sort_hot.setChecked(true);
        }

        if (Objects.equals(frag_recycler.sort_by, "new")) {
            rad_sort_new.setChecked(true);
        }

        if (Objects.equals(frag_recycler.sort_by, "top")) {
            rad_sort_top.setChecked(true);
        }

        if (Objects.equals(frag_recycler.sort_by, "controversial")) {
            rad_sort_controversial.setChecked(true);
        }

        sort_popup = new PopupWindow(customView, DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
        sort_popup.setFocusable(true);
        sort_popup.showAtLocation(content_main, Gravity.CENTER, 0, 0);

        rad_sort_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                int radioButtonID = rad_sort_group.getCheckedRadioButtonId();
                View radioButton = rad_sort_group.findViewById(radioButtonID);
                int idx = rad_sort_group.indexOfChild(radioButton);
                RadioButton r = (RadioButton)  rad_sort_group.getChildAt(idx);
                String selected_text = r.getText().toString();
                Log.d("SORT",selected_text);

                if (!Objects.equals(frag_recycler.sort_by, selected_text)) {
                    frag_recycler.sort_by = selected_text;
                    Spinner spinner = (Spinner) findViewById(R.id.sub_spinner);
                    frag_recycler.to_fragment_get_posts_from_sub_action(spinner.getSelectedItem().toString());
                }
                sort_popup.dismiss();
            }
        });



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