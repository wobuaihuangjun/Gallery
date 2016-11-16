package com.hzj.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hzj.pickup.ImageLoader;

import java.util.List;

/**
 * 本地图库多选数据适配器
 *
 * @author huangzj
 */
public class SelectResultAdapter extends BaseAdapter {

    private Context context;
    private List<String> data;

    public SelectResultAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int arg0) {
        return data.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        Holder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.picture_item, null);
            holder = new Holder();
            holder.imageView = (ImageView) view.findViewById(R.id.imageView);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        String path = data.get(position);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(path, holder.imageView);

        return view;
    }

    private static class Holder {
        ImageView imageView;
    }

}
