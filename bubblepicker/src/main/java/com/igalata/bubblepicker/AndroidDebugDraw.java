package com.igalata.bubblepicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.*;
import org.jbox2d.pooling.arrays.IntArray;
import org.jbox2d.pooling.arrays.Vec2Array;

public class AndroidDebugDraw extends DebugDraw {

    // Total render points for each circle
    public static int CIRCLE_POINTS = 20;
    private final Vec2Array vec2Array = new Vec2Array();
    private final Vec2 sp1 = new Vec2();
    private final Vec2 sp2 = new Vec2();
    private final Vec2 saxis = new Vec2();
    // TODO change IntegerArray to a specific class for int[] arrays
    private final Vec2 temp = new Vec2();
    private final Vec2 temp2 = new Vec2();
    public Canvas canvas;

    public AndroidDebugDraw(Canvas canvas) {
        super(new OBBViewportTransform());
        //this.viewportTransform.setYFlip(true);
        this.viewportTransform.setExtents(canvas.getWidth() / 2, canvas.getHeight() / 2);
        this.canvas = canvas;
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawCircle(org.jbox2d.common.Vec2, float, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawCircle(final Vec2 center, final float radius, final Color3f color) {
        final Vec2[] vecs = vec2Array.get(CIRCLE_POINTS );
        generateCirle(center, radius, vecs, CIRCLE_POINTS);
        drawPolygon(vecs, CIRCLE_POINTS, color);
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawPoint(org.jbox2d.common.Vec2, float, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawPoint(final Vec2 argPoint, final float argRadiusOnScreen, final Color3f color) {
        getWorldToScreenToOut(argPoint, sp1);

        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setARGB(255, (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));

        sp1.x -= argRadiusOnScreen;
        sp1.y -= argRadiusOnScreen;
        canvas.drawPoint(sp1.x, sp1.y, bgPaint);
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawSegment(org.jbox2d.common.Vec2, org.jbox2d.common.Vec2, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawSegment(final Vec2 p1, final Vec2 p2, final Color3f color) {
        getWorldToScreenToOut(p1, sp1);
        getWorldToScreenToOut(p2, sp2);
        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setARGB(255, (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));
//bgPaint.setColor(Color.BLACK);

        canvas.drawLine(sp1.x, sp1.y, sp2.x, sp2.y, bgPaint);
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawSolidCircle(org.jbox2d.common.Vec2, float, org.jbox2d.common.Vec2, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawSolidCircle(final Vec2 center, final float radius, final Vec2 axis, final Color3f color) {
        final Vec2[] vecs = vec2Array.get(CIRCLE_POINTS);
        generateCirle(center, radius, vecs, CIRCLE_POINTS);
        drawSolidPolygon(vecs, CIRCLE_POINTS, color);
        if(axis != null) {
            saxis.set(axis).mulLocal(radius).addLocal(center);
            drawSegment(center, saxis, color);
        }
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawSolidPolygon(org.jbox2d.common.Vec2[], int, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawSolidPolygon(final Vec2[] vertices, final int vertexCount, final Color3f color) {
        final Path path = new Path();

        getWorldToScreenToOut(vertices[0], temp);
        path.moveTo(temp.x, temp.y);

        for(int i = 1; i < vertexCount; i++) {
            getWorldToScreenToOut(vertices[i], temp);
            path.lineTo(temp.x, temp.y);
        }

        path.close();

        Paint bgPaint = new Paint();
        bgPaint.setStrokeWidth(100);
        bgPaint.setPathEffect(null);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bgPaint.setARGB(255, (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));

        canvas.drawPath(path, bgPaint);
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawString(float, float, java.lang.String, org.jbox2d.common.Color3f)
     */
    @Override
    public void drawString(final float x, final float y, final String s, final Color3f color) {

        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setARGB(255, (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));

        canvas.drawText(s, x, y, bgPaint);
    }

    /**
     * @see org.jbox2d.callbacks.DebugDraw#drawTransform(org.jbox2d.common.Transform)
     */
    @Override
    public void drawTransform(final Transform xf) {
        getWorldToScreenToOut(xf.position, temp);
        temp2.setZero();
        final float k_axisScale = 0.4f;

        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setARGB(255, 255,0,0);
        temp2.x = xf.position.x + k_axisScale * xf.R.col1.x;
        temp2.y = xf.position.y + k_axisScale * xf.R.col1.y;
        getWorldToScreenToOut(temp2, temp2);
        canvas.drawLine(temp.x, temp.y, temp2.x, temp2.y, bgPaint);

        Paint bgPaint2 = new Paint();
        bgPaint2.setStyle(Paint.Style.FILL);
        bgPaint2.setARGB(255, 0,255,0);
        temp2.x = xf.position.x + k_axisScale * xf.R.col2.x;
        temp2.y = xf.position.y + k_axisScale * xf.R.col2.y;
        getWorldToScreenToOut(temp2, temp2);
        canvas.drawLine(temp.x, temp.y, temp2.x, temp2.y, bgPaint2);
    }

    // Circle Generator
    private void generateCirle(final Vec2 argCenter, final float argRadius, final Vec2[] argPoints, final int argNumPoints) {
        final float inc = MathUtils.TWOPI / argNumPoints;

        for(int i=0; i<argNumPoints; i++){
            argPoints[i].x = (argCenter.x + MathUtils.cos(i*inc)*argRadius);
            argPoints[i].y = (argCenter.y + MathUtils.sin(i*inc)*argRadius);
        }
    }
}
