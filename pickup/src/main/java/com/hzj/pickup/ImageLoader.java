package com.hzj.pickup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;


import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import timber.log.Timber;

/**
 * 加载本地图片，按控件大小获取缩略图
 *
 * @author huangzj
 */
public class ImageLoader {

    private static final byte[] LOCKED = new byte[0];

    /**
     * 此对象用来保持Bitmap的回收顺序,保证最后使用的图片被回收
     */
    private LinkedList<String> cahceEntries = new LinkedList<String>();
    /** 已经存在的图片加载线程，防止同一图片多次请求 */
    // private List<String> existTask = new ArrayList<String>();
    /**
     * 保存队列中正在处理的图片的key,有效防止重复添加到请求创建队列
     */
    private Set<String> existTask = new HashSet<String>();
    /**
     * 图片缓存的核心类
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 线程池的线程数量，默认为1
     */
    private static final int mThreadCount = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTasks;
    /**
     * 轮询的线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHander;

    /**
     * 运行在UI线程的handler，用于给ImageView设置图片
     */
    private Handler mHandler;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
     */
    private volatile Semaphore mSemaphore = new Semaphore(0);

    /**
     * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;

    private static ImageLoader mInstance;

    /**
     * 队列的调度方式
     */
    public enum Type {
        FIFO, LIFO
    }

    /**
     * 获取默认的启用单线程、采用后进先出的方式加载图片，图片加载单例对象
     */
    public static ImageLoader getInstance() {

        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(mThreadCount, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取图片加载单例对象
     *
     * @param threadCount 开启线程个数
     * @param type        加载方式。Type.LIFO后进先出，Type.FIFO先进先出
     * @return
     */
    public static ImageLoader getInstance(int threadCount, Type type) {

        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {
        // 轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                // 轮询执行
                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        setUseMemorySize();

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<Runnable>();
        mType = type == null ? Type.LIFO : type;
    }

    private void setUseMemorySize() {
        /** 获取应用程序最大可用内存 */
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        // long freeMemory = Runtime.getRuntime().freeMemory();
        // DLog.e("应用运行时最大内存=" + maxMemory / 1024 + " ,用于图片缓存的内存=" + cacheSize
        // / 1024 + " ,freeMemory=" + freeMemory / (1024 * 1024));

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }

            ;
        };
    }

    /**
     * 加载图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {

        if (imageView == null) {
            return;
        }

        // UI线程
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // 运行在主线程中，响应图片加载完成的消息，设置图片背景
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    if (imageView.getTag().toString().equals(path)
                            && bm != null && !bm.isRecycled()) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }

        // set tag
        imageView.setTag(path);

        final ImageSize imageSize = getImageViewWidth(imageView);
        final String key = createKey(path, imageSize.width, imageSize.height);

        Bitmap bm = getBitmapFromLruCache(key);
        if (bm != null) {
            // 图片已经存在缓存，发送完成的消息
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.bitmap = bm;
            holder.imageView = imageView;
            holder.path = path;
            Message message = Message.obtain();
            message.obj = holder;
            mHandler.sendMessage(message);
        } else {

            // 图片缓存不存在，判断任务队列是否已经存在，不存在，加入任务队列

            if (existTask(key)) {
                Timber.v("加载任务已经存在了");
                return;
            }
            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    int reqWidth = imageSize.width;
                    int reqHeight = imageSize.height;
                    // DLog.v("宽=" + reqWidth + "，高=" + reqHeight);

                    ImgBeanHolder holder = new ImgBeanHolder();
                    Bitmap bt = getBitmapFromSdcard(path, reqWidth, reqHeight);
                    if (bt != null && !bt.isRecycled()) {
                        holder.bitmap = bt;
                        holder.imageView = imageView;
                        holder.path = path;
                        Message message = Message.obtain();
                        message.obj = holder;
                        mHandler.sendMessage(message);

                        existTask.remove(key);
                    }

                    mPoolSemaphore.release();
                }
            };

            synchronized (LOCKED) {
                addTask(runnable);
                existTask.add(key);
            }

        }

    }

    /**
     * 判断任务是否已经存在
     */
    private boolean existTask(String key) {
        return existTask.contains(key);
    }

    /**
     * 添加一个任务
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        try {
            // 请求信号量，防止mPoolThreadHander为null
            if (mPoolThreadHander == null)
                mSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        mTasks.add(runnable);

        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    /**
     * 取出一个任务
     *
     * @return
     */
    private synchronized Runnable getTask() {
        try {
            if (mType == Type.FIFO) {
                return mTasks.removeFirst();
            } else if (mType == Type.LIFO) {
                return mTasks.removeLast();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     */
    private Bitmap getBitmapFromLruCache(String key) {
        Bitmap bitmap = null;
        synchronized (LOCKED) {
            bitmap = mLruCache.get(key);
            if (null != bitmap) {
                if (cahceEntries.remove(key)) {
                    cahceEntries.addFirst(key);
                }
            }
        }
        return bitmap;
    }

    /**
     * 往LruCache中添加一张图片
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromLruCache(key) == null) {
            if (bitmap != null)
                synchronized (LOCKED) {
                    mLruCache.put(key, bitmap);
                    cahceEntries.addFirst(key);
                }
        }
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

    /**
     * 创建键
     */
    private static String createKey(String path, int width, int height) {
        if (null == path || path.length() == 0) {
            return "";
        }
        return path + "_" + width + "_" + height;
    }

    /**
     * 获取图片，捕获内存溢出
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap getBitmapFromSdcard(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        try {
            String key = createKey(path, reqWidth, reqHeight);
            bitmap = decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
            addBitmapToLruCache(key, bitmap);
        } catch (OutOfMemoryError e) {
            Timber.e("内存溢出la !path=" + path);
            clearCache();
            // return getBitmapFromSdcard(path, reqWidth, reqHeight);
        }
        return bitmap;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        int degree = BitmapUtil.getExifOrientation(pathName);
        if (degree != 0) {
            bitmap = BitmapUtil.rotateBitmap(degree, bitmap);
        }
        return bitmap;
    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        int tmpWidth = options.outWidth;
        int tmpHeight = options.outHeight;
        int i = 0;
        while (true) {
            tmpWidth = tmpWidth >> i;
            tmpHeight = tmpHeight >> i;
            if ((tmpWidth <= reqWidth) && (tmpHeight <= reqHeight)) {
                // 计算缩放比例=2的i次方
                inSampleSize = (int) Math.pow(2.0D, i);
                break;
            }
            i += 1;
        }
        return inSampleSize;
    }

    /**
     * 根据ImageView获得适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        LayoutParams lp = imageView.getLayoutParams();

        int height = imageView.getHeight();// 获取imageview的实际高度
        if (height <= 0) {
            height = lp.height;// 获取imageview在layout中声明的宽度
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }

        int width = imageView.getWidth();// 获取imageview的实际宽度
        if (width <= 0) {
            width = lp.width;// 获取imageview在layout中声明的宽度
        }
        if (width <= 0) {
            // width = imageView.getMaxWidth();// 检查最大值
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {

            width = displayMetrics.widthPixels;
            if (width > height) {
                width = height;
            }
        }

        // if (imageSize.width > width) {
        imageSize.width = width;
        // }
        // if (imageSize.height > height) {
        imageSize.height = height;
        // }

        return imageSize;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
                Timber.e("控件宽或高的最大值=" + value);
            }
        } catch (Exception e) {
            Timber.e("getImageViewFieldValue" + e.toString());
        }
        return value;
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize {
        int width;
        int height;
    }

}
