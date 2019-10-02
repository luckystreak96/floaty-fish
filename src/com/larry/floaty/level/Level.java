package com.larry.floaty.level;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.larry.floaty.Main;
import com.larry.floaty.math.*;
import com.larry.floaty.sound.OpenALPlayer;
import org.lwjgl.glfw.GLFW;

import com.larry.floaty.graphics.Font;
import com.larry.floaty.graphics.Shader;
import com.larry.floaty.graphics.VertexArray;
import com.larry.floaty.input.Input;
import com.larry.floaty.utils.FileUtils;

public class Level {
	
	private float pipeXIndex, pipeYIndex, pipeYVariation, OFFSET;
	private int xScroll, xScrollSpeed;
    private float prevPos = 0;
	private static int bestCl = 0;
	private static int bestMa = 0;
    private static int bestCh = 0;
    private static int bestSa = 0;
    private static int targetHS = 0;
    private static boolean help = false;
    static int totalFloats = 0;
    private static int totalPoints = 0;
    static int totalBubbles = 0;
	private int index = 0;
	private int scoreIndex = 0;
    private int stage = 0;
    private boolean firstNewRecord = false;

    static boolean slowMo = false;
    static boolean giantBubbles = false;
    static boolean classy = false;
    static boolean darkMode = false;
    static boolean invincible = false;
    static boolean tinyFish = false;
    static boolean bubbleGun = false;

	private ParticleEffect effect = new ParticleEffect(0, 0, "res/baseParticle.png");

	//private VertexArray background, fade;//class Level is the one who generates the bg and the fish etc
	private VertexArray fade;//class Level is the one who generates the bg and the fish etc
	private Background background;
	
	private int map = 0;
	private int score = 0;
	
	static boolean hardcore = true;
    static boolean simplicity = false;
	
	private Fish fish;
	private Font fontMedium, fontLarge, fontLargeColor, fontXLargeOrange, fontSmall;
	
	private Pipe[] pipes = new Pipe[5 * 2];//wont need more than 5 pipes on bottom and 5 on top cause more than that wont fit on the screen
	private Random random = new Random();
	
	private float time = 0.0f;

	private boolean control = true;
	private boolean stop = false;
	public boolean menu = true;
	public boolean askExit = false;

    private ParticleEffect coralEffect;
	
	private boolean saved = false;
	
	public Level(){

        coralEffect = new ParticleEffect(0, 0, "res/baseParticle.png");
        coralEffect.setParticleAttr(120, new Vector2f(0.05f, 0.05f));
        coralEffect.colorMod = new Vector4f(1, 1, 1, 1);
        coralEffect.setVelocity(RndVel.choice.MultAddXY, new float[]{0.6f, 1, -0.2f, 0});
        coralEffect.setGravity(-0.0f, -0.00f, 0);

        effect.setParticleAttr(40, new Vector2f(0.05f, 0.05f));

        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            FileUtils.genScoreFile();
        }

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
		
		
		//background = new VertexArray(vertices, indices, tcs);
		
		float[] vertices = new float[] {
				-10.0f, -10.0f*9.0f / 16.0f, 0.0f,
				-10.0f, 10.0f*9.0f / 16.0f, 0.0f,
				 10.0f, 10.0f*9.0f / 16.0f, 0.0f,
				 10.0f, -10.0f*9.0f / 16.0f, 0.0f
		};

		fade = new VertexArray(vertices, indices, tcs);
		fish = new Fish(0, 1);
        fontSmall = new Font("res/Fonts/Basic.bmp", 0.5f);
		fontMedium = new Font("res/Fonts/BasicMedium.bmp", 0.5f);
		fontLarge = new Font("res/Fonts/BasicLarge.bmp", 1.0f);
        fontLargeColor = new Font("res/Fonts/BasicLargeColor.bmp", 1.0f);
        fontXLargeOrange = new Font("res/Fonts/BasicXLargeOrange.bmp", 2.0f);
		background = new Background();

