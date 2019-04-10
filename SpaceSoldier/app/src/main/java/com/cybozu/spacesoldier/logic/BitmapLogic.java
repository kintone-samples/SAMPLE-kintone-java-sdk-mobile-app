package com.cybozu.spacesoldier.logic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.cybozu.spacesoldier.entities.BitmapScaleInfo;

public class BitmapLogic {
    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxWidth > 0 && maxHeight > 0) {
            BitmapScaleInfo resizeScale = getScaleSize(image, maxWidth, maxHeight);

            image = Bitmap.createScaledBitmap(image, resizeScale.getScaleWidth(), resizeScale.getScaleHeight(), true);
            return image;
        } else {
            return image;
        }
    }

    public static BitmapScaleInfo getScaleSize(Bitmap image, int maxWidth, int maxHeight) {
        BitmapScaleInfo scale = new BitmapScaleInfo();

        int targetWidth = image.getWidth();
        int targetHeight = image.getHeight();

        float targetAspectRatio = (float) targetWidth / (float) targetHeight;
        float maxAspectRatio = (float) maxWidth / (float) maxHeight;

        int fixWidth = maxWidth;
        int fixHeight = maxHeight;

        if (maxAspectRatio > 1) {
            fixWidth = (int) ((float) maxHeight * targetAspectRatio);
        } else if (maxAspectRatio == 1) {
            if (targetWidth < targetHeight) {
                fixWidth = (int) ((float) maxHeight * targetAspectRatio);
            } else {
                fixHeight = (int) ((float) maxWidth / targetAspectRatio);
            }
        } else {
            fixHeight = (int) ((float) maxWidth / targetAspectRatio);
        }

        scale.setScaleWidth(fixWidth);
        scale.setScaleHeight(fixHeight);
        return scale;
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectf = new RectF(0, 0, width, height);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.drawRoundRect(rectf, width / 7, height / 7, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;

    }
}
