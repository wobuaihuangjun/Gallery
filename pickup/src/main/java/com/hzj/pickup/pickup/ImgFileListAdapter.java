package com.hzj.pickup.pickup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzj.pickup.ImageLoader;
import com.hzj.pickup.R;

import java.util.HashMap;
import java.util.List;

/**
 * 本地图片文件夹列表适配器
 *
 * @author huangzj
 */
public class ImgFileListAdapter extends BaseAdapter {

    private Context context;
    private String filecount = "filecount";
    private String filename = "filename";
    private String imgpath = "imgpath";
    private List<HashMap<String, String>> listdata;
    // private LoadBitmapUtil loadBitmapUtil;
    private Bitmap defauteBitmap;

    public ImgFileListAdapter(Context context,
                              List<HashMap<String, String>> listdata) {
        this.context = context;
        this.listdata = listdata;
        // loadBitmapUtil = new LoadBitmapUtil(context);
        defauteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.load);
    }

    @Override
    public int getCount() {
        return listdata.size();
    }

    @Override
    public Object getItem(int arg0) {
        return listdata.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(context).inflate(
                    R.layout.feedback_imgfile_adapter, null);
            holder.photo_imgview = (ImageView) view
                    .findViewById(R.id.filephoto_imgview);
            holder.filecount_textview = (TextView) view
                    .findViewById(R.id.filecount_textview);
            holder.filename_textView = (TextView) view
                    .findViewById(R.id.filename_textview);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.filename_textView.setText(listdata.get(position).get(filename));
        holder.filecount_textview
                .setText(listdata.get(position).get(filecount));

        holder.photo_imgview.setImageBitmap(defauteBitmap);

        String path = listdata.get(position).get(imgpath);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(path,
                holder.photo_imgview);

        // loadBitmapUtil.imgExcute(holder.photo_imgview, new ImgCallBack() {
        // @Override
        // public void resultImgCall(ImageView imageView, Bitmap bitmap) {
        // if (bitmap != null && !bitmap.isRecycled()) {
        // imageView.setImageBitmap(bitmap);
        // }
        // }
        // }, listdata.get(position).get(imgpath));

        return view;
    }

    class Holder {
        public ImageView photo_imgview;
        public TextView filecount_textview;
        public TextView filename_textView;
    }

}
