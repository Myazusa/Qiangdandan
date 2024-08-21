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
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import github.myazusa.service.ImageRecognition;

@RunWith(RobolectricTestRunner.class)
public class RecognizeTest {
    static {
        System.loadLibrary("libopencv_java4.so");
    }
    @Test
    public void recognizeButtonText() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("github.myazusa.qiangdandan", appContext.getPackageName());
        Bitmap jiedanSrceenshot = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.jietan_test);
        Point point = ImageRecognition.recognizeButton(appContext, jiedanSrceenshot, R.drawable.jiedan_button);
        Log.i("TEST","坐标为："+point);

        saveBitmapToFile(appContext,drawRedCircleOnBitmap(jiedanSrceenshot,point), "modified_image.png");
    }
    private Bitmap drawRedCircleOnBitmap(Bitmap bitmap, Point matchLoc) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // 创建画布和画笔
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        int radius = 20;
        canvas.drawCircle((float) matchLoc.x, (float) matchLoc.y, radius, paint);
//        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
//        ImageView imageView = new ImageView(activity);
//        imageView.setImageBitmap(drawRedCircleOnBitmap(bitmap,matchLoc));
//        assertNotNull(imageView.getDrawable());
        return mutableBitmap;
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