package github.myazusa.qiangdandan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import github.myazusa.service.ImageRecognition;

@RunWith(JUnit4.class)
public class RecognizeTest {
    static {
        if (!OpenCVLoader.initLocal()) {
            Log.w("TEST", "opencv加载失败");
        }else {
            Log.w("TEST", "opencv加载成功");
        }
    }
    @Test
    public void recognizeButtonText() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("github.myazusa.qiangdandan", appContext.getPackageName());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap jiedanSrceenshot = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.jietan_test,options);
        Point point = ImageRecognition.recognizeButton(appContext, jiedanSrceenshot, R.drawable.jiedan_button);
        Log.i("TEST","坐标为："+point);
        //saveBitmapToFile(appContext,drawRedCircleOnBitmap(jiedanSrceenshot,point), "modified_image.png");
    }
    private void saveBitmapToFile(Context context,Bitmap bitmap, String fileName) {
        // 获取项目根目录路径
        String root = context.getExternalFilesDir(null).toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // 创建文件
        File file = new File(myDir, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d("ImageSave", "Image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}