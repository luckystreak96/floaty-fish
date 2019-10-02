package com.larry.floaty.math;

public class Vector4f {

	public float x, y, z, a;//z is the fish being in front etc

	public Vector4f(){//default constructor
		x = 0;
		y = 0;
		z = 0;
		a = 1;
	}

	public Vector4f(float x, float y, float z, float a){//simple assignment
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
	}
}
