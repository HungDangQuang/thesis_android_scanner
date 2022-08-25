package uit.app.document_scanner.cropDocument;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uit.app.document_scanner.R;

public class PolygonView extends FrameLayout {
    private Paint paint = null;
    private ImageView pointer1 = null;
    private ImageView pointer2 = null;
    private ImageView pointer3 = null;
    private ImageView pointer4 = null;
    private ImageView midPointer13 = null;
    private ImageView midPointer12 = null;
    private ImageView midPointer34 = null;
    private ImageView midPointer24 = null;
    private PolygonView polygonView = null;
    private Paint circleFillPaint;

    private final Context context;

    private Map<Integer,Point> points;

    private String TAG = PolygonView.class.getSimpleName();

    public Map<Integer, Point> getPoints() {
         ArrayList<Point> points = new ArrayList<Point>();
         points.add(new Point((double) pointer1.getX(), (double) pointer1.getY()));
         points.add(new Point((double) pointer2.getX(), (double) pointer2.getY()));
         points.add(new Point((double) pointer3.getX(), (double) pointer3.getY()));
         points.add(new Point((double) pointer4.getX(), (double) pointer4.getY()));
         return getOrderedPoints(points);
    }

    public void setPoints( Map<Integer,Point> pointFMap){
        Log.d(TAG, "setPoints:" + pointFMap.size());
        if (pointFMap.size() == 4){
            setPointsCoordinates(pointFMap);
        }
    }

    public PolygonView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PolygonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PolygonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public PolygonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init(){
        polygonView = this;
        pointer1 = getImageView(0,0);
        pointer2 = getImageView(getWidth(),0);
        pointer3 = getImageView(0,getHeight());
        pointer4 = getImageView(getWidth(),getHeight());

        midPointer13 = getImageView(0,getHeight()/2);
        midPointer13.setOnTouchListener(new MidPointTouchListenerImpl(pointer1,pointer3));

        midPointer12 = getImageView(0, getWidth() / 2);
        midPointer12.setOnTouchListener(new MidPointTouchListenerImpl(pointer1, pointer2));

        midPointer34 = getImageView(0, getHeight() / 2);
        midPointer34.setOnTouchListener(new MidPointTouchListenerImpl(pointer3, pointer4));

        midPointer24 = getImageView(0, getHeight() / 2);
        midPointer24.setOnTouchListener(new MidPointTouchListenerImpl(pointer2, pointer4));

        addView(pointer1);
        addView(pointer2);
        addView(midPointer13);
        addView(midPointer12);
        addView(midPointer34);
        addView(midPointer24);
        addView(pointer3);
        addView(pointer4);

        initPaint();

    }

