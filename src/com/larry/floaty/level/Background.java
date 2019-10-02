package com.larry.floaty.level;

import com.larry.floaty.graphics.Shader;
import com.larry.floaty.graphics.Texture;
import com.larry.floaty.graphics.VertexArray;
import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.math.Vector3f;

class Background {

	private VertexArray[] vao = new VertexArray[2];
	private Texture[] textures = new Texture[2];
	
	Background(){
		for(int i = 0; i < 2; i++){
			float[] vertices = new float[] {
					-10.0f, -10.0f*9.0f / 16.0f, 0.0f,
					-10.0f,  (10.0f*9.0f / 16.0f) - 11*i, 0.0f,
					 0.0f,   (10.0f*9.0f / 16.0f) - 11*i, 0.0f,
					 0.0f,  -10.0f*9.0f / 16.0f, 0.0f
			};

			byte[] indices = new byte[] {//first triangle uses points 0, 1 and 2, second uses 2, 3 and 0
					0, 1, 2,
					2, 3, 0
			};

			float[] tcs = new float[] {//tcs --> texturecoordinates
					0, 1,
					0, 0,
					1, 0,
					1, 1
			};


			vao[i] = new VertexArray(vertices, indices, tcs);
		}
		textures[0] = new Texture("res/bg.png");
		textures[1] = new Texture("res/bg1.png");
	}
	
	void renderBackdrop(Fish fish, int map, int xScroll, int texLoc, int xScrollspeed, double interpol){
		Shader.BG.enable();
		Shader.BG.setUniform2f("fish", fish.getX(), fish.getY(interpol));
		textures[texLoc].bind();
		vao[texLoc].bind();
		for(int o = map; o < map + 4; o++){
			Shader.BG.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f(o * 10 + ((xScroll - (float)(xScrollspeed * interpol)) / 336f) * 10, 0.0f, 0.0f)));
			vao[texLoc].draw();
		}
		textures[texLoc].unbind();
		Shader.BG.disable();
	}

	void renderSeafloor(Fish fish, int map, int xScroll, int texLoc, int xScrollspeed, double interpol){
		Shader.BG.enable();
		Shader.BG.setUniform2f("fish", fish.getX(), fish.getY(interpol));
		textures[texLoc].bind();
		vao[texLoc].bind();
		for(int o = map; o < map + 4; o++){
			Shader.BG.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f(o * 10 + ((xScroll - (float)(xScrollspeed * interpol)) / 336f) * 10, 0.0f, 2.0f)));
			vao[texLoc].draw();
		}
		textures[texLoc].unbind();
		Shader.BG.disable();
	}
}
