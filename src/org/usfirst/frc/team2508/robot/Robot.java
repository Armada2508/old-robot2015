
package org.usfirst.frc.team2508.robot;

import java.util.Date;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;

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
	Talon talon = new Talon(4);
	
    RobotDrive chassis = new RobotDrive(0, 1, 2, 3);
    LogitechGamepad gamepad = new LogitechGamepad();
    Solenoid pneumatic0 = new Solenoid(0);
    Solenoid pneumatic1 = new Solenoid(1);
    Relay relayLight = new Relay(0);
    Compressor compressor = new Compressor(1);
    Encoder encoder = new Encoder(0, 1);
    
    // Camera
    CameraServer camera = CameraServer.getInstance();
    Image image = null;
   
    // System
    Date lastSolenoidEnable = new Date();
    
    // Variables
    double speedFactor = 1.0; // multiplier for directional speed
    double rotationSpeed = 0.3;  // multiplier for rotation speed
	double wheelCircumference = 5.0; // circumference in meters of encoded wheels

    public Robot() {
    	// Setup camera
        camera.setQuality(50);
        image = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
        int session = NIVision.IMAQdxOpenCamera("cam1", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
    	NIVision.IMAQdxConfigureGrab(session);
    	NIVision.IMAQdxStartAcquisition(session);
    	camera.setImage(image);
        
        // Setup chassis
        chassis.setInvertedMotor(MotorType.kFrontRight, true);
        chassis.setInvertedMotor(MotorType.kRearRight, true);
        chassis.setExpiration(0.1);
    }
    
    public void autonomous() {
        chassis.setSafetyEnabled(false);
    }
    
    public void operatorControl() {
        chassis.setSafetyEnabled(true);
        
        compressor.setClosedLoopControl(false);
        
        while (isOperatorControl() && isEnabled()) {
        	//-------------------------------------------------------------
        	// Encoder
        	//-------------------------------------------------------------
        	// Measures the number of rotations of a wheel.
        	// getRaw() of 2000 is approximately one rotation
        	{
        		int encoderValue = encoder.getRaw();

            	if (Math.abs(encoderValue) >= 1940) {
            		talon.set(0);
            	}
            	
            	if (gamepad.getFirstPressY()) {
            		encoder.reset();
            		talon.set(1);
            	}
        	}
        	
        	//-------------------------------------------------------------
        	// Pneumatic Piston Control Using Solenoid
        	//-------------------------------------------------------------
        	// Using a 2-way solenoid, in order for pneumatic to extend, pneumatic0 
        	// must be open (set to true) and pneumatic1 must be closed (set to false)
        	// Vice-versa to retract pneumatic piston.
        	
        	{
    			Date now = new Date();
        		int timeSince = (int) (now.getTime() - lastSolenoidEnable.getTime());
        		
        		if (gamepad.getButtonRB() == true && timeSince > 1000) {
        			pneumatic0.set(true);
        			pneumatic1.set(false);
        			lastSolenoidEnable = now;
        		}
        		else if (timeSince > 1000) {
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
        	// Driving tankDrive or mecanumDrive
        	//-------------------------------------------------------------
        	{
            	// gamePad grabs Y value of thumbstick and multiplies by 
            	// speedFactor to get leftSpeed and rightSpeed
	        	double xMovement = (gamepad.getLeftStickX() * speedFactor);
	        	double yMovement = gamepad.getLeftStickY() * speedFactor;
	        	double rotation = gamepad.getRightStickX() * rotationSpeed;
	        	
	        	// Tank drive at modified speed
	            // chassis.tankDrive(leftSpeed, rightSpeed);
	            
	            // Mecanum drive at modified speed
	            // 3rd parameter specifies rate of rotation
	        	chassis.mecanumDrive_Cartesian(xMovement, yMovement, rotation, 0);
        	}
        	
        	// Relay light switch
        	{
        		if (gamepad.getFirstPressX()) {
        			if (relayLight.get() == Value.kOn)
        				relayLight.set(Value.kOff);
        			else
        				relayLight.set(Value.kOn);
        		}
        	}
        	
        	// Compressor 
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
	        	SmartDashboard.putNumber("Left Stick Y", gamepad.getLeftStickY());
	        	SmartDashboard.putNumber("Right Stick Y", gamepad.getRightStickY());;
	        	SmartDashboard.putNumber("Speed Factor", speedFactor);
	        	SmartDashboard.putNumber("Rotation Factor", rotationSpeed);
        		SmartDashboard.putBoolean("Compressor", compressor.enabled());
	        	SmartDashboard.putBoolean("Solenoid Status", pneumatic0.get() && !pneumatic1.get());
	        	SmartDashboard.putBoolean("Relay Light Status", relayLight.get() == Value.kOn);
	        	SmartDashboard.putNumber("Encoder", encoder.getRaw());
        	}
            
            gamepad.updatePrevButtonStates();
            Timer.delay(0.01);
        }
    }
    
    public void test() {
    }
}
