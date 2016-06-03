package com.leon.opencv4android.imagemanipulations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mCameraView;
    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HIST = 1;
    public static final int VIEW_MODE_CANNY = 2;
    public static final int VIEW_MODE_SEPIA = 3;
    public static final int VIEW_MODE_SOBEL = 4;
    public static final int VIEW_MODE_ZOOM = 5;
    public static final int VIEW_MODE_PIXELIZE = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;
    public static int mViewMode = VIEW_MODE_RGBA;
    private Mat mIntermediateMat;
    private Mat mSepiaKernel;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.surfaceCamera);
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_rgba:
                mViewMode = VIEW_MODE_RGBA;
                break;
            case R.id.action_histograms:
                mViewMode = VIEW_MODE_HIST;
                break;
            case R.id.action_canny:
                mViewMode = VIEW_MODE_CANNY;
                break;
            case R.id.action_sepia:
                mViewMode = VIEW_MODE_SEPIA;
                break;
            case R.id.action_sobel:
                mViewMode = VIEW_MODE_SOBEL;
                break;
            case R.id.action_zoom:
                mViewMode = VIEW_MODE_ZOOM;
                break;
            case R.id.action_pixelize:
                mViewMode = VIEW_MODE_PIXELIZE;
                break;
            case R.id.action_posterize:
                mViewMode = VIEW_MODE_POSTERIZE;
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    @Override
    public void onCameraViewStopped() {
        if (mIntermediateMat != null) {
            mIntermediateMat.release();
        }
        mIntermediateMat = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();
        int rows = rgba.rows();
        int cols = rgba.cols();
        Mat rgbaInnerWindow = rgba.submat(0, rows, cols / 4, cols * 3 / 4);

        switch (mViewMode) {
            case VIEW_MODE_RGBA:
                break;
            case VIEW_MODE_HIST:
                Mat hist = new Mat();
                int mHistSizeNum = 25;
                int thikness = cols / (mHistSizeNum + 10) / 5;
                if (thikness > 5) thikness = 5;
                int offset = (cols - (5 * mHistSizeNum + 4 * 10) * thikness) / 2;
                MatOfInt mChannels[] = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
                MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
                MatOfFloat mRanges = new MatOfFloat(0f, 256f);
                float mBuff[] = new float[mHistSizeNum];
                Point mP1 = new Point();
                Point mP2 = new Point();
                Scalar mColorsHue[] = new Scalar[]{
                        new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                        new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                        new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                        new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                        new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
                };
                Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
                Scalar mWhilte = Scalar.all(255);
                // RGB
                for (int c = 0; c < 3; c++) {
                    Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], new Mat(), hist, mHistSize, mRanges);
                    Core.normalize(hist, hist, rows / 2, 0, Core.NORM_INF);
                    hist.get(0, 0, mBuff);
                    for (int h = 0; h < mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                        mP1.y = rows - 1;
                        mP2.y = mP1.y - 2 - (int) mBuff[h];
                        Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
                    }
                }
                // Value and Hue
                Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
                // Value
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], new Mat(), hist, mHistSize, mRanges);
                Core.normalize(hist, hist, rows / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = rows - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
                }
                // Hue
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], new Mat(), hist, mHistSize, mRanges);
                Core.normalize(hist, hist, rows / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = rows - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);
                }
                break;
            case VIEW_MODE_CANNY:
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                break;
            case VIEW_MODE_SOBEL:
                Mat gray = inputFrame.gray();
                Mat grayInnerWindow = gray.submat(0, rows, cols / 4, cols * 3 / 4);
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                break;
            case VIEW_MODE_SEPIA:
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
                break;
            case VIEW_MODE_ZOOM:
                Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
                Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
                Size wsize = mZoomWindow.size();
                Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
                zoomCorner.release();
                mZoomWindow.release();
                break;
            case VIEW_MODE_PIXELIZE:
                Imgproc.resize(rgbaInnerWindow, mIntermediateMat, new Size(), 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
                break;
            case VIEW_MODE_POSTERIZE:
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1. / 16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
                break;
        }
        rgbaInnerWindow.release();
        return rgba;
    }
}
