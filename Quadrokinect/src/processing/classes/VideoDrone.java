package processing.classes;

import com.shigeodayo.ardrone.ARDrone;

import processing.core.PImage;
import processing.sketch.QuadroKinectSketch;

public class VideoDrone {

	private QuadroKinectSketch parent;
	private ARDrone drone;
	private boolean connected = true;
	private int side;

	private int w = 425;
	private int h = 250;

	public VideoDrone(QuadroKinectSketch p, ARDrone d, int side) {
		this.parent = p;
		if (d == null)
			connected = false;
		else
			drone = d;
		this.side = side;
	}

	public void display() {
		parent.pushMatrix();
		{
			parent.translate(parent.width / 2, parent.height / 2);
			if (side < 0)
				parent.translate(-parent.width / 2 + 10,
						-parent.height / 2 + 300);
			else
				parent.translate(parent.width / 2 - w - 10,
						-parent.height / 2 + 300);
			if (connected) {
				PImage im = drone.getVideoImage(true);
				if (im != null) {
					im.resize(w, h);
					parent.image(im, 0, 0);
				} else {
					parent.text("No image from drone", w / 3, h / 2);
				}
			} else {
				parent.text("Drone not connected", w/3, h/2);
			}
			parent.stroke(255);
			parent.noFill();
			parent.rect(0, 0, w, h);
		}
		parent.popMatrix();
	}
}
