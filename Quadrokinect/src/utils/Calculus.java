package utils;

public class Calculus {

	public static float getDistance(float[] a, float[] b) {
		float res = 0;
		for (int i = 0; i < min(a.length, b.length); i++) {
			res += Math.pow(a[i] - b[i], 2);
		}
		return (float) Math.sqrt(res);
	}

	public static float getDistance(Float[] a, Float[] b) {
		float res = 0;
		for (int i = 0; i < min(a.length, b.length); i++) {
			res += Math.pow(a[i] - b[i], 2);
		}
		return (float) Math.sqrt(res);
	}

	public static float calculateAngleBetween(float rightHand) {
		return 0;
	}

	public static int vectorProduct(float[] a, float[] b) {
		int res = 0;
		for (int i = 0; i < a.length; i++) {
			res += a[i] * b[i];
		}
		return res;
	}

	public static boolean isPointInsideSphere(float[] point,
			float[] sphereCenter, float sphereSize) {
		return getDistance(point, sphereCenter) < sphereSize;
	}

	public static boolean isPointInsideSquare(float[] point, float x1,
			float y1, float x2, float y2) {
		float maxX = max(x1, x2);
		float maxY = max(y1, y2);
		float minX = min(x1, x2);
		float minY = min(y1, y2);

		return point[0] > minX && point[0] < maxX && point[1] > minY
				&& point[1] < maxY;
	}

	public static float max(float a, float b) {
		if (a > b) {
			return a;
		} else
			return b;
	}

	public static float min(float a, float b) {
		if (a < b)
			return a;
		else
			return b;
	}

	public static float getDistance(Float[] rightHand, float[] safeZoneCoords) {
		float[] temp = new float[rightHand.length];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = rightHand[i];
		}
		return getDistance(temp, safeZoneCoords);
	}
}
