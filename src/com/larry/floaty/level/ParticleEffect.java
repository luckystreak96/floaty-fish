package com.larry.floaty.level;

import com.larry.floaty.graphics.Shader;
import com.larry.floaty.graphics.Texture;
import com.larry.floaty.graphics.VertexArray;
import com.larry.floaty.math.Matrix4f;
import com.larry.floaty.math.Vector2f;
import com.larry.floaty.math.Vector3f;
import com.larry.floaty.math.Vector4f;
import com.larry.floaty.math.RndVel;
import com.larry.floaty.utils.SLList;

import java.util.Random;

/**
 * Created by Yanik on 01/06/2016.
 * Each instance of this class handles a set type of particles
 * with specified parameters, including optimal draw and update calls
 */
class ParticleEffect {


    private String resource;

    public static Random random = new Random();
    private Texture texture;
    private VertexArray mesh;
    private SLList<Particle> pList;
    private int numGenerations = 0;
    private float particlesRate = 0;
    private float particlesProg = 0;

    public Vector3f position = new Vector3f();
    Vector4f colorMod = new Vector4f();
    float lifeTime = 20;
    Vector2f particleSize = new Vector2f(0.2f, 0.2f);
    float[] velFrm;
    Vector3f gravity = new Vector3f(-0.005f, -0.001f, 0);

    public void create(String resource){
        this.resource = resource;

        //determines size/shape
        float[] vertices = new float[] {//changes between different textures
                0.0f, 0.0f, position.z,
                0.0f, particleSize.y, position.z,
                particleSize.x, particleSize.y, position.z,
                particleSize.x, 0.0f, position.z
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
        texture = new Texture(resource);

    }

    void resetPList(){
        pList = null;
    }

    ParticleEffect(float x, float y, String resource){
        position.x = x;
        position.y = y;
        create(resource);
    }

    void setParticleAttr(float lifeTime, Vector2f size){
        this.lifeTime = lifeTime;
        this.particleSize = size;
        create(resource);
    }

    void setParticleSize(Vector2f size){
        particleSize = new Vector2f(size.x, size.y);
        create(resource);
    }

    public void setPosition(Vector3f vector){
        position = vector;
        create(resource);
    }

    public VertexArray getMesh(){
        return mesh;
    }

    public Texture getTexture(){
        return texture;
    }

    public void generate(int amount){
        if(resource.equals("res/bubble.png")){
            Level.totalBubbles += amount;
        }
        int numGens = amount;
        while(numGens > 0){
            pList = new SLList<>(new Particle(this), pList);
            numGens--;
        }
    }

    public void generate(int amount, int frames){
        generate(1);
        particlesProg = 0;
        particlesRate = (float)amount / (float)frames;
        numGenerations = frames;
    }

    void updateParticles(){

        if(pList == null){
            numGenerations = 0;
            return;
        }

        SLList<Particle> prev = pList;
        int countSize = 0;

        for(SLList<Particle> cur = pList; cur != null; cur = cur.tail) {
            if(countSize > 1000)
                cur.head.age++;

            if(!cur.head.isAlive()){
                if(pList != cur){
                    if (prev != null) {
                        prev.tail = cur.tail;
                    }
                }
                else{
                    pList = pList.tail;
                    prev = pList;
                }
            }
            else{
                cur.head.update();
                countSize++;
            }

            if(prev != cur && prev != null)
                prev = prev.tail;
        }

        if(numGenerations > 0){
            particlesProg += particlesRate;
            if(particlesProg >= 1){
                generate((int)particlesProg);
                particlesProg -= (int)particlesProg;
            }
            numGenerations--;
        }
    }

    void renderParticles(double interpol){
        Shader.PARTICLE.enable();
        getTexture().bind();
        getMesh().bind();

        Vector3f pos;

        for(SLList<Particle> cur = pList; cur != null; cur = cur.tail) {
            pos = cur.head.position.clone();
            pos.x = pos.x + (cur.head.velocity.x + cur.head.gravity.x) * (float)interpol;
            pos.y = pos.y + (cur.head.velocity.y + cur.head.gravity.y) * (float)interpol;
            Shader.PARTICLE.setUniformMat4f("vw_matrix", Matrix4f.translate(pos));
            Shader.PARTICLE.setUniform1f("lifeTime", cur.head.lifePerc());
            Shader.PARTICLE.setUniform4f("partColor", cur.head.color);

            getMesh().draw();
        }
        getMesh().unbind();
        getTexture().unbind();
    }

    void setVelocity(RndVel.choice met, float params[]){
        velFrm = new float[params.length + 1];
        velFrm[0] = met.ordinal();
        for(int i = 1; i <= params.length; i++){
            System.arraycopy(params, i - 1, velFrm, i, 1);
        }
    }

    void setGravity(float x, float y, float z){
        gravity = new Vector3f(x, y, z);
    }

    static void setFishPosition(float x, float y){
        Shader.PARTICLE.enable();
        Shader.PARTICLE.setUniform2f("fish", x, y);
    }
}
