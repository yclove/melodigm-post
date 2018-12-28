package com.melodigm.post.util;

import android.location.Location;

public class DistanceUtil {
	public static float getDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
		float[] distance = new float[1];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distance);
		return distance[0];
	}
}
