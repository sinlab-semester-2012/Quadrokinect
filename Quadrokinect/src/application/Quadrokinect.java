package application;

import ardrone.ArdroneGroup;
import gui.ControllerSketchWindow;

/**
 * Quadrokinect starting point
 * 
 * @author nikita grishin
 *
 */
public class Quadrokinect {

	public static ArdroneGroup mainArdroneGroup;

	public static void main(String[] args) {
		mainArdroneGroup = ControllerSketchWindow.show();
	}
}
