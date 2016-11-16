package com.hzj.pickup.pickup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.hzj.pickup.R;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * 系统图片多选界面
 *
 * @author huangzj
 */
public class ImgsActivity extends Activity {

    private static final String TAG = "ImgsActivity";

    private FileTraversal fileTraversal;
    private ImgsAdapter imgsAdapter;
    private ArrayList<String> fileList;

    private TextView sureTv;

    /**
     * 可选择数量
     */
    private int listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_photo_grally);

        listSize = getIntent().getIntExtra(ImgFileListActivity.MAX_SIZE, ImgFileListActivity.DEFAUT_MAX_SIZE);

        GridView imgGridView = (GridView) findViewById(R.id.gridView1);

        sureTv = (TextView) findViewById(R.id.sure);
        sureTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFiles();
            }
        });

        Bundle bundle = getIntent().getExtras();
        fileTraversal = bundle.getParcelable("data");
        imgsAdapter = new ImgsAdapter(this, fileTraversal.filecontent,
                onItemClickClass);
        imgsAdapter.setmaxSize(listSize);
        imgGridView.setAdapter(imgsAdapter);
        fileList = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        // ImageLoader.getInstance().clearCache();
        super.onDestroy();
    }

    ImgsAdapter.OnItemClickClass onItemClickClass = new ImgsAdapter.OnItemClickClass() {
        @Override
        public void OnItemClick(int Position, boolean isCheck) {
            String filePath = fileTraversal.filecontent.get(Position);
            if (!isCheck) {
                fileList.remove(filePath);
                sureTv.setText("确定(" + fileList.size() + "/" + listSize + ")");
            } else {
                try {
                    Timber.i("img choise position->" + Position);
                    fileList.add(filePath);
                    sureTv.setText("确定(" + fileList.size() + "/" + listSize + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            imgsAdapter.setSelectedSize(fileList.size());
        }
    };

    /**
     * FIXME 只需要在这个方法把选中的文档目录以list的形式传过去即可
     */
    public void sendFiles() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("files", fileList);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

}
