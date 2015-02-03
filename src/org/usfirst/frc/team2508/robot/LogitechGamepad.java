/*
* See class javadoc for usage instructions.
*/
package org.usfirst.frc.team2508.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;


/**
 * This class represents a Logitech Gamepad controller providing as much access 
 * to the controller as FRC edu.wpi.first.wpilibj.GenericHID library allows.
 * <p>
 * The user of this class must call the method #updatePrevButtonStates() at the end of every pass through
 * the robot control loop.  This will ensure correct functionality of the methods that determine if 
 * a button is being pressed down for the first time when it wasn't pressed before.
 * <p>
 * The easiest way to use this class is: in the major, outer control loop of the operatorControl() method 
 * of the class driving the robot, add a line that calls #updatePrevButtonStates() at the 
 * end of the loop.  Then use the method #getFirstPress(int) passing the class constants
 * that begin with the prefix "BUTTON_" as the int parameter, in order to check
 * if a button is being pressed during a pass through the loop and it wasn't on the last pass
 * through the loop.  Otherwise, use the method #getRawButton(int) passing the same constants.
 * To get the value of an axis, use the method #getRawAxis(int), passing as a parameter
 * the constants that begin with "LEFT_" or "RIGHT_".  That's it!
 * 
 * <p>
 * <p>
 *
 * There are also named methods to check the X and Y positions of the 
 * gamepad's left and right sticks:
 * #getLeftStickX(), #getLeftStickY(), #getRightStickX(), #getRightStickY().  
 * They start with the prefix "getLeftStick" or "getRightStick".
 * <p>
 * To check whether specific buttons are pressed, there are also named methods:
 * #getButtonX(), #getButtonA(), etc.  They start with the prefix "getButton".
 * <p>
 * If you are calling the method #updatePrevButtonStates() at the end of each pass through 
 * the robot control loop, then you can also use the named methods to check whether 
 * specific buttons are pressed now but weren't pressed on the last 
 * pass through the control loop.  They are:
 * #getFirstPressX(), #getFirstPressA(), etc.  They start with the prefix "getFirstPress".
 * <p>
 * Methods inherited from edu.wpi.first.wpilibj.GenericHID are also implemented; they are
 * #getRawAxis(int), #getRawButton(int), #getX(GenericHID.Hand), and #getY(GenericHID.Hand).
 * <p>
 * The integer values to pass into #getRawAxis(int) are the public 
 * constants #LEFT_X_AXIS, #LEFT_Y_AXIS, #RIGHT_X_AXIS, and #RIGHT_Y_AXIS.
 * The integer values to pass into #getRawButton(int) are the public 
 * constants #BUTTON_X, #BUTTON_A, #BUTTON_B, #BUTTON_Y, etc. (They begin with the prefix "BUTTON_").
 * <p>
 * To set a value by which to scale the values returned for the Y axes (for example 
 * to implement speed states for driving the robot), use the 
 * method #setYAxisScalingFactor(double).
 * <p>
 * @author Bruce M
 * @since 2013-01-15
 * @version 1.2
 */
public class LogitechGamepad extends edu.wpi.first.wpilibj.GenericHID {
    /** The edu.wpi.first.wpilibj.Joystick reference that allows retrieving values from the physical device. 
    * You may ask yourself why I chose to extend Joystick's parent class GenericHID, then make the implementation
    * wrap Joystick.  Why didn't I just extend Joystick?  The reason is that Joystick contained new 
    * public methods that don't make sense in terms of a gamepad, so I didn't want those methods to be made 
    * available through inheritance.  But in order to use the edu.wpi.first.wpilibj.RobotDrive class, I wanted to make 
    * use of its method TankDrink that accepted a GenericHID type.
    */
    private Joystick m_joystick;