		setLevelVars();
	}

    private void loadData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("score.txt"));
        int[] data = new int[7];
        int i = 0;
        String cur;
        while(i < data.length){
            cur = reader.readLine();
            if(cur == null){
                reader.close();
                return;
            }
            System.out.println(cur);
            data[i] = Integer.parseInt(cur.substring(0, cur.length()));//\r\n shit is ignored by default
            i++;
        }
        reader.close();
        bestCl = data[0];
        bestMa = data[1];
        bestCh = data[2];
        bestSa = data[3];
        totalPoints = data[4];
        totalFloats = data[5];
        totalBubbles = data[6];
    }
	
	public void reset(){//reset all the values to start anew
        fish = new Fish(menu ? 0: -5f, tinyFish ? 0.5f : 1);
		setLevelVars();
		saved = false;
		fish.control(true);
		time = 0.0f;
		control = true;
		score = 0;
        firstNewRecord = false;
        stage = 0;
	}
	
	private void setLevelVars(){//reset the sprite values etc
        coralEffect.resetPList();
		control = true;
		xScroll = 0;
		index = 0;
		scoreIndex = 0;
		map = 0;
		if(hardcore){
            fish.setBubbleVelocityFast();
            coralEffect.setVelocity(RndVel.choice.MultAddXY, new float[]{1.0f, 1, 0.0f, 0});
			pipeXIndex = 5.0f;
            if(simplicity){
                targetHS = bestSa;
                pipeYIndex = 12.0f;//12.0 for typical hardcore
                pipeYVariation = 4.0f;//4.0 for typical hardcore
            }
            else{
                targetHS = bestMa;
                pipeYIndex = 12.0f;//12.0 for typical hardcore
                pipeYVariation = 3.0f;//4.0 for typical hardcore
            }
			xScrollSpeed = 12;//must be a multiple of 3360
			OFFSET = 18.0f;
		}
		else{
            fish.setBubbleVelocitySlow();
            coralEffect.setVelocity(RndVel.choice.MultAddXY, new float[]{0.3f, 0.4f, 0.0f, 0});
			pipeXIndex = 3.0f;
            if(simplicity){
                targetHS = bestCh;
                pipeYIndex = 11.0f;
            }
            else{
                targetHS = bestCl;
                pipeYIndex = 12.5f;
            }
			pipeYIndex = 12.0f;
			pipeYVariation = 4.5f;
			xScrollSpeed = 3;//must be a multiple of 3360
			OFFSET = 4.0f;
		}
		createPipes();
	}
	
	private void createPipes(){
		Pipe.create();
		for(int i = 0; i < 5*2; i += 2){
			pipes[i] = new Pipe(OFFSET + index * pipeXIndex, random.nextFloat() * pipeYVariation);//top pipe
            if(hardcore && !simplicity){
                pipes[i] = new Pipe(OFFSET + index * pipeXIndex, prevPos + random.nextFloat() * pipeYVariation);//top pipe
            }
			pipes[i + 1] = new Pipe(pipes[i].getX(), pipes[i].getY() - pipeYIndex);//bottom pipe, aligns with top pipe in both the x and y
			index += 2;
		}
	}
	
	private void updatePipes(){//creates 2 new pipes, one on the bottom and one on top
		pipes[index % 10] = new Pipe(OFFSET + index * pipeXIndex, random.nextFloat() * pipeYVariation);
        if(hardcore && !simplicity){
            pipes[index % 10] = new Pipe(OFFSET + index * pipeXIndex, prevPos + random.nextFloat() * pipeYVariation);//top pipe
        }
		pipes[(index + 1) % 10] = new Pipe(pipes[index % 10].getX(), pipes[index % 10].getY() - pipeYIndex);
		index += 2;
	}
	
	public void update(){

        if(menu){
            if(Input.isKeyPressed(GLFW.GLFW_KEY_1) && totalBubbles >= 600){
                if(classy){
                    OpenALPlayer.playSound(2, 1);
                }
                else{
                    OpenALPlayer.playSound(5, 1);
                }
                classy = !classy;
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_2) && totalBubbles >= 1800){
                darkMode = !darkMode;
                if(!invincible){
                    Shader.FISH.enable();
                    Shader.FISH.setUniform1i("darkMode", darkMode ? 1 : 0);
                }
                Shader.BG.enable();
                Shader.BG.setUniform1i("darkMode", darkMode ? 1 : 0);
                Shader.PARTICLE.enable();
                Shader.PARTICLE.setUniform1i("darkMode", darkMode ? 1 : 0);
                Shader.PIPE.enable();
                Shader.PIPE.setUniform1i("darkMode", darkMode ? 1 : 0);
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_3) && totalBubbles >= 2400){
                tinyFish = !tinyFish;
                reset();
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_4) && totalBubbles >= 3000){
                giantBubbles = !giantBubbles;
                if(giantBubbles){
                    fish.bubbleSizeChange(new Vector2f(1.0f, 1.0f));
                }
                else{
                    fish.bubbleSizeChange(new Vector2f(0.2f, 0.2f));
                }
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_5) && totalBubbles >= 3500){
                bubbleGun = !bubbleGun;
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_6) && totalBubbles >= 30000){
                invincible = !invincible;
                Shader.FISH.enable();
                Shader.FISH.setUniform1i("darkMode", invincible ? 2 : 0);
            }
            else if(Input.isKeyPressed(GLFW.GLFW_KEY_S)){
                simplicity = !simplicity;
            }
            if(Input.isKeyHeld(GLFW.GLFW_KEY_A)){
                help = true;
                time = -0.05f;
            }
            else{
                help = false;
            }
        }

        if(!simplicity && Input.isKeyHeld(GLFW.GLFW_KEY_RIGHT_CONTROL) && totalBubbles >= 1200){
            slowMo = true;
            Main.SKIP_TICKS = 1000/10;
        }
        else if(Main.SKIP_TICKS > 1000/30){
            slowMo = false;
            Main.SKIP_TICKS -= 1000/150;
            if(Main.SKIP_TICKS <= 1000/30){
                Main.SKIP_TICKS = 1000/30;
            }
        }
		if(Input.isKeyPressed(GLFW.GLFW_KEY_M)){
			menu = true;	reset();
		}
		if(Input.isKeyPressed(GLFW.GLFW_KEY_H)){
			hardcore = true;
			if(menu) menu = false;
			reset();
		}
		if(Input.isKeyPressed(GLFW.GLFW_KEY_N)){
			hardcore = false;
			if(menu) menu = false;
			reset();	
		}
		if(Input.isKeyPressed(256) || Input.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)){//256 is the ESC key on my keyboard
			stop = true;
            askExit = true;
            save();
		}
        if(Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)){
            if(askExit){
                stop = false;
                askExit = false;
            }
            //If not stopping, fish gotta keep hopping
            else{
                Input.keys[GLFW.GLFW_KEY_SPACE] = true;
            }
        }

		if(!stop){//when not in stop mode, update normally
            effect.updateParticles();

			if(control){//if not dead, update normally

				xScroll -= xScrollSpeed;//-- because it goes backwards
				if(-xScroll % 336 == 0){
					map++;//update map
				}
				if(pipes[index % 10].getX() + xScroll * 0.05f < -10f - Pipe.getWidth()){//update pipes
					updatePipes();
				}
				if(!menu && pipes[scoreIndex % 10].getX() + xScroll * 0.05f + Pipe.getWidth() <= fish.getX()){//score calculator normal
					//the <= works cuz the scoreindex then switches to check the next set of pipes
					score++;
                    if(!simplicity && !hardcore){
                        switch(score){
                            case 10:
                                stage = 1;
                                pipeYIndex = 11.3f;
                                break;
                            case 20:
                                stage = 2;
                                pipeYIndex = 11.1f;
                                break;
                            case 30:
                                stage = 3;
                                pipeYIndex = 11.0f;
                                break;
                            case 50:
                                stage = 4;
                                pipeYIndex = 10.8f;
                                break;
                            case 80:
                                stage = 5;
                                pipeYIndex = 10.7f;
                                break;
                            case 100:
                                stage = 6;
                                pipeYIndex = 10.6f;
                                break;
                            case 150:
                                pipeYIndex = 10.5f;
                                break;
                        }
                    }
                    else{
                        switch(score){
                            case 20:
                                stage = 2;
                                break;
                            case 40:
                                stage = 3;
                                break;
                            case 60:
                                stage = 4;
                                break;
                            case 80:
                                stage = 2;
                                break;
                        }
                    }
                    if(!simplicity && hardcore && pipeYIndex > 10.0f){
                        pipeYIndex -= 0.017f;
                    }
                    totalPoints++;
					scoreIndex += 2;

                    //Play sound FX
					if(score > targetHS && !firstNewRecord){
                        int numBubbles = (score / 5 + 1) * 4 * 3;
                        fish.genBubbles(numBubbles, 5);
                        OpenALPlayer.playSound(3, 0);
                        firstNewRecord = true;
                    }
                    else if(score % 5 == 0){
                        int numBubbles = (score / 5 + 1) * 4;
                        if(numBubbles > 150)
                            numBubbles = 150;
                        fish.genBubbles(numBubbles, 5);
                        OpenALPlayer.setFXGain(OpenALPlayer.playSound(4, 0), 4.0f);
                    }
                    else{
                        fish.genBubbles(stage, 1);
                        OpenALPlayer.setFXGain(OpenALPlayer.playSound(4, 0), 4.0f);
                    }

                    //Update the high score
                    if(score > targetHS){
                        if(!simplicity && !hardcore){
                            bestCl = score;
                        }
                        else if(simplicity && !hardcore) {
                            bestCh = score;
                        }
                        else if(!simplicity && hardcore) {
                            bestMa = score;
                        }
                        else if(simplicity && hardcore) {
                            bestSa = score;
                        }
                    }
				}
			}

            coralEffect.updateParticles();
			fish.update(menu);//update fish

			if(!menu){//if actually doing stuff, fish can die etc
				if(control && collision() || control && !fish.getControl()){
					OpenALPlayer.playSound(0, 0);
					if(fish.getControl()) fish.fall(-0.25f);
					else fish.fall(-0.45f);
					control = false;
					fish.control(false);
					xScrollSpeed = 0;
				}
				if(!control && !saved){
                    save();
                    saved = true;
				}
			}
			if(time < 2.0f) time += 0.1f;//for fade
		}
	}

    private void save(){
        FileUtils.changeScore(
                bestCl + "\n" +
                        bestMa + "\n" +
                        bestCh + "\n" +
                        bestSa + "\n" +
                        totalPoints + "\n" +
                        totalFloats + "\n" +
                        totalBubbles + "\n");
    }
	
	private boolean collision(){
        if(invincible)
            return false;
		for (int i = 0; i< 5*2; i++){
			float bx = -xScroll * 0.05f + fish.getX();
			float by = fish.getY(0);//by = fishYCoord
			float px = pipes[i].getX();
			float py = pipes[i].getY();

			//figure out the hitbox from the 4 corners of fish
			float bx0 = bx - fish.getWidth() / 2.0f + 0.07f;
			float bx1 = bx + fish.getWidth() / 2.0f - 0.07f;
			float by0 = by - fish.getHeight() / 2.0f + 0.07f;
			float by1 = by + fish.getHeight() / 2.0f - 0.07f;
			
			//same for pipe
			float px1 = px + Pipe.getWidth();
			float py1 = py + Pipe.getHeight();
			
			if(bx1 > px && bx0 < px1){
				if(by1 > py && by0 < py1){
                    float coralX = fish.getX();
                    float coralY = fish.getY(0);
                    if(bx1 - px < 0.3f){
                        coralX += 0.4f;
                    }
                    if(fish.delta < 0){
                        coralY += 0.3f;
                    }
                    else{
                        coralY -= 0.3f;
                    }
                    coralEffect.position = new Vector3f(coralX, coralY, 1.1f);
                    coralEffect.generate(hardcore ? 60 : 18, 5);
					return true;
				}
			}
		}
		return false;
	}
	
	private void renderPipes(double interpol){
		Shader.PIPE.enable();
		Shader.PIPE.setUniform2f("fish", fish.getX(), fish.getY(interpol));
		Pipe.getTexture().bind();
		Pipe.getMesh().bind();
        //System.out.println("" + (xScroll - (float)(xScrollSpeed * interpol)) * 0.05f + " Interpol: " + interpol);
		Shader.PIPE.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f((xScroll - (float)(xScrollSpeed * interpol)) * 0.05f, 0.0f, 0.0f)));

		for(int i = 0; i < 5 * 2; i++){
			Shader.PIPE.setUniform1i("top", i % 2 == 0 ? 1 : 0);
			Shader.PIPE.setUniformMat4f("ml_matrix", pipes[i].getModelMatrix());
			Pipe.getMesh().draw();
		}

		Pipe.getMesh().unbind();
		Pipe.getTexture().unbind();
	}
	
	public void render(double interpol, int framerate){
		if(stop)
			interpol = 0;//for it not to spazz out

		background.renderBackdrop(fish, map, xScroll, 0, xScrollSpeed, interpol);

		if(!menu){
			renderPipes(interpol);
			fish.render(interpol);
            coralEffect.renderParticles(interpol);
            drawText(1);
		}else{
            ParticleEffect.setFishPosition(fish.getX(), fish.getY(interpol));
            effect.renderParticles(interpol);
			fish.render(interpol);
			background.renderSeafloor(fish, map, xScroll, 1, xScrollSpeed, interpol);
            drawText(0);
		}
		
		Shader.FADE.enable();
		Shader.FADE.setUniform1f("time", time);
        Shader.FADE.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f(0, 0, 4.0f)));
		fade.render();
		Shader.FADE.disable();

        fontSmall.drawString("FPS: " + framerate, 8.0f, 5.2f, 5.0f, false);
	}

	private void drawMenuText(){
        if(help){
            fontSmall.drawString("Button   Toggle Effect    Requirement" +
                               "\n------   -------------    -----------", -9.5f, 4.6f, 5.0f, false);
            fontSmall.drawString("   \"1\" - Classic Fish  -   600 bubbles", -9.5f, 3.6f, 5.0f, false);
            fontSmall.drawString("R-CTRL - The Matrix    -  1200 bubbles", -9.5f, 2.6f, 5.0f, false);
            fontSmall.drawString("   \"2\" - Dark Mode     -  1800 bubbles", -9.5f, 1.6f, 5.0f, false);
            fontSmall.drawString("   \"3\" - Tiny Fish     -  2400 bubbles", -9.5f, 0.6f, 5.0f, false);
            fontSmall.drawString("   \"4\" - Giant Bubbles -  3000 bubbles", -9.5f, -0.6f, 5.0f, false);
            fontSmall.drawString("   \"5\" - Bubble Bath   -  3500 bubbles", -9.5f, -1.6f, 5.0f, false);
            fontSmall.drawString("   \"6\" - The Ghost     - 30000 bubbles", -9.5f, -2.6f, 5.0f, false);

            fontSmall.drawString("\"S\" - Other modes", 0.8f, 4.6f, 5.0f, false);
            fontSmall.drawString("\"W\" - Slow fall (Mania only)", 0.8f, 3.6f, 5.0f, false);
            //fontSmall.drawString("\"R-Ctrl\" - Slow-mo (Classic only)", 0.8f, 2.6f, 5.0f, false);
            fontSmall.drawString("The Matrix is Classic and Mania only", 0.8f, 2.6f, 5.0f, false);

            fontSmall.drawString("Classic & Mania - Gets progressively" +
                               "\n                  harder", 0.8f, 1.6f, 5.0f, false);
            fontSmall.drawString("Chill & Sanic   - No progressive " +
                               "\n                  difficulty", 0.8f, 0.6f, 5.0f, false);
            fontSmall.drawString("Higher Stage -> More bubbles", 0.8f, -0.6f, 5.0f, false);

            fontSmall.drawString("Press Space To Jump", 0, -4f, 5.0f, true);
            fontSmall.drawString("VSYNC: " + (Main.vsync == 1), -9.5f, -4f, 5.0f, false);
            fontSmall.drawString("(\"V\" to toggle)", -9.5f, -4.5f, 5.0f, false);
        }
        fontSmall.drawString("\"T\" to mute sounds", -9.7f, -4.6f, false);
        fontSmall.drawString("\"F\" to toggle fullscreen", -9.7f, -5.1f, false);
        fontSmall.drawString("\"ESC\" - Quit", -8.0f, 5.2f, true);
        if(!simplicity){
            fontMedium.drawString("Press N to enter Classic Mode", 0, -3f, 0.5f, true);
            fontMedium.drawString("Press H to enter Mania Mode!", 0, -3.5f, 0.5f, true);
        }
        else{
            fontMedium.drawString("Press N to enter Chill Mode", 0, -3f, 0.5f, true);
            fontMedium.drawString("Press H to enter Sanic Mode!", 0, -3.5f, 0.5f, true);
        }
		//fontMedium.drawString("Press Space to swim!", 0, -2.5f, true);
        fontMedium.drawString("Hold \"A\" for help", 0, -2.5f, 0.5f, true);
        fontMedium.drawString("Bubble Count: " + totalBubbles, 4.6f, -5.0f, false);
		fontXLargeOrange.drawString("Floaty Fish", 0.2f, 4.5f, 0.5f, true);
	}

    private void drawSpecialOptionsText(){
        //600
        //1200
        //1800
        //2400
        //3000
        fontMedium.drawString("Bubbles  -  Reward  -  Button", 0, 0f, true);
    }

	private void drawPlayText(){
		fontLarge.drawString("Score: " + score, -6.7f, -4.5f, true);
        fontMedium.drawString("Stage " + (stage + 1), -9.0f, 4.4f);
        if(hardcore){
            if(simplicity){
                fontMedium.drawString("Sanic Best: " + bestSa, -9f, 5.0f, false);
            }
            else{
                fontMedium.drawString("Mania Best: " + bestMa, -9f, 5.0f, false);
            }
        }
        else{
            if(simplicity){
                fontMedium.drawString("Chill Best: " + bestCh, -9f, 5f, false);//added spaces to make the # of chars be similar to hardcore
            }
            else{
                fontMedium.drawString("Classic Best: " + bestCl, -9f, 5f, false);//added spaces to make the # of chars be similar to hardcore
            }
        }
        fontMedium.drawString("Bubble Count: " + totalBubbles, 4.6f, -5.0f, false);
		if(!control){
			fontMedium.drawString("Press R to restart", -6, -2.5f);
			fontMedium.drawString("Press M to return to the Menu", -6, -3f);
		}
	}

    private void drawExitText(){
        fontLargeColor.drawString("Press ESC again to exit game,\n  or press SPACE to resume.", 7, 2.0f, true);
    }

	private void drawText(int state){
        //Don't draw the other text when asking to exit
        if(askExit){
            drawExitText();
            return;
        }

        //Content

        //Options

        //Visuals

        switch (state){
            case 0: drawMenuText();
                break;
            case 1: drawPlayText();
                break;
            case 2: drawSpecialOptionsText();
                break;
        }
	}

}
