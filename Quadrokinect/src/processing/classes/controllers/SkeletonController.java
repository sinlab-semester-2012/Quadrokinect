package processing.classes.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Set;
import javax.swing.Timer;

import processing.classes.CommandsStatus.CommandsForwardBackwardEnum;
import processing.classes.CommandsStatus.CommandsLeftRightEnum;
import processing.classes.CommandsStatus.CommandsSpinLeftSpinRightEnum;
import processing.classes.CommandsStatus.CommandsTakeOffLandingEnum;
import processing.classes.CommandsStatus.CommandsUpDownStopEnum;
import processing.sketch.QuadroKinectSketch;
import utils.Calculus;

public class SkeletonController extends Controller implements ActionListener{

	private HashMap<String, Float[]> skeleton = new HashMap<String, Float[]>();

	private Timer timer;
	private processing.classes.Timer processingTimer;

	boolean leftDroneInAir = false;
	boolean rightDroneInAir = false;

	private float[] limitationBox;
	private float limitationBoxSize = 0;

	private boolean skeletonBox = false;
	private boolean isSkeletonInsideBox = true;

	private CommandsUpDownStopEnum leftHandSet = CommandsUpDownStopEnum.NOTHING;
	private CommandsUpDownStopEnum rightHandSet = CommandsUpDownStopEnum.NOTHING;
	private CommandsTakeOffLandingEnum leftTakeoffLandingSet;
	private CommandsTakeOffLandingEnum rightTakeoffLandingSet;

	private CommandsSpinLeftSpinRightEnum spinSet = CommandsSpinLeftSpinRightEnum.NOTHING;
	private CommandsLeftRightEnum leftRightSet = CommandsLeftRightEnum.NOTHING;
	private CommandsForwardBackwardEnum forwardBakwardSet = CommandsForwardBackwardEnum.NOTHING;

	public SkeletonController(QuadroKinectSketch p) {
		super(p);
		timer = new Timer(50, this);
		timer.start();
		processingTimer = new processing.classes.Timer(1000, parent);
	}

	@Override
	public void update(String bodyPart, Float[] coords) {
		skeleton.put(bodyPart, coords);
	}

	public void display() {
		drawSceleton();
		drawZones();
	}

	public void reset() {
		skeleton.clear();
	}

	public void drawSceleton() {
		Float[] temp;
		Set<String> set = skeleton.keySet();
		try {
			for (String bodyPart : set) {
				parent.pushMatrix();
				{
					parent.fill(0, 255, 0, 100);
					parent.noStroke();
					temp = skeleton.get(bodyPart);
					parent.translate(temp[0], temp[1], temp[2]);
					parent.sphere(15);
					parent.stroke(255);
					parent.fill(0);
				}
				parent.popMatrix();
			}
		} catch (ConcurrentModificationException e) {
		}
	}

