package processing.classes.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import processing.classes.CommandsStatus.CommandsForwardBackwardEnum;
import processing.classes.CommandsStatus.CommandsLeftRightEnum;
import processing.classes.CommandsStatus.CommandsSpinLeftSpinRightEnum;
import processing.classes.CommandsStatus.CommandsTakeOffLandingEnum;
import processing.classes.CommandsStatus.CommandsUpDownStopEnum;
import processing.sketch.QuadroKinectSketch;
import utils.Calculus;

public class HandsController extends Controller implements ActionListener {

	public static final float handsSphereSize = 5;
	public static final float controllerSphereSize = 20;
	public static final float safeZoneSphereSize = 30;
	public static final float safeAngle = (float) (Math.PI / 6.0f);

	public static final float minDistanceForClack = 25;

	private Timer timer;
	private processing.classes.Timer processingTimer;

	Float[] rightHand;
	Float[] leftHand;
	float[] controller;

	float[] safeZoneCoords;

	private CommandsTakeOffLandingEnum takeoffLandingSet = CommandsTakeOffLandingEnum.LANDING;
	private CommandsUpDownStopEnum upDownStopSet = CommandsUpDownStopEnum.NOTHING;
	private CommandsLeftRightEnum leftRightSet = CommandsLeftRightEnum.NOTHING;
	private CommandsForwardBackwardEnum forwardBackwardSet = CommandsForwardBackwardEnum.NOTHING;
	private CommandsSpinLeftSpinRightEnum spinSet = CommandsSpinLeftSpinRightEnum.NOTHING;

	public HandsController(QuadroKinectSketch p) {
		super(p);
		controller = new float[] { 0, 0, 0 };
		timer = new Timer(50, this);
		processingTimer = new processing.classes.Timer(2000, parent);
		timer.start();
	}

	@Override
	public void display() {
		drawScene();
		drawControllers();
	}

	@Override
	public void update(String bodyPart, Float[] coords) {
		if (bodyPart.equals("r_hand")) {
			rightHand = coords;
			updateController();
		} else if (bodyPart.equals("l_hand")) {
			leftHand = coords;
			updateController();
		}
	}

	@Override
	public void reset() {
		rightHand = null;
		leftHand = null;
		controller = new float[] { 0, 0, 0 };
	}

