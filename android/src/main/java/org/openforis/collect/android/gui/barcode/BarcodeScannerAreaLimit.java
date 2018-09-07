package org.openforis.collect.android.gui.barcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BarcodeScannerAreaLimit extends View {

    private Rect croppingRect;

    public BarcodeScannerAreaLimit(Context context) {
        this(context, null);
    }

    public BarcodeScannerAreaLimit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (croppingRect != null) {
            //draw a rectangle for the scanning area
            Paint rectPaint = new Paint();
            rectPaint.setColor(Color.YELLOW);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);
            canvas.drawRect(croppingRect, rectPaint);

            //draw a red line in the middle
            Paint linePaint = new Paint();
            linePaint.setColor(Color.RED);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(2.0f);

            float halfHeight = croppingRect.height() / 2;

            canvas.drawLine(croppingRect.left, croppingRect.top + halfHeight,
                    croppingRect.right, croppingRect.top + halfHeight,
                    linePaint);
        }
    }

    public void setCroppingRect(Rect croppingRect) {
        this.croppingRect = croppingRect;
    }
}
