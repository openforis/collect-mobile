package org.openforis.collect.android.gui.barcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BarcodeScannerAreaLimit extends View {

    private Rect rect;

    public BarcodeScannerAreaLimit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int topBorder = Math.round((float) (canvas.getHeight() * 0.4));
        int bottomBorder = Math.round((float) (canvas.getHeight() * 0.4));
        int leftBorder = Math.round((float) (canvas.getWidth() * 0.1));
        int rightBorder = leftBorder;

        //draw a rectangle for the scanning area
        Rect rect = new Rect(leftBorder, topBorder, canvas.getWidth() - rightBorder, canvas.getHeight() - bottomBorder);
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.YELLOW);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.0f);
        canvas.drawRect(rect, rectPaint);
        this.rect = rect;

        //draw a red line in the middle
        Paint linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.0f);
        canvas.drawLine(leftBorder, topBorder + rect.height() / 2,
                canvas.getWidth() - rightBorder, topBorder + rect.height() / 2,
                linePaint);
    }

    public Rect getRect() {
        return rect;
    }
}
