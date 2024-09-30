package com.dji.sdk.sample.demo.ILM;

/**
 * This interface defines a set of methods for flying a drone to a specific waypoint and maintaining
 * its position relative to the waypoint if it changes.
 * Implementing classes can use this interface to control the flight behavior of a drone,
 * directing it to a specific waypoint and ensuring it follows the waypoint even if it changes.
 */
public interface ILM_iGoTo {
    /**
     * This method moves the drone towards the waypoint.
     */
    public void goTo();

    public double distance();

    /**
     * There are 2 GoTo drone modes:
     * 1- The drone rotates itself and then flies towards the waypoint.
     * 2- The drone flies towards the waypoint without rotating itself.
     *
     * @return One of the 2 modes.
     */
    public int getMode();

    /**
     * This method sets the GoTo drone mode.
     *
     * @param mode - 1 or 2 (defined above)
     */
    public void setMode(int mode);

    /**
     * This method sets the waypoint that the drone will follow.
     *
     * @param lat - latitude.
     * @param lon - longitude.
     * @param alt - altitude.
     */
    public void setWaypoint(Double lat, Double lon, Double alt);

    /**
     * This method returns the latitude of the waypoint.
     *
     * @return the latitude of the waypoint.
     */
    public double getLat();

    /**
     * This method returns the longitude of the waypoint.
     *
     * @return the longitude of the waypoint.
     */
    public double getLon();

    /**
     * This method returns the altitude of the waypoint.
     *
     * @return the altitude of the waypoint.
     */
    public double getAlt();

    /**
     * This method sets the drone's speed.
     *
     * @param speed - the desired speed.
     */
    public void setSpeed(double speed);

    /**
     * This method sets the drone's radius, if the drone wasn't in the desired radius it stops following the waypoint or lands.
     *
     * @param radius - the desired radius.
     */
    public void setRadius(double radius);

    /**
     * This method sets the distance that the drone should keep from the waypoint.
     *
     * @param minDistance - the desired distance.
     */
    public void setMinDistance(double minDistance);
}

