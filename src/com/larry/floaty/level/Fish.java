package com.larry.floaty.level;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Random;

import com.larry.floaty.graphics.Shader;
import com.larry.floaty.graphics.Texture;
import com.larry.floaty.graphics.VertexArray;
import com.larry.floaty.input.Input;
import com.larry.floaty.math.*;
import com.larry.floaty.sound.OpenALPlayer;

class Fish {

	private float width = 1.0f;
	private float height = 1.0f;
	private VertexArray[] mesh = new VertexArray[(int) Math.pow(512 / 32, 2)];
	private Texture texture;
    private static Texture hat = new Texture("res/class.png");
    private VertexArray hatMesh;
	private float gravity = 0.025f;
	
	private Vector3f position = new Vector3f();
	float delta = -0.15f;
	
	private boolean control = true;
    private ParticleEffect sandEffect;
    private ParticleEffect bubbleEffect;

	private Random random = new Random();
	private int rand;
	private int maxTex = 5;
	private int state;
	
	Fish(float x, float size){
        float z = 0.2f;
        width = size;
        height = size;

        sandEffect = new ParticleEffect(0, 0, "res/baseParticle.png");
        sandEffect.setParticleAttr(120, new Vector2f(0.03f, 0.03f));
        sandEffect.setPosition(new Vector3f(0, 0, 3.1f));
        sandEffect.setVelocity(RndVel.choice.MultXY, new float[]{0.25f, 0.125f});

        bubbleEffect = new ParticleEffect(0, 0, "res/bubble.png");
        bubbleEffect.setParticleAttr(80, new Vector2f(0.2f, 0.2f));
        bubbleEffect.setPosition(new Vector3f(0, 0, 2.1f));
        setBubbleVelocitySlow();
        if(Level.giantBubbles){
            bubbleSizeChange(new Vector2f(1.0f, 1.0f));
        }


		int texW, texU, texH;//texture size, texture Unit size
		texW = 53*maxTex;
		texU = 53;
		texH = 53*2;//height if not a square
		texture = new Texture("res/fish.png");//bitmapfont spritesheet thingy?

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

		for(int i = 0; i < (texW / texU) * (texH / texU); i++){//256*256 = bitmap texture size

			float col = i / (texW / texU);
			float row = i % maxTex;

			float u0 = row / maxTex;
			float u1 = (row + 1) / maxTex;

			float v0 = col / (texH / texU);
			float v1 = (col + 1) / (texH / texU);

			float[] tcs = new float[] {//tcs --> texturecoordinates
					u0, v1,
					u0, v0,
					u1, v0,
					u1, v1
			};

			mesh[i] = new VertexArray(vertices, indices, tcs);
		}

        if(Level.tinyFish){
            vertices = new float[] {//changes between different textures
                    -width / 2.0f, -height / 2.0f + 0.09f, z,//last value is the z value??
                    -width / 2.0f,  height / 2.0f + 0.09f, z,
                    width / 2.0f,  height / 2.0f + 0.09f, z,
                    width / 2.0f, -height / 2.0f + 0.09f, z
            };
        }
        else{
            vertices = new float[] {//changes between different textures
                    -width / 2.0f, -height / 2.0f + 0.17f, z,//last value is the z value??
                    -width / 2.0f,  height / 2.0f + 0.17f, z,
                    width / 2.0f,  height / 2.0f + 0.17f, z,
                    width / 2.0f, -height / 2.0f + 0.17f, z
            };
        }

        float[] tcs = new float[] {//tcs --> texturecoordinates; DOESNT CHANGE BETWEEN DIFF TEXTURES
                0, 1,
                0, 0,
                1, 0,
                1, 1
        };

        hatMesh = new VertexArray(vertices, indices, tcs);

		rand = random.nextInt(maxTex);
		position.x = x;
        Shader.FISH.enable();
        Shader.FISH.setUniform1i("darkMode", Level.darkMode ? 1 : 0);
        if(Level.invincible){
            Shader.FISH.setUniform1i("darkMode", 2);
        }
        Shader.FISH.disable();
	}

	public float getX()
	{
		return position.x;
	}
	
	float getY(double interpolation){
		return position.y - (float)(delta * interpolation);
	}

	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public void control(boolean control){
		this.control = control;
	}

	public boolean getControl(){
		return control;
	}

    void setBubbleVelocityFast(){
        bubbleEffect.setVelocity(RndVel.choice.MultAddXY, new float[]{0.25f, 0.125f, -0.55f, 0.45f});
        if(Level.giantBubbles){
            bubbleEffect.setGravity(-0.025f, 0.007f, 0);
        }
        else{
            bubbleEffect.setGravity(-0.025f, 0.005f, 0);
        }
    }

