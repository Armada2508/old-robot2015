
package org.usfirst.frc.team2508.robot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.ColorMode;
import com.ni.vision.NIVision.IMAQdxCameraControlMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;
import com.ni.vision.NIVision.MeasurementType;
import com.ni.vision.NIVision.Range;
import com.ni.vision.VisionException;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends SampleRobot {
	
	// Robot
    RobotDrive chassis = new RobotDrive(0, 1, 2, 3);
    LogitechGamepad gamepad = new LogitechGamepad();
    Solenoid pneumatic0 = new Solenoid(0);
    Solenoid pneumatic1 = new Solenoid(1);
    Relay relayLight = new Relay(0);
    Compressor compressor = new Compressor(1);
    Encoder encoder = new Encoder(0, 1);
	Talon lift = new Talon(4);
	Talon leftArm = new Talon(5);
	Talon rightArm = new Talon(6);
    
    // Camera
    CameraServer camera = CameraServer.getInstance();
    Image image = null;
    int session = 0;
   
    // System
    boolean armsIntake = true;
    LiftState liftState = LiftState.GROUND;
    Date clampTime = new Date();
    Date lastSolenoidEnable = new Date();
    
    // Variables
    boolean cameraFilter = false; // false = Raw footage; true = Filter
    boolean autoRun;
    double speedFactor = 1.0; // multiplier for directional speed
    double rotationSpeed = 0.3;  // multiplier for rotation speed
	double wheelCircumference = 5.0; // circumference in meters of encoded wheels
	int encoderValue = 0;
	int robotState = 0;
	boolean cameraPlugged = true;

    public Robot() {
        try {
            image = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);

        session = NIVision.IMAQdxOpenCamera("cam1", IMAQdxCameraControlMode.CameraControlModeListener);
        NIVision.IMAQdxConfigureGrab(session);
    	NIVision.IMAQdxStartAcquisition(session);
        } catch(VisionException vi) {
        	System.out.println("camera not plugged in");
        	cameraPlugged = false;
        }
        
        
        // Setup chassis
        chassis.setInvertedMotor(MotorType.kFrontRight, true);
        chassis.setInvertedMotor(MotorType.kRearLeft, true);
        chassis.setInvertedMotor(MotorType.kRearRight, false);
        chassis.setExpiration(0.1);
    }
    
    public void autonomous() {
        chassis.setSafetyEnabled(false);
        autoRun = true;
        
        if (isAutonomous() && isEnabled()) {
        	while (autoRun) {
        		if (!autoRun)
        			break;
        		
        		switch (robotState) {	// Code will run through difference "states" according to each case
    			case 0:
    				chassis.drive(0.4, 0);
    				
    				robotState = 10;
    			case 10:
    				robotState = 20;
    			case 20:
    				robotState = 30;
    			case 30:
    				robotState = 40;
    			case 40:
    				robotState = 50;
    				autoRun = false;
    				break;
    			}
        		
        	}
        
        }
        
    }
    
    public void operatorControl() {
        chassis.setSafetyEnabled(true);
        encoder.reset();
        compressor.setClosedLoopControl(false);
        pneumatic0.set(false);
        pneumatic1.set(true);
        
        while (isOperatorControl() && isEnabled()) {
        	//-------------------------------------------------------------
        	// Lift
        	//-------------------------------------------------------------
        	// Measures the number of rotations of a wheel.
        	// getRaw() of 2000 is approximately one rotation
        	{
        		boolean pressed = gamepad.getFirstPressY();
        		
        		if (liftState == LiftState.GROUND) {
        			
        		}
        		
        		if (gamepad.getFirstPressY()) {
        			
        		}
            	//if (Math.abs(encoderValue) >= 1940) {
            	//	talon4.set(0);
            	//}
        		
            	
            	if (gamepad.getFirstPressY()) {
            		encoder.reset();
            		lift.set(0.3);
            	}
        	}

        	//-------------------------------------------------------------
        	// Arms
        	//-------------------------------------------------------------
        	{
        		double currentSpeed = leftArm.get();
        		double newSpeed = currentSpeed;
        		
        		if (gamepad.getFirstPressX()) {
        			if (currentSpeed == 0)
        				newSpeed = 0.5;
        			else
        				newSpeed = 0;
        		}
        		
        		if (gamepad.getFirstPressA())
        			armsIntake = !armsIntake;
        		
        		if (!armsIntake)
        			newSpeed = -newSpeed;
        		
        		if (currentSpeed != newSpeed) {
            		leftArm.set(newSpeed);
            		rightArm.set(-newSpeed);
        		}
        	}
        	
        	//-------------------------------------------------------------
        	// Pneumatic Piston Control Using Solenoid
        	//-------------------------------------------------------------
        	// Using a 2-way solenoid, in order for atic to extend, pneumatic0 
        	// must be open (set to true) and pneumatic1 must be closed (set to false)
        	// Vice-versa to retract pneumatic piston.
        	if (true) {
        		
        		if (gamepad.getFirstPressY() == true && pneumatic1.get()) {
        			pneumatic0.set(true);
        			pneumatic1.set(false);        			
        		}
        		else if (gamepad.getFirstPressY() == true && pneumatic0.get()) {
        			pneumatic0.set(false);
        			pneumatic1.set(true);
        		}
        	}
        	
        	//-------------------------------------------------------------
        	// Drive Speed & Rotation Factor
        	//-------------------------------------------------------------
        	{
        		// Pressing 'LT' on gamePad decreases speedFactor by 0.1
        		// Pressing 'RT' on gamePad increases speedFactor by 0.1
        		
        		if (gamepad.getFirstPressLT())
        			speedFactor -= 0.1;
        		if (gamepad.getFirstPressRT())
        			speedFactor += 0.1;
        		
        		// Pressing
        		if (gamepad.getFirstPressLeftStickPress())
        			rotationSpeed -= 0.1;
        		if (gamepad.getFirstPressRightStickPress())
        			rotationSpeed += 0.1;
        		
        		// Keep factor between 0.3 and 1
        		speedFactor = Math.max(0.1, Math.min(1, speedFactor));
        		rotationSpeed = Math.max(0.2, Math.min(1, rotationSpeed));
        	}
        	
        	//-------------------------------------------------------------
        	// Lifting arm
        	//-------------------------------------------------------------
        	
        	{
        		if (gamepad.getButtonRB())
        			lift.set(0.6);
        		else if (gamepad.getButtonLB())
        			lift.set(-0.6);
        		else 
        			lift.set(0);
        	}
        	
        	//-------------------------------------------------------------
        	// Driving tankDrive or mecanumDrive
        	//-------------------------------------------------------------
        	{
            	// gamePad grabs Y value of thumbstick and multiplies by 
            	// speedFactor to get leftSpeed and rightSpeed
	        	double xMovement = gamepad.getLeftStickX() * speedFactor;
	        	double yMovement = gamepad.getLeftStickY() * speedFactor;
	        	double rotation = gamepad.getRightStickX() * rotationSpeed;
	        	
	        	// Tank drive at modified speed
	            // chassis.tankDrive(leftSpeed, rightSpeed);
	            
	            // Mecanum drive at modified speed
	            // 3rd parameter specifies rate of rotation
	        	chassis.mecanumDrive_Cartesian(xMovement, yMovement, rotation, 0);
        	}

        	//-------------------------------------------------------------
        	// Light Switch
        	//-------------------------------------------------------------
        	{
        		if (gamepad.getFirstPressB()) {
        			if (relayLight.get() == Value.kOn)
        				relayLight.set(Value.kOff);
        			else
        				relayLight.set(Value.kOn);
        		}
        	}

        	//-------------------------------------------------------------
        	// Compressor
        	//-------------------------------------------------------------
        	{
        		if (gamepad.getFirstPressY()) {
        			if (compressor.enabled())
        				compressor.start();
        			else
        				compressor.stop();
        		}
        	}
        	
        	//-------------------------------------------------------------
        	// Smart Dashboard
        	//-------------------------------------------------------------
        	{
        		SmartDashboard.putNumber("Right Stick X", gamepad.getRightStickX());
	        	SmartDashboard.putNumber("Left Stick Y", gamepad.getLeftStickY());
	        	SmartDashboard.putNumber("Right Stick Y", gamepad.getRightStickY());
	        	SmartDashboard.putNumber("Speed Factor", speedFactor);
	        	SmartDashboard.putNumber("Rotation Factor", rotationSpeed);
        		// SmartDashboard.putBoolean("Compressor", compressor.enabled());
	        	// SmartDashboard.putBoolean("Solenoid Status", pneumatic0.get() && !pneumatic1.get());
	        	SmartDashboard.putBoolean("Relay Light Status", relayLight.get() == Value.kOn);
	        	SmartDashboard.putNumber("Encoder", encoderValue);
        	}

        	//-------------------------------------------------------------
        	// Image Processing
        	//-------------------------------------------------------------
        	//
        	{
				// Write new data to image variable.
        		if (cameraPlugged) {
				NIVision.IMAQdxGrab(session, image, 1);
        		}
				if (cameraFilter) {
	
					// Draw a sphere (for testing)
					// NIVision.imaqDrawShapeOnImage(image, image, new
					// Rect(10,10,100,100), DrawMode.PAINT_VALUE,
					// ShapeMode.SHAPE_OVAL, 5.0f);
	
					Range red = new Range(120, 250);
					Range green = new Range(170, 255);
					Range blue = new Range(235, 255);
	
					Image binary = NIVision.imaqCreateImage(ImageType.IMAGE_U8, 100);
	
					NIVision.imaqColorThreshold(binary, image, 255, ColorMode.RGB, red, green, blue);
	
					List<Target> targets = new ArrayList<Target>();
	
					int particles = NIVision.imaqCountParticles(binary, 0);
	
					for (int i = 0; i < particles; i++) {
						double x = NIVision.imaqMeasureParticle(binary, i, 0, MeasurementType.MT_BOUNDING_RECT_LEFT);
						double y = NIVision.imaqMeasureParticle(binary, i, 0, MeasurementType.MT_BOUNDING_RECT_TOP);
						double area = NIVision.imaqMeasureParticle(binary, i, 0, MeasurementType.MT_AREA);
						double width = NIVision.imaqMeasureParticle(binary, i, 0, MeasurementType.MT_BOUNDING_RECT_WIDTH);
						double height = NIVision.imaqMeasureParticle(binary, i, 0, MeasurementType.MT_BOUNDING_RECT_HEIGHT);
	
						if (height > 3 && width > 50)
							targets.add(new Target(x, y, width, height, area));
					}
	
					List<Pair> pairs = new ArrayList<Pair>();
	
					for (Target target : targets) {
						for (Target test : targets) {
							if (target == test)
								continue;
	
							if (test.isPair(target) && !pairs.contains(test)) {
								pairs.add(new Pair(target, test));
								break;
							}
						}
					}
	
					SmartDashboard.putNumber("Targets", targets.size());
					SmartDashboard.putNumber("Pairs", pairs.size());
	
					for (Pair pair : pairs) {
						pair.a.fill(image);
						pair.b.fill(image);
						SmartDashboard.putNumber("Angle", pair.getAngle());
						break;
					}
	
					// Send image to SmartDashboard
					camera.setImage(binary);
				}
				else {
					if (cameraPlugged) 
					camera.setImage(image);
				}
        	}
			// End of Image Processing
            
            gamepad.updatePrevButtonStates();
            Timer.delay(0.1);
        }
    }
    
    public void test() {
    }
    
    public void lift (double speed, int time) {
    	// speed is the rate it lifts (can be negative)
    	// time is in milliseconds
    	
    	
    }
    
    public void strafe (double speed) {
    	// Ex: chassis.stafe(0.4);
        // Mecanum drive at modified speed
    	// 1st parameter specifies xMovement
    	chassis.mecanumDrive_Cartesian(speed, 0, 0, 0);
    }
    
    
    public void rotate (double speed) {
    	// Ex: chassis.rotate (0.4);
    	
    	// Mecanum drive at modified speed
        // 3rd parameter specifies rate of rotation
    	chassis.mecanumDrive_Cartesian(0, 0, speed, 0);
    }
    
}
