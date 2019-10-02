package com.larry.floaty.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Input extends GLFWKeyCallback {//when u press a button, its numerical value will
											//be used as the arg "action"
	
	public static boolean[] keys = new boolean[65536];
	
	public void invoke(long window, int key, int scancode, int action, int mods){
        if(key >=0 && key <= 65536){
            keys[key] = action == GLFW.GLFW_PRESS;
        }
        if(key == GLFW.GLFW_KEY_RIGHT_CONTROL){
            keys[GLFW.GLFW_KEY_RIGHT_CONTROL] = action != GLFW.GLFW_RELEASE;
        }
        else if(key == GLFW.GLFW_KEY_W){
            keys[GLFW.GLFW_KEY_W] = action != GLFW.GLFW_RELEASE;
        }
        else if(key == GLFW.GLFW_KEY_A){
            keys[GLFW.GLFW_KEY_A] = action != GLFW.GLFW_RELEASE;
        }
		//System.out.println(key);
		//keys[key] = action != GLFW.GLFW_RELEASE;//the return of that code is true/false
		//depending on if the specific key is being released, see update in main()
	}
	
	public static boolean isKeyPressed(int keycode){
		if(keys[keycode]){
			resetPress(keycode);
			return true;
		}
		else
			return false;
	}

    public static boolean isKeyHeld(int keycode){
        return keys[keycode];
    }
	
	private static void resetPress(int keycode){
		keys[keycode] = false;
	}

}
