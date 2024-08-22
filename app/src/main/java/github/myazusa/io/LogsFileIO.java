package github.myazusa.io;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogsFileIO extends AbstractFileIO{
    private static final String TAG = LogsFileIO.class.getName();
    public static void writeLogsToExternal(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            File externalFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "qiangdandan_logs_"+ simpleDateFormat.format(date) + ".log");

            StringBuilder logBuilder = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("logcat -d *:E");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                Log.i(TAG,"获取日志失败");
                e.printStackTrace();
            }

            try( FileOutputStream fos = new FileOutputStream(externalFile)) {
                fos.write(logBuilder.toString().getBytes());
                Log.i(TAG,"写入文件成功");
            } catch (IOException e) {
                Log.i(TAG,"写入文件失败");
                e.printStackTrace();
            }
        }
    }
    public static String readLogsToView(Context context){
        // 如果文件夹为空，就返回null
        if (isExternalStorageFolderEmpty(context,Environment.DIRECTORY_DOCUMENTS)){
            return null;
        }
        File folder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(folder == null){
            return null;
        }

        // 找到修改时间更晚的文件
        File[] files = folder.listFiles();
        File latestFile = files[0];
        for (File file : files) {
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
        }

        StringBuilder logBuilder = new StringBuilder();
        try( BufferedReader reader = new BufferedReader(new FileReader(latestFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logBuilder.append(line).append("\n");
            }
            reader.close();
            Log.i(TAG,"读取文件成功");
        } catch (IOException e) {
            Log.i(TAG,"读取文件失败");
            e.printStackTrace();
            return null;
        }
        return logBuilder.toString();
    }
}
