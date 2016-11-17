package com.hzj.pickup.browse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.hzj.pickup.R;

import java.util.ArrayList;

/**
 * picture browse
 *
 * @author huangzj
 */
public class BrowseImageActivity extends Activity {

    public static final String IMAGE_INDEX = "image_index";
    public static final String IMAGE_URLS = "image_urls";

    public static final String CURRENT_POSITION = "current_position";

    GalleryViewPager mViewPager;
    TextView imageCount;

    private int imageLength;

    private int currentPosition;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_image);
        int pagerPosition = getIntent().getIntExtra(IMAGE_INDEX, 0);
        ArrayList<String> picturePaths = getIntent().getStringArrayListExtra(IMAGE_URLS);

        imageLength = picturePaths.size();

        imageCount = (TextView) findViewById(R.id.image_count);

        BasePagerAdapter pagerAdapter = new BasePagerAdapter(this, picturePaths);// picture path

        pagerAdapter.setOnItemChangeListener(new BasePagerAdapter.OnItemChangeListener() {
            @Override
            public void onItemChange(int index) {
                // update progress
                currentPosition = index;
                imageCount.setText((currentPosition + 1) + "/" + imageLength);
            }
        });

        mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(pagerPosition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            setResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setResult() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_POSITION, currentPosition);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

}
