package org.phivetech.serverview;

import java.util.Arrays;

import android.graphics.Color;

public class Cube {
	
	private static float[] DEFAULT_VERTICES = { // Cube 1
	    // Front face
	    -1.0f, 1.0f, 1.0f,
	    -1.0f, -1.0f, 1.0f,
	    1.0f, 1.0f, 1.0f,
	    -1.0f, -1.0f, 1.0f,
	    1.0f, -1.0f, 1.0f,
	    1.0f, 1.0f, 1.0f,
	
	    // Right face
	    1.0f, 1.0f, 1.0f,
	    1.0f, -1.0f, 1.0f,
	    1.0f, 1.0f, -1.0f,
	    1.0f, -1.0f, 1.0f,
	    1.0f, -1.0f, -1.0f,
	    1.0f, 1.0f, -1.0f,
	
	    // Back face
	    1.0f, 1.0f, -1.0f,
	    1.0f, -1.0f, -1.0f,
	    -1.0f, 1.0f, -1.0f,
	    1.0f, -1.0f, -1.0f,
	    -1.0f, -1.0f, -1.0f,
	    -1.0f, 1.0f, -1.0f,
	
	    // Left face
	    -1.0f, 1.0f, -1.0f,
	    -1.0f, -1.0f, -1.0f,
	    -1.0f, 1.0f, 1.0f,
	    -1.0f, -1.0f, -1.0f,
	    -1.0f, -1.0f, 1.0f,
	    -1.0f, 1.0f, 1.0f,
	
	    // Top face
	    -1.0f, 1.0f, -1.0f,
	    -1.0f, 1.0f, 1.0f,
	    1.0f, 1.0f, -1.0f,
	    -1.0f, 1.0f, 1.0f,
	    1.0f, 1.0f, 1.0f,
	    1.0f, 1.0f, -1.0f,
	
	    // Bottom face
	    1.0f, -1.0f, -1.0f,
	    1.0f, -1.0f, 1.0f,
	    -1.0f, -1.0f, -1.0f,
	    1.0f, -1.0f, 1.0f,
	    -1.0f, -1.0f, 1.0f,
	    -1.0f, -1.0f, -1.0f,
	};
	
	private float[] vertices;
	private float[] colors;
	private float[] normals;
	
	public Cube(int[] colorList){
		vertices = Arrays.copyOf(DEFAULT_VERTICES, DEFAULT_VERTICES.length);
		colors = new float[144];
		int i = 0;
		for(int f = 0; f < 6; f++){ // for each face
			colors[i++] = Color.red(colorList[f]);
			colors[i++] = Color.green(colorList[f]);
			colors[i++] = Color.blue(colorList[f]);
			colors[i++] = Color.alpha(colorList[f]);
		}
	}

}
