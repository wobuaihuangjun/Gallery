package com.hzj.pickup.browse;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.hzj.pickup.R;

import java.util.ArrayList;

/**
 * 图片浏览
 *
 * @author huangzj
 */
public class BrowseImageActivity extends Activity {

    public static final String IMAGE_INDEX = "image_index";
    public static final String IMAGE_URLS = "image_urls";

    GalleryViewPager mViewPager;
    TextView imageCount;

    private int imageLength;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_browse_image);
        int pagerPosition = getIntent().getIntExtra(IMAGE_INDEX, 0);
        ArrayList<String> keyList = getIntent().getStringArrayListExtra(IMAGE_URLS);

        imageLength = keyList.size();

        imageCount = (TextView) findViewById(R.id.image_count);

        // BasePagerAdapter pagerAdapter = new BasePagerAdapter(this,
        // keyToPath(keyList));// 传本地路径
        BasePagerAdapter pagerAdapter = new BasePagerAdapter(this, keyList);// 传图片key

        pagerAdapter.setOnItemChangeListener(new BasePagerAdapter.OnItemChangeListener() {
            @Override
            public void onItemChange(int currentPosition) {
                // 更新图片显示进度
                imageCount.setText((currentPosition + 1) + "/" + imageLength);
            }
        });

        mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(pagerPosition);
    }

}
