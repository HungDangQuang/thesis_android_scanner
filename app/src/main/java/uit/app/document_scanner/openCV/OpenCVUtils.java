package uit.app.document_scanner.openCV;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uit.app.document_scanner.cropDocument.PolygonView;

public class OpenCVUtils {

    private String TAG = OpenCVUtils.class.getSimpleName();
    public List<Point> getContourEdgePoints(Bitmap bitmap){

        boolean hasContour = false;

        Mat matReceipt = new Mat();

        matReceipt = convertBitmapToMat(bitmap);

        compressDown(matReceipt,matReceipt);
        compressDown(matReceipt,matReceipt);

        Mat matconvertedGray = new Mat();

        // source - destination - code
        Imgproc.cvtColor(matReceipt,matconvertedGray,Imgproc.COLOR_RGB2GRAY);

        double otsuThreshold = Imgproc.threshold(matconvertedGray, new Mat(),0.0,255.0,Imgproc.THRESH_OTSU);


        Mat matMedianFilter = new Mat();
        Imgproc.medianBlur(matconvertedGray,matMedianFilter,11);

        Mat matEdges = new Mat();
        Imgproc.Canny(matconvertedGray,matEdges,otsuThreshold * 0.05, otsuThreshold);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(matEdges,contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        int height = matconvertedGray.height();
        int width = matconvertedGray.width();

        double maxAreaFound = (double) ((width - 20) * (height - 20)/20);
        Point[] myPoints = {new Point(5.0,5.0),
                            new Point(5.0, (double) height - 5),
                            new Point((double) width - 5, (double) height - 5),
                            new Point((double) width - 5, 5.0)};

        MatOfPoint receiptContour = new MatOfPoint(myPoints);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);

            MatOfPoint2f boxContour2f = new MatOfPoint2f(contour.toArray());
            RotatedRect boxArea = Imgproc.minAreaRect(boxContour2f);

            if(contour.toArray().length >= 4 && maxAreaFound < boxArea.size.area()){
                maxAreaFound = boxArea.size.area();
                receiptContour = contour;

                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(receiptContour,hull,false);
                MatOfPoint mopOut = new MatOfPoint();

                mopOut.create((int) hull.size().height,1,CvType.CV_32SC2);

                int j = 0;

                while (j < hull.size().height){
                    int index = (int) hull.get(j,0)[0];
                    Log.d(TAG, "getContourEdgePoints: index:" + index);
                    double[] point = {receiptContour.get(index,0)[0],receiptContour.get(index,0)[1]};
                    mopOut.put(j,0,point);

                    j++;
                }
                receiptContour = mopOut;
                hasContour = true;
            }
        }

        Point centrePoint = getCentrePointOfContour(receiptContour);

        List<Point> listPoints = receiptContour.toList();

        if (listPoints == null || listPoints.size() < 4){
            return listPoints;
        }

        double pMaxX = getMaxX(listPoints);
        double pMinX = getMinX(listPoints);
        double espX = (pMaxX - pMinX) / 4;

        double pMaxY = getMaxY(listPoints);
        double pMinY = getMinY(listPoints);
        double espY = (pMaxY - pMinY) / 4;

        Point pointTL = getPointTlWithMaxLength(listPoints,centrePoint,espX,espY);
        Point pointTR = getPointTrWithMaxLength(listPoints,centrePoint,espX,espY);
        Point pointBR = getPointBrWithMaxLength(listPoints,centrePoint,espX,espY);
        Point pointBL = getPointBlWithMaxLength(listPoints,centrePoint,espX,espY);

        ArrayList<Point> cornerPoints = new ArrayList<Point>();
        cornerPoints.add(pointTL);
        cornerPoints.add(pointTR);
        cornerPoints.add(pointBL);
        cornerPoints.add(pointBR);

