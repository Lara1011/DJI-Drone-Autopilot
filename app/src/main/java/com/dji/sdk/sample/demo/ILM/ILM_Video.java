package com.dji.sdk.sample.demo.ILM;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.utils.VideoFeedView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.keysdk.callback.SetCallback;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.sdkmanager.DJISDKManager;

public class ILM_Video {

    private static final String TAG = "ILMVideoController";

    private VideoFeeder.PhysicalSourceListener sourceListener;
    private VideoFeedView videoFeedView;
    private SetCallback setBandwidthCallback;
    private HOGDescriptor hog;
    private ImageView imageView;
    private Handler backgroundHandler;
    private TextureView videoSurface;
    private Net net;
    private TextView peopleDetected;
    private boolean isDetectionEnabled = false;


    public ILM_Video() {
        initialize();
    }

    public ILM_Video(VideoFeedView videoFView, ImageView imgView, TextureView videoSurface, TextView peopleDetected) {
        this.videoSurface = videoSurface;
        this.videoFeedView = videoFView;
        this.imageView = imgView;
        this.peopleDetected = peopleDetected;
        initialize();
    }

    public static String copyAssetToStorage(Context context, String fileName) {
        Log.e("copyAssetToStorage", "in");
        File file = new File(context.getFilesDir(), fileName);

        try (InputStream inputStream = context.getAssets().open(fileName);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("copyAssetToStorage", "out - " + file.getAbsolutePath());

        return file.getAbsolutePath();
    }

    private void initialize() {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed.");
        } else {
            Log.d(TAG, "OpenCV initialized successfully.");
        }
        // Add a check for assets existence and log the paths for debugging
        String modelPath = copyAssetToStorage(this.videoSurface.getContext(), "yolov7.weights");
        String configPath = copyAssetToStorage(this.videoSurface.getContext(), "yolov7.cfg");
        Log.e("modelPath", modelPath);
        Log.e("configPath", configPath);
        File modelFile = new File(modelPath);
        File configFile = new File(configPath);

        if (!modelFile.exists() || !configFile.exists()) {
            return;
        }
        Log.e("net", "in");
        net = Dnn.readNetFromDarknet(configPath, modelPath);
        Log.e("net", "out");

        if (net.empty()) {
            return;
        }

        hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        // Initialize background handler for processing
        HandlerThread handlerThread = new HandlerThread("BackgroundHandlerThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        setBandwidthCallback = new SetCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.setResultToToast("Set key value successfully");
                if (videoFeedView != null) {
                    videoFeedView.changeSourceResetKeyFrame();
                }
            }

            @Override
            public void onFailure(@NonNull DJIError error) {
                ToastUtils.setResultToToast("Failed to set: " + error.getDescription());
            }
        };
    }


    public void displayVideo() {
        sourceListener = new VideoFeeder.PhysicalSourceListener() {
            @Override
            public void onChange(VideoFeeder.VideoFeed videoFeed, PhysicalSource newPhysicalSource) {
            }
        };

        if (VideoFeeder.getInstance() == null) {
            return;
        }

        final BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product == null) {
            return;
        }

        VideoFeeder.VideoDataListener primaryVideoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                backgroundHandler.post(() -> processFrame());
            }
        };

        videoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(primaryVideoDataListener);
        VideoFeeder.getInstance().addPhysicalSourceListener(sourceListener);
    }


    private void processFrame() {
        Bitmap bitmap = videoSurface.getBitmap();

        if (bitmap == null) {
            return;
        }

        if (!isDetectionEnabled) {
            Mat frame = new Mat();
            Utils.bitmapToMat(bitmap, frame);
            displayFrame(frame); // Show the current frame without detection
            return;
        }

        Mat frame = new Mat();
        Utils.bitmapToMat(bitmap, frame);

        // Convert the frame from 4 channels (RGBA) to 3 channels (RGB)
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        // Preprocess frame for YOLO input
        Mat blob = Dnn.blobFromImage(frame, 1 / 255.0, new Size(416, 416), new Scalar(0, 0, 0), true, false);

        if (blob.empty()) {
            return;
        }

        net.setInput(blob);

        // Get output layers from YOLO
        List<Mat> outputLayers = new ArrayList<>();
        net.forward(outputLayers, getOutputLayerNames(net));

        // Process the detection results
        //detectPeopleWithYOLO(outputLayers, frame);
        detectPeopleWithYOLOv7(outputLayers, frame);
        // Optionally display the frame
        displayFrame(frame);
    }


    private List<String> getOutputLayerNames(Net net) {
        List<String> names = new ArrayList<>();
        try {
            for (int i : net.getUnconnectedOutLayers().toArray()) {
                names.add(net.getLayerNames().get(i - 1));
            }
        } catch (Exception e) {
        }
        return names;
    }

    private void updatePeopleCount(int count) {
        if (peopleDetected != null) {
            // Update the TextView on the UI thread
            peopleDetected.post(() -> peopleDetected.setText("People detected: " + count));
        }
    }

    private void detectPeopleWithYOLO(List<Mat> outputLayers, Mat frame) {
        List<Rect> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        int peopleCount = 0;

        // Extract detections from YOLO output layers
        for (Mat output : outputLayers) {
            for (int i = 0; i < output.rows(); i++) {
                float confidence = (float) output.get(i, 4)[0];

                if (confidence > 0.3) {  // Lowered confidence threshold
                    int classId = (int) output.get(i, 5)[0];

                    if (classId == 0) {  // Assuming class 0 is 'person'
                        peopleCount++;

                        // Extract bounding box coordinates
                        int centerX = (int) (output.get(i, 0)[0] * frame.cols());
                        int centerY = (int) (output.get(i, 1)[0] * frame.rows());
                        int width = (int) (output.get(i, 2)[0] * frame.cols());
                        int height = (int) (output.get(i, 3)[0] * frame.rows());

                        // Create the bounding box and add to list
                        Rect boundingBox = new Rect(centerX - width / 2, centerY - height / 2, width, height);
                        boxes.add(boundingBox);
                        confidences.add(confidence);

                        // Draw raw bounding box before NMS (in blue)
                        // Imgproc.rectangle(frame, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0), 2);
                    }
                }
            }
        }

        // Display the raw detections before applying NMS
        displayFrame(frame);

        // Proceed with NMS if we have detections
        if (!boxes.isEmpty() && !confidences.isEmpty()) {
            applyNMSAndDisplay(boxes, confidences, frame);
        } else {
            updatePeopleCount(0);
        }
    }

    private void applyNMSAndDisplay(List<Rect> boxes, List<Float> confidences, Mat frame) {
        List<org.opencv.core.Rect2d> boxes2d = new ArrayList<>();
        for (Rect box : boxes) {
            boxes2d.add(new org.opencv.core.Rect2d(box.x, box.y, box.width, box.height));
        }

        MatOfRect2d matOfBoxes2d = new MatOfRect2d();
        matOfBoxes2d.fromList(boxes2d);

        MatOfFloat matOfConfidences = new MatOfFloat();
        float[] confidenceArray = new float[confidences.size()];
        for (int i = 0; i < confidences.size(); i++) {
            confidenceArray[i] = confidences.get(i);
        }
        matOfConfidences.fromArray(confidenceArray);

        MatOfInt indices = new MatOfInt();

        // Apply NMS
        Dnn.NMSBoxes(matOfBoxes2d, matOfConfidences, 0.3f, 0.2f, indices);

        if (indices.empty()) {
            updatePeopleCount(0);
            return;
        }

        // Draw bounding boxes after NMS (in green)
        int[] selectedIndices = indices.toArray();
        for (int index : selectedIndices) {
            Rect box = boxes.get(index);
            Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
        }

        updatePeopleCount(selectedIndices.length);
        displayFrame(frame);
    }

    private void detectPeople(Mat frame) {
        // Keep track of original frame size for drawing purposes
        int originalWidth = videoSurface.getWidth();
        int originalHeight = videoSurface.getHeight();

        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();

        hog.detectMultiScale(frame, foundLocations, foundWeights);

        // Draw rectangles around detected people based on original frame size
        for (Rect rect : foundLocations.toArray()) {
            // Scale rectangle coordinates back to original frame size
            Rect scaledRect = new Rect(
                    new Point(rect.tl().x * originalWidth / frame.width(), rect.tl().y * originalHeight / frame.height()),
                    new Point(rect.br().x * originalWidth / frame.width(), rect.br().y * originalHeight / frame.height())
            );

            Imgproc.rectangle(frame, scaledRect.tl(), scaledRect.br(), new Scalar(0, 255, 0), 2);
        }

        // Display the frame with detection results
        displayFrame(frame);
    }


    private void displayFrame(Mat frame) {
        if (imageView == null) {
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bitmap);

        // Update the ImageView on the UI thread
        imageView.post(() -> imageView.setImageBitmap(bitmap));
    }

    //-------------------------------------------new code-----------------------------------------
    private void detectPeopleWithYOLOv7(List<Mat> outputLayers, Mat frame) {
        List<Rect> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        int peopleCount = 0;

        // Extract detections from YOLO output layers
        for (Mat output : outputLayers) {
            for (int i = 0; i < output.rows(); i++) {
                // Extract the confidence for the detection
                float confidence = (float) output.get(i, 4)[0];

                // Apply confidence threshold
                if (confidence > 0.5) {  // Increase the threshold to reduce false positives
                    // Get class scores and determine the most confident class
                    float[] classScores = new float[(int) output.size(1) - 5];
                    for (int j = 5; j < output.size(1); j++) {
                        classScores[j - 5] = (float) output.get(i, j)[0];
                    }
                    int classId = argMax(classScores);
                    float classConfidence = classScores[classId];

                    // Check if detected object is a person (class 0) with a sufficient confidence level
                    if (classId == 0 && classConfidence > 0.5) {
                        peopleCount++;

                        // Extract bounding box coordinates
                        int centerX = (int) (output.get(i, 0)[0] * frame.cols());
                        int centerY = (int) (output.get(i, 1)[0] * frame.rows());
                        int width = (int) (output.get(i, 2)[0] * frame.cols());
                        int height = (int) (output.get(i, 3)[0] * frame.rows());

                        // Create the bounding box and add to list
                        Rect boundingBox = new Rect(centerX - width / 2, centerY - height / 2, width, height);
                        boxes.add(boundingBox);
                        confidences.add(confidence);
                    }
                }
            }
        }

        // Proceed with NMS if we have detections
        if (!boxes.isEmpty() && !confidences.isEmpty()) {
            applyNMSAndDisplay(boxes, confidences, frame);
        } else {
            updatePeopleCount(0);
        }
    }

    // Helper function to find the index of the maximum value
    private int argMax(float[] values) {
        int maxIndex = 0;
        float maxValue = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > maxValue) {
                maxValue = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public void setDetectionEnabled(boolean enabled) {
        this.isDetectionEnabled = enabled;
    }

    public boolean isDetectionEnabled() {
        return this.isDetectionEnabled;
    }
}
