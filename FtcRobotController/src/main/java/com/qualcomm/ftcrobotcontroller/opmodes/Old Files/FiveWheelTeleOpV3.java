package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftcrobotcontroller.R;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.ftccommon.FtcEventLoopHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FiveWheelTeleOpV3 extends OpMode
{
    DcMotor motorQ1; //FL if manip considered the front, BR if center wheel is front
    DcMotor motorQ2; //FR if manip considered the front, BL if center wheel is front
    DcMotor motorQ3; //BL if manip considered the front, FR if center wheel is front
    DcMotor motorQ4; //BR if manip considered the front, FL if center wheel is front
    DcMotor center;
    DcMotor debrisLift;
    DcMotor motorManip;

    Servo tiltServo;
    Servo servoHitClimberL;
    Servo servoHitClimberR;
    Servo servoLFlap;
    Servo servoRFlap;

    double getServoHitClimberLPos;
    double getServoHitClimberRPos;
    double tiltServoPos;
    double servoLFlapPos;
    double servoRFlapPos;

    int curMode; //Current mode
    int prevMode;
    boolean hasControllerBeenUsed;
    String prevLiftSide;

    double g1y1;
    double g1y2;
    double g1x1;
    double g1x2;
    double g2y1;
    double g2y2;
    double g2x1;
    double g2x2;
    boolean g1Lbump;
    boolean g1Rbump;
    boolean g2Lbump;
    boolean g2Rbump;
    double g1Ltrig;
    double g1Rtrig;
    double g2Ltrig;
    double g2Rtrig;
    boolean g1XPressed;
    boolean g2XPressed;
    boolean g1YPressed;
    boolean g2YPressed;
    boolean g1APressed;
    boolean g2APressed;
    boolean g1BPressed;
    boolean g2BPressed;

    boolean startPhaseRunning;
    boolean startPhaseOver;
    boolean autoHangRunning;
    
    boolean isTiltRight;
    boolean isTiltLeft;

    public void FiveWheelTeleOpV2() {}

    public void init() //Initialization
    {
        center = hardwareMap.dcMotor.get("center");
        motorQ1 = hardwareMap.dcMotor.get("motorq1");
        motorQ2 = hardwareMap.dcMotor.get("motorq2");
        motorQ3 = hardwareMap.dcMotor.get("motorq3");
        motorQ4 = hardwareMap.dcMotor.get("motorq4");
        motorManip = hardwareMap.dcMotor.get("manip");
        debrisLift = hardwareMap.dcMotor.get("debrisLift");
        tiltServo = hardwareMap.servo.get("tiltServo");
        servoRFlap = hardwareMap.servo.get("servoRFlap");
        servoLFlap = hardwareMap.servo.get("servoLFlap");
		servoHitClimberL = hardwareMap.servo.get("servoHitClimberL");
		servoHitClimberR = hardwareMap.servo.get("servoHitClimberR");

    	curMode = 1; //Sets mode to 1
    	//startPhaseRunning = false;
    	//startPhaseOver = false;
    	//autoHangRunning = false;
    	prevLiftSide = "right";
    	
    	//Side dependent
    	servoRFlap.setPosition(0.46); //Lock
        servoLFlap.setPosition(0.62); //Lock
        tiltServo.setPosition(0.6); //Left
    	servoRFlapPos = 0.46;
    	servoLFlapPos = 0.62;
    	tiltServoPos = 0.6;

    }

    private double scaleInputSimple(double pwr) //Scales input power
    {
        if(pwr > 0.0)
        {
            if(pwr < 0.05) //0 PWR on chart
                return 0.0;
            else if(pwr >= 0.05 && pwr < 0.10) //0.05 on chart
                return 0.01;
            else if(pwr >= 0.10 && pwr < 0.15) //0.10 on chart
                return 0.02;
            else if(pwr >= 0.15 && pwr < 0.20) //0.15 on chart
                return 0.03;
            else if(pwr >= 0.20 && pwr < 0.25) //0.20 on chart
                return 0.04;
            else if(pwr >= 0.25 && pwr < 0.30) //0.25 on chart
                return 0.05;
            else if(pwr >= 0.30 && pwr < 0.35) //0.30 on chart
                return 0.06;
            else if(pwr >= 0.35 && pwr < 0.40) //0.35 on chart
                return 0.07;
            else if(pwr >= 0.40 && pwr < 0.45) //0.40 on chart
                return 0.075;
            else if(pwr >= 0.45 && pwr < 0.50) //0.45 on chart
                return 0.08;
            else if(pwr >= 0.50 && pwr < 0.55) //0.50 on chart
                return 0.09;
            else if(pwr >= 0.55 && pwr < 0.60) //0.55 on chart
                return 0.10;
            else if(pwr >= 0.60 && pwr < 0.65) //0.60 on chart
                return 0.113;
            else if(pwr >= 0.65 && pwr < 0.70) //0.65 on chart
                return 0.126;
            else if(pwr >= 0.70 && pwr < 0.75) //0.70 on chart
                return 0.14;
            else if(pwr >= 0.75 && pwr < 0.80) //0.75 on chart
                return 0.15;
            else if(pwr >= 0.80 && pwr < 0.85) //0.80 on chart
                return 0.19;
            else if(pwr >= 0.85 && pwr < 0.90) //0.85 on chart
                return 0.225;
            else
                return 1.0;
        }
        else
        {
            if(pwr > -0.05) //0 PWR on chart
                return 0.0;
            else if(pwr <= -0.05 && pwr > -0.10) //0.05 on chart
                return -0.01;
            else if(pwr <= -0.10 && pwr > -0.15) //0.10 on chart
                return -0.02;
            else if(pwr <= -0.15 && pwr > -0.20) //0.15 on chart
                return -0.03;
            else if(pwr <= -0.20 && pwr > -0.25) //0.20 on chart
                return -0.04;
            else if(pwr <= -0.25 && pwr > -0.30) //0.25 on chart
                return -0.05;
            else if(pwr <= -0.30 && pwr > -0.35) //0.30 on chart
                return -0.06;
            else if(pwr <= -0.35 && pwr > -0.40) //0.35 on chart
                return -0.07;
            else if(pwr <= -0.40 && pwr > -0.45) //0.40 on chart
                return -0.075;
            else if(pwr <= -0.45 && pwr > -0.50) //0.45 on chart
                return -0.08;
            else if(pwr <= -0.50 && pwr > -0.55) //0.50 on chart
                return -0.09;
            else if(pwr <= -0.55 && pwr > -0.60) //0.55 on chart
                return -0.10;
            else if(pwr <= -0.60 && pwr > -0.65) //0.60 on chart
                return -0.113;
            else if(pwr <= -0.65 && pwr > -0.70) //0.65 on chart
                return -0.126;
            else if(pwr <= -0.70 && pwr > -0.75) //0.70 on chart
                return -0.14;
            else if(pwr <= -0.75 && pwr > -0.80) //0.75 on chart
                return -0.15;
            else if(pwr <= -0.80 && pwr > -0.85) //0.80 on chart
                return -0.19;
            else if(pwr <= -0.85 && pwr > -0.90) //0.85 on chart
                return -0.225;
            else
                return -1.0;
        }
    }

    public void updateVals() //Updates all variable values
    {
        g1y1 = gamepad1.left_stick_y;
        g1y2 = gamepad1.right_stick_y;
        g1x1 = gamepad1.left_stick_x;
        g1x2 = gamepad1.right_stick_x;
        g2y1 = gamepad2.left_stick_y;
        g2y2 = gamepad2.right_stick_y;
        g2x1 = gamepad2.left_stick_x;
        g2x2 = gamepad2.right_stick_x;
        g1Lbump = gamepad1.left_bumper;
        g1Rbump = gamepad1.right_bumper;
        g2Lbump = gamepad2.left_bumper;
        g2Rbump = gamepad2.right_bumper;
        g1Ltrig = gamepad1.left_trigger;
        g1Rtrig = gamepad1.right_trigger;
        g2Ltrig = gamepad2.left_trigger;
        g2Rtrig = gamepad2.right_trigger;
        g1XPressed = gamepad1.x;
        g1APressed = gamepad1.a;
        g1YPressed = gamepad1.y;
        g1BPressed = gamepad1.b;
        g2XPressed = gamepad2.x;
        g2APressed = gamepad2.a;
        g2YPressed = gamepad2.y;
        g2BPressed = gamepad2.b;
        prevMode = curMode; //Changes the previous mode to the current mode
        if(curMode != 0) //Updates current mode
        {
        	if(g2XPressed) //If Controller 2 X is pressed, switch to mode 1
            	curMode = 1;
        	else if(g2BPressed) //If Controller 2 B is pressed, switch to mode 2
            	curMode = 2;
            //Else keep current mode
        }
    }

	public void runManip(double speed) //Moves manipulator
	{
		motorManip.setPower(speed);
	}
    
	public void moveDebrisLift(double speed) //Moves lift
	{
		debrisLift.setPower(speed);
	}

	public void runOddSide(double speed) //Runs Q1 and Q3
	{
		motorQ1.setPower(speed);
		motorQ3.setPower(speed);
	}
	
	public void runEvenSide(double speed) //Runs Q2 and Q4
	{
		motorQ2.setPower(speed);
		motorQ4.setPower(speed);
	}
	
	public void stopWheels() //Stops all wheels
	{
		motorQ1.setPower(0.0);
		motorQ2.setPower(0.0);
		motorQ3.setPower(0.0);
		motorQ4.setPower(0.0);
	}
	
	/*public void setDebrisLiftServos(double pos)
	{
		debrisLiftL.setPosition(pos);
		debrisLiftR.setPosition(1.0 - pos);
	}

	public void moveDebrisLiftServos(double speed)
	{
		debrisLiftPos += speed / 10.0;
		if(debrisLiftPos > 1.0)
			debrisLiftPos = 1.0;
		else if(debrisLiftPos < 1.0)
            debrisLiftPos = 0.0;
		setDebrisLiftServos(debrisLiftPos);
	}

	public void resetDebrisLiftServos() {
        debrisLiftPos = 0.0;
		setDebrisLiftServos(debrisLiftPos);
	}

	public void moveDebrisLiftBasket(double change)
	{
		double ch = change / 10.0;
		debrisLiftServoPos1 += ch;
		debrisLiftServoPos2 += ch;
		debrisLiftServo1.setPosition(debrisLiftServoPos1);
        debrisLiftServo2.setPosition(debrisLiftServoPos2);
	}*/

    //SERVO 1 Values
    //0.6 for down
    //0.1 for up
    //SERVO 2 values
    //0.475 for down
    //0.9 for up
    //SERVO 3 Values
    //Left side (Manip = front) = 0.6
    //Right side (Manip = front) = 0.3
    public void basketNeutral() //Returns basket to neutral position
    {
        servoLFlap.setPosition(0.39); //Locked
        servoRFlap.setPosition(0.68); //Locked
        servoLFlapPos = 0.39;
        servoRFlapPos = 0.68;
    }
    
    /*public void basketNeutralDown(String param)
    {
    	if(param.equals("right"))
    	{
    		/*debrisLiftServo1.setPosition(0.6); //Down
        	debrisLiftServo2.setPosition(0.9); //Up
        	debrisLiftServo3.setPosition(0.45); //Neutral
        	debrisLiftServoPos1 = 0.6;
        	debrisLiftServoPos2 = 0.9;
        	debrisLiftServoPos3 = 0.45;
    	}
    	else if(param.equals("left"))
    	{
    		/*debrisLiftServo1.setPosition(0.1); //Up
        	debrisLiftServo2.setPosition(0.475); //Down
        	debrisLiftServo3.setPosition(0.45); //Neutral
        	debrisLiftServoPos1 = 0.1;
        	debrisLiftServoPos2 = 0.475;
        	debrisLiftServoPos3 = 0.45;
    	}
    }*/

    public void basketLeft()
    {
        servoLFlap.setPosition(0.97); //Down
        servoRFlap.setPosition(0.47); //Up
        tiltServo.setPosition(0.6); //Left
        servoLFlapPos = 0.97;
        servoRFlapPos = 0.47;
        tiltServoPos = 0.6;
    }

    public void basketRight()
    {
        servoLFlap.setPosition(0.62); //Up
        servoRFlap.setPosition(0.05); //Down
        tiltServo.setPosition(0.91); //Right
        servoLFlapPos = 0.2;
        servoRFlapPos = 0.05;
        tiltServoPos = 0.91;
    }
	
    public void loop()
    {
        //curMode = 1;
        updateVals();
        /*if(!startPhaseOver) //Robot has done nothing since start of match
        {
            if(g1y1 > 0.3 || g1y1 < -0.3 || g1y2 > 0.3 || g1y2 < -0.3)
            {
                startPhaseOver = true;
                curMode = 2;
            }
            else if(g1XPressed)
            {
                startPhaseOffRamp();
            }
        }*/
        /*if(curMode == 0) //Stop robot Mode
        {
        	runOddSide(0.0);
        	runEvenSide(0.0);
        	center.setPower(0.0);
        	moveDebrisLift(0.0);
        	moveDebrisLiftServos(0.0);
        	if(g1YPressed)
        		curMode = 2;
        }*/

        if(curMode == 1) //Debris collection Mode
        {
            //Controller 1
            //Left Stick
            runOddSide(scaleInputSimple(-g1y1)); //Moves left side of robot (Manipulator is front of robot)
            //Right Stick
			runEvenSide(scaleInputSimple(g1y2)); //Moves right side of robot (Manipulator is front of robot)
            
            //Controller 2
        	//Left stick
            moveDebrisLift(scaleInputSimple(-g2y1)); //Moves the lift
            
            //Right stick
            if (g2y2 > 0.3 || g2y2 < 0.3) //Moves manipulator
            {
                runManip(-g2y2);
            } else { //Stops manipulator
                runManip(0.0);
            }
            
        	//Moves basket
        	//Right Bumper
            if (g2Rbump)
                basketNeutral(); //Moves basket back to neutral position
			else if(g2Rtrig > 0.3) //Right Trigger
                basketLeft(); //Dumps debris to the left
			else if(g2Lbump) //Left Bumper
				basketNeutral(); //Moves basket back to neutral position
			else if(g2Ltrig > 0.3) //Left Trigger
				basketRight(); //Dumps debris to the right

			
			center.setPower(0.0); //Stops center wheel
        }
        else if(curMode == 2) //Ramp Climbing Mode
        {
        	//Controller 1
        	
        	//Left Stick
            runEvenSide(scaleInputSimple(-g1y1)); //Moves left side of robot (Center wheel is front of robot)
            //Right Stick
            runOddSide(scaleInputSimple(g1y2)); //Moves right side of robot (Center wheel is front of robot)
            
            //Both sticks
			if((g1y1 > 0.1 && g1y1 > 0.1) || (g1y2 < -0.1 && g1y2 < -0.1)) //If both sticks being moved
				center.setPower(scaleInputSimple(g1y2)); //Move center wheel along with other wheels
            else
                center.setPower(0.0);
            
            //Controller 2
            //Left stick
            moveDebrisLift(scaleInputSimple(-g2y1)); //Moves the lift
            
            if (g2Rbump) //Right Bumper
            {
                basketNeutral(); //Goes to neutral
            }
			else if(g2Rtrig > 0.3) //Right trigger
			{
				prevLiftSide = "left";
				basketRight(); //Dumps on right side
			}
			else if(g2Lbump) //Left Bumper
			{
				basketNeutral(); //Goes to neutral
			}
			else if(g2Ltrig > 0.3) //Left Trigger
			{
				prevLiftSide = "right";
				basketLeft(); //Dumps on left side
			}
            motorManip.setPower(0.0); //Turns off manipulator
        }
        /*else if(curMode == 3) //Hanging Mode
        {
        	runManip(0.0);
        	//retractDebrisLift();
        	resetDebrisLiftServos();
			runOddSide(scaleInputSimple(g1y2));
            runEvenSide(scaleInputSimple(-g1y1));
            if((g1y1 > 0.1 && g1y2 > 0.1) || (g1y1 < -0.1 && g1y2 < -0.1))
				center.setPower(scaleInputSimple(-g1y2));
        }
        else if(curMode == 4) //Manual Override Mode
        {
            //After Scrimmage
        }
        else if(curMode == 5) //Individual Wheel control
        {
            //After Scrimmage
        }*/
        if (curMode == 1) {
            telemetry.addData("Mode", " Debris Collection");
        }
        else if (curMode == 2) {
            telemetry.addData("Mode", " Mountain Mode");
        }
        else {
            telemetry.addData("Mode", " ERROR");
        }
    }
}