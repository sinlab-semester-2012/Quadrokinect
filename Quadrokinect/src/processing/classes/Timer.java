package processing.classes;

import processing.core.PApplet;

// Learning Processing
// Daniel Shiffman
// http://www.learningprocessing.com

 
public class Timer {

	  public int savedTime;
	  int totalTime;
	  
	  PApplet parent;
	  
	  public Timer(int tempTotalTime, PApplet p) {
	    totalTime = tempTotalTime;
	    parent = p;
	  }
	  
	  public void start() {
	    savedTime = parent.millis(); 
	  }
	  
	  public boolean isFinished() {
	    int passedTime = parent.millis()- savedTime;
	    if (passedTime > totalTime) {
	      return true;
	    } else {
	      return false;
	    }
	  }
}