    // ==== BEGIN SECTION: MAPPING OF GAMEPAD BUTTON AND AXIS NUMBERS - PUBLIC CONSTANTS ====
    // Buttons on the Logitch Gampad are numbered as integers counting from 1.
    // That means joystick button 0 does nothing.
    public static final int BUTTON_X     = 1;      // xBox = 3;
    public static final int BUTTON_A     = 2;      // xBox = 1;
    public static final int BUTTON_B     = 3;      // xBox = 2;
    public static final int BUTTON_Y     = 4;      // xBox = 4;
    public static final int BUTTON_LB    = 5;
    public static final int BUTTON_RB    = 6;
    public static final int BUTTON_LT    = 7;
    public static final int BUTTON_RT    = 8;
    public static final int BUTTON_BACK  = 9;
    public static final int BUTTON_START = 10;
    public static final int BUTTON_LEFT_STICK_PRESS  = 11;
    public static final int BUTTON_RIGHT_STICK_PRESS = 12;

    // Axis of the gamepad's left and right joysticks.  Numbers 1 and 2 are for the 
    // left stick, 3 and 4 for the right stick.
    public static final int LEFT_X_AXIS  = 0;
    public static final int LEFT_Y_AXIS  = 1;
    public static final int RIGHT_X_AXIS = 2;      // Logitech gamepad = 3;
    public static final int RIGHT_Y_AXIS = 3;      // Logitech gamepad = 4;
    // ==== END SECTION ====

    
    // Lowest and highest button numbers, so code can loop through them by number.
    private static final int BUTTON_NUM_LOWEST = 1;
    private static final int BUTTON_NUM_HIGHEST = 12;
  
    // Assume the FRC Driver Station has the gamepad on what it considers to be port 1 
    // as set up in the "Setup" tab of the FRC Driver Station.
    private static final int DEFAULT_GAMEPAD_PORT_NUMBER_IN_FRC_DRIVER_STATION = 0;

    // Stores scaling factor multiplied by the Y axis positions of the joysticks 
    // to scale the speed that the robot drives around at.
    private double m_yAxisScalingFactor;

    // Stores the state of buttons pressed on the previous iteration through control loop,
    // so that we can see if a button is pressed on a current pass through the loop when
    // it wasn't pressed during the last iteration.
    private boolean [] m_priorButtonStates;


    /**
     * Public constructor for Logitech Gamepad on default port.
     */
    public LogitechGamepad() {
        // Use the default joystick port number.
        this(DEFAULT_GAMEPAD_PORT_NUMBER_IN_FRC_DRIVER_STATION); 
    }

    /**
     * Private implementation of constructor of Logitech Gamepad written to 
     * be availiable in order to ease a future transition in case future 
     * users want more than one gamepad on the driver station.
     * <p>
     * The joystickPortNumber is the port number assigned to the gamepad in 
     * the FRC Driver Station software on the "Setup" tab of the FRC Driver Station.
     *
     * @param joystickPortNumber the port number assigned to the gamepad in the FRC Driver Station.
     */
    private LogitechGamepad(int joystickPortNumber) {
        m_joystick = new Joystick(joystickPortNumber);
        m_yAxisScalingFactor = 1.0;
        m_priorButtonStates = new boolean[BUTTON_NUM_HIGHEST + 1];
        for (int i = BUTTON_NUM_LOWEST; i <= BUTTON_NUM_HIGHEST; i++) {
            m_priorButtonStates[i] = false;
        }
    }
    

    /**
     * Must be called at the end of the robot control look, records which buttons are currently 
     * being pressed or not pressed.  This must be called for the methods "getFirstPress" methods to work.
     */
    public void updatePrevButtonStates
            () {
        for (int i = BUTTON_NUM_LOWEST; i <= BUTTON_NUM_HIGHEST; i++) {
            m_priorButtonStates[i] = this.getRawButton(i);
        }
    }
    

  /**
   * Get the value of a button that is pressed, by specifying the button number 1-12.
   * Note for ease, this class declares constants for each button number, their names being
   * #BUTTON_X, #BUTTON_A, #BUTTON_B, #BUTTON_Y, etc., with the prefix "BUTTON_".
   * This method overrides a method in the parent class (so don't change the name).
   * 
   * @param button number of the button whose value is sought, 1-12.
   * @return true if pressed, false otherwise.
   */
  public boolean getRawButton(int button) {
    return m_joystick.getRawButton(button);
  }
  
  
  
