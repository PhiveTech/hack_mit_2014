package org.phivetech.serverview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Color;
import android.opengl.Matrix;

import static android.opengl.GLES20.*;

/**
 * Represents a drawable colored cube in 3-space.
 * @author zrneely
 */
public class Cube extends DrawableObject {
	
	private static float[] DEFAULT_VERTICES = {
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
	private static final float[] DEFAULT_NORMALS = {
		// Front face
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,

        // Right face
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,

        // Back face
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,

        // Left face
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,

        // Top face
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,

        // Bottom face
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f
	};
	
	// The definition of the cube
	private FloatBuffer vertices;
	private FloatBuffer ordinaryColors;
	private FloatBuffer foundColors;
	private FloatBuffer normals;
	
	// The actual cube matrix
	private float[] model;
	// The view on the cube
	private float[] view;
	// The projected view on the cube
	private float[] viewProjection;
	
	private float distance;
	
	/**
	 * Creates a colored cube.
	 * @param normalColors An array of 6 colors, used when the cube is not focused
	 * @param foundColors An array of 6 colors, used when the cube is focused
	 */
	public Cube(int[] normalColors, int[] foundColors, float objectDistance){
		this.distance = objectDistance;
		
		// Load the vertices
		ByteBuffer bbVertices = ByteBuffer.allocateDirect(108 * 4);
		bbVertices.order(ByteOrder.nativeOrder());
		vertices = bbVertices.asFloatBuffer();
		vertices.put(DEFAULT_VERTICES);
		
		// Load the ordinary colors
		ByteBuffer bbOColors = ByteBuffer.allocateDirect(144 * 4);
		bbOColors.order(ByteOrder.nativeOrder());
		this.ordinaryColors = bbOColors.asFloatBuffer();
		for(int f = 0; f < 6; f++){ // for each face
			for(int n = 0; n < 6; n++){
				this.ordinaryColors.put(Color.red(normalColors[f]) / 255f);
				this.ordinaryColors.put(Color.green(normalColors[f]) / 255f);
				this.ordinaryColors.put(Color.blue(normalColors[f]) / 255f);
				this.ordinaryColors.put(Color.alpha(normalColors[f]) / 255f);
			}
		}
		
		// Load the "found it" colors
		ByteBuffer bbFColors = ByteBuffer.allocateDirect(144 * 4);
		bbFColors.order(ByteOrder.nativeOrder());
		this.foundColors = bbFColors.asFloatBuffer();
		for(int f = 0; f < 6; f++){
			for(int n = 0; n < 6; n++){
				this.foundColors.put(Color.red(foundColors[f]) / 255f);
				this.foundColors.put(Color.green(foundColors[f]) / 255f);
				this.foundColors.put(Color.blue(foundColors[f]) / 255f);
				this.foundColors.put(Color.alpha(foundColors[f]) / 255f);
			}
		}
		
		// Load the normals
		ByteBuffer bbNormals = ByteBuffer.allocateDirect(DEFAULT_NORMALS.length * 4);
		bbNormals.order(ByteOrder.nativeOrder());
		this.normals = bbNormals.asFloatBuffer();
		this.normals.put(DEFAULT_NORMALS);
		
		this.model = new float[16];
		Matrix.setIdentityM(this.model, 0);
		Matrix.translateM(this.model, 0, 0, 0, -distance);
		
		this.view = new float[16];
		this.viewProjection = new float[16];
	}
	
	// TODO take a camera object
	private void updateView(float[] headView){
		Matrix.multiplyMM(this.view, 0, headView, 0, model, 0);
	}
	
	/**
	 * Detects if this object is in the center of the field of view.
	 * @param headView The current head view TODO take a camera object
	 * @param pitchLimit How lenient to be in terms of pitch
	 * @param yawLimit How lenient to be in terms of yaw
	 * @return Is the object within pitchLimit and yawLimit of the center of the
	 * 		field of view?
	 */
	public boolean isLookingAt(float[] headView, float pitchLimit, float yawLimit){
		float[] initVec = {0, 0, 0, 1f};
		float[] objPos = new float[4];
		updateView(headView);
		// Calculate the object position in the view
		Matrix.multiplyMV(objPos, 0, view, 0, initVec, 0);
		float pitch = (float) Math.atan2(objPos[1], -objPos[2]);
		float yaw  = (float) Math.atan2(objPos[0], -objPos[2]);
		return Math.abs(pitch) < pitchLimit && Math.abs(yaw) < yawLimit;
	}
	
	/**
	 * 
	 * @param headView
	 * @param perspective
	 * @param programPointer
	 * @param modelPointer
	 * @param viewPointer
	 * @param viewProjectionPointer
	 */
	public void draw(float[] headView, float[] perspective, int programPointer,
			int modelPointer, int viewPointer, int viewProjectionPointer){
		// Do matrix multiplications
		updateView(headView);
		Matrix.multiplyMM(viewProjection, 0, perspective, 0, headView, 0);
		// Set OpenGL variables
		int isFloorPointer = glGetAttribLocation(programPointer, "u_IsFloor");
		// TODO we just got these in MainActivity.onDrawEye
		int positionPointer = glGetAttribLocation(programPointer, "a_Position");
		int normalPointer = glGetAttribLocation(programPointer, "a_Normal");
		int colorPointer = glGetAttribLocation(programPointer, "a_Color");
		// We aren't a fucking floor
		glUniform1f(isFloorPointer, 0f);
		// Set the model in the shader
		glUniformMatrix4fv(modelPointer, 1, false, model, 0);
		// Set the view in the shader
		glUniformMatrix4fv(viewPointer, 1, false, view, 0);
		// Set the position of the cube
		glVertexAttribPointer(positionPointer, 3, GL_FLOAT, false, 0, vertices);
		// Set the view projection matrix in the shader
		glUniformMatrix4fv(viewProjectionPointer, 1, false, viewProjection, 0);
		// Set the normals of the cube in the shader
		glVertexAttribPointer(normalPointer, 3, GL_FLOAT, false, 0, normals);
		// Set the color in the shader
		if(isLookingAt(headView, MainActivity.PITCH_LIMIT, MainActivity.YAW_LIMIT)){
			glVertexAttribPointer(colorPointer, 4, GL_FLOAT, false, 0, ordinaryColors);
		}
		else {
			glVertexAttribPointer(colorPointer, 4, GL_FLOAT, false, 0, foundColors);
		}
		// Draw all 36 triangles
		glDrawArrays(GL_TRIANGLES, 0, 36);
		
		MainActivity.checkGLError("drawCube");
	}
	
	/**
	 * Randomly places the cube.
	 */
	public void randomizeLocation(){
		float[] rotation = new float[16];
		float[] posVec = new float[4];
		
		float angleXZ = (float) Math.random() * 180 + 90;
		Matrix.setRotateM(rotation, 0, angleXZ, 0f, 1f, 0f);
		float oldDist = distance;
		distance = (float) Math.random() * 15 + 5;
		float objScaling = distance / oldDist;
		Matrix.scaleM(rotation, 0, objScaling, objScaling, objScaling);
		Matrix.multiplyMV(posVec, 0, rotation, 0, model, 12);
		
		float angleY = (float) Math.random() * 80 - 40;
		angleY = (float) Math.toRadians(angleY);
		float newY = (float) Math.tan(angleY) * distance;
		
		Matrix.setIdentityM(model, 0);
		Matrix.translateM(model, 0, posVec[0], newY, posVec[2]);
	}
	
	/**
	 * Rotates the cube.
	 * @param angle The angle to rotate by, in degrees.
	 * @param x X-Axis component
	 * @param y Y-Axis component
	 * @param z Z-Axis component
	 */
	public void rotate(float angle, float x, float y, float z) {
		Matrix.rotateM(model, 0, angle, x, y, z);
	}

}
