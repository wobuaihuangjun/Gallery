package com.hzj.gallery.browse;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 我的反馈图片浏览组件
 * <p>
 * Class wraps URLs to adapter, then it instantiates <b>UrlTouchImageView</b>
 * objects to paging up through them.
 *
 * @author huangzj
 */
public class BasePagerAdapter extends PagerAdapter {

    protected final List<String> mResources;
    protected final Context mContext;
    protected int mCurrentPosition = -1;
    protected OnItemChangeListener mOnItemChangeListener;

    public BasePagerAdapter() {
        mResources = null;
        mContext = null;
    }

    public BasePagerAdapter(Context context, List<String> resources) {
        this.mResources = resources;
        this.mContext = context;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, final int position,
                               Object object) {
        super.setPrimaryItem(container, position, object);
        if (mCurrentPosition == position)
            return;
        GalleryViewPager galleryContainer = ((GalleryViewPager) container);
        if (galleryContainer.mCurrentView != null) {
            galleryContainer.mCurrentView.resetScale();
        }
        mCurrentPosition = position;
        if (mOnItemChangeListener != null)
            mOnItemChangeListener.onItemChange(mCurrentPosition);

        ((GalleryViewPager) container).mCurrentView = ((UrlTouchImageView) object)
                .getImageView();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final UrlTouchImageView iv = new UrlTouchImageView(mContext);
        iv.setUrl(mResources.get(position));
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        collection.addView(iv, 0);
        return iv;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void finishUpdate(ViewGroup arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(ViewGroup arg0) {
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setOnItemChangeListener(OnItemChangeListener listener) {
        mOnItemChangeListener = listener;
    }

    public static interface OnItemChangeListener {
        public void onItemChange(int currentPosition);
    }
};