  /**
   * Determine if gamepad button corresponding to the button number specified 
   * by buttonNumber is pressed for the first time since #updatePrevButtonStates() called.
   * @param buttonNumber Use this class' public constants like #BUTTON_A, BUTTON_B, etc. to specify a button number.
   * @return true if pressed, false otherwise.
   */
  public boolean getFirstPress(int buttonNumber) {
    return m_joystick.getRawButton(buttonNumber) && !m_priorButtonStates[buttonNumber];
  }
  
  
  
  /**
   * Alternative method to get the value of axis of either of the two sticks, by specifying the axis number 1-4.
   * Note for ease, this class declares constants for each axis number, their 
   * names being #LEFT_X_AXIS, #LEFT_Y_AXIS, #RIGHT_X_AXIS, #RIGHT_Y_AXIS.
   * 
   * This method is called by the FRC provided class edu.wpi.first.wpilibj.RobotDrive.
   * 
   * @param axis number of the axis whose value is sought, 1-4.
   * @return double indicating value.
   */
  public double getRawAxis(int axis) {
    if (axis == LEFT_Y_AXIS || axis == RIGHT_Y_AXIS) {
      return m_yAxisScalingFactor * m_joystick.getRawAxis(axis);
    }
    return m_joystick.getRawAxis(axis);
  }
  
  
  
  

    
    /**
     * Determine if gamepad button X is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonX() {
        return m_joystick.getRawButton(BUTTON_X);
    }
   
    /**
     * Determine if gamepad button A is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonA() {
        return m_joystick.getRawButton(BUTTON_A);
    }

    /**
     * Determine if gamepad button B is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonB() {
        return m_joystick.getRawButton(BUTTON_B);
    }
    
    /**
     * Determine if gamepad button Y is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonY() {
        return m_joystick.getRawButton(BUTTON_Y);
    }
    
    /**
     * Determine if gamepad button LB is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonLB() {
        return m_joystick.getRawButton(BUTTON_LB);
    }
    
    /**
     * Determine if gamepad button RB is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonRB() {
        return m_joystick.getRawButton(BUTTON_RB);
    }
    
    /**
     * Determine if gamepad button LT is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonLT() {
        return m_joystick.getRawButton(BUTTON_LT);
    }
    
    /**
     * Determine if gamepad button RT is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonRT() {
        return m_joystick.getRawButton(BUTTON_RT);
    }
    
    /**
     * Determine if gamepad button "BACK" is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonBack() {
        return m_joystick.getRawButton(BUTTON_BACK);
    }
    
    /**
     * Determine if gamepad button "START" is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonStart() {
        return m_joystick.getRawButton(BUTTON_START);
    }

    /**
     * Determine if gamepad button corresponding to pushing the left stick down is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonLeftStickPress() {
        return m_joystick.getRawButton(BUTTON_LEFT_STICK_PRESS);
    }

     /**
     * Determine if gamepad button corresponding to pushing the right stick down is pressed.
     * @return true if pressed, false otherwise.
     */
    public boolean getButtonRightStickPress() {
        return m_joystick.getRawButton(BUTTON_RIGHT_STICK_PRESS);
    }

    
    
    
     /**
     * Determine if gamepad button X is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressX() {
        return m_joystick.getRawButton(BUTTON_X) && !m_priorButtonStates[BUTTON_X];
    }
    
    /**
     * Determine if gamepad button A is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressA() {
        //System.out.println("Button A: " + m_joystick.getRawButton(BUTTON_A));
        //System.out.println("Last Button A: " + m_priorButtonStates[BUTTON_A]);
        return m_joystick.getRawButton(BUTTON_A) && !m_priorButtonStates[BUTTON_A];
    }

    /**
     * Determine if gamepad button B is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressB() {
        return m_joystick.getRawButton(BUTTON_B) && !m_priorButtonStates[BUTTON_B];
    }
    
    /**
     * Determine if gamepad button Y is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressY() {
        return m_joystick.getRawButton(BUTTON_Y) && !m_priorButtonStates[BUTTON_Y];
    }
    
    /**
     * Determine if gamepad button LB is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressLB() {
        return m_joystick.getRawButton(BUTTON_LB) && !m_priorButtonStates[BUTTON_LB];
    }
    
    /**
     * Determine if gamepad button RB is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressRB() {
        return m_joystick.getRawButton(BUTTON_RB) && !m_priorButtonStates[BUTTON_RB];
    }
    
    /**
     * Determine if gamepad button LT is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressLT() {
        return m_joystick.getRawButton(BUTTON_LT) && !m_priorButtonStates[BUTTON_LT];
    }
    
    /**
     * Determine if gamepad button RT is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressRT() {
        return m_joystick.getRawButton(BUTTON_RT) && !m_priorButtonStates[BUTTON_RT];
    }
    
    /**
     * Determine if gamepad button "BACK" is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressBack() {
        return m_joystick.getRawButton(BUTTON_BACK) && !m_priorButtonStates[BUTTON_BACK];
    }
    
    /**
     * Determine if gamepad button "START" is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed when wasn't pressed last time #updatePrevButtonStates() was called, false otherwise.
     */
    public boolean getFirstPressStart() {
        return m_joystick.getRawButton(BUTTON_START) && !m_priorButtonStates[BUTTON_START];
    }

