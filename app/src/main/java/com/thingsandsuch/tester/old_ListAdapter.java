package com.thingsandsuch.tester;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryan on 5/15/2017.
 */

public class old_ListAdapter extends ArrayAdapter<String> {
    private final Context context;
//    private final ArrayList<String> titles;

    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;

    ArrayList<Bitmap> previews;
    ArrayList<String> urls;


    public old_ListAdapter(Context context, List<String> titles, HashMap<String, List<String>> listHashMap) {
//    public old_ListAdapter(Context context, ArrayList<String> titles) {
        super(context,R.layout.posts_list_group, titles);
        this.listDataHeader = titles;
        this.listHashMap = listHashMap;

        this.context = context;
//        this.titles = titles;

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.posts_list_group,null);
        }

        ImageView img=(ImageView) view.findViewById(R.id.imageView1);
        try{
            Bitmap bmp = previews.get(position);
            img.setImageBitmap(bmp);
        }catch (Exception e) {
            Log.e("get_preview","FAILED");
        }

        return view;
    }




    public void update_data(ArrayList<String> post_titles, HashMap<String, List<String>> listHash, ArrayList<String> post_urls) {
        this.listDataHeader = post_titles;
        this.listHashMap = listHash;
        urls = post_urls;
    }

    public void update_previews(ArrayList<Bitmap> post_previews) {
        previews = post_previews;
    }







}