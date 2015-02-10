package org.usfirst.frc.team2508.robot;

public class Pair {

	private Target a;
	private Target b;
	
	public Pair(Target a, Target b) {
		this.a = a;
		this.b = b;
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
