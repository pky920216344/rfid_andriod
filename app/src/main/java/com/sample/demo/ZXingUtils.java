package com.sample.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * 生成条形码和二维码的工具
 */
public class ZXingUtils {
    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createQRImage(String url, String text, int width, int height) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            if (!TextUtils.isEmpty(text)) {
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setTextAlign(Paint.Align.LEFT);
                //字符串宽度
                float textWidth = paint.measureText(text);
                canvas.drawText(text, (width - textWidth) / 2, height - 10, paint);
            }

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}