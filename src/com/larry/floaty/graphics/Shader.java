package com.larry.floaty.graphics;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;
import java.util.Map;

import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.math.Vector4f;
import com.larry.floaty.utils.ShaderUtils;

public class Shader {

    static final int VERTEX_ATTRIB = 0;
	static final int TCOORD_ATTRIB = 1;

	public  static Shader BG, FISH, PIPE, FADE, FONT, PARTICLE;
	
	private static boolean enabled = false;
	
	private final int ID;
	private Map<String, Integer> locationCache = new HashMap<>();
	
	public Shader(String vertex, String fragment){
		ID = ShaderUtils.load(vertex, fragment);
	}
	
	public static void loadAll(){
		BG = new Shader("shaders/bg.vert", "shaders/bg.frag");
		FISH = new Shader("shaders/fish.vert", "shaders/fish.frag");
		PARTICLE = new Shader("shaders/particle.vert", "shaders/particle.frag");
		PIPE = new Shader("shaders/pipe.vert", "shaders/pipe.frag");
		FADE = new Shader("shaders/fade.vert", "shaders/fade.frag");
		FONT = new Shader("shaders/font.vert", "shaders/font.frag");
	}
	
	private int getUniform(String name){
		if(locationCache.containsKey(name))
			return locationCache.get(name);

		int result = glGetUniformLocation(ID, name);

		if(result == -1)
			System.err.println("Could not find uniform variable '" + name + "'!");
		else
			locationCache.put(name,  result);
		return result;
	}
	
	public void setUniform1i(String name, int value){
		if(!enabled) enable();
		glUniform1i(getUniform(name), value);
	}

	public void setUniform1f(String name, float value){
		if(!enabled) enable();
		glUniform1f(getUniform(name), value);
	}

	public void setUniform2f(String name, float x, float y){
		if(!enabled) enable();
		glUniform2f(getUniform(name), x, y);
	}

    /*
	public void setUniform3f(String name, Vector3f vector){
		if(!enabled) enable();
		glUniform3f(getUniform(name), vector.x, vector.y, vector.z);
	}
	*/

	public void setUniform4f(String name, Vector4f vector){
		if(!enabled) enable();
		glUniform4f(getUniform(name), vector.x, vector.y, vector.z, vector.a);
	}
	
	public void setUniformMat4f(String name, Matrix4f matrix){
		if(!enabled) enable();
		glUniformMatrix4fv(getUniform(name), false, matrix.toFloatBuffer());
	}
	
	public void enable(){
		glUseProgram(ID);
		enabled = true;
	}
	
	public void disable(){
		glUseProgram(0);
		enabled = false;
	}

}
