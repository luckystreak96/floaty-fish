package com.larry.floaty.math;

import java.util.Random;

/**
 * Created by Yanik on 02/06/2016.
 * This class generates random numbers according to specified parameters
 */
public class RndVel {
    public enum choice {Mult, Add, MultXY, MultAddXY}
    private static Random rnd = new Random();

    public static float[] rndChoice(int chx, float param[]){
        switch(chx){
            case 0:
                return rndFMult(param[0]);
            case 1:
                return new float[]{0};
            case 2:
                return rndFMultXY(param[0], param[1]);
            case 3:
                return rndFMultXYADD(param[0], param[1], param[2], param[3]);
        }

        return new float[]{404};//lol error 404
    }

    private static float[] rndFMult(float multiplier){
        return new float[]{
                (rnd.nextFloat() - 0.5f) * multiplier,
                (rnd.nextFloat() - 0.5f) * multiplier
        };
    }

    private static float[] rndFMultXY(float x, float y){
        return new float[]{
                (rnd.nextFloat() - 0.5f) * x,
                (rnd.nextFloat() - 0.5f) * y
        };
    }

    private static float[] rndFMultXYADD(float x, float y, float ax, float ay){
        return new float[]{
                (rnd.nextFloat() - 0.5f + ax) * x,
                (rnd.nextFloat() - 0.5f + ay) * y
        };
    }
}
