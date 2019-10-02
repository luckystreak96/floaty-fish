package com.larry.floaty.graphics;


import java.util.HashMap;

import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.math.Vector3f;


public class Font {
	
	private float width = 0.75f;
	private float height = 0.75f;
	private VertexArray[] mesh = new VertexArray[(int) Math.pow(512 / 32, 2)];
	private VertexArray[] message;
	private Texture texture;
	private HashMap<Character, Integer> hash = new HashMap<>();
	private	float baseX = 0;
	private	float baseY = 0;
	
	private Vector3f position = new Vector3f();
	
	public Font(String path, float size){
        int bitmapWidth = 16;
		width = size;
		height = size;
		int texS, texU;//texture size, texture Unit size
		if(width <= 1.0f){
			texS = 512;
			texU = 32;
		}else if(width >= 1.5){
			texS = 2048;
			texU = 128;
		}else{
			texS = 1024;
			texU = 64;
		}
		createHash();
		texture = new Texture(path);//bitmapfont spritesheet thingy?
		for(int i = 0; i < Math.pow((texS / texU), 2); i++){//256*256 = bitmap texture size

			float col = i / bitmapWidth;
			float row = i % bitmapWidth;

			float u0 = row / bitmapWidth;
			float u1 = (row + 1) / bitmapWidth;

			float v0 = col / bitmapWidth;
			float v1 = (col + 1) / bitmapWidth;

            float z = 0.2f;
			float[] vertices = new float[] {//changes between different textures
					-width / 2.0f, -height / 2.0f, z,//last value is the z value??
					-width / 2.0f,  height / 2.0f, z,
					 width / 2.0f,  height / 2.0f, z,
					 width / 2.0f, -height / 2.0f, z
			};

			byte[] indices = new byte[] {//first triangle uses points 0, 1 and 2, second uses 2, 3 and 0; DOESNT CHANGE BETWEEN DIFF TEXTURES
					0, 1, 2,
					2, 3, 0
			};

			float[] tcs = new float[] {//tcs --> texturecoordinates; DOESNT CHANGE BETWEEN DIFF TEXTURES
					u0, v1,
					u0, v0,
					u1, v0,
					u1, v1
			};

			mesh[i] = new VertexArray(vertices, indices, tcs);
		}
	}
	
	private void createHash(){
		String charList = "\n !\"#$%&'()*+,-."
						+ "/0123456789:;<=>"
						+ "?@ABCDEFGHIJKLMN"
						+ "OPQRSTUVWXYZ[\\]^"
						+ "_`abcdefghijklmn"
						+ "opqrstuvwxyz{|}~";
		for(int i = 0; i < charList.length(); i++){
			hash.put(charList.charAt(i), i);
		}
	}
	
	private void setString(String text, float x, float y, boolean centered){
		message = new VertexArray[text.length()];
		for(int i = 0; i < text.length(); i++){
			message[i] = mesh[charToCode(text.charAt(i))];
		}
		if(centered){
			baseX = x - text.length() * width / 2 / 2;//the spacing is too much, so halve the width again to remove the spacing
		}else{
			baseX = x;
		}
		baseY = y;
	}
	

    /*
	public void setString(String text, float x, float y){
		setString(text, x, y, false);
	}
	*/

	public void drawString(String text, float x, float y, boolean centered){
		setString(text, x, y, centered);
        position.z = 3.0f;
		render();
	}

    public void drawString(String text, float x, float y, float z, boolean centered){
        position.z = z;
        setString(text, x, y, centered);
        render();
    }

	public void drawString(String text, float x, float y){
		drawString(text, x, y, false);
	}
	
	private int charToCode(char c){
		return hash.get(c);
	}
	
	public void update(){
	}

	public void render(){
		Shader.FONT.enable();
		position.x = baseX;
		position.y = baseY;
        for (VertexArray letter: message) {
            //Shader.FONT.setUniformMat4f("ml_matrix", Matrix4f.translate(position));
            Shader.FONT.setUniformMat4f("vw_matrix", Matrix4f.translate(position));
            texture.bind();
            position.x += (width / 2);//the spacing is too much, so halve the width again to remove the spacing
            if(letter == mesh[0]){
                position.y -= height;
                position.x = baseX;
            }
            else
                letter.render();
        }

		Shader.FONT.disable();
	}
	

}