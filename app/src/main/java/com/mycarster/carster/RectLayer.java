package com.mycarster.carster;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.Toast;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Administrator on 2016/5/25.
 */
public class RectLayer extends ImageView {
    MainActivity mainactivity;
    public RectLayer(Context context) {
        this(context, null);
        mainactivity = (MainActivity) context;
    }

    public RectLayer(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
        mainactivity = (MainActivity) context;
    }

    public RectLayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mainactivity = (MainActivity) context;
        //this.setAlpha(0.3f);
    }

    private Rect rect;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = getWidth();
        int height = getHeight();
        rect = new Rect(width/12, height/5, width * 11 / 12, height *7 /10);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setAlpha(76);
        canvas.drawRect(0, 0, width, rect.top, p);
        canvas.drawRect(0, rect.bottom, width, height, p);
        canvas.drawRect(0, rect.top, rect.left, rect.bottom, p);
        canvas.drawRect(rect.right, rect.top, width, rect.bottom, p);

        p.setColor(Color.RED);
        p.setAlpha(255);
        p.setStrokeWidth(3);

        if(mainactivity.lastOrientation == 0) {  //is portrate
//            Log.d("startOrientation","ORIENTATION_PORTRAIT" );
//            Toast.makeText(mainactivity, "Redraw_ORIENTATION_PORTRAIT", Toast.LENGTH_SHORT).show();
            int lineVerticalPos = rect.top + (rect.height() >> 1);
            canvas.drawLine(rect.left + 10, lineVerticalPos, rect.right - 10, lineVerticalPos, p);
        }else{
//            Log.d("startOrientation","ORIENTATION_LANDSPACE" );
//            Toast.makeText(mainactivity, "Redraw_ORIENTATION_LANDSPACE", Toast.LENGTH_SHORT).show();
            int lineHorizontalPos = rect.left + (rect.width() >> 1);
            canvas.drawLine(lineHorizontalPos, rect.top + 10, lineHorizontalPos, rect.bottom - 10, p);
        }

        p.setColor(Color.WHITE);
        p.setStrokeWidth(3);
        float[] vertice = {rect.left, rect.top, rect.right, rect.top,
                rect.right, rect.top, rect.right, rect.bottom,
                rect.right, rect.bottom, rect.left, rect.bottom,
                rect.left, rect.top, rect.left, rect.bottom};
        canvas.drawLines(vertice, p);

        p.setARGB(255, 255, 141, 22);
        p.setStrokeWidth(5);
        canvas.drawLine(rect.left, rect.top, rect.left + 40, rect.top, p);
        canvas.drawLine(rect.right - 40, rect.top, rect.right, rect.top, p);
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + 40, p);
        canvas.drawLine(rect.right, rect.bottom - 40, rect.right, rect.bottom, p);
        canvas.drawLine(rect.right - 40, rect.bottom, rect.right, rect.bottom, p);
        canvas.drawLine(rect.left, rect.bottom, rect.left + 40, rect.bottom, p);
        canvas.drawLine(rect.left, rect.bottom - 40, rect.left, rect.bottom, p);
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + 40, p);
    }

}
