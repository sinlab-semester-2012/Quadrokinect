package processing.classes.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.shigeodayo.ardrone.ARDrone;

import jp.nyatla.nyar4psg.MultiMarker;
import jp.nyatla.nyar4psg.NyAR4PsgConfig;

import processing.classes.CommandsStatus.CommandsUpDownStopEnum;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import processing.sketch.QuadroKinectSketch;
import utils.Calculus;

public class MarkerController implements ActionListener {

	private QuadroKinectSketch parent;
	private int id;

	private MultiMarker nya;
	private String camParamPath = "libs/nyar4psg-1.2.0/data/camera_para.dat";
	private String patternPath = "libs/nyar4psg-1.2.0/data/patt.hiro";

	private PVector[] defaultPosition;
	private PVector markerPoint = new PVector();

	private float delta = 50;
	private float defaultDistance = 100;

	private PVector speed = new PVector();

	private Timer timer;

	private int width = 640;
	private int height = 320;
	private boolean inAir = false;
	private boolean connected = true;

	private ARDrone drone;

	public MarkerController(QuadroKinectSketch p, ARDrone d, int id) {
		this.parent = p;
		nya = new MultiMarker(parent, width, height, camParamPath,
				NyAR4PsgConfig.CONFIG_PSG);
		nya.addARMarker(patternPath, 80);// id=0
		this.id = id;
		timer = new Timer(50, this);
		if (d == null)
			connected = false;
		else
			drone = d;
	}

	public void display() {
		parent.pushMatrix();
		parent.translate(parent.width - 320, 460);
		if (connected) {
			PImage im = drone.getVideoImage(true);
			if (im != null) {
				parent.imageMode(PImage.CENTER);
				im.resize(width, height);
				parent.image(im, 0, 0);

				try {
					nya.detect(im);
					if (nya.isExistMarker(0)) {
						parent.stroke(255, 0, 255);
						parent.strokeWeight(10);
						parent.noFill();

						defaultPosition = nya.getMarkerVertex2D(0);
						for (int i = 0; i < defaultPosition.length; i++) {
							parent.line(defaultPosition[i].x - width / 2,
									defaultPosition[i].y - height / 2,
									defaultPosition[i].z,
									defaultPosition[(i + 1)
											% defaultPosition.length].x
											- width / 2,
									defaultPosition[(i + 1)
											% defaultPosition.length].y
											- height / 2,
									defaultPosition[(i + 1)
											% defaultPosition.length].z);
						}

						markerPoint.x = (defaultPosition[0].x + defaultPosition[2].x)
								/ 2 - width / 2;
						markerPoint.y = (defaultPosition[0].y + defaultPosition[2].y)
								/ 2 - height / 2;
						markerPoint.z = defaultDistance
								- PVector.dist(defaultPosition[0],
										defaultPosition[2]);
					} else {
						markerPoint.x = 0;
						markerPoint.y = 0;
						markerPoint.z = 0;
					}
				} catch (Exception e) {
					markerPoint.x = 0;
					markerPoint.y = 0;
					markerPoint.z = 0;
				}
				parent.point(markerPoint.x, markerPoint.y);

				parent.strokeWeight(1);
				parent.fill(255, 255, 255);
				parent.textFont(parent.createFont(PFont.list()[0], 20));
				parent.text(markerPoint.x + ", " + markerPoint.y + ", "
						+ markerPoint.z, markerPoint.x + 10, markerPoint.y + 10);

				parent.stroke(0, 255, 0);
				parent.strokeWeight(10);
				parent.point(0, 0);
				parent.strokeWeight(3);
				parent.noFill();
				parent.rect(-delta, -delta, 2 * delta, 2 * delta);
			} else {
				parent.text("No image from drone", 280, 180);
			}

			performMovement();
		} else {
			parent.text("Drone not connected", 280, 180);
		}
		parent.stroke(255);
		parent.noFill();
		parent.rect(-320, -180, 640, 360);
		parent.imageMode(PImage.CORNER);
		parent.popMatrix();
	}

	public void performMovement() {
		if (inAir) {
			float[] temp = new float[] { markerPoint.x, markerPoint.y };
			if (Calculus
					.isPointInsideSquare(temp, -delta, -delta, delta, delta)) {
				if (Math.abs(speed.x) == Controller.default_speed) {
					speed.x = -Controller.default_speed / 2;
				}

				if (Math.abs(speed.y) == Controller.default_speed) {
					speed.y = -Controller.default_speed / 2;
				}
			} else {
				if (markerPoint.x < -delta) {
					speed.x = Controller.default_speed;
				} else if (markerPoint.x > delta) {
					speed.x = -Controller.default_speed;
				}

				if (markerPoint.y < -delta) {
					speed.y = -Controller.default_speed;
				} else if (markerPoint.y > delta) {
					speed.y = Controller.default_speed;
				}

			}

			if (speed.z > -delta && speed.x < delta) {
				if (Math.abs(speed.z) == Controller.default_speed) {
					speed.z = -Controller.default_speed / 2;
				}
			} else if (markerPoint.z < -delta) {
				speed.z = Controller.default_speed;
			} else if (markerPoint.x > delta) {
				speed.z = -Controller.default_speed;
			}
		}
	}

	public void setInAir(boolean bool) {
		inAir = bool;
		if (bool) {
			timer.start();
		} else {
			timer.stop();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (inAir) {
			if (speed.x == 0 && speed.y == 0 && speed.z == 0) {
				parent.updateCommand(id, CommandsUpDownStopEnum.STOP);
			} else {

				parent.updateCommand(id, CommandsUpDownStopEnum.NOTHING);
				parent.updateCommand(id, (int) (speed.y), (int) (speed.x),
						(int) (speed.z), 0);
				if (Math.abs(speed.x) == Controller.default_speed / 2) {
					speed.x = 0;
				}
				if (Math.abs(speed.y) == Controller.default_speed / 2) {
					speed.y = 0;
				}
				if (Math.abs(speed.z) == Controller.default_speed / 2) {
					speed.z = 0;
				}
			}
		}
	}
}
