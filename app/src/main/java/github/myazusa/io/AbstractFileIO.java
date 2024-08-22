package github.myazusa.io;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Optional;

public class AbstractFileIO {
    private static final String TAG = AbstractFileIO.class.getName();
    public static File[] getInternalStorageFiles(Context context){
        return context.getFilesDir().listFiles();
    }
    public static boolean isFileExists(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }
    public static boolean isFileEmpty(File file) {
        if (file.exists() && file.isFile()) {
            return file.length() == 0;
        }
        return true;
    }

    /**
     *
     * @param type 请传入属于Environment.DIRECTORY的常量
     * @return 目录不存在为true，否则为false
     */
    public static boolean isExternalStorageFolderEmpty(Context context,String type) {
        String state = Environment.getExternalStorageState();
        // 外部存储不可用返回空
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.w(TAG,"外部私有存储不可用");
            return true;
        }

        // 获取目录不存在返回空
        File folder = context.getExternalFilesDir(type);
        if(folder == null){
            return true;
        }


        if (folder.exists() && folder.isDirectory()) {
            // 获取该目录下的文件和子目录列表
            File[] files = folder.listFiles();

            // 判断文件夹是否为空
            if(files != null && files.length != 0){
                return false;
            }
        }
        return true;
    }
}
