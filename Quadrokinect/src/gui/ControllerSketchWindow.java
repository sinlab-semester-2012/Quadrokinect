package gui;

import javax.swing.JFrame;

import ardrone.ArdroneGroup;

import processing.sketch.QuadroKinectSketch;

/**
 * Class, used to show the window with QuadroKinectSketch inside
 * @author nikita grishin
 *
 */
public class ControllerSketchWindow {
	
	public static ArdroneGroup show(){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setTitle("Controller");
		QuadroKinectSketch sketch = new QuadroKinectSketch();
		frame.add(sketch);
		sketch.init();
		frame.setSize(QuadroKinectSketch.w, QuadroKinectSketch.h+20);
		return sketch.getArdrones();
	}
}
