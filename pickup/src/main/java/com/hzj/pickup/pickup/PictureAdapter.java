package com.hzj.pickup.pickup;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hzj.pickup.ImageLoader;
import com.hzj.pickup.LoadResourceImage;
import com.hzj.pickup.R;

import java.util.ArrayList;

/**
 * picture gallery adpter
 *
 * @author huangzj
 */
public class PictureAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private ArrayList<String> data;
    private Bitmap defaultBitmap;

    private boolean[] selectedFlag;

    private int maxSize;
    private int selectedSize;

    private OnItemClickListener onItemClickListener;

    public PictureAdapter(Context context, ArrayList<String> data, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.data = data;
        this.onItemClickListener = onItemClickListener;
        this.inflater = LayoutInflater.from(context);
        defaultBitmap = LoadResourceImage.getInstance().getBitmap(context, R.drawable.load);
        selectedFlag = new boolean[data.size()];
    }

    /**
     * 当前已选择数量
     */
    public void setSelectedSize(int size) {
        selectedSize = size;
    }

    /**
     * 最大可选择数
     */
    public void setMaxSize(int size) {
        maxSize = size;
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null) {
            return data.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.picture_gallery_list_item, parent, false);
            holder = new Holder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
            holder.selectLayout = (RelativeLayout) convertView.findViewById(R.id.picture_gallery_select_layout);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.imageView.setImageBitmap(defaultBitmap);

        String path = data.get(position);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(path, holder.imageView);

        if (selectedFlag[position]) {
            holder.imageView.setColorFilter(R.color.select_color_filter);
            holder.checkBox.setChecked(true);
        } else {
            holder.imageView.setColorFilter(null);
            holder.checkBox.setChecked(false);
        }
        holder.selectLayout.setOnClickListener(new OnPhotoSelectListener(position, holder));

        convertView.setOnClickListener(new OnPhotoClick(position));
        return convertView;
    }

    private class OnPhotoSelectListener implements OnClickListener {
        int position;
        Holder holder;

        public OnPhotoSelectListener(int position, Holder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            if (holder.checkBox.isChecked()) {
                holder.imageView.setColorFilter(null);
                holder.checkBox.setChecked(false);
                selectedFlag[position] = false;
            } else {
                if (selectedSize >= maxSize) {
                    Toast.makeText(context, "max size", Toast.LENGTH_LONG).show();
                    return;
                }
                holder.imageView.setColorFilter(R.color.select_color_filter);
                holder.checkBox.setChecked(true);
                selectedFlag[position] = true;
            }

            if (data != null && onItemClickListener != null) {
                onItemClickListener.OnItemCheckedChanged(position, holder.checkBox.isChecked());
            }
        }
    }

    private class OnPhotoClick implements OnClickListener {
        int position;

        public OnPhotoClick(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null){
                onItemClickListener.onItemClick(position);
            }
        }
    }

    public interface OnItemClickListener {
        void OnItemCheckedChanged(int Position, boolean isCheck);

        void onItemClick(int position);
    }

    private static class Holder {
        ImageView imageView;

        RelativeLayout selectLayout;
        CheckBox checkBox;
    }

}
