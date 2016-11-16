package com.hzj.pickup.pickup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.hzj.pickup.ImageLoader;
import com.hzj.pickup.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 本地图片文件夹列表界面
 *
 * @author huangzj
 */
public class ImgFileListActivity extends Activity implements OnItemClickListener {

    public static final String TAG = "ImgFileListActivity";

    public static final String MAX_SIZE = "max_size";
    public static final int DEFAUT_MAX_SIZE = 3;

    private ListView listView;
    private TextView friendlyTip;

    private ImgFileListAdapter listAdapter;
    private List<FileTraversal> localList;

    /**
     * 可选择数量
     */
    private int listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_imgfile_list);
        initIalize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void initIalize() {
        initValue();
        initWidget();
        initData();
    }

    private void initData() {
        List<HashMap<String, String>> listData = new ArrayList<>();
        if (localList != null && localList.size() > 0) {
            for (int i = 0; i < localList.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("filecount", localList.get(i).filecontent.size() + "张");
                map.put("imgpath",
                        localList.get(i).filecontent.get(0) == null ? null
                                : (localList.get(i).filecontent.get(0)));// 文件夹中的第一张图片
                map.put("filename", localList.get(i).filename);
                listData.add(map);
            }
            friendlyTip.setVisibility(View.GONE);
        } else {
            friendlyTip.setVisibility(View.VISIBLE);
        }
        listAdapter = new ImgFileListAdapter(this, listData);
        listView.setAdapter(listAdapter);
    }

    private void initWidget() {
        friendlyTip = (TextView) findViewById(R.id.friendly_tip);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
    }

    private void initValue() {
        listSize = getIntent().getIntExtra(MAX_SIZE, DEFAUT_MAX_SIZE);
        Util util = new Util(this);
        localList = util.LocalImgFileList();
        // localList = splitLargeFolder();
    }

    // /**
    // * 拆分大文件夹
    // *
    // * @return
    // */
    // private List<FileTraversal> splitLargeFolder() {
    //
    // int maxSize = 200;// 文件夹最大容量
    //
    // List<FileTraversal> list = new ArrayList<FileTraversal>();
    //
    // for (int i = 0; i < localList.size(); i++) {
    //
    // FileTraversal temp = localList.get(i);
    // String name = temp.filename;
    // List<String> content = temp.filecontent;
    //
    // int size = content.size();
    //
    // int j = 0;
    // while (j * maxSize < size) {
    // FileTraversal a1 = new FileTraversal();
    //
    // if (j != 0) {
    // a1.filename = name + "(" + j + ")";
    // } else {
    // a1.filename = name;
    // }
    //
    // for (int x = 0; (x < maxSize && x + (j * maxSize) < size); x++) {
    // a1.filecontent.add(content.get(x + (j * maxSize)));
    // }
    // list.add(a1);
    // j++;
    //
    // }
    //
    // }
    // return list;
    // }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, ImgsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", localList.get(arg2));
        intent.putExtra(MAX_SIZE, listSize);
        intent.putExtras(bundle);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            Bundle bundle = data.getExtras();
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearCache();
        // BitmapUtil.releaseAll();
    }
}