    void setBubbleVelocitySlow(){
        bubbleEffect.setVelocity(RndVel.choice.MultAddXY, new float[]{0.15f, 0.055f, -0.55f, 0.45f});
        bubbleEffect.setGravity(-0.0005f, 0.005f, 0);
        if(Level.giantBubbles){
            bubbleEffect.setGravity(-0.0005f, 0.007f, 0);
        }
        else{
            bubbleEffect.setGravity(-0.0005f, 0.005f, 0);
        }
    }

    void bubbleSizeChange(Vector2f size){
        bubbleEffect.setParticleSize(size);
    }

	public void fall(float angle) {//what happens when u die liek scrub
		delta = angle;//0.20f is the legit way to go
	}

    void genBubbles(int num, int seconds){
        //bubbleEffect.position = new Vector3f(position.x + 0.55f, position.y - 0.1f, 0);
        bubbleEffect.generate(num, seconds);
    }
	
	public void update(boolean menu){//this will be called in level.update
		position.y -= delta;
		if(Math.abs(position.y) / 5 >= 1 && control){
            //Went too deep... or got too high
			if(!menu){
                control = false;
                fall(position.y > 1 ? -0.3f: -0.52f);
            }
			else{
                //Went too high
                if(position.y > 1){
                    fall(0.3f);
                }
                //hit the sand
                else{
                    fall(-0.35f);
                    sandEffect.position = new Vector3f(position.x, position.y - 0.2f, 0);
                    sandEffect.generate(30);
                }
            }
			//position.y = position.y + delta;
			//delta = 0.0f;
		}

        //This setup tries to save processor time
        if(menu){
            if(random.nextInt(25) == 1){
                genBubbles(0, 1);
            }
        }
        if(Level.bubbleGun){
            genBubbles(2, 1);
        }

        ParticleEffect.setFishPosition(position.x, position.y);

        sandEffect.updateParticles();

        if(Input.isKeyHeld(GLFW_KEY_W) && Level.hardcore && !Level.simplicity && delta > 0 && control){
            delta = 0.02f;
        }
        else{
            if(Level.slowMo){
                delta += gravity / 2;
            }
            else{
                delta += gravity;
            }
        }

		if(Input.isKeyPressed(GLFW_KEY_SPACE) && control){
			delta = -0.25f;
            if (Level.slowMo) {
                delta = -0.125f;
            }
            Level.totalFloats++;
			OpenALPlayer.setFXGain(OpenALPlayer.playSound(1, 0), 0.7f);
		}

		float rot = -delta * 90.0f;//the rotation will be set according to the directional momentum
		state = delta < 0 ? rand + maxTex: rand;

        Vector3f bubPos = position.clone();
        bubPos.z += 0.1f;
        if(Level.giantBubbles){
            bubPos.x += 0.45f - Math.abs(0.55f * (rot / 90));
            bubPos.y -= 0.45f - 0.5f * (rot / 90);
        }
        else{
            bubPos.x += 0.5f - Math.abs(0.55f * (rot / 90));
            bubPos.y -= 0.07f - 0.5f * (rot / 90);
        }
        if(Level.tinyFish){
            bubPos.x -= 0.25f;
            bubPos.y -= 0.10f;
        }
        bubbleEffect.setPosition(bubPos);
        bubbleEffect.updateParticles();
	}

	public void render(double interpol){
		Vector3f temp = new Vector3f();
		temp.x = position.x;
		temp.z = 1.0f;
        //System.out.println("Y1: " + position.y);
		temp.y = (float)(position.y - (delta + gravity) * interpol);
        //System.out.println("Y2: " + temp.y);
        //Vector2f temper = new Vector2f(getX(), getY(interpol));

		Shader.FISH.enable();
        Shader.FISH.setUniform2f("center", getX(), getY(interpol));
		Shader.FISH.setUniformMat4f("vw_matrix", Matrix4f.translate(temp).multiply(Matrix4f.rotate((-(delta + (float)(gravity * interpol))) * 90.0f)));//remove multiply part to remove the rotation
		texture.bind();
		mesh[state].render();
        if(Level.classy){
            hat.bind();
            temp.z = 2.0f;
            Shader.FISH.setUniformMat4f("vw_matrix", Matrix4f.translate(temp).multiply(Matrix4f.rotate((-(delta + (float)(gravity * interpol))) * 90.0f)));//remove multiply part to remove the rotation
            hatMesh.render();
        }
		Shader.FISH.disable();
        sandEffect.renderParticles(interpol);
        bubbleEffect.renderParticles(interpol);
	}
	

}