    public ArrayList<Point> getListPoint(){
        ArrayList<Point> listPoints = new ArrayList<Point>();
        listPoints.add(new Point((double) pointer1.getX() + pointer1.getWidth()/2, (double) pointer1.getY() + pointer1.getHeight()/2));
        listPoints.add(new Point((double) pointer2.getX() + pointer1.getWidth()/2, (double) pointer2.getY() + pointer2.getHeight()/2));
        listPoints.add(new Point((double) pointer4.getX() + pointer1.getWidth()/2, (double) pointer4.getY() + pointer4.getHeight()/2));
        listPoints.add(new Point((double) pointer3.getX() + pointer1.getWidth()/2, (double) pointer3.getY() + pointer3.getHeight()/2));
        return listPoints;
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    @SuppressLint("ResourceAsColor")
    private void initPaint(){
        paint = new Paint();
        paint.setColor(R.color.crop_color);
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(true);

        circleFillPaint = new Paint();
        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setColor(getResources().getColor(R.color.teal_200));
        circleFillPaint.setAntiAlias(true);
    }

    // Arrange list of points and store in HashMap
    public Map<Integer,Point> getOrderedPoints(List<Point> points){
        Point centerPoint = new Point();

        // find center's coordination.
        int size = points.size();
        for (Point point : points) {
            centerPoint.x += point.x / size;
            centerPoint.y += point.y / size;
        }

        Map<Integer, Point> orderedPoints = new HashMap<>();
        for (Point point : points) {
            int index = -1;
            // point 1: top-left
            if (point.x < centerPoint.x && point.y < centerPoint.y) {
                index = 0;
            }
            // point 2: top-right
            else if (point.x > centerPoint.x && point.y < centerPoint.y) {
                index = 1;
            }
            // point 3: bot-left
            else if (point.x < centerPoint.x && point.y > centerPoint.y) {
                index = 2;
            }
            // point 4: bot-right
            else if (point.x > centerPoint.x && point.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, point);
        }
        return orderedPoints;
    }

    private void setPointsCoordinates(Map<Integer, Point> pointMap) {

        if ((float) pointMap.get(2).x - pointer3.getWidth()/2 >= 0){
            Log.d(TAG, "setPointsCoordinates: pointer 3 x is get into condition 1");
            pointer3.setX((float)pointMap.get(2).x);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 3 x is get into condition 2");
            pointer3.setX((float) pointMap.get(2).x - pointer3.getWidth()/2);
        }

        if ((float) pointMap.get(2).y + pointer3.getHeight()/2 <= polygonView.getHeight()){
            Log.d(TAG, "setPointsCoordinates: pointer 3 y is get into condition 1");
            pointer3.setY((float)pointMap.get(2).y);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 3 y is get into condition 2");
            pointer3.setY((float) pointMap.get(2).y - pointer3.getHeight());
        }

        if ((float) pointMap.get(3).x + pointer4.getWidth()/2 <= polygonView.getWidth()){
            Log.d(TAG, "setPointsCoordinates: pointer 4 x is get into condition 1");
            pointer4.setX((float)pointMap.get(3).x);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 4 x is get into condition 2");
            pointer4.setX((float) pointMap.get(3).x - pointer4.getWidth()/2);
        }

        if ((float) pointMap.get(3).y + pointer4.getHeight()/2 <= polygonView.getHeight()){
            Log.d(TAG, "setPointsCoordinates: pointer 4 y is get into condition 1");
            pointer4.setY((float)pointMap.get(3).y);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 4 y is get into condition 2");
            pointer4.setY((float) pointMap.get(3).y - pointer4.getHeight()/2);
        }

        if ((float) pointMap.get(0).x - pointer1.getWidth()/2 < 0){
            Log.d(TAG, "setPointsCoordinates: pointer 1 x is get into condition 1");
            pointer1.setX((float)pointMap.get(0).x);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 1 x is get into condition 2");
            pointer1.setX((float) pointMap.get(0).x - pointer1.getWidth()/2);
        }

        if ((float) pointMap.get(0).y + pointer1.getHeight()/2 > polygonView.getHeight()){
            Log.d(TAG, "setPointsCoordinates: pointer 1 y is get into condition 1");
            pointer1.setY((float)pointMap.get(0).y);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 1 y is get into condition 2");
            pointer1.setY((float) pointMap.get(0).y - pointer1.getHeight()/2);
        }

        if ((float) pointMap.get(1).x - pointer2.getWidth()/2 < 0){
            Log.d(TAG, "setPointsCoordinates: pointer 2 x is get into condition 1");
            pointer2.setX((float)pointMap.get(1).x);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 2 x is get into condition 2");
            pointer2.setX((float) pointMap.get(1).x - pointer2.getWidth()/2);
        }

        if ((float) pointMap.get(1).y + pointer2.getHeight() > polygonView.getHeight()){
            Log.d(TAG, "setPointsCoordinates: pointer 2 y is get into condition 1");
            pointer2.setY((float)pointMap.get(1).y);
        }
        else {
            Log.d(TAG, "setPointsCoordinates: pointer 2 y is get into condition 2");
            pointer2.setY((float) pointMap.get(1).y - pointer2.getHeight()/2);
        }



//        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
//        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
//        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
//        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
//        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
//        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
//        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
//        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));
        invalidate();
//        pointer1.setX((float) pointMap.get(0).x);
//        pointer1.setY((float) pointMap.get(0).y);
//
//        pointer2.setX((float) pointMap.get(1).x);
//        pointer2.setY((float) pointMap.get(1).y);
//
//        pointer3.setX((float) pointMap.get(2).x);
//        pointer3.setY((float) pointMap.get(2).y);
//
//        pointer4.setX((float) pointMap.get(3).x);
//        pointer4.setY((float) pointMap.get(3).y);
//
//        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
//        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
//        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
//        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
//        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
//        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
//        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
//        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), paint);
        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), paint);
        canvas.drawLine(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        canvas.drawLine(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));

        Log.d(TAG, "dispatchDraw: point 3:" + pointer3.getX() + " " + pointer3.getY());
        Log.d(TAG, "dispatchDraw: polygon view:" + getWidth() + " " + getHeight());
        Log.d(TAG,"width:" + pointer1.getWidth() + " " + pointer2.getWidth() + " " + pointer3.getWidth() + " " + pointer4.getWidth());

        int radius = dp2px(context,11);
        canvas.drawCircle(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), radius, circleFillPaint);