	private void updateController() {
		for (int i = 0; i < 3; i++) {
			controller[i] = (rightHand[i] + leftHand[i]) * 0.5f;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (rightHand != null && leftHand != null) {
			if (Calculus.getDistance(rightHand, leftHand) < minDistanceForClack) {
				if (processingTimer.isFinished()) {
					processingTimer.start();
					switch (takeoffLandingSet) {
					case TAKEOFF:
						takeoffLandingSet = CommandsTakeOffLandingEnum.LANDING;
						break;
					case LANDING:
						takeoffLandingSet = CommandsTakeOffLandingEnum.TAKEOFF;
						setUpSafeZone();
						break;
					}
					parent.updateCommand(takeoffLandingSet);
				}
			}

			if (takeoffLandingSet == CommandsTakeOffLandingEnum.TAKEOFF
					&& processingTimer.isFinished()) {
				// System.out.println(1);

				if (Calculus.isPointInsideSphere(controller, safeZoneCoords,
						safeZoneSphereSize)) {
					upDownStopSet = CommandsUpDownStopEnum.STOP;
					leftRightSet = CommandsLeftRightEnum.NOTHING;
					forwardBackwardSet = CommandsForwardBackwardEnum.NOTHING;
				} else {
					if (controller[1] - safeZoneCoords[1] < -safeZoneSphereSize) {
						upDownStopSet = CommandsUpDownStopEnum.UP;
					} else if (controller[1] - safeZoneCoords[1] > safeZoneSphereSize) {
						upDownStopSet = CommandsUpDownStopEnum.DOWN;
					} else {
						upDownStopSet = CommandsUpDownStopEnum.NOTHING;
					}

					if (controller[0] - safeZoneCoords[0] < -safeZoneSphereSize) {
						leftRightSet = CommandsLeftRightEnum.LEFT;
					} else if (controller[0] - safeZoneCoords[0] > safeZoneSphereSize) {
						leftRightSet = CommandsLeftRightEnum.RIGHT;
					} else {
						leftRightSet = CommandsLeftRightEnum.NOTHING;
					}

					if (controller[2] - safeZoneCoords[2] < -safeZoneSphereSize) {
						forwardBackwardSet = CommandsForwardBackwardEnum.FORWARD;
					} else if (controller[2] - safeZoneCoords[2] > safeZoneSphereSize) {
						forwardBackwardSet = CommandsForwardBackwardEnum.BACKWARD;
					} else {
						forwardBackwardSet = CommandsForwardBackwardEnum.NOTHING;
					}
				}

				float ang = (float) Math.asin(Math.abs(rightHand[2]
						- controller[2])
						/ Calculus.getDistance(rightHand, controller));
				//System.out.println(ang);
				if (ang > safeAngle) {
					if (rightHand[2] - controller[2] > 0)
						spinSet = CommandsSpinLeftSpinRightEnum.SPIN_LEFT;
					else
						spinSet = CommandsSpinLeftSpinRightEnum.SPIN_RIGHT;
				} else
					spinSet = CommandsSpinLeftSpinRightEnum.NOTHING;

				int fbSpeed = 0;
				int lrSpeed = 0;
				int vrSpeed = 0;
				int angSpeed = 0;

				switch (spinSet) {
				case SPIN_LEFT:
					angSpeed = default_speed;
					break;
				case SPIN_RIGHT:
					angSpeed = -default_speed;
					break;
				case NOTHING:
					angSpeed = 0;
					break;
				}

				switch (leftRightSet) {
				case LEFT:
					lrSpeed = default_speed;
					break;
				case RIGHT:
					lrSpeed = -default_speed;
					break;
				case NOTHING:
					lrSpeed = 0;
					break;
				}

				switch (forwardBackwardSet) {
				case FORWARD:
					fbSpeed = default_speed;
					break;
				case BACKWARD:
					fbSpeed = -default_speed;
					break;
				case NOTHING:
					fbSpeed = 0;
					break;
				}

				switch (upDownStopSet) {
				case STOP:
					fbSpeed = 0;
					lrSpeed = 0;
					vrSpeed = 0;
					break;
				case UP:
					vrSpeed = -default_speed;
					break;
				case DOWN:
					vrSpeed = default_speed;
					break;
				case NOTHING:
					vrSpeed = 0;
					break;
				}

				if (fbSpeed == 0 && lrSpeed == 0 && vrSpeed == 0
						&& angSpeed == 0) {
					parent.updateCommand(-1, CommandsUpDownStopEnum.STOP);
				} else {
					parent.updateCommand(-1, CommandsUpDownStopEnum.NOTHING);
					parent.updateCommand(-1, fbSpeed, lrSpeed, vrSpeed,
							angSpeed);
				}
			}
		}
	}

	private void drawControllers() {
		if (rightHand != null && leftHand != null) {
			parent.pushMatrix();
			{
				parent.noStroke();
				parent.fill(255, 100);
				parent.pushMatrix();
				parent.translate(rightHand[0], rightHand[1], rightHand[2]);
				parent.sphere(handsSphereSize);
				parent.popMatrix();
				parent.pushMatrix();
				parent.translate(leftHand[0], leftHand[1], leftHand[2]);
				parent.sphere(handsSphereSize);
				parent.popMatrix();
				parent.pushMatrix();
				parent.fill(255, 0, 255, 100);
				parent.translate(controller[0], controller[1], controller[2]);
				parent.sphere(controllerSphereSize);
				parent.popMatrix();
			}
			parent.popMatrix();
		}
	}

	private void drawScene() {
		if (takeoffLandingSet == CommandsTakeOffLandingEnum.TAKEOFF) {
			parent.pushMatrix();
			parent.translate(safeZoneCoords[0], safeZoneCoords[1],
					safeZoneCoords[2]);
			parent.noStroke();
			parent.fill(255, 0, 255, 100);
			parent.sphere(safeZoneSphereSize);
			parent.popMatrix();
		}
	}

	private void setUpSafeZone() {
		safeZoneCoords = controller.clone();
	}

}