    /**
     * Determine if gamepad button corresponding to pushing the left stick down is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed, false otherwise.
     */
    public boolean getFirstPressLeftStickPress() {
        return m_joystick.getRawButton(BUTTON_LEFT_STICK_PRESS) && !m_priorButtonStates[BUTTON_LEFT_STICK_PRESS];
    }

     /**
     * Determine if gamepad button corresponding to pushing the right stick down is pressed for the first time since #updatePrevButtonStates() called.
     * @return true if pressed, false otherwise.
     */
    public boolean getFirstPressRightStickPress() {
        return m_joystick.getRawButton(BUTTON_RIGHT_STICK_PRESS) && !m_priorButtonStates[BUTTON_RIGHT_STICK_PRESS];
    }

    
    
    
    
   
    /**
     * Get left-to-right value of left stick's position.
     * @return value of the left stick as a double from -1.0 being leftmost position to +1.0 being rightmost position.
     */
    public double getLeftStickX() {
        return m_joystick.getRawAxis(LEFT_X_AXIS);
    }
    
    /**
     * Get up-to-down value of left stick's position.
     * @return value of the left stick as a double from -1.0 being upmost position to +1.0 being downmost position.
     */
    public double getLeftStickY() {
        return this.getRawAxis(LEFT_Y_AXIS);  // Must call method is "this" to apply scaling factor.
    }
    
    /**
     * Get left-to-right value of right stick's position.
     * @return value of the left stick as a double from -1.0 being leftmost position to +1.0 being rightmost position.
     */
    public double getRightStickX() {
        return m_joystick.getRawAxis(RIGHT_X_AXIS);
    }
    
    /**
     * Get up-to-down value of right stick's position.
     * @return value of the right stick as a double from -1.0 being upmost position to +1.0 being downmost position.
     */
    public double getRightStickY() {
        return this.getRawAxis(RIGHT_Y_AXIS);  // Must call method is "this" to apply scaling factor.
    }
    

  
    /**
     * Sets a scaling factor to apply to Y Axis values of left and right controllers.
     * @param newScalingFactor scaling factor of 0.0 to 1.0 to set.
     */
    public void setYAxisScalingFactor(double newScalingFactor) {
        m_yAxisScalingFactor = newScalingFactor;
    }


    /**
     * Reimplemented for Logitech Gamepad, returns X value of right or left stick on controller based on value of hand specified.
     * It is preferrable to use the methods #getLeftStickX() and #getRightStickX().
     * @param hand  Is GenericHID.Hand.kLeft for left hand, GenericHID.Hand.kRight for right hand.
     * @return X value of specified stick, -1.0 (all the way left) to 1.0 (all the way right).
     */
    public double getX(GenericHID.Hand hand) {
        if (hand == GenericHID.Hand.kLeft) {
            return this.getLeftStickX();
        }
        else {
            return this.getRightStickX();
        }
    }

