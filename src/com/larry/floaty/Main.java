package com.larry.floaty;

import static com.larry.floaty.utils.BufferUtils.ioResourceToByteBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.larry.floaty.sound.OpenALPlayer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import com.larry.floaty.graphics.Shader;
import com.larry.floaty.input.Input;
import com.larry.floaty.level.Level;
import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.utils.FileUtils;

public class Main implements Runnable {//implementing runnable is necessary for threads
	
    public static int SKIP_TICKS = 1000/30;

	private boolean running = false;//the boolean that decides whether the loop should keep running
    //if the input starts getting GC'd, use this
	//private Input inputNoGC;
    private boolean isFullscreen = false;

    public static int vsync = 1;
    private static int frameRate = 0;
	
	private long window;//window identifier, glfw is a c++ thing so its a long used as an identifier instead of an object

	private Level level;
	
	public void start(){//start is called initially in the Main function
		running = true;
		Thread thread = new Thread(this, "Game");//create game thread
		thread.start();//calls run()
	}

	private void init(boolean fullscreen){
		if(!glfwInit()){
			return;
		}

        int width = 1280;//window width/height
        int height = 720;

		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);//setup some parameters

        //Determine the main monitor + it's size etc
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());//find monitor/position window

        //Fullscreen -- To make it fullscreen, set the monitor to glfwGetPrimaryMonitor() - otherwise leave as NULL
        if(fullscreen)
            window = glfwCreateWindow(vidmode.width(), vidmode.height(), "Floaty Fish", glfwGetPrimaryMonitor(), NULL);
        else
            window = glfwCreateWindow(width, height, "Floaty Fish", NULL, window);

		if(window == NULL) return;
		SetWindowIcon();


		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		
		Input inputNoGC = new Input();
		glfwSetKeyCallback(window, inputNoGC);//setup the input so it'll read it

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);//create the window tho
		GL.createCapabilities();//open the openGL context

		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);//clear to this color
		glEnable(GL_DEPTH_TEST);//test?
		//more textures can be active by setting corresponding numbers
		glActiveTexture(GL_TEXTURE1);//same as for setUniform1i("tex", -->1<--);
		glEnable(GL_BLEND);//this allows fade.vert/frag to alpha blend slowly in
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_TEXTURE_2D);
		
		System.out.println("OpenGL: " + glGetString(GL_VERSION));//return version of OpenGL
		Shader.loadAll();
		
		//Projection matrix must be set for every 'thing' that needs t be rendered
		Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -10.0f, 10.0f);//pr_matrix --> Projection matrix
		Shader.BG.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.BG.setUniform1i("tex", 1);
		Shader.BG.disable();

		//THIS MUST BE SET FOR EVERY NEW EXISTING SHADER OR IT WONT BE DRAWN
		Shader.FISH.setUniformMat4f("pr_matrix", pr_matrix);//FISH needs its projection matrix to be drawn
		Shader.FISH.setUniform1i("tex", 1);
		Shader.FISH.disable();

		Shader.PIPE.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.PIPE.setUniform1i("tex", 1);
		Shader.PIPE.disable();

		Shader.PARTICLE.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.PARTICLE.setUniform1i("tex", 1);
		Shader.PARTICLE.disable();

		Shader.FONT.setUniformMat4f("pr_matrix", pr_matrix);//FISH needs its projection matrix to be drawn
		Shader.FONT.setUniform1i("tex", 1);
		Shader.FONT.disable();

        Shader.FADE.setUniformMat4f("pr_matrix", pr_matrix);

		//pr_matrix order of op.: create shader --> set this up there --> Shader.java setup --> create its own class(flappy.level.Pipe)

	}

	public void run(){//called by start()
        //initialize openal
        OpenALPlayer.init();
        OpenALPlayer.playSound(2, 1);

        //initialize glfw window and opengl
		init(false);

        level = new Level();

        double interpolation;
        //final long FRAMES_GOAL = 1000/60;
        final int MAX_FRAMESKIP = 5;

        double next_game_tick = System.currentTimeMillis();
        int loops;

		long lastTime = System.currentTimeMillis();//Previous time to measure for each passing second
		long timer;
		int frames = 0;

		while(running)
        {
            timer = System.currentTimeMillis();//The current time
            loops = 0;
			glfwPollEvents();
            while (System.currentTimeMillis() >= next_game_tick && loops <= MAX_FRAMESKIP) {
                update();
                next_game_tick += SKIP_TICKS;
                loops++;
            }

            //The system that renders half-frames - allowing the game to be smooth with realy high fps and low update ticks
            interpolation = (System.currentTimeMillis() + SKIP_TICKS - next_game_tick) / ((double) SKIP_TICKS);
            render(interpolation);
            if(vsync == 0){
                /*
                try {
                    Thread.sleep(FRAMES_GOAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
            }
            frames++;

            if(timer - lastTime >= 1000){
                lastTime = System.currentTimeMillis();
                frameRate = frames;
                //System.out.println("Frames: " + frames);
                frames = 0;
            }
        }

        OpenALPlayer.exitOpenAL();
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	private void update(){
		if(Input.isKeyPressed(GLFW_KEY_R)){
			level.reset();
		}
        else if(Input.isKeyPressed(GLFW_KEY_F) && level.menu){
            glfwDestroyWindow(window);//Destroys window to make space for the other one
            if(!isFullscreen)
                init(true);
            else
                init(false);
            isFullscreen = !isFullscreen;
            level = new Level();//The vao's must be reinitialized or nothing will draw
        }
        else if(Input.isKeyPressed(GLFW_KEY_T))
            OpenALPlayer.toggleBGM();
        if(Input.isKeyPressed(GLFW_KEY_ESCAPE))//256 = ESC
            if(level.askExit)
                running = false;
            else
                Input.keys[GLFW_KEY_ESCAPE] = true;//bypass resetpress so they can both check

        if(Input.isKeyPressed(GLFW_KEY_V)){
            if(vsync == 0){
                glfwSwapInterval(1);
                vsync = 1;
            }
            else{
                glfwSwapInterval(0);
                vsync = 0;
            }
        }

        level.update();
	}
	
	private void render(double interpolation){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//helps with color and depth issues
		level.render(interpolation, frameRate);

		int error = glGetError();
		if(error != GL_NO_ERROR)
			System.out.println(error);

		glfwSwapBuffers(window);
	}

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
		FileUtils.genScoreFile();
		new Main().start();//the actual start function

	}

	private void SetWindowIcon()
	{
		IntBuffer w = org.lwjgl.BufferUtils.createIntBuffer(1);
		IntBuffer h = org.lwjgl.BufferUtils.createIntBuffer(1);
		IntBuffer comp = org.lwjgl.BufferUtils.createIntBuffer(1);

		ByteBuffer icon16;
		//ByteBuffer icon32;
		try {
			icon16 = ioResourceToByteBuffer("res\\icon.png", 8 * 1024);
			//icon32 = ioResourceToByteBuffer("C:\\Files\\Programming\\Eclipse\\FloatyFish\\Floaty\\res\\bbb.ico", 8 * 1024);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try ( GLFWImage.Buffer icons = GLFWImage.malloc(1) ) {
			ByteBuffer pixels16 = stbi_load_from_memory(icon16, w, h, comp, 0);
			icons
					.position(0)
					.width(w.get(0))
					.height(h.get(0))
					.pixels(pixels16);

            /*
            ByteBuffer pixels32 = stbi_load_from_memory(icon32, w, h, comp, 0);
            icons
                    .position(1)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels32);

*/
			icons.position(0);
			glfwSetWindowIcon(window, icons);

			//stbi_image_free(pixels32);
			stbi_image_free(pixels16);
		}
	}
}
