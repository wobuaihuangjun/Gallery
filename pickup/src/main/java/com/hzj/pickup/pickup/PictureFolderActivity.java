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
public class PictureFolderActivity extends Activity implements OnItemClickListener {

    public static final String TAG = "PictureFolderActivity";

    public static final String MAX_SIZE = "max_size";
    public static final int DEFAULT_MAX_SIZE = 3;

    public static final String FOLDER_PICTURE_PATH = "folder_picture_path";

    private ListView listView;
    private TextView friendlyTip;

    private PictureFolderAdapter listAdapter;
    private List<FileTraversal> localList;

    /**
     * 可选择数量
     */
    private int listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_folder);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void initialize() {
        initValue();
        initWidget();
        initData();
    }

    private void initData() {
        List<HashMap<String, String>> listData = new ArrayList<>();
        if (localList != null && localList.size() > 0) {
            for (int i = 0; i < localList.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(PictureFolderAdapter.FILE_COUNT, localList.get(i).fileContent.size() + "");
                map.put(PictureFolderAdapter.PICTURE_PATH,
                        localList.get(i).fileContent.get(0) == null ? null
                                : (localList.get(i).fileContent.get(0)));// 文件夹中的第一张图片
                map.put(PictureFolderAdapter.FILE_NAME, localList.get(i).fileName);
                listData.add(map);
            }
            friendlyTip.setVisibility(View.GONE);
        } else {
            friendlyTip.setVisibility(View.VISIBLE);
        }
        listAdapter = new PictureFolderAdapter(this, listData);
        listView.setAdapter(listAdapter);
    }

    private void initWidget() {
        friendlyTip = (TextView) findViewById(R.id.friendly_tip);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
    }

    private void initValue() {
        listSize = getIntent().getIntExtra(MAX_SIZE, DEFAULT_MAX_SIZE);
        Util util = new Util(this);
        localList = util.LocalImgFileList();
//        localList = splitLargeFolder(localList, 200);
    }

    /**
     * 拆分大文件夹
     */
    private List<FileTraversal> splitLargeFolder(List<FileTraversal> fileTraversals, int maxSize) {
        List<FileTraversal> list = new ArrayList<>();
        for (int i = 0; i < fileTraversals.size(); i++) {
            FileTraversal temp = fileTraversals.get(i);
            String name = temp.fileName;
            List<String> content = temp.fileContent;

            int size = content.size();
            int j = 0;
            while (j * maxSize < size) {
                FileTraversal a1 = new FileTraversal();

                if (j != 0) {
                    a1.fileName = name + "(" + j + ")";
                } else {
                    a1.fileName = name;
                }

                for (int x = 0; (x < maxSize && x + (j * maxSize) < size); x++) {
                    a1.fileContent.add(content.get(x + (j * maxSize)));
                }
                list.add(a1);
                j++;
            }
        }
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, PictureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(FOLDER_PICTURE_PATH, localList.get(arg2));
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
