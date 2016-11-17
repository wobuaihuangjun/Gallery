package com.hzj.gallery.pickup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzj.gallery.ImageLoader;
import com.hzj.gallery.R;

import java.util.HashMap;
import java.util.List;

/**
 * picture folder browse adapter
 *
 * @author huangzj
 */
public class PictureFolderAdapter extends BaseAdapter {

    public static final String FILE_COUNT = "file_count";
    public static final String FILE_NAME = "file_name";
    public static final String PICTURE_PATH = "picture_path";

    private List<HashMap<String, String>> listData;
    private Bitmap defaultBitmap;

    private LayoutInflater inflater;

    public PictureFolderAdapter(Context context, List<HashMap<String, String>> listData) {
        this.listData = listData;
        this.inflater = LayoutInflater.from(context);
        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.load);
    }

    @Override
    public int getCount() {
        if (listData != null) {
            return listData.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (listData != null) {
            return listData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.picture_folder_list_item, parent, false);
            holder.pictureIv = (ImageView) convertView.findViewById(R.id.filephoto_imgview);
            holder.fileCountTv = (TextView) convertView.findViewById(R.id.filecount_textview);
            holder.fileNameTv = (TextView) convertView.findViewById(R.id.filename_textview);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.fileNameTv.setText(listData.get(position).get(FILE_NAME));
        holder.fileCountTv.setText(listData.get(position).get(FILE_COUNT));

        holder.pictureIv.setImageBitmap(defaultBitmap);

        String path = listData.get(position).get(PICTURE_PATH);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(path, holder.pictureIv);
        return convertView;
    }

    private static class Holder {
        ImageView pictureIv;
        TextView fileCountTv;
        TextView fileNameTv;
    }

}
