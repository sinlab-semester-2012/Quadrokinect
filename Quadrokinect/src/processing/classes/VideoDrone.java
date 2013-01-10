package processing.classes;

import com.shigeodayo.ardrone.ARDrone;

import processing.core.PImage;
import processing.sketch.QuadroKinectSketch;

public class VideoDrone {

	private QuadroKinectSketch parent;
	private ARDrone drone;
	private boolean connected = true;
	private int side;

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
				parent.translate(parent.width / 2 - 740,
						-parent.height / 2 + 300);
			if (connected) {
				PImage im = drone.getVideoImage(true);
				if (im != null){
					im.resize(640,  360);
					parent.image(im, 0, 0);					
				}
				else {					
					parent.text("No image from drone", 280, 180);
				}
			} else {
				parent.text("Drone not connected", 280, 180);
			}
			parent.stroke(255);
			parent.noFill();
			parent.rect(0, 0, 640, 360);
		}
		parent.popMatrix();
	}
}