     /**
     * Reimplemented for Logitech Gamepad, returns Y value of right or left stick on controller based on value of hand specified.
     * It is preferrable to use the methods #getLeftStickY() and #getRightStickY().
     * @param hand  Is GenericHID.Hand.kLeft for left hand, GenericHID.Hand.kRight for right hand.
     * @return Y value of specified stick, -1.0 (all the way up) to 1.0 (all the way down).
     */
    public double getY(GenericHID.Hand hand) {
        if (hand == GenericHID.Hand.kLeft) {
            return this.getLeftStickY();
        }
        else {
            return this.getRightStickY();
        }
    }
    



    
    
   /**
    * Produces a string containing debugging information about buttons pressed.
    * @return a string containing information about which buttons are pressed.
    */
    public String debugGetButtonsPressedStr1() {
        String outStr = "";
        outStr = outStr + "= BUTTONS => ";

        for (int i = BUTTON_NUM_LOWEST; i <= BUTTON_NUM_HIGHEST; i++) {
            outStr = outStr + i + " " + this.getRawButton(i) + ";  ";
        }
        return outStr;
    }

   /**
    * Produces a string containing debugging information about buttons pressed.
    * @return a string containing information about which buttons are pressed, 2nd debug method.
    */
    public String debugGetButtonsPressedStr2() {
        String outStr = "";
        outStr = outStr + "= BUTTONS => ";
        outStr += "X: " + this.getButtonX() + ";  A:" + this.getButtonA() + ";  B:" + this.getButtonB() + ";  Y:" + this.getButtonY();
        outStr += ";  LB:" + this.getButtonLB() + ";  RB:" + this.getButtonRB() + ";  LT:" + this.getButtonLT() + ";  RT:" + this.getButtonRT();
        outStr += ";  BACK:" + this.getButtonBack() + ";  START:" + this.getButtonStart();
        outStr += ";  Left Stick:" + this.getButtonLeftStickPress() + ";  Right Stick:" + this.getButtonRightStickPress();
        return outStr;
    }
        
    /**
     * Produces a string containing debugging information about stick axes.
     * @return a string containing information about each axis.
     */
    public String debugGetAxisStr1() {
        String outStr = "leftX: " + this.getLeftStickX() + ";  leftY:" + this.getLeftStickY();
        outStr += ";  rightX:" + this.getRightStickX() + ";  rightY:" + this.getRightStickY();
        return outStr;
    }


    /**
     * Produces a string containing debugging information about stick axes.
     * @return a string containing information about each axis, 2nd method.
     */
    public String debugGetAxisStr2() {
        String outStr = "axis1: " + this.getRawAxis(1) + ";  axis2:" + this.getRawAxis(2) + ";  axis3:" + this.getRawAxis(3) + ";  axis4:" + this.getRawAxis(4);
        return outStr;
    }
    
    
     
    /**
     * Not supported in Logitech Gamepad.
     * @return 0.0;
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */
    public double getThrottle() {
        return this.getRightStickY();
    }
    /**
     * Not supported in Logitech Gamepad.
     * @return 0.0;
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */
    public double getTwist() {
        return 0.0;
    }
    /**
     * Not supported in Logitech Gamepad.
     * @return false
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */
    public boolean getBumper(GenericHID.Hand hand) {
        // Inheriting from the GenericHID class required implementing this method.
        return false;
    }
    /**
     * Not supported in Logitech Gamepad, but returns value of button A.
     * @return true if button A pressed, false otherwise;
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */
    public boolean getTop(GenericHID.Hand hand) {
        return this.getButtonA();
    }
    /**
     * Not supported in Logitech Gamepad, but returns value of button X.
     * @return true if button X pressed, false otherwise;
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */ 
    public boolean getTrigger(GenericHID.Hand hand) {
        return this.getButtonX();
    }
    /**
     * Not supported in Logitech Gamepad, but returns Right Stick's X value.
     * @param hand 
     * @return Right Stick's X value.
     * @deprecated  Not supported in Logitech Gamepad.  
     *    Replaced by {@link #getLeftStickX()}, {@link #getLeftStickY()}, {@link #getRightStickX()}, {@link #getRightStickY()}.
     */
    public double getZ(GenericHID.Hand hand) {
        return this.getRightStickX();
    }

	@Override
	public int getPOV(int pov) {
		// TODO Auto-generated method stub
		return 0;
	}
}
