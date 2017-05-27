package com.thingsandsuch.tester;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class old_ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;

    ArrayList<Bitmap> previews;
    ArrayList<String> urls;

    public old_ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;

    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) { // view = convertView  viewGroup = parent
        if (view == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.posts_list_group,null);
         }
        ImageView img=(ImageView) view.findViewById(R.id.imageView1);
//        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        img.setEnabled(false);

        try{
            Bitmap bmp = previews.get(groupPosition);
            img.setImageBitmap(bmp);
        }catch (Exception e) {
            Log.e("get_preview","FAILED");
        }


        return view;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) { // view = convertView  viewGroup = parent
        final String str_title = (String)getGroup(groupPosition);
        final String str_author = (String)getChild(groupPosition, childPosition);

        if ( view == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item,null);
        }

        TextView lbl_title = (TextView)view.findViewById(R.id.lbl_list_item_title);
        lbl_title.setText(str_title);

        TextView lbl_author = (TextView)view.findViewById(R.id.lbl_list_item_author);
        lbl_author.setText(str_author);

//        Button btn_set_wall  = (Button) view.findViewById(R.id.btn_set_wall);
//        btn_set_wall.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                ((MainActivity)context).set_wallpaper(urls.get(groupPosition));
//            }
//        });

//        Button btn_full_screen  = (Button) view.findViewById(R.id.btn_full_screen);
//        btn_full_screen.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                ((MainActivity)context).show_fullscreen(hd_urls.get(groupPosition));
//            }
//        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public void update_data(ArrayList<String> post_titles, HashMap<String, List<String>>listHash, ArrayList<String> post_urls) {
        this.listDataHeader = post_titles;
        this.listHashMap = listHash;
        urls = post_urls;


    }

    public void update_previews(ArrayList<Bitmap> post_previews) {
        previews = post_previews;
    }




}
