package org.usfirst.frc.team2508.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.Point;
import com.ni.vision.NIVision.Rect;
import com.ni.vision.NIVision.ShapeMode;

public class Target {

	private double x;
	private double y;
	private double width;
	private double height;
	private double area;
	
	public Target(double x, double y, double width, double height, double area) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.area = area;
	}

	public void fill(Image image) {
		Rect rect = new Rect((int) y, (int) x, (int) height, (int) width);
		NIVision.imaqDrawShapeOnImage(image, image, rect, DrawMode.PAINT_VALUE, ShapeMode.SHAPE_RECT, 5);
	}
	
}
