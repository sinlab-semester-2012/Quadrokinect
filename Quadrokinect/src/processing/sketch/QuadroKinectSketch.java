package processing.sketch;

import com.shigeodayo.ardrone.ARDrone;

import ardrone.ArdroneGroup;
import listeners.OscListener;
import processing.classes.CommandsStatus;
import processing.classes.CommandsStatus.CommandsTakeOffLandingEnum;
import processing.classes.CommandsStatus.CommandsUpDownStopEnum;
import processing.classes.controllers.Controller;
import processing.classes.controllers.HandsController;
import processing.classes.controllers.MarkerController;
//import processing.classes.controllers.SkeletonController;
import processing.classes.VideoDrone;
import processing.core.PApplet;

/**
 * Processing sketch
 * 
 * @author nikitagrishin
 * 
 */
public class QuadroKinectSketch extends PApplet {

	public static final String IP_ADDRESS_MASK = "192.168.0.";

	private static final long serialVersionUID = 1L;

	OscListener oscListener;
	Controller controller;
	CommandsStatus commandsRight;
	CommandsStatus commandsLeft;
	VideoDrone videoLeft;
	VideoDrone videoRight;

	MarkerController markerControlled;

	ArdroneGroup controlGroup;

	private int userTrackedID = -1;

	private int sceneZoom = -100;
	private int sceneRotX = 0;
	private int sceneRotY = 0;
	private int sceneRotZ = 0;
	private int sceneStep = 10;

	public static final int w = 1280;
	public static final int h = 800;

	public void setup() {
		size(w, h, P3D);
		frameRate(20);
		// Listener of OSC messages, comming from OSCeleton
		oscListener = new OscListener(this);
		// Adding drones in this group
		// addARDrones(controlGroup, 2);
		// ARDrone drone1 = new ARDrone("192.168.0.3");
		// ARDrone drone2 = new ARDrone("192.168.0.4");
		// controlGroup.addArdrone(drone1, 0);
		// controlGroup.addArdrone(drone2, 1);
		controlGroup.addArdrone(new ARDrone("192.168.1.1"), 0);
		controlGroup.connect();
		controlGroup.setMaxAltitude(3000);

		// controller = new HandsController(this);
		// commandsRight = new CommandsStatus(this, controlGroup.getARDrone(0),
		// -1);
		// videoRight = new VideoDrone(this, controlGroup.getARDrone(0), -1);
		commandsLeft = new CommandsStatus(this, controlGroup.getARDrone(0), 1);
		markerControlled = new MarkerController(this,
				controlGroup.getARDrone(0), 1);
		// videoLeft = new VideoDrone(this, controlGroup.getARDrone(1), 1);

	}

	public void draw() {
		background(0);
		lights();
		if (commandsRight != null)
			commandsRight.display();
		if (videoRight != null)
			videoRight.display();
		if (commandsLeft != null)
			commandsLeft.display();
		if (videoLeft != null)
			videoLeft.display();
		if (markerControlled != null)
			markerControlled.display();
		translate(width / 2, height / 2, sceneZoom);
		rotateX(sceneRotX);
		rotateY(sceneRotY);
		rotateZ(sceneRotZ);
		if (controller != null)
			controller.display();
	}

	public void updateSkeleton(String bodyPart, Float[] values) {
		controller.update(bodyPart, values);
	}

	public void resetSkeleton() {
		controller.reset();
	}

	public int getUserTrackedID() {
		return userTrackedID;
	}

	public void setUserTrackedID(int id) {
		userTrackedID = id;
	}

	public void addARDrones(ArdroneGroup group, int nb) {
		for (int i = 3; i < nb + 3; i++) {
			ARDrone drone = new ARDrone(IP_ADDRESS_MASK + i);
			group.addArdrone(drone, i - 3);
		}
	}

	public ArdroneGroup getArdrones() {
		return controlGroup;
	}

	public QuadroKinectSketch() {
		controlGroup = new ArdroneGroup(0);
	}

	public void keyPressed() {
		if (key == '-') {
			sceneZoom -= sceneStep;
		}
		if (key == '+') {
			sceneZoom += sceneStep;
		}
		if (key == ' ' || key == 'q') {
			System.out.println("SAFE!!!");
			controlGroup.safeDrone();
			markerControlled.setInAir(false);
		}
		if (key == 's') {
			controlGroup.stop();
		}
		if (key == 't') {
			controlGroup.takeOff();
			markerControlled.setInAir(true);
		}
	}

	public void updateCommand(int drone,
			CommandsTakeOffLandingEnum takeoffLandingSet) {
		switch (drone) {
		case 1:
			if (commandsLeft != null)
				commandsLeft.updateCommand(takeoffLandingSet);
			break;
		case -1:
			if (commandsRight != null)
				commandsRight.updateCommand(takeoffLandingSet);
			break;
		}
	}

	public void updateCommand(int drone, CommandsUpDownStopEnum command) {
		switch (drone) {
		case 1:
			if (commandsLeft != null)
				commandsLeft.stopDrone(command == CommandsUpDownStopEnum.STOP);
			break;
		case -1:
			if (commandsRight != null)
				commandsRight.stopDrone(command == CommandsUpDownStopEnum.STOP);
			break;
		}
	}

	public void updateCommand(int drone, int lrSpeed, int fbSpeed, int vrSpeed,
			int angSpeed) {
		switch (drone) {
		case 1:
			if (commandsLeft != null)
				commandsLeft.updateSpeeds(lrSpeed, fbSpeed, vrSpeed, angSpeed);
			break;
		case -1:
			if (commandsRight != null)
				commandsRight.updateSpeeds(lrSpeed, fbSpeed, vrSpeed, angSpeed);
			break;
		}
	}

	public void updateCommand(CommandsTakeOffLandingEnum takeoffLandingSet) {
		if (commandsLeft != null)
			commandsLeft.updateCommand(takeoffLandingSet);
		if (commandsRight != null)
			commandsRight.updateCommand(takeoffLandingSet);
	}

}
