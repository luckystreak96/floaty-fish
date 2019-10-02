package com.larry.floaty.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static com.larry.floaty.utils.BufferUtils.ioResourceToByteBuffer;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.ALC_MONO_SOURCES;
import static org.lwjgl.openal.ALC11.ALC_STEREO_SOURCES;
import static org.lwjgl.openal.EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by Yanik on 29/05/2016.
 * Simple openAL engine that deals with sounds and music
 */
public class OpenALPlayer implements Runnable {
    private static int bgm = 0;
    private int typeResource;
    private int reqResource = 0;
    private int curSource = 0;
    private static boolean muted = false;

    private static String[] resources = new String[]{
        "aw06", "FX317", "This Sky Of Mine", "FX302", "plop", "w7"
         //0        1         2                3        4      5
    };

    private static int[] buffers = new int[resources.length];

    private OpenALPlayer(int curSource, int reqResource, int typeResource){
        this.curSource = curSource;
        this.reqResource = reqResource;
        this.typeResource = typeResource;
    }

    public void run(){
        if(muted)
            return;
        switch (typeResource) {
            case 0:
                playBasicSound(reqResource, curSource);
                break;
            case 1:
                playBGM(reqResource, curSource);
                break;
        }
    }

    public static void init()
    {
        //Init stuff
        for(int i = 0; i < resources.length; i++){
            resources[i] = "res/sounds/" + resources[i] + ".ogg";
        }

        long device = alcOpenDevice((ByteBuffer)null);
        if ( device == NULL ){
            throw new IllegalStateException("Failed to open the default device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        System.out.println("OpenALC10: " + deviceCaps.OpenALC10);
        System.out.println("OpenALC11: " + deviceCaps.OpenALC11);
        System.out.println("caps.ALC_EXT_EFX = " + deviceCaps.ALC_EXT_EFX);

        if ( deviceCaps.OpenALC11 ) {
            List<String> devices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
            for ( int i = 0; i < devices.size(); i++ )
                System.out.println(i + ": " + devices.get(i));
        }

        String defaultDeviceSpecifier = alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER);
        System.out.println("Default device: " + defaultDeviceSpecifier);

        long context = alcCreateContext(device, (IntBuffer)null);
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        System.out.println("ALC_FREQUENCY: " + alcGetInteger(device, ALC_FREQUENCY) + "Hz");
        System.out.println("ALC_REFRESH: " + alcGetInteger(device, ALC_REFRESH) + "Hz");
        System.out.println("ALC_SYNC: " + (alcGetInteger(device, ALC_SYNC) == ALC_TRUE));
        System.out.println("ALC_MONO_SOURCES: " + alcGetInteger(device, ALC_MONO_SOURCES));
        System.out.println("ALC_STEREO_SOURCES: " + alcGetInteger(device, ALC_STEREO_SOURCES));

        // Query for Effect Extension
        if ( !deviceCaps.ALC_EXT_EFX ) {
            alcCloseDevice(device);
            //throw new Exception("No EXTEfx supported by driver.");
        }
        System.out.println("EXTEfx found.");

        long alContext = alcCreateContext(device, (IntBuffer)null);
        alcMakeContextCurrent(alContext);


        //Load the sound files into buffers
        try ( STBVorbisInfo info = STBVorbisInfo.malloc() ) {
            ShortBuffer pcm;
            for(int i = 0; i < resources.length; i++){
                buffers[i] = alGenBuffers();
                //sources[i] = alGenSources();
                pcm = readVorbis(resources[i], 256 * 1024, info);
                alBufferData(buffers[i], AL_FORMAT_STEREO16, pcm, info.sample_rate());
                //alSourcei(sources[i], AL_BUFFER, buffers[i]);
            }
        }
    }

    public static void setFXGain(int source, float value){
        alSourcef(source, AL_GAIN, value);
    }

    private static void playBasicSound(int reqResource, int source)
    {
        alSourcei(source, AL_BUFFER, buffers[reqResource]);//assign the right sound buffer

        alSourcePlay(source);//play the sound
        while(alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING){//chill out until the sound finishes
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        alDeleteSources(source);//clear the source to free the memory for more
    }

    private static void playBGM(int reqRes, int source)
    {
        alSourcei(source, AL_BUFFER, buffers[reqRes]);//assign the right sound buffer
        alSourcei(source, AL_LOOPING, AL_TRUE);//set the looping

        if(alGetSourcei(bgm, AL_SOURCE_STATE) == AL_PLAYING)//stop the other bgm
            alSourceStop(bgm);

        bgm = source;
        alSourcePlay(source);
    }

    public static void toggleBGM(){
        if(alGetSourcei(bgm, AL_SOURCE_STATE) == AL_PLAYING)
            alSourcePause(bgm);
        else if(alGetSourcei(bgm, AL_SOURCE_STATE) == AL_PAUSED)
            alSourcePlay(bgm);

        muted = !muted;
    }

    public static int playSound(int reqRes, int resType){
        int lol = alGenSources();
        new Thread(new OpenALPlayer(lol, reqRes, resType)).start();
        return lol;
    }

    public static void exitOpenAL(){
        long Context = alcGetCurrentContext();
        long Device = alcGetContextsDevice(Context);
        alcMakeContextCurrent(NULL);
        alcDestroyContext(Context);
        alcCloseDevice(Device);
    }

    private static ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) {

        ByteBuffer vorbis;
        try {
            vorbis = ioResourceToByteBuffer(resource, bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);
        if ( decoder == NULL )
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

        ShortBuffer pcm = BufferUtils.createShortBuffer(lengthSamples*channels);

        stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
        stb_vorbis_close(decoder);

        return pcm;
    }
}
