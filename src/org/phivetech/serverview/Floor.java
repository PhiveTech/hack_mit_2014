package org.phivetech.serverview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Color;
import android.opengl.Matrix;

import static android.opengl.GLES20.*;

/**
 * Represents the floor in 3-space.
 * @author zrneely
 */
public class Floor extends DrawableObject {
	
	private static float[] DEFAULT_VERTICES = {
        200f, 0, -200f,
        -200f, 0, -200f,
        -200f, 0, 200f,
        200f, 0, -200f,
        -200f, 0, 200f,
        200f, 0, 200f,
	};
	private static final float[] DEFAULT_NORMALS = {
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
	};
	
	private FloatBuffer vertices;
	private FloatBuffer colors;
	private FloatBuffer normals;
	
	private float[] model;
	private float[] view;
	private float[] viewProjection;
	
	/**
	 * Creates the floor.
	 * @param colorList A single color.
	 */
	public Floor(int color, float floorDepth){
		ByteBuffer bbVertices = ByteBuffer.allocateDirect(18 * 4);
		bbVertices.order(ByteOrder.nativeOrder());
		vertices = bbVertices.asFloatBuffer();
		vertices.put(DEFAULT_VERTICES);
		
		ByteBuffer bbColors = ByteBuffer.allocateDirect(96 * 4);
		bbColors.order(ByteOrder.nativeOrder());
		colors = bbColors.asFloatBuffer();
		for(int n = 0; n < 6; n++){
			colors.put(Color.red(color) / 255f);
			colors.put(Color.green(color) / 255f);
			colors.put(Color.blue(color) / 255f);
			colors.put(Color.alpha(color) / 255f);
		}
		
		ByteBuffer bbNormals = ByteBuffer.allocateDirect(18 * 4);
		bbNormals.order(ByteOrder.nativeOrder());
		normals = bbNormals.asFloatBuffer();
		normals.put(DEFAULT_NORMALS);
		
		this.model = new float[16];
		Matrix.setIdentityM(this.model, 0);
		Matrix.translateM(this.model, 0, 0, -floorDepth, 0);
		this.view = new float[16];
		this.viewProjection = new float[16];
	}
	
	public void draw(float[] headView, float[] perspective, int programPointer,
			int modelPointer, int viewPointer, int viewProjectionPointer) {
		// Do matrix multiplications
		updateView(headView);
		Matrix.multiplyMM(viewProjection, 0, perspective, 0, headView, 0);
		// Set up OpenGL pointers
		int isFloorPointer = glGetAttribLocation(programPointer, "u_IsFloor");
		// TODO we just got these in MainActivity.onDrawEye
		int positionPointer = glGetAttribLocation(programPointer, "a_Position");
		int normalPointer = glGetAttribLocation(programPointer, "a_Normal");
		int colorPointer = glGetAttribLocation(programPointer, "a_Color");
		// We are the floor
		glUniform1f(isFloorPointer, 1f);
		// Set the model in the shader
		glUniformMatrix4fv(modelPointer, 1, false, model, 0);
		// Set the view in the shader
		glUniformMatrix4fv(viewPointer, 1, false, headView, 0);
		// Set the view projection in the shader
		glUniformMatrix4fv(viewProjectionPointer, 1, false, viewProjection, 0);
		// Set the position of the cube
		glVertexAttribPointer(positionPointer, 3, GL_FLOAT, false, 0, vertices);
		// Set the normals in the shader
		glVertexAttribPointer(normalPointer, 3, GL_FLOAT, false, 0, normals);
		// Set the color, woo
		glVertexAttribPointer(colorPointer, 4, GL_FLOAT, false, 0, colors);
		// Draw all 6 floor triangles.
		glDrawArrays(GL_TRIANGLES, 0, 6);
		
		MainActivity.checkGLError("drawFloor");
	}

	public void updateView(float[] view){
		Matrix.multiplyMM(this.view, 0, view, 0, model, 0);
	}

}
