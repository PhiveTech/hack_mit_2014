package org.phivetech.serverview;

import java.util.Arrays;

import android.opengl.Matrix;

import static android.opengl.GLES20.glUniform3f;

public class PointLight {
	
	private float[] inWorld;
	
	public PointLight(float[] location){
		if(location.length != 4){
			throw new RuntimeException("let there be dark");
		}
		inWorld = Arrays.copyOf(location, 4);
	}
	
	public void place(int lightPointer, Camera camera){
		float[] inEyeView = new float[4];
		Matrix.multiplyMV(inEyeView, 0, camera.getView(), 0, inWorld, 0);
		glUniform3f(lightPointer, inEyeView[0], inEyeView[1], inEyeView[2]);
	}

}
