package com.hzj.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.hzj.gallery.browse.BrowseImageActivity;
import com.hzj.gallery.pickup.PictureFolderActivity;
import com.hzj.gallery.pickup.PictureActivity;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int PICK_UP_INTENT = 100;

    ArrayList<String> dataList;
    SelectResultAdapter selectResultAdapter;
    GridView imgGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.all_picture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(PictureFolderActivity.MAX_SIZE, 3);
                intent.setClass(MainActivity.this, PictureFolderActivity.class);
                startActivityForResult(intent, PICK_UP_INTENT);
            }
        });

        dataList = new ArrayList<>();

        imgGridView = (GridView) findViewById(R.id.all_picture_gridView);
        imgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageBrowse(position);
            }
        });

        selectResultAdapter = new SelectResultAdapter(this, dataList);
        imgGridView.setAdapter(selectResultAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_UP_INTENT) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                ArrayList<String> temp = bundle.getStringArrayList(PictureActivity.SELECT_PICTURE_PATH);
                if (temp != null) {
                    dataList.addAll(temp);
                }
                selectResultAdapter.notifyDataSetChanged();
            }
        }
    }

    protected void imageBrowse(int position) {
        Intent intent = new Intent(this, BrowseImageActivity.class);
        // 图片url,一般从数据库中或网络中获取
        intent.putExtra(BrowseImageActivity.IMAGE_URLS, dataList);
        intent.putExtra(BrowseImageActivity.IMAGE_INDEX, position);
        startActivity(intent);
    }
}
