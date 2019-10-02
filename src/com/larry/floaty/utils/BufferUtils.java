package com.larry.floaty.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BufferUtils {

	private BufferUtils(){//this is private to prevent instantiation
	}
	
	public static ByteBuffer createByteBuffer(byte[] array){
		ByteBuffer result = ByteBuffer.allocateDirect(array.length).order(ByteOrder.nativeOrder());
		result.put(array).flip();
		return result;
	}

	public static FloatBuffer createFloatBuffer(float[] array){
		FloatBuffer result = ByteBuffer.allocateDirect(array.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		result.put(array).flip();
		return result;
	}

    public static FloatBuffer createFloatBuffer(int num){
        FloatBuffer result = ByteBuffer.allocateDirect(num).order(ByteOrder.nativeOrder()).asFloatBuffer();
        return result;
    }

	public static IntBuffer createIntBuffer(int[] array){
		IntBuffer result = ByteBuffer.allocateDirect(array.length << 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		result.put(array).flip();
		return result;
	}

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if ( Files.isReadable(path) ) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = org.lwjgl.BufferUtils.createByteBuffer((int)fc.size() + 1);

                int temp = 0;
                while (temp != -1) {
                    temp = fc.read(buffer);
                }
            }
        } else {
            try (
                    InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = org.lwjgl.BufferUtils.createByteBuffer(bufferSize);

                while ( true ) {
                    int bytes = rbc.read(buffer);
                    if ( bytes == -1 )
                        break;
                    if ( buffer.remaining() == 0 )
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = org.lwjgl.BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

}
