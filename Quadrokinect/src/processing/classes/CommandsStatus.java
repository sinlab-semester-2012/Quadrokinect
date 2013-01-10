package processing.classes;

import com.shigeodayo.ardrone.ARDrone;

import processing.classes.controllers.SkeletonController;
import processing.core.PFont;
import processing.sketch.QuadroKinectSketch;

public class CommandsStatus {

	private QuadroKinectSketch parent;

	private ARDrone drone;
	private boolean connected = true;
	private boolean inAir = false;

	private int side;
	private String command = "";

	public CommandsStatus(QuadroKinectSketch p, ARDrone g, int side) {
		parent = p;
		drone = g;
		if (drone == null) {
			connected = false;
		}
		this.side = side;
	}

	public int fbSpeed = 0;
	public int lrSpeed = 0;
	public int vrSpeed = 0;
	public int angSpeed = 0;

	public void display() {
		parent.pushMatrix();
		{
			parent.translate(parent.width / 2, parent.height / 2);
			if (side < 0)
				parent.translate(-parent.width / 2 + 10,
						-parent.height / 2 + 10);
			else
				parent.translate(parent.width / 2 - 400,
						-parent.height / 2 + 10);
			parent.noFill();
			if (fbSpeed == 0)
				parent.stroke(255, 255, 255);
			else if (fbSpeed > 0)
				parent.stroke(255, 255 - fbSpeed * 3, 255);
			else if (fbSpeed < 0)
				parent.stroke(255, 255, 255 + fbSpeed * 3);
			parent.strokeWeight(Math.abs(fbSpeed) + 1);
			parent.translate(50, 50);
			parent.ellipse(0, 0, 100, 100);
			//parent.rotateX((float) Math.PI);
			parent.stroke(0, 255, 0);
			parent.strokeWeight(3);
			parent.line(
					0,
					0,
					(float) ((float) -lrSpeed * (50.0 / (float) SkeletonController.default_speed)),
					(float) ((float) vrSpeed * (50.0 / (float) SkeletonController.default_speed)));
			parent.translate(60, -50);
			parent.stroke(255);
			parent.fill(255);
			//parent.rotateX((float) Math.PI);
			//parent.translate(0, -100);
			parent.textFont(parent.createFont(PFont.list()[1], 20));
			parent.text("Forward/Backward speed: " + fbSpeed, 0, 10);
			parent.text("Left/Right speed: " + lrSpeed, 0, 40);
			parent.text("Up/Down speed: " + vrSpeed, 0, 70);
			parent.text("Angular speed: " + angSpeed, 0, 100);
			parent.text("In air: " + (inAir ? "yes" : "no"), 0, 130);
			parent.text("Last command: " + command, 0, 160);
			parent.text("Connected: " + connected, 0, 190);
			if (connected) {
				parent.translate(-60, 230);
				parent.text("Battery: " + drone.getBatteryPercentage() + "%",
						0, 0);
				parent.text("Altitude: " + drone.getAltitude(), 0, 30);
			}
		}
		parent.popMatrix();
	}

	public enum CommandsTakeOffLandingEnum {
		TAKEOFF, LANDING;
	}

	public enum CommandsUpDownStopEnum {
		UP, DOWN, STOP, NOTHING;
	}

	public enum CommandsLeftRightEnum {
		LEFT, RIGHT, NOTHING;
	}

	public enum CommandsForwardBackwardEnum {
		FORWARD, BACKWARD, NOTHING;
	}

	public enum CommandsSpinLeftSpinRightEnum {
		SPIN_LEFT, SPIN_RIGHT, NOTHING;
	}

	public void updateCommand(CommandsTakeOffLandingEnum takeoffLanding) {
		command = takeoffLanding.toString();
		inAir = !inAir;
		System.out.println("Drone " + drone + ", " + takeoffLanding);
		if (connected) {
			switch (takeoffLanding) {
			case TAKEOFF:
				drone.takeOff();
				break;
			case LANDING:
				drone.landing();
				break;
			}
		}
	}

	public void stopDrone(boolean stop) {
		if (stop && !command.equals("STOP")) {
			command = "STOP";
			fbSpeed = 0;
			lrSpeed = 0;
			vrSpeed = 0;
			angSpeed = 0;
			System.out.println("Drone " + drone + ", STOP");
			if (drone != null && connected)
				drone.stop();
		} else if (!stop && command.equals("STOP")) {
			command = "";
		}
	}

	public void updateSpeeds(int fb, int lr, int vr, int ang) {
		if (!command.equals("STOP")) {
			if (lr != lrSpeed || fb != fbSpeed || vr != vrSpeed
					|| ang != angSpeed) {
				lrSpeed = lr;
				fbSpeed = fb;
				vrSpeed = vr;
				angSpeed = ang;
				System.out.println("Drone " + drone + ", move3d(" + lr + ", "
						+ fb + ", " + vr + ", " + ang + ")");
				if (drone != null && connected)
					drone.move3D(fbSpeed, lrSpeed, vrSpeed, angSpeed);
				if (!command.equals("MOVE"))
					command = "MOVE";
			}
		}
	}

	public void setConnected(boolean b) {
		connected = false;
	}

}
