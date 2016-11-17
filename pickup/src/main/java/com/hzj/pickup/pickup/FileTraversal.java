package com.hzj.pickup.pickup;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件类
 *
 * @author huangzj
 */
@SuppressLint("ParcelCreator")
public class FileTraversal implements Parcelable {

    public String fileName;//所属图片的文件名称
    public ArrayList<String> fileContent = new ArrayList<>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeList(fileContent);
    }

    public static final Parcelable.Creator<FileTraversal> CREATOR = new Creator<FileTraversal>() {

        @Override
        public FileTraversal[] newArray(int size) {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public FileTraversal createFromParcel(Parcel source) {
            FileTraversal fileTraversal = new FileTraversal();
            fileTraversal.fileName = source.readString();
            fileTraversal.fileContent = source.readArrayList(FileTraversal.class.getClassLoader());

            return fileTraversal;
        }

    };
}