	public void drawZones() {
		// Takeoff/Landing line
		parent.pushMatrix();
		{
			try {
				Float[] rightShoulder = skeleton.get("r_shoulder");
				Float[] leftShoulder = skeleton.get("l_shoulder");
				parent.translate(rightShoulder[0], rightShoulder[1],
						rightShoulder[2]);
				parent.line(0, 0, leftShoulder[0] - rightShoulder[0],
						leftShoulder[1] - rightShoulder[1]);
			} catch (NullPointerException e) {
			}
		}
		parent.popMatrix();

		// Right side hand
		parent.pushMatrix();
		{
			try {
				Float[] elbow = skeleton.get("r_elbow");
				Float[] neck = skeleton.get("neck");
				parent.noFill();
				parent.translate(elbow[0], elbow[1], elbow[2]);

				parent.rect(0, 0, neck[0] - elbow[0], neck[1] - elbow[1]);// Permission
																			// to
																			// move
				parent.rect(0, 0, elbow[0] - neck[0], elbow[1] - neck[1]);// DOWN
																			// square
				parent.rect(0, 0, neck[0] - elbow[0], elbow[1] - neck[1]);// Stop
																			// square
				parent.rect(0, 0, elbow[0] - neck[0], neck[1] - elbow[1]); // Up
																			// square
			} catch (NullPointerException e) {
			}
		}
		parent.popMatrix();

		// Left side hand
		parent.pushMatrix();
		{
			try {
				Float[] elbow = skeleton.get("l_elbow");
				Float[] neck = skeleton.get("neck");
				parent.noFill();
				parent.translate(elbow[0], elbow[1], elbow[2]);

				parent.rect(0, 0, neck[0] - elbow[0], neck[1] - elbow[1]);// Permission
																			// to
																			// move
				parent.rect(0, 0, elbow[0] - neck[0], elbow[1] - neck[1]);// Down
																			// square
				parent.rect(0, 0, neck[0] - elbow[0], elbow[1] - neck[1]);// Stop
																			// square
				parent.rect(0, 0, elbow[0] - neck[0], neck[1] - elbow[1]); // Up
																			// square
			} catch (NullPointerException e) {
			}
		}
		parent.popMatrix();

		// SkeletonBox
		if (skeletonBox) {
			parent.pushMatrix();
			{
				try {
					if (isSkeletonInsideBox) {
						parent.fill(0, 255, 0, 100);
						parent.stroke(0);
					} else {
						parent.noFill();
						parent.stroke(255);
					}
					parent.translate(limitationBox[1], limitationBox[2],
							limitationBox[3]);

					parent.box(limitationBoxSize);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			parent.popMatrix();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			Float[] rightHand = skeleton.get("r_hand");
			Float[] leftHand = skeleton.get("l_hand");
			Float[] rightElbow = skeleton.get("r_elbow");
			Float[] leftElbow = skeleton.get("l_elbow");

			// TAKEOFF and LANDING for both
			if (processingTimer.isFinished()) {
				boolean needToSetupSkeletonBox = !rightDroneInAir
						&& !leftDroneInAir;
				if (rightHand[0] > skeleton.get("r_shoulder")[0]
						&& rightHand[0] < skeleton.get("l_shoulder")[0]
						&& rightHand[1] < skeleton.get("r_shoulder")[1]) {
					if (!rightDroneInAir) {
						rightDroneInAir = true;
						// TAKEOFF RIGHT
						rightTakeoffLandingSet = CommandsTakeOffLandingEnum.TAKEOFF;
					} else {
						rightDroneInAir = false;
						// LANDING RIGHT
						rightTakeoffLandingSet = CommandsTakeOffLandingEnum.LANDING;
					}
					parent.updateCommand(-1, rightTakeoffLandingSet);
				}

				if (leftHand[0] > skeleton.get("r_shoulder")[0]
						&& leftHand[0] < skeleton.get("l_shoulder")[0]
						&& leftHand[1] < skeleton.get("r_shoulder")[1]) {
					if (!leftDroneInAir) {
						leftDroneInAir = true;
						// TAKEOFF LEFT
						leftTakeoffLandingSet = CommandsTakeOffLandingEnum.TAKEOFF;
					} else {
						leftDroneInAir = false;
						// LANDING LEFT
						leftTakeoffLandingSet = CommandsTakeOffLandingEnum.LANDING;
					}
					parent.updateCommand(1, leftTakeoffLandingSet);
				}
				processingTimer.start();

				if (needToSetupSkeletonBox) {
					limitationBox = new float[] {
							skeleton.get("r_shoulder")[0],
							skeleton.get("head")[0], skeleton.get("head")[1],
							skeleton.get("head")[2] };
					skeletonBox = false;
					limitationBoxSize = Math.abs(limitationBox[1]
							- limitationBox[0]);
				} else {
					skeletonBox = true;
				}
			}

			float[] rightHandVector = new float[] {
					rightHand[0] - rightElbow[0], rightHand[1] - rightElbow[1] };
			float[] leftHandVector = new float[] { leftHand[0] - leftElbow[0],
					leftHand[1] - leftElbow[1] };

			// RIGHT HAND//
			// ////////STOP UP DOWN states/////////
			if (Calculus.isPointInsideSquare(rightHandVector, 0, 0,
					skeleton.get("neck")[0] - rightElbow[0], rightElbow[1]
							- skeleton.get("neck")[1])) {
				// STOP
				// System.out.println("STOP");
				rightHandSet = CommandsUpDownStopEnum.STOP;
			} else if (Calculus.isPointInsideSquare(rightHandVector, 0, 0,
					rightElbow[0] - skeleton.get("neck")[0],
					skeleton.get("neck")[1] - rightElbow[1])) {
				// UP
				// System.out.println("UP");
				rightHandSet = CommandsUpDownStopEnum.UP;
			} else if (Calculus.isPointInsideSquare(rightHandVector, 0, 0,
					rightElbow[0] - skeleton.get("neck")[0], rightElbow[1]
							- skeleton.get("neck")[1])) {
				// DOWN
				// System.out.println("DOWN");
				rightHandSet = CommandsUpDownStopEnum.DOWN;
			} else {
				// System.out.println("NOTHING");
				rightHandSet = CommandsUpDownStopEnum.NOTHING;
			}
			// DONE RIGHT HAND//

			// LEFT HAND//
			// ////////STOP UP DOWN states/////////
			if (Calculus.isPointInsideSquare(leftHandVector, 0, 0,
					skeleton.get("neck")[0] - leftElbow[0], leftElbow[1]
							- skeleton.get("neck")[1])) {
				// STOP
				// System.out.println("STOP");
				leftHandSet = CommandsUpDownStopEnum.STOP;
			} else if (Calculus.isPointInsideSquare(leftHandVector, 0, 0,
					leftElbow[0] - skeleton.get("neck")[0],
					skeleton.get("neck")[1] - leftElbow[1])) {
				// UP
				// System.out.println("UP");
				leftHandSet = CommandsUpDownStopEnum.UP;
			} else if (Calculus.isPointInsideSquare(leftHandVector, 0, 0,
					leftElbow[0] - skeleton.get("neck")[0], leftElbow[1]
							- skeleton.get("neck")[1])) {
				// DOWN
				// System.out.println("DOWN");
				leftHandSet = CommandsUpDownStopEnum.DOWN;
			} else {
				// System.out.println("NOTHING");
				leftHandSet = CommandsUpDownStopEnum.NOTHING;
			}
			// DONE LEFT HAND//

			// ////////LEFT RIGHT states/////////
			if (skeleton.get("head")[0] < limitationBox[1] - limitationBoxSize
					/ 2) {
				// LEFT
				// if (skeletonBox)
				// System.out.println("LEFT");
				leftRightSet = CommandsLeftRightEnum.LEFT;
			} else if (skeleton.get("head")[0] > limitationBox[1]
					+ limitationBoxSize / 2) {
				// RIGHT
				// if (skeletonBox)
				// System.out.println("RIGHT");
				leftRightSet = CommandsLeftRightEnum.RIGHT;
			} else {
				leftRightSet = CommandsLeftRightEnum.NOTHING;
			}

			// ////////FORWARD BACKWARD states/////////
			if (skeleton.get("head")[2] > limitationBox[3]
							+ limitationBoxSize / 2) {
				// BACKWARD
				// if (skeletonBox)
				// System.out.println("BACKWARD");
				forwardBakwardSet = CommandsForwardBackwardEnum.BACKWARD;
			} else if (skeleton.get("head")[2] < limitationBox[3] - limitationBoxSize
					/ 2) {
				// FORWARD
				// if (skeletonBox)
				// System.out.println("FORWARD");
				forwardBakwardSet = CommandsForwardBackwardEnum.FORWARD;
			} else {
				// if (skeletonBox)
				// System.out.println("NOTHING");
				forwardBakwardSet = CommandsForwardBackwardEnum.NOTHING;
			}

			isSkeletonInsideBox = forwardBakwardSet == CommandsForwardBackwardEnum.NOTHING
					&& leftRightSet == CommandsLeftRightEnum.NOTHING;

			// Analyse states for left hand
			if (leftDroneInAir) {
				if (leftHandSet == CommandsUpDownStopEnum.STOP) {
					parent.updateCommand(1, CommandsUpDownStopEnum.STOP);
				} else {
					parent.updateCommand(1, CommandsUpDownStopEnum.NOTHING);
					int lrSpeed = 0;
					int fbSpeed = 0;
					int vrSpeed = 0;
					int angSpeed = 0;

					switch (leftHandSet) {
					case UP:
						vrSpeed = default_speed;
						break;
					case DOWN:
						vrSpeed = -default_speed;
						break;
					case NOTHING:
						vrSpeed = 0;
						break;
					case STOP:
						System.out
								.println("Should not be here! If it is - I'm stupid guy, should work in McDonnald's =(");
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

					switch (forwardBakwardSet) {
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

					parent.updateCommand(1, lrSpeed, fbSpeed, vrSpeed, angSpeed);
				}
			}

			// Analyse states for right hand
			if (rightDroneInAir) {
				if (rightHandSet == CommandsUpDownStopEnum.STOP) {
					parent.updateCommand(-1, CommandsUpDownStopEnum.STOP);
				} else {
					parent.updateCommand(-1, CommandsUpDownStopEnum.NOTHING);
					int lrSpeed = 0;
					int fbSpeed = 0;
					int vrSpeed = 0;
					int angSpeed = 0;
					
					switch (rightHandSet) {
					case UP:
						vrSpeed = default_speed;
						break;
					case DOWN:
						vrSpeed = -default_speed;
						break;
					case NOTHING:
						vrSpeed = 0;
						break;
					case STOP:
						System.out
								.println("Should not be here! If it is - I'm stupid guy, should work in McDonnald's =(");
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

					switch (forwardBakwardSet) {
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
					
					//System.out.println("WTF?!?!");
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
					parent.updateCommand(-1, lrSpeed, fbSpeed, vrSpeed,
							angSpeed);
				}
			}
		} catch (NullPointerException e) {
		}
	}

}
