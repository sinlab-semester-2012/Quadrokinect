package ardrone;

import java.util.HashMap;

import processing.sketch.QuadroKinectSketch;

import com.shigeodayo.ardrone.ARDrone;

public class ArdroneGroup {

	public static final int STARTING_PORT = 5556;
	public static final int NAV_PORT = 6666;
	private int id;

	private boolean connected = true;

	private HashMap<Integer, ARDrone> ardrones = new HashMap<Integer, ARDrone>();

	private boolean inAir = false;
	private boolean stopped = true;

	public ArdroneGroup(int groupID) {
		id = groupID;
	}

	public int getID() {
		return id;
	}

	public ARDrone getARDrone(int id) {
		return ardrones.get(id);
	}

	public void addArdrone(ARDrone a, int id) {
		if (!ardrones.containsKey(id)) {
			ardrones.put(id, a);
			a.setMaxAltitude(6000);
		}
	}

	public boolean connect() {
		boolean hasConnection = false;
		if (connected) {
			for (int i : ardrones.keySet()) {
				hasConnection = ardrones.get(i).connect(STARTING_PORT + i);
				if (hasConnection) {
					ardrones.get(i).connectNav(NAV_PORT + i);
					System.out.println("Connecting video for "
							+ QuadroKinectSketch.IP_ADDRESS_MASK + i);
					ardrones.get(i).connectVideo();
					ardrones.get(i).start();
					System.out.println("Drone conected: " + i);
				} else {
					System.out.println("No connection with ardrone on "
							+ QuadroKinectSketch.IP_ADDRESS_MASK + i);
				}
			}
		}
		return hasConnection;
	}

	public void safeDrone() {
		if (connected)
			for (ARDrone a : ardrones.values()) {
				a.stop();
				a.landing();
			}
		inAir = false;
		stopped = true;
	}

	public void stop() {
		if (connected)
			for (ARDrone a : ardrones.values()) {
				a.stop();
			}
		stopped = true;
	}

	public void takeOff() {
		if (connected) {
			for (ARDrone a : ardrones.values()) {
				a.takeOff();
			}
		}
		inAir = true;
		stopped = true;
	}

	public void landing() {
		if (connected) {
			for (ARDrone a : ardrones.values()) {
				a.landing();
			}
		}
		inAir = false;
		stopped = true;
	}

	public boolean inAir() {
		return inAir;
	}

	public boolean isStopped() {
		return stopped;
	}

	public int getArdroneNumber() {
		if (ardrones.size() > 0)
			return ardrones.size();
		else
			return 1;
	}

	public boolean isConnected() {
		return connected;
	}

	public void toggleCamera() {
		if (connected) {
			for (ARDrone drone : ardrones.values()) {
				drone.toggleCamera();
			}
		}
	}

	public void move3d(int x, int y, int z, int ang) {
		System.out.println("x=" + x + " y=" + y + " z=" + z + " ang=" + ang);
		if (connected) {
			for (ARDrone drone : ardrones.values()) {
				drone.move3D(x, y, z, ang);
			}
		}
	}

	public void setMaxAltitude(int i) {
		if (connected) {
			for (ARDrone drone : ardrones.values()) {
				drone.setMaxAltitude(i);
			}
		}
	}
}
