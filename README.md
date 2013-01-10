README.MD

Quadrokinect, SinLab, EPFL 2012

Supervisied by: Alex Barchiesi

The idea of the project is to make a system that will control a group of ARDrones using the Kinect.

Presentation (prezi): http://prezi.com/-5l-jnnzpha0/quadrokinect-11122012/

Demo video: http://www.youtube.com/watch?v=6Ql-eCO1XeY

Content of this GitHub:

-ARDroneForP5_modified: the ARDroneForP5 library (http://kougaku-navi.net/ARDroneForP5/index_en.html) with some changes to work with the second version of Parrot ARDrones:

	• Completely changed video manager (no support for the first version of ARDrone)
	• Some changes in NAV manager (controls) (Hopefully still supports the ARDrone v1)

-Quadrokinect: the project's code. It uses the ARDroneForP5_modified project.

HowTo:

To test the Quadrokinect project, you will need:

	-Kinect
	-OpenNI Driver
	-OSCeleton (https://github.com/Sensebloom/OSCeleton)
	The installation tutorial for OpenNI and OSCeleton can be found at OSCeleton's GitHub

	-Parrot ARDrone (one or more)
	-Router (its IP must be 192.168.0.50, the server's (your machine) IP: 192.168.0.1)

After installing and buying all these components, you have to:

	-Connect to each ARDrone via WiFi
	-Connect to it by telnet:
		telnet 192.168.1.1
	-Run the command (described also in Quadrokinect/ArdroneChangeWiFi.info)
		ifconfig ath0 down ; iwconfig ath0 mode managed essid ArdroneNetwork ap any channel auto commit; ifconfig ath0 192.168.0.{!!!$(3+$THE_ARDRONE_NUMBER)!!!} netmask 255.255.255.0 up

		Note the 192.168.0.{!!!$(3+$THE_ARDRONE_NUMBER)!!!}: this IP address will be given to ARDrone. So for example, if you have 3 ARDrones:
			•The first one must be : 192.168.0.3;
			•The second one must be : 192.168.0.4;
			•The third one must be : 192.168.0.5;
			And so on...

		Of course, all IP settings can be changed, but you will also have to change the code for Quadrokinect.

Now ARDrones are connected.

	-Copy the OSCeleton launcher script (Quadrokinect/launcher) in OSCeleton root folder and launch it with ./launcher
	-Start the Quadrokinect project.

	There are some controlers in the Quadrokinect project, so your next actions will depend on them.

If you have any trouble (and you probably will :) ), send me an e-mail at: nikita.grishin@epfl.ch
