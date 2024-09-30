package com.dji.sdk.sample.demo.ILM;

/**
 * This interface defines a method that controls the drone's camera angles.
 * Implementing it to change the roll/yaw/pitch angle of the camera.
 */

public interface ILM_iAdjustCamera {
    /**
     * This method is used to adjust camera angles.
     */
    public void adjustCamera();

    /**
     * This method defines the camera's yaw angle.
     *
     * @param value - yaw angle.
     */
    public void setYaw(int value);

    /**
     * This method returns the camera's yaw angle.
     *
     * @return yaw angle.
     */
    public int getYaw();

    /**
     * This method defines the camera's roll angle.
     *
     * @param value - roll angle.
     */
    public void setRoll(int value);

    /**
     * This method returns the camera's roll angle.
     *
     * @return roll angle.
     */
    public int getRoll();

    /**
     * This method defines the camera's pitch angle.
     *
     * @param value - pitch angle.
     */
    public void setPitch(int value);

    /**
     * This method returns the camera's pitch angle.
     *
     * @return pitch angle.
     */
    public int getPitch();
}
