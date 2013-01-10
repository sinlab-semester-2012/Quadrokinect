package processing.classes.controllers;

import processing.sketch.QuadroKinectSketch;

public abstract class Controller {

	public static final int default_speed = 10;

	
	public abstract void display();
	public abstract void update(String bodyPart, Float[] coords);
	public abstract void reset();
	
	protected QuadroKinectSketch parent;
	
	public Controller(QuadroKinectSketch p){
		this.parent = p;
	}
}
