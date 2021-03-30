package com.unary.starfieldview;

/**
 * Attributes for an individual star. Since there may be thousands of instantiated stars this a
 * minimal class.
 */
public class CircularStar {

    /**
     * Point on the angle
     */
    float point;

    /**
     * Angle in radians
     */
    float theta;

    /**
     * Change to advance
     */
    float delta;

    /**
     * Radius of the star
     */
    float radius;
}