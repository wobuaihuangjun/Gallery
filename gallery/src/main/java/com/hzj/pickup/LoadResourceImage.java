package com.hzj.pickup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import timber.log.Timber;

/***
 * 加载res文件夹中的图片
 *
 * @author huangzj
 */
public class LoadResourceImage {

    /**
     * 图片缓存的核心类
     */
    private LruCache<String, Bitmap> mLruCache;

    private static LoadResourceImage mInstance;

    private LoadResourceImage() {
        setUseMemorySize();
    }

    public static LoadResourceImage getInstance() {

        if (mInstance == null) {
            synchronized (LoadResourceImage.class) {
                if (mInstance == null) {
                    mInstance = new LoadResourceImage();
                }
            }
        }
        return mInstance;
    }

    private void setUseMemorySize() {
        /** 获取应用程序最大可用内存 */
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }

            ;
        };
    }

    /***
     * 加载资源文件中的Bitmap图片
     *
     * @param context
     * @param resourceId
     * @return
     */
    public Bitmap getBitmap(Context context, int resourceId) {
        Bitmap bitmap = null;

        try {
            bitmap = getBitmapFromCache(resourceId + "");
            if (bitmap != null && !bitmap.isRecycled()) {
                return bitmap;
            }

            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    resourceId);
            if (bitmap != null) {
                mLruCache.put(resourceId + "", bitmap);
            }
        } catch (OutOfMemoryError err) {
            Timber.e("内存溢出la !");
            clearCache();
            System.gc();
            return getBitmap(context, resourceId);
        }
        return bitmap;
    }

    /**
     * 加载资源文件中的Drawable图片
     *
     * @param context
     * @param resourceId
     * @return
     */
    public Drawable getDrawable(Context context, int resourceId) {
        Bitmap bitmap = getBitmap(context, resourceId);
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    private Bitmap getBitmapFromCache(String resourceId) {
        Bitmap bitMap = null;
        bitMap = (Bitmap) mLruCache.get(resourceId);
        return bitMap;
    }

    public void clearCache() {
        if (mLruCache != null) {
            if (mLruCache.size() > 0) {
                mLruCache.evictAll();
            }
        }
        Timber.e("释放所有缓存");
        System.gc();
    }
}
