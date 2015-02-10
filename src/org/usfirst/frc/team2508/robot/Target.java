package org.usfirst.frc.team2508.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.Rect;
import com.ni.vision.NIVision.ShapeMode;

public class Target {

	double x;
	double y;
	double width;
	double height;
	double area;
	
	public Target(double x, double y, double width, double height, double area) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.area = area;
	}

	public void fill(Image image) {
		Rect area = new Rect((int) y, (int) x, (int) height, (int) width);
		NIVision.imaqDrawShapeOnImage(image, image, area, DrawMode.PAINT_VALUE, ShapeMode.SHAPE_RECT, 5);
		
		Rect pointer = new Rect((int) y, (int) x, 5, 5);
		// NIVision.imaqDrawShapeOnImage(image, image, pointer, DrawMode.PAINT_VALUE, ShapeMode.SHAPE_OVAL, 5);
	}
	
	public boolean isPair(Target target) {
		int range = 5;
		return (target.y >= y - range) && (target.y <= y + range);
	}
	
	@Override
	public String toString() {
		return "Target{x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + ",area=" + area + "}";
	}
	
}
