package github.myazusa.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import github.myazusa.collection.FixedSizeQueue;
import github.myazusa.config.ApplicationConfig;

public class ImageRecognition {
    private final static String _TAG = ImageRecognition.class.getName();
    private static Bitmap buttonImage = null;
    private static Mat buttonMatGray = null;
    public static long maxElapsedTime = 0;
    public static long minElapsedTime = 1000000;
    private static Boolean autoScaled = Boolean.valueOf(ApplicationConfig.getInstance()
            .getPreferences().getBoolean("autoScaled",true));
    public static FixedSizeQueue<Long> elapsedTimeQueue = new FixedSizeQueue<>(Stream.of(200L, 300L, 400L, 500L, 600L).collect(Collectors.toSet()));;

    /**
     * 匹配方法，一般耗时200到400ms，因此请设置大于这个值
     * @param screenShot 截屏获得的图片
     * @return 匹配到图像位置的左上角坐标值
     */

    private static Point findButtonInBitmap(Bitmap screenShot) {
        long startTime = System.currentTimeMillis();

        Log.d(_TAG,"原始宽是"+screenShot.getWidth()+"，原始高是"+screenShot.getHeight());

        // 转换为opencv的mat对象
        Mat screenShotMat = new Mat();
        Utils.bitmapToMat(screenShot, screenShotMat);

        // 对图像进行灰度处理
        Mat screenShotMatGray = new Mat();
        Imgproc.cvtColor(screenShotMat, screenShotMatGray, Imgproc.COLOR_BGR2GRAY);

        // 定义缩放比例范围
        double minScale = 0.7;
        double maxScale = 1.3;
        double scaleStep = 0.1;
        double bestMatchValue = 0;
        Point bestMatchLoc = null;
        double bestScale = 0.0;

        // 遍历不同的缩放比例
        for (double scale = minScale; scale <= maxScale; scale += scaleStep) {
            // 调整模板的尺寸
            Size newSize = new Size(buttonMatGray.cols() * scale, buttonMatGray.rows() * scale);
            Mat scaledButtonMatGray = new Mat();
            Imgproc.resize(buttonMatGray, scaledButtonMatGray, newSize);

            // 检查缩放后的模板是否比目标图像大
            if (scaledButtonMatGray.cols() > screenShotMatGray.cols() || scaledButtonMatGray.rows() > screenShotMatGray.rows()) {
                continue;
            }

            // 创建结果矩阵并进行模板匹配
            int resultCols = screenShotMatGray.cols() - scaledButtonMatGray.cols() + 1;
            int resultRows = screenShotMatGray.rows() - scaledButtonMatGray.rows() + 1;

            Mat result = new Mat(resultRows, resultCols, screenShotMatGray.type());
            Imgproc.matchTemplate(screenShotMatGray, scaledButtonMatGray, result, Imgproc.TM_CCOEFF_NORMED);

            // 获取匹配结果
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            if (mmr.maxVal > bestMatchValue && mmr.maxVal >= 0.75) {
                bestScale = scale;
                bestMatchValue = mmr.maxVal;
                bestMatchLoc = mmr.maxLoc;
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        if(elapsedTime > maxElapsedTime){
            maxElapsedTime = elapsedTime;
        }
        if(elapsedTime < minElapsedTime){
            minElapsedTime = elapsedTime;
        }
        elapsedTimeQueue.add(elapsedTime);

        // 原本计算得到的bestMatchLoc是左上角值，要转换为中央值
        Point buttonCenter = null;
        if (bestMatchLoc != null) {
            Log.d(_TAG,"完美匹配到图像，缩放大小:" + bestScale +"，耗时（毫秒）：" + elapsedTime);
            buttonCenter = new Point(
                    bestMatchLoc.x + buttonImage.getWidth() * bestScale / 2.0,
                    bestMatchLoc.y + buttonImage.getHeight() * bestScale / 2.0
            );
            Log.d(_TAG, "位于屏幕：" + buttonCenter + "。原坐标为："+ bestMatchLoc);
        } else {
            Log.d(_TAG,"匹配度不足，没有找到，耗时（毫秒）：" + elapsedTime);
        }
        return buttonCenter;
    }

    /**
     * 匹配图片方法
     * @param context 程序的上下文
     * @param screenShot 截屏获得的图片
     * @param image 要匹配的图片
     * @return 匹配到图片所在的中心坐标
     */
    public static Point recognizeButton(Context context,Bitmap screenShot ,int image) {
        if(buttonMatGray == null || buttonImage == null){
            // 预处理button图片
            buttonImage = getBitmapFromResource(context,image);
            Mat buttonMat = new Mat();
            Utils.bitmapToMat(buttonImage, buttonMat);
            buttonMatGray = new Mat();
            Imgproc.cvtColor(buttonMat, buttonMatGray, Imgproc.COLOR_BGR2GRAY);
        }
        return findButtonInBitmap(screenShot);
    }

    private static Bitmap getBitmapFromResource(Context context,int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 开启自适应屏幕dpi缩放
        options.inScaled = autoScaled;
        return BitmapFactory.decodeResource(context.getResources(), resId,options);
    }
}
