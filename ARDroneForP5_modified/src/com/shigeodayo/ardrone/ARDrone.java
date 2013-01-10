/*
 *
  Copyright (c) <2011>, <Shigeo Yoshida>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
The names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.shigeodayo.ardrone;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import processing.core.PConstants;
import processing.core.PImage;

import com.shigeodayo.ardrone.command.CommandManager;
import com.shigeodayo.ardrone.navdata.AttitudeListener;
import com.shigeodayo.ardrone.navdata.BatteryListener;
import com.shigeodayo.ardrone.navdata.DroneState;
import com.shigeodayo.ardrone.navdata.NavDataManager;
import com.shigeodayo.ardrone.navdata.StateListener;
import com.shigeodayo.ardrone.navdata.VelocityListener;
import com.shigeodayo.ardrone.video.ImageListener;
import com.shigeodayo.ardrone.video.VideoManager;

public class ARDrone implements ARDroneInterface, ImageListener,
		AttitudeListener, BatteryListener, StateListener, VelocityListener {

	/** default ip address */
	private static final String IP_ADDRESS = "192.168.1.1";

	private String ipaddr = null;
	private InetAddress inetaddr = null;

	// managers
	private CommandManager manager = null;
	private VideoManager videoManager = null;
	private NavDataManager navdataManager = null;

	// listeners
	private ImageListener imageListener = null;
	private AttitudeListener attitudeListener = null;
	private BatteryListener batteryListener = null;
	private StateListener stateListener = null;
	private VelocityListener velocityListener = null;

	private BufferedImage videoImage = null;

	private float pitch = 0.0f;
	private float roll = 0.0f;
	private float yaw = 0.0f;
	private float altitude = 0.0f;

	private int battery = 0;

	private DroneState state = null;

	private float vx = 0.0f;
	private float vy = 0.0f;
	private float[] velocity = new float[2];

	/** constructor */
	public ARDrone() {
		this(IP_ADDRESS);
	}

	/**
	 * constructor
	 * 
	 * @param ipaddr
	 */
	public ARDrone(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	/** connect to AR.Drone */
	@Override
	public boolean connect(int port) {
		if (inetaddr == null) {
			inetaddr = getInetAddress(ipaddr);
		}
		manager = new CommandManager(inetaddr);
		return manager.connect(port);
	}

	/** connect video */
	@Override
	public boolean connectVideo() {
		if (inetaddr == null) {
			inetaddr = getInetAddress(ipaddr);
		}
		videoManager = new VideoManager(inetaddr, manager);
		videoManager.setImageListener(new ImageListener() {
			@Override
			public void imageUpdated(BufferedImage image) {
				if (imageListener != null) {
					imageListener.imageUpdated(image);
				}
			}
		});
		addImageUpdateListener(this);
		return videoManager.connect();
	}

	/** connect navdata */
	@Override
	public boolean connectNav(int port) {
		if (inetaddr == null) {
			inetaddr = getInetAddress(ipaddr);
		}
		navdataManager = new NavDataManager(inetaddr, manager);
		navdataManager.setAttitudeListener(new AttitudeListener() {
			@Override
			public void attitudeUpdated(float pitch, float roll, float yaw,
					int altitude) {
				if (attitudeListener != null) {
					attitudeListener
							.attitudeUpdated(pitch, roll, yaw, altitude);
				}
			}
		});
		navdataManager.setBatteryListener(new BatteryListener() {
			@Override
			public void batteryLevelChanged(int percentage) {
				if (batteryListener != null) {
					batteryListener.batteryLevelChanged(percentage);
				}
			}
		});
		navdataManager.setStateListener(new StateListener() {
			@Override
			public void stateChanged(DroneState state) {
				if (stateListener != null) {
					stateListener.stateChanged(state);
				}
			}
		});
		navdataManager.setVelocityListener(new VelocityListener() {
			@Override
			public void velocityChanged(float vx, float vy, float vz) {
				if (velocityListener != null) {
					velocityListener.velocityChanged(vx, vy, vz);
				}
			}
		});
		addAttitudeUpdateListener(this);
		addBatteryUpdateListener(this);
		addStateUpdateListener(this);
		addVelocityUpdateListener(this);
		return navdataManager.connect(port);
	}

	@Override
	public void disconnect() {
		stop();
		landing();
		manager.close();
		if (videoManager != null)
			videoManager.close();
		if (navdataManager != null)
			navdataManager.close();
	}

	@Override
	public void start() {
		if (manager != null)
			new Thread(manager).start();
		if (videoManager != null)
			new Thread(videoManager).start();
		if (navdataManager != null)
			new Thread(navdataManager).start();
	}

	@Override
	public void toggleCamera() {
		if (manager != null)
			manager.toggleCamera();
	}

	@Override
	public void landing() {
		if (manager != null)
			manager.landing();
	}

	@Override
	public void takeOff() {
		if (manager != null)
			manager.takeOff();
	}

	@Override
	public void reset() {
		if (manager != null)
			manager.reset();
	}

	@Override
	public void stop() {
		if (manager != null)
			manager.stop();
	}

	/**
	 * 0.01-1.0 -> 1-100%
	 * 
	 * @return 1-100%
	 */
	@Override
	public int getSpeed() {
		if (manager == null)
			return -1;
		return manager.getSpeed();
	}

	@Override
	public void setMaxAltitude(int altitude) {
		if (manager != null)
			manager.setMaxAltitude(altitude);
	}

	@Override
	public void setMinAltitude(int altitude) {
		if (manager != null)
			manager.setMinAltitude(altitude);
	}

	@Override
	public void move3D(int speedX, int speedY, int speedZ, int speedSpin) {
		if (manager != null)
			manager.move3D(speedX, speedY, speedZ, speedSpin);
		else 
			System.out.println("Error: manager = null!!!");
	}

	// update listeners
	public void addImageUpdateListener(ImageListener imageListener) {
		this.imageListener = imageListener;
	}

	public void addAttitudeUpdateListener(AttitudeListener attitudeListener) {
		this.attitudeListener = attitudeListener;
	}

	public void addBatteryUpdateListener(BatteryListener batteryListener) {
		this.batteryListener = batteryListener;
	}

	public void addStateUpdateListener(StateListener stateListener) {
		this.stateListener = stateListener;
	}

	public void addVelocityUpdateListener(VelocityListener velocityListener) {
		this.velocityListener = velocityListener;
	}

	// remove listeners
	public void removeImageUpdateListener() {
		imageListener = null;
	}

	public void removeAttitudeUpdateListener() {
		attitudeListener = null;
	}

	public void removeBatteryUpdateListener() {
		batteryListener = null;
	}

	public void removeStateUpdateListener() {
		stateListener = null;
	}

	public void removeVelocityUpdateListener() {
		velocityListener = null;
	}

	/**
	 * print error message
	 * 
	 * @param message
	 * @param obj
	 */
	public static void error(String message, Object obj) {
		System.err.println("[" + obj.getClass() + "] " + message);
	}

	private InetAddress getInetAddress(String ipaddr) {
		InetAddress inetaddr = null;
		StringTokenizer st = new StringTokenizer(ipaddr, ".");
		byte[] ipBytes = new byte[4];
		if (st.countTokens() == 4) {
			for (int i = 0; i < 4; i++) {
				ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
			}
		} else {
			error("Incorrect IP address format: " + ipaddr, this);
			return null;
		}
		try {
			inetaddr = InetAddress.getByAddress(ipBytes);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return inetaddr;
	}

	public PImage getVideoImage(boolean autoResize) {
		if (videoImage == null)
			return null;
		if (autoResize) {
			if (videoImage.getWidth() == 176) {
				return convertToPImage(resize(videoImage, 320, 240));
			}
		}
		return convertToPImage(videoImage);

	}

	@Override
	public void imageUpdated(BufferedImage image) {
		this.videoImage = image;
	}

	@Override
	public void velocityChanged(float vx, float vy, float vz) {
		this.vx = vx;
		this.vy = vy;
		velocity[0] = vx;
		velocity[1] = vy;
	}

	@Override
	public void stateChanged(DroneState state) {
		this.state = state;
	}

	@Override
	public void batteryLevelChanged(int percentage) {
		this.battery = percentage;
	}

	@Override
	public void attitudeUpdated(float pitch, float roll, float yaw, int altitude) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
		this.altitude = altitude;
	}

	public float getPitch() {
		return pitch;
	}

	public float getRoll() {
		return roll;
	}

	public float getYaw() {
		return yaw;
	}

	public float getAltitude() {
		return altitude;
	}

	public float getVelocityX() {
		return vx;
	}

	public float getVelocityY() {
		return vy;
	}

	public float[] getVelocity() {
		return velocity;
	}

	public int getBatteryPercentage() {
		return battery;
	}

	private PImage convertToPImage(BufferedImage bimg) {
		try {
			PImage img = new PImage(bimg.getWidth(), bimg.getHeight(),
					PConstants.ARGB);
			bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
			img.updatePixels();
			return img;
		} catch (Exception e) {
			// System.err.println("Can't create image from buffer");
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * resize bufferedimage
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	private BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
}