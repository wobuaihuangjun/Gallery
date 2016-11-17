package com.hzj.gallery.pickup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.hzj.gallery.ImageLoader;
import com.hzj.gallery.R;
import com.hzj.gallery.browse.BrowseImageActivity;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * picture select gallery
 *
 * @author huangzj
 */
public class PictureActivity extends Activity {

    public static final String TAG = "PictureActivity";

    public static final String SELECT_PICTURE_PATH = "select_picture_path";

    private static final int BROWSE_INTENT = 100;

    private FileTraversal fileTraversal;
    private PictureAdapter pictureAdapter;

    private ArrayList<String> picturePath;
    private ArrayList<String> selectPictureList;

    private TextView sureTv;
    private GridView imgGridView;

    /**
     * 可选择数量
     */
    private int listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_grally);

        initialize();
    }

    private void initialize() {
        initWidget();
        initData();
    }

    private void initWidget() {
        imgGridView = (GridView) findViewById(R.id.gridView1);

        sureTv = (TextView) findViewById(R.id.sure);
        sureTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFiles();
            }
        });
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        listSize = getIntent().getIntExtra(PictureFolderActivity.MAX_SIZE, PictureFolderActivity.DEFAULT_MAX_SIZE);
        fileTraversal = bundle.getParcelable(PictureFolderActivity.FOLDER_PICTURE_PATH);
        if (fileTraversal != null) {
            picturePath = fileTraversal.fileContent;
        } else {
            picturePath = new ArrayList<>();
        }
        pictureAdapter = new PictureAdapter(this, picturePath, onItemClickListener);
        pictureAdapter.setMaxSize(listSize);
        imgGridView.setAdapter(pictureAdapter);
        selectPictureList = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        ImageLoader.getInstance().clearCache();
        super.onDestroy();
    }

    PictureAdapter.OnItemClickListener onItemClickListener = new PictureAdapter.OnItemClickListener() {
        @Override
        public void OnItemCheckedChanged(int Position, boolean isCheck) {
            String filePath = fileTraversal.fileContent.get(Position);
            if (!isCheck) {
                selectPictureList.remove(filePath);
                int size = selectPictureList.size();
                if (size > 0) {
                    sureTv.setText("Sure(" + size + "/" + listSize + ")");
                } else {
                    sureTv.setText("Sure");
                }
            } else {
                Timber.i("img select position->" + Position);
                selectPictureList.add(filePath);
                sureTv.setText("Sure(" + selectPictureList.size() + "/" + listSize + ")");
            }
            pictureAdapter.setSelectedSize(selectPictureList.size());
        }

        @Override
        public void onItemClick(int position) {
            imageBrowse(position);
        }
    };

    protected void imageBrowse(int position) {
        Intent intent = new Intent(this, BrowseImageActivity.class);
        // 图片url,一般从数据库中或网络中获取
        intent.putExtra(BrowseImageActivity.IMAGE_URLS, picturePath);
        intent.putExtra(BrowseImageActivity.IMAGE_INDEX, position);
        startActivityForResult(intent, BROWSE_INTENT);
    }

    public void sendFiles() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(SELECT_PICTURE_PATH, selectPictureList);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK == resultCode && requestCode == BROWSE_INTENT) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int selectPosition = bundle.getInt(BrowseImageActivity.CURRENT_POSITION);
                imgGridView.setSelection(selectPosition);
            }
        }
    }
}
