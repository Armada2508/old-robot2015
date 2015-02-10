package org.usfirst.frc.team2508.robot;

import com.ni.vision.NIVision.Image;

public class Pair {

	Target a;
	Target b;
	
	public Pair(Target a, Target b) {
		this.a = a;
		this.b = b;
	}
	
	public double getHeightToWidth() {
		double ratioA = a.height / a.width;
		double ratioB = b.height / b.width;
		return (ratioA + ratioB) / 2.0;
	}
	
	public double getAngle() {
		// -89.85x^2 + 313.72x - 219.194
		double ratio = getHeightToWidth();
		double angle = 0;
		angle += -89.85 * Math.pow(ratio, 2);
		angle += 313.72 * ratio;
		angle += -219.194;
		return angle;
	}
	
	public void fill(Image image) {
		a.fill(image);
		b.fill(image);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;
		
		Pair p = (Pair) o;
		
		if (p.a == a && p.b == b)
			return true;
		else if (p.a == b && p.b == a)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return "Pair{" + a.toString() + ", " + b.toString() + "}";
	}
	
}