//        canvas.drawCircle(midPointer13.getX() + (midPointer13.getWidth() / 2), midPointer13.getY() + (midPointer13.getHeight() / 2), radius, circleFillPaint);
//        canvas.drawCircle(midPointer24.getX() + (midPointer24.getWidth() / 2), midPointer24.getY() + (midPointer24.getHeight() / 2), radius, circleFillPaint);
//        canvas.drawCircle(midPointer34.getX() + (midPointer34.getWidth() / 2), midPointer34.getY() + (midPointer34.getHeight() / 2), radius, circleFillPaint);
//        canvas.drawCircle(midPointer12.getX() + (midPointer12.getWidth() / 2), midPointer12.getY() + (midPointer12.getHeight() / 2), radius, circleFillPaint);
    }

    private ImageView getImageView(int x, int y){
        ImageView imageView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.circle);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setOnTouchListener(new TouchListenerImpl());
        return imageView;
    }

    private class MidPointTouchListenerImpl implements OnTouchListener {

        final PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'

        private final ImageView mainPointer1;
        private final ImageView mainPointer2;
        PointF latestPoint = new PointF();
        PointF latestPoint1 = new PointF();
        PointF latestPoint2 = new PointF();

        public MidPointTouchListenerImpl(ImageView mainPointer1, ImageView mainPointer2) {
            this.mainPointer1 = mainPointer1;
            this.mainPointer2 = mainPointer2;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);

                    if (Math.abs(mainPointer1.getX() - mainPointer2.getX()) > Math.abs(mainPointer1.getY() - mainPointer2.getY())) {
                        if (((mainPointer2.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer2.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setY((int) (mainPointer2.getY() + mv.y));
                        }
                        if (((mainPointer1.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer1.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setY((int) (mainPointer1.getY() + mv.y));
                        }
                    } else {
                        if ((mainPointer2.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer2.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setX((int) (mainPointer2.getX() + mv.x));
                        }
                        if ((mainPointer1.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer1.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setX((int) (mainPointer1.getX() + mv.x));
                        }
                    }

                    break;
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
//                    latestPoint = new PointF(v.getX(), v.getY());
//                    latestPoint1 = new PointF(mainPointer1.getX(), mainPointer1.getY());
//                    latestPoint2 = new PointF(mainPointer2.getX(), mainPointer2.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.crop_color);
                        latestPoint.x = v.getX();
                        latestPoint.y = v.getY();
                        latestPoint1.x = mainPointer1.getX();
                        latestPoint1.y = mainPointer1.getY();
                        latestPoint2.x = mainPointer2.getX();
                        latestPoint2.y = mainPointer2.getY();
                    } else {
                        color = getResources().getColor(R.color.crop_color);
//                        v.setX(latestPoint.x);
//                        v.setY(latestPoint.y);
//                        mainPointer1.setX(latestPoint1.x);
//                        mainPointer1.setY(latestPoint1.y);
//                        mainPointer2.setX(latestPoint2.x);
//                        mainPointer2.setY(latestPoint2.y);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public boolean isValidShape(Map<Integer,Point> pointFMap){
        Log.d(TAG, "isValidShape: " + pointFMap.size());
        return pointFMap.size() == 4;
    }

    private class TouchListenerImpl implements OnTouchListener {

        final PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'
//        PointF latestPoint = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);
                    if (((StartPT.x + mv.x + v.getWidth()) < polygonView.getWidth() && (StartPT.y + mv.y + v.getHeight() < polygonView.getHeight())) && ((StartPT.x + mv.x) > 0 && StartPT.y + mv.y > 0)) {
                        v.setX((int) (StartPT.x + mv.x));
                        v.setY((int) (StartPT.y + mv.y));
                        StartPT = new PointF(v.getX(), v.getY());
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
//                    ScanActivity.allDraggedPointsStack.push(new PolygonPoints(new PointF(pointer1.getX(), pointer1.getY()),
//                            new PointF(pointer2.getX(), pointer2.getY()),
//                            new PointF(pointer3.getX(), pointer3.getY()),
//                            new PointF(pointer4.getX(), pointer4.getY())));
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
//                    latestPoint = new PointF(v.getX(), v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.crop_color);
//                        latestPoint.x = v.getX();
//                        latestPoint.y = v.getY();
                    } else {
//                        ScanActivity.allDraggedPointsStack.pop();
                        color = getResources().getColor(R.color.crop_color);
//                        v.setX(latestPoint.x);
//                        v.setY(latestPoint.y);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    public static int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return Math.round(px);
    }
}
