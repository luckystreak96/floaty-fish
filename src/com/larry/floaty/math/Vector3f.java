package com.larry.floaty.math;

public class Vector3f {

	public float x, y, z;//z is the fish being in front etc
	
	public Vector3f(){//default constructor
		x = 0;
		y = 0;
		z = 1;
	}

	public Vector3f clone(){
		return new Vector3f(x, y, z);
	}
	
	public Vector3f(float x, float y, float z){//simple assignment
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
