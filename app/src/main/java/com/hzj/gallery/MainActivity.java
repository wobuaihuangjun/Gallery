package com.hzj.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.hzj.pickup.browse.BrowseImageActivity;
import com.hzj.pickup.pickup.ImgFileListActivity;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ArrayList<String> dataList;
    PictureAdapter pictureAdapter;
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
                intent.putExtra(ImgFileListActivity.MAX_SIZE, 3);
                intent.setClass(MainActivity.this, ImgFileListActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        dataList = new ArrayList<>();

        imgGridView = (GridView) findViewById(R.id.all_picture_gridView);
        imgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageBrowes(position);
            }
        });

        pictureAdapter = new PictureAdapter(this, dataList);
        imgGridView.setAdapter(pictureAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            Bundle bundle = data.getExtras();
            if (bundle != null
                    && bundle.getStringArrayList("files") != null) {
                ArrayList<String> temp = bundle.getStringArrayList("files");
                if (temp != null) {
                    dataList.addAll(temp);
                }
                pictureAdapter.notifyDataSetChanged();
            }
        }
    }

    protected void imageBrowes(int position) {
        Intent intent = new Intent(this, BrowseImageActivity.class);
        // 图片url,一般从数据库中或网络中获取
        intent.putExtra(BrowseImageActivity.IMAGE_URLS, dataList);
        intent.putExtra(BrowseImageActivity.IMAGE_INDEX, position);
        startActivity(intent);
    }
}
