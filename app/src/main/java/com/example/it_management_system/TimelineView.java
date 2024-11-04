package com.example.it_management_system;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

public class TimelineView extends View {

    private Paint linePaint;
    private Paint circlePaint;
    private int position;
    private int itemCount;

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.green));
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint();
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.orange));
        circlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw vertical line
        canvas.drawLine(10, 0, 10, getHeight(), linePaint);

        // Draw circle
        canvas.drawCircle(10, getHeight() / 2, 10, circlePaint);
    }

    public void setPosition(int position, int itemCount) {
        this.position = position;
        this.itemCount = itemCount;
        invalidate();
    }
}