        if (!isConvexShape(cornerPoints)){
            Rect box = Imgproc.boundingRect(receiptContour);

            cornerPoints.clear();
            pointTL = box.tl();
            pointBR = box.br();
            pointTR = new Point(pointBR.x,pointTL.y);
            pointBL = new Point(pointTL.x, pointBR.y);

            cornerPoints.add(pointTL);
            cornerPoints.add(pointTR);
            cornerPoints.add(pointBL);
            cornerPoints.add(pointBR);
        }

        if (hasContour){
            Bitmap pyrDownReceipt = convertMatToBitmap(matReceipt);
            double widthRatio = ((double) bitmap.getWidth()) / (double) pyrDownReceipt.getWidth();
            double heightRatio = ((double) bitmap.getHeight()) / (double) pyrDownReceipt.getHeight();

            ArrayList<Point> convertedCorners = new ArrayList<Point>();

            for (Point corner: cornerPoints) {
                convertedCorners.add(new Point(corner.x * widthRatio,corner.y * heightRatio));
            }
            return convertedCorners;
        }
        else{
            return listPoints;
        }
    }

    public Map<Integer,Point> getEdgePoints(Bitmap bitmap, PolygonView polygonView){
        List<Point> pointFs = getContourEdgePoints(bitmap);
        return getOrderedValidEdgePoint(bitmap,pointFs,polygonView);
    }

    private Map<Integer,Point> getOutlinePoints(Bitmap bitmap){
        HashMap<Integer,Point> outlinePoints = new HashMap<Integer,Point>();
        outlinePoints.put(0,new Point((double) 0f, (double) 0f));
        outlinePoints.put(1,new Point((double) bitmap.getWidth(), (double) 0f));
        outlinePoints.put(2,new Point((double) 0f, (double) bitmap.getHeight()));
        outlinePoints.put(3,new Point((double) bitmap.getWidth(), (double) bitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, Point> getOrderedValidEdgePoint(Bitmap bitmap, List<Point> pointFs, PolygonView polygonView){
        Map<Integer,Point> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)){
            orderedPoints = getOutlinePoints(bitmap);
        }
        return orderedPoints;
    }

    public Point getCentrePointOfContour(MatOfPoint contour){
        Moments moments = Imgproc.moments(contour);
        if(moments != null){
            return new Point(moments.m10/moments.m00,moments.m01/moments.m00);
        }
        return null;
    }

    private Bitmap convertMatToBitmap(Mat m){
        Bitmap bm = Bitmap.createBitmap(m.cols(),m.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m,bm);
        return bm;
    }

    private Mat convertBitmapToMat(Bitmap bitmap){
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Utils.bitmapToMat(bmp32,mat);
        return mat;
    }

    private void compressDown(Mat large, Mat rgb){
        Imgproc.pyrDown(large,rgb);
        Imgproc.pyrDown(rgb,rgb);
    }

    double getMaxX(List<Point> listPoint){
        if (listPoint == null || listPoint.size() == 0){
            return 0.0;
        }
        int pos = 0;
        double maxX = listPoint.get(0).x;
        for (int i = 0; i < listPoint.size(); i++) {
            if (maxX < listPoint.get(i).x){
                pos = i;
                maxX = listPoint.get(i).x;
            }
        }
        return listPoint.get(pos).x;
    }

    double getMinX(List<Point> listPoint){
        if (listPoint == null || listPoint.size() == 0){
            return 0.0;
        }
        int pos = 0;
        double minX = listPoint.get(0).x;
        for (int i = 0; i < listPoint.size(); i++) {
            if (minX >= listPoint.get(i).x){
                pos = i;
                minX = listPoint.get(i).x;
            }
        }
        return listPoint.get(pos).x;
    }

    double getMinY(List<Point> listPoint){
        if (listPoint == null || listPoint.size() == 0){
            return 0.0;
        }
        int pos = 0;
        double minY = listPoint.get(0).y;
        for (int i = 0; i < listPoint.size(); i++) {
            if (minY >= listPoint.get(i).y){
                pos = i;
                minY = listPoint.get(i).y;
            }
        }
        return listPoint.get(pos).y;
    }

    double getMaxY(List<Point> listPoint){
        if (listPoint == null || listPoint.size() == 0){
            return 0.0;
        }
        int pos = 0;
        double maxY = listPoint.get(0).y;
        for (int i = 0; i < listPoint.size(); i++) {
            if (maxY < listPoint.get(i).x){
                pos = i;
                maxY = listPoint.get(i).x;
            }
        }
        return listPoint.get(pos).x;
    }

    public Point getPointTlWithMaxLength(List<Point> listPointInContour, Point centrePoint, double espX, double espY){
        if (listPointInContour == null || listPointInContour.size() == 0){
            return null;
        }

        double maxLength = 0.0;
        int pos = 0;
        for (int i = 0; i < listPointInContour.size(); i++) {

            Point point = listPointInContour.get(i);
            double length = getDistanceBetweenPoints(point,centrePoint);
            if (point.x <= centrePoint.x + espX && point.y <= centrePoint.y - espY && maxLength < length){
                pos = i;
                maxLength = length;
            }
        }
        return listPointInContour.get(pos);
    }

    public Point getPointTrWithMaxLength(List<Point> listPointInContour, Point centrePoint, double espX, double espY){
        if (listPointInContour == null || listPointInContour.size() == 0){
            return null;
        }

        double maxLength = 0.0;
        int pos = 0;
        for (int i = 0; i < listPointInContour.size(); i++) {

            Point point = listPointInContour.get(i);
            double length = getDistanceBetweenPoints(point,centrePoint);
            if (point.x > centrePoint.x + espX && point.y <= centrePoint.y + espY && maxLength < length){
                pos = i;
                maxLength = length;
            }
        }
        return listPointInContour.get(pos);
    }

    public Point getPointBrWithMaxLength(List<Point> listPointInContour, Point centrePoint, double espX, double espY){
        if (listPointInContour == null || listPointInContour.size() == 0){
            return null;
        }

        double maxLength = 0.0;
        int pos = 0;
        for (int i = 0; i < listPointInContour.size(); i++) {

            Point point = listPointInContour.get(i);
            double length = getDistanceBetweenPoints(point,centrePoint);
            if (point.x > centrePoint.x - espX && point.y > centrePoint.y + espY && maxLength < length){
                pos = i;
                maxLength = length;
            }
        }
        return listPointInContour.get(pos);
    }

    public Point getPointBlWithMaxLength(List<Point> listPointInContour, Point centrePoint, double espX, double espY){
        if (listPointInContour == null || listPointInContour.size() == 0){
            return null;
        }

        double maxLength = 0.0;
        int pos = 0;
        for (int i = 0; i < listPointInContour.size(); i++) {

            Point point = listPointInContour.get(i);
            double length = getDistanceBetweenPoints(point,centrePoint);
            if (point.x <= centrePoint.x - espX && point.y > centrePoint.y - espY && maxLength < length){
                pos = i;
                maxLength = length;
            }
        }
        return listPointInContour.get(pos);
    }

    public double getDistanceBetweenPoints(Point point1, Point point2){
        return Math.sqrt((point1.x - point2.x)*(point1.x - point2.x) + (point1.y - point2.y)*(point1.y - point2.y));
    }

    public boolean isConvexShape(List<Point> corners){
        int size = 0;
        boolean result = false;

        if (corners == null || corners.isEmpty()){
            return false;
        }
        else {
            size = corners.size();
            Log.d(TAG, "isConvexShape:" + corners.size());
        }

        if (size > 0){
            for (int i = 0; i < size ; i++) {
                double dx1 = corners.get((i+2) % size).x - corners.get((i+1) % size).x;
                double dy1 = corners.get((i+2) % size).y - corners.get((i+1) % size).y;
                double dx2 = corners.get(i).x - corners.get((i+1) % size).x;
                double dy2 = corners.get(i).y - corners.get((i+1) % size).y;
                double crossProduct = dx1 * dy2 - dy1 * dx2;
                if (i == 0){
                    result = crossProduct > 0 ;
                }
                else {
                    if (result != crossProduct > 0){
                        return false;
                    }
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    public Bitmap rotate(Bitmap bitmap, int degree){
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix mtx = new Matrix();
            mtx.postRotate((float) degree);
            return Bitmap.createBitmap(bitmap,0,0,w,h,mtx,false);
        }catch (Exception e){
            return bitmap;
        }
    }

    public Bitmap cropImageByFourPoints(Bitmap receipt, ArrayList<Point> cornerPoints, int screenWidth, int screenHeight){
        if (cornerPoints == null || cornerPoints.size() != 4){
            Log.d(TAG, "cropReceiptByFourPoints: if condtion is true");
            return null;
        }

        Mat originalReceiptMat = convertBitmapToMat(receipt);

        double widthRatio = (double) receipt.getWidth() / (double) screenWidth;
        double heightRatio = (double) receipt.getHeight() / (double) screenHeight;

        ArrayList<Point> corners = new ArrayList<Point>();
        for (int i = 0; i < cornerPoints.size(); i++) {
            corners.add(new Point(cornerPoints.get(i).x * widthRatio, cornerPoints.get(i).y * heightRatio));
        }

        Mat srcPoints = Converters.vector_Point2f_to_Mat(corners);

        double maxY = getPointWithMaxCorY(corners).y;
        double minY = getPointWithMinCorY(corners).y;

        double maxX = getPointWithMaxCorX(corners).x;
        double minX = getPointWithMinCorX(corners).x;

        double maxWidth = maxX - minX;
        double maxHeight = maxY - minY;

        Mat correctedImage = new Mat((int) maxHeight, (int) maxWidth, originalReceiptMat.type());
        Mat destPoints = Converters.vector_Point2f_to_Mat(Arrays.asList(new Point(0.0,0.0),
                                                                        new Point(maxWidth - 1, 0.0),
                                                                        new Point(maxWidth - 1, maxHeight - 1),
                                                                        new Point(0.0,maxHeight -1)));

        Mat transformation = Imgproc.getPerspectiveTransform(srcPoints,destPoints);
        Imgproc.warpPerspective(originalReceiptMat,correctedImage,transformation,correctedImage.size());
        Log.d(TAG, "cropReceiptByFourPoints: correct image size:" + correctedImage.size());
        return convertMatToBitmap(correctedImage);
    }

    Point getPointWithMaxCorY(List<Point> listPoint){
        if(listPoint == null || listPoint.size() == 0){
            return null;
        }

        double maxY = listPoint.get(0).y;
        int maxYPos = 0;
        for (int i = 0; i < listPoint.size(); i++) {
            if(maxY < listPoint.get(i).y){
                maxY = listPoint.get(i).y;
                maxYPos = i;
            }
        }
        return listPoint.get(maxYPos);
    }

    Point getPointWithMinCorY(List<Point> listPoint){
        if(listPoint == null || listPoint.size() == 0){
            return null;
        }

        double minY = listPoint.get(0).y;
        int minYPos = 0;
        for (int i = 0; i < listPoint.size(); i++) {
            if(minY > listPoint.get(i).y){
                minY = listPoint.get(i).y;
                minYPos = i;
            }
        }
        return listPoint.get(minYPos);
    }

    Point getPointWithMaxCorX(List<Point> listPoint){
        if(listPoint == null || listPoint.size() == 0){
            return null;
        }

        double maxX = listPoint.get(0).x;
        int maxXPos = 0;
        for (int i = 0; i < listPoint.size(); i++) {
            if(maxX < listPoint.get(i).x){
                maxX = listPoint.get(i).x;
                maxXPos = i;
            }
        }
        return listPoint.get(maxXPos);
    }

    Point getPointWithMinCorX(List<Point> listPoint){
        if(listPoint == null || listPoint.size() == 0){
            return null;
        }

        double minX = listPoint.get(0).x;
        int minXPos = 0;
        for (int i = 0; i < listPoint.size(); i++) {
            if(minX > listPoint.get(i).y){
                minX = listPoint.get(i).y;
                minXPos = i;
            }
        }
        return listPoint.get(minXPos);
    }

}
