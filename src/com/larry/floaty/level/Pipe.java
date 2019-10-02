package com.larry.floaty.level;


import com.larry.floaty.graphics.Texture;
import com.larry.floaty.graphics.VertexArray;
import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.math.Vector3f;

class Pipe {
	
	private Matrix4f ml_matrix;
	private Vector3f position = new Vector3f();

	private static Texture texture;
	private static VertexArray mesh;
	private static float width = 1.5f, height = 8.0f;
	
	public static void create(){
		float[] vertices = new float[] {//changes between different textures
				0.0f, 0.0f, 0.1f,
				0.0f, height, 0.1f,
				width, height, 0.1f,
				width, 0.0f, 0.1f
		};
		
		byte[] indeces = new byte[] {//first triangle uses points 0, 1 and 2, second uses 2, 3 and 0; DOESNT CHANGE BETWEEN DIFF TEXTURES
				0, 1, 2,
				2, 3, 0
		};
		
		float[] tcs = new float[] {//tcs --> texturecoordinates; DOESNT CHANGE BETWEEN DIFF TEXTURES
				0, 1,
				0, 0,
				1, 0,
				1, 1
		};
		
		
		mesh = new VertexArray(vertices, indeces, tcs);
		texture = new Texture("res/pipe.png");
		
	}
	
	Pipe(float x, float y){
		position.x = x;
		position.y = y;
		ml_matrix = Matrix4f.translate(position);
	}

	public float getX(){
		return position.x;
	}

	public float getY(){
		return position.y;
	}
	
	Matrix4f getModelMatrix() {
		return ml_matrix;
	}
	
	public static VertexArray getMesh(){
		return mesh;
	}

	public static Texture getTexture(){
		return texture;
	}

	public static float getWidth(){
		return width;
	}

	public static float getHeight(){
		return height;
	}


}




