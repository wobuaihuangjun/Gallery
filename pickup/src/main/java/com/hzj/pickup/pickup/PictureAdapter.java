package com.hzj.pickup.pickup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.hzj.pickup.ImageLoader;
import com.hzj.pickup.LoadResourceImage;
import com.hzj.pickup.R;

import java.util.List;

/**
 * 本地图库多选数据适配器
 *
 * @author huangzj
 */
public class PictureAdapter extends BaseAdapter {

    private Context context;
    private List<String> data;
    private Bitmap defauteBitmap;
    // private LoadBitmapUtil loadBitmapUtil;
    private OnItemClickClass onItemClickClass;

    // private int index = -1;
    // private List<View> holderlist;
    // private Bitmap bitmaps[];
    private boolean[] seletedFlag;

    public PictureAdapter(Context context, List<String> data,
                          OnItemClickClass onItemClickClass) {
        this.context = context;
        this.data = data;
        this.onItemClickClass = onItemClickClass;
        // loadBitmapUtil = new LoadBitmapUtil(context);
        defauteBitmap = LoadResourceImage.getInstance().getBitmap(context,
                R.drawable.load);
        seletedFlag = new boolean[data.size()];
        // BitmapFactory.decodeResource(context.getResources(),
        // R.drawable.load);
        // holderlist = new ArrayList<View>();
        // bitmaps = new Bitmap[data.size()];
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

    // public void setIndex(int index) {
    // this.index = index;
    // }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup arg2) {

        Holder holder;
        // if (position != index && position > index) {
        if (view == null) {
            // index = position;
            view = LayoutInflater.from(context).inflate(
                    R.layout.feedback_imgs_item, null);
            holder = new Holder();
            holder.imageView = (ImageView) view.findViewById(R.id.imageView1);
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
            view.setTag(holder);
            // holderlist.add(view);
        } else {
            // view = holderlist.get(position);
            holder = (Holder) view.getTag();
        }

        holder.imageView.setImageBitmap(defauteBitmap);

        String path = data.get(position);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(path, holder.imageView);

        // String imageUrl = Scheme.FILE.wrap(data.get(position));
        // FeedBackUtils.imageLoader(holder.imageView, imageUrl);

        // if (bitmaps[position] == null) {
        // loadBitmapUtil.imgExcute(holder.imageView, new ImgClallBackLisner(
        // position), data.get(position));
        // } else {
        // holder.imageView.setImageBitmap(bitmaps[position]);
        // }
        // if (checkList[position]) {
        // holder.imageView.setColorFilter(Color.parseColor("#77000000"));
        // holder.checkBox.setChecked(true);
        // } else {
        // holder.imageView.setColorFilter(null);
        // holder.checkBox.setChecked(false);
        //
        // }
        if (seletedFlag[position]) {
            holder.imageView.setColorFilter(Color.parseColor("#77000000"));
            holder.checkBox.setChecked(true);
        } else {
            holder.imageView.setColorFilter(null);
            holder.checkBox.setChecked(false);
        }

        view.setOnClickListener(new OnPhotoClick(position, holder));
        return view;
    }

    private static class Holder {
        ImageView imageView;
        CheckBox checkBox;
    }

    // public class ImgClallBackLisner implements ImgCallBack {
    //
    // Integer num;
    //
    // public ImgClallBackLisner(Integer num) {
    // this.num = num;
    // }
    //
    // @Override
    // public void resultImgCall(ImageView imageView, Bitmap bitmap) {
    //
    // if (bitmap != null && !bitmap.isRecycled()) {
    // imageView.setImageBitmap(bitmap);
    // }
    // }
    // }

    private int maxSize;
    private int selectedSize;

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

    public interface OnItemClickClass {
        void OnItemClick(int Position, boolean isCheck);
    }

    private class OnPhotoClick implements OnClickListener {
        int position;
        Holder holder;

        public OnPhotoClick(int position, Holder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {

            if (holder.checkBox.isChecked()) {
                holder.imageView.setColorFilter(null);
                holder.checkBox.setChecked(false);
                seletedFlag[position] = false;
            } else {
                if (selectedSize >= maxSize) {
                    Toast.makeText(context, "max size", Toast.LENGTH_LONG).show();
                    return;
                }
                holder.imageView.setColorFilter(Color.parseColor("#77000000"));
                holder.checkBox.setChecked(true);
                seletedFlag[position] = true;
            }

            if (data != null && onItemClickClass != null) {
                onItemClickClass.OnItemClick(position, holder.checkBox.isChecked());
            }
        }
    }

}
