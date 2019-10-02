package com.larry.floaty.level;

import com.larry.floaty.math.RndVel;
import com.larry.floaty.math.Vector2f;
import com.larry.floaty.math.Vector3f;
import com.larry.floaty.math.Vector4f;

/**
 * Created by Yanik on 01/06/2016.
 * This class(more like struct) handles the
 * properties of an individual particle
 */
class Particle {
    public Vector3f position;
    Vector3f velocity;
    Vector4f color;
    //float angle;
    private Vector2f size;
    float age = 0;
    private float lifeTime;
    Vector3f gravity;

    Particle(ParticleEffect daddy){
        color = daddy.colorMod;
        lifeTime = daddy.lifeTime;
        size = daddy.particleSize;
        position = daddy.position.clone();
        position.z += 0.01f * daddy.random.nextFloat();
        gravity = daddy.gravity;
        //velocity = new Vector3f( (ParticleEffect.random.nextFloat() - 0.5f) / 4, (ParticleEffect.random.nextFloat() - 0.5f) / 8, 1);

        float[] params = new float[daddy.velFrm.length - 1];
        for(int i = 1; i < daddy.velFrm.length; i++){
            System.arraycopy(daddy.velFrm, i, params, i - 1, 1);
        }

        float[] result = RndVel.rndChoice((int)daddy.velFrm[0], params);

        velocity = new Vector3f(result[0], result[1], 1);
        //System.out.println(velocity.x + "____" + velocity.y);
    }

    public void update(){
        position.x += velocity.x;
        position.y += velocity.y;

        if(gravity.z == 0){
            velocity.x += gravity.x;
            velocity.y += gravity.y;
        }
        else{
            velocity.x *= gravity.x;
            velocity.y *= gravity.y;
        }

        age++;
    }

    /*
    public void setRandomColorsBright(){
        color = new Vector4f(ParticleEffect.random.nextFloat(), ParticleEffect.random.nextFloat(), ParticleEffect.random.nextFloat(), 1);
    }
    */

    float lifePerc(){
        return (5 - (age * 5 / lifeTime)) - 0.1f;
    }

    boolean isAlive(){
        return age < lifeTime;
    }
}
