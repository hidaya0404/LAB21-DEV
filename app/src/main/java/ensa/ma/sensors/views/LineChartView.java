package ensa.ma.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private final List<Float> values = new ArrayList<>();
    private final int maxPoints = 80;

    private Paint linePaint;
    private Paint axisPaint;
    private Paint textPaint;
    private Paint pointPaint;

    private String title = "Sensor values";
    private String unit = "";

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);

        axisPaint = new Paint();
        axisPaint.setAntiAlias(true);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(32f);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }

    public void addValue(float value) {
        if (values.size() >= maxPoints) {
            values.remove(0);
        }

        values.add(value);
        invalidate();
    }

    public void clearValues() {
        values.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = 70;
        int paddingRight = 30;
        int paddingTop = 70;
        int paddingBottom = 60;

        float graphLeft = paddingLeft;
        float graphTop = paddingTop;
        float graphRight = width - paddingRight;
        float graphBottom = height - paddingBottom;

        canvas.drawText(title, paddingLeft, 40, textPaint);

        canvas.drawLine(graphLeft, graphTop, graphLeft, graphBottom, axisPaint);
        canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, axisPaint);

        if (values.size() < 2) {
            canvas.drawText("En attente des mesures...", paddingLeft, height / 2f, textPaint);
            return;
        }

        float min = values.get(0);
        float max = values.get(0);

        for (float v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
        }

        if (min == max) {
            min = min - 1;
            max = max + 1;
        }

        canvas.drawText("Max: " + max + " " + unit, graphLeft, graphTop - 15, textPaint);
        canvas.drawText("Min: " + min + " " + unit, graphLeft, graphBottom + 40, textPaint);

        Path path = new Path();

        for (int i = 0; i < values.size(); i++) {
            float value = values.get(i);

            float x = graphLeft + (i * (graphRight - graphLeft) / (maxPoints - 1));
            float y = graphBottom - ((value - min) * (graphBottom - graphTop) / (max - min));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 4f, pointPaint);
        }

        canvas.drawPath(path, linePaint);
    }
}
