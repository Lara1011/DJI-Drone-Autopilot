# DJI Drone Autopilot Android Application

This application is primarily designed to create and execute **waypoint missions** for drones, including importing waypoints, navigating to them, and detecting people with OpenCV using YOLOv7.

# Functionalities:
- Take Off
- Land
- Stop
- Record the Flight
- Manual Flight Control using Virtual Sticks
- Detect People

The application displays a **mini-map** and a **status bar** that shows all the necessary parameters for the drone: **Battery, Speed, X, Y, Z, Pitch, Roll, Yaw, Time & Date, Latitude, Longitude, and Altitude**.

The flight data is saved to a **CSV** file on the Android device, containing all the above parameters.

# How To Run:
1) Download the released version file and extract it.
2) Open Android Studio (currently using Android Studio Koala | 2024.1.1 Patch 2).
3) Open DJI_Mobile_SDK_Android_V4-2.0.0.
4) Change Gradle JDK to 15.0.2 (`Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`).
5) Gradle Version 6.7.1 and Android Gradle Plugin Version 4.2.2 (`In Project Structure` -> `Project`).
6) Download OpenCV 4.10.0 and extract it ([Link for download](https://github.com/opencv/opencv/releases/download/4.10.0/opencv-4.10.0-android-sdk.zip))
7) Import the OpenCV module inside your Android Studio project (`File` -> `New` -> `Import Module` -> Enter the Source directory for the OpenCV's SDK and click `Ok` -> Name the Module as "OpenCV" and click `Finish`)
8) You may get an error importing, navigate to `build.gradle (Module: openCV)` inside your project, and [paste this inside](https://github.com/Malak47/DJI_Mobile_SDK_Android_V4/blob/fc803ec79002155ce2565655bd0657ff4d6b4b1d/OpenCV_build_temp.gradle)
9) Click on `Sync Project with Gradle Files`.
10) Open `Project Structure` -> `Dependencies`, under `Modules` click on `<All Modules>`, under `All dependencies` click the `+ Add dependency` button, click on `Module Dependency`, select `app` and click `Ok`, then check the box for OpenCV and click `Ok`.
11) Download yolov7-tiny.weights and add it to your project's assets folder. ([Link for download](https://github.com/AlexeyAB/darknet/releases/download/yolov4/yolov7-tiny.weights))
12) Plug in your Android device and run the code.
13) The interface is named ILM Remote Controller.
