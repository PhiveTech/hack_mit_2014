package org.phivetech.serverview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends CardboardActivity implements
			CardboardView.StereoRenderer {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final float CAMERA_Z = 0.01f;
	private static final float TIME_DELTA = 0.13f;
	private static final float YAW_LIMIT = 0.12f;
	private static final float PITCH_LIMIT = 0.12f;
	private static final int COORDS_PER_VERTEX = 3;
	
	private final float[] mLightPosInWorldSpace = new float[]{0.0f, 2.0f, 0.0f, 1.0f};
	private final float[] mLightPosInEyeSpace = new float[4];
	private FloatBuffer mFloorVertices;
	private FloatBuffer mFloorColors;
	private FloatBuffer mFloorNormals;
	private FloatBuffer mCubeVertices;
	private FloatBuffer mCubeColors;
	private FloatBuffer mCubeFoundColors;
	private FloatBuffer mCubeNormals;
	private int mGlProgram;
	private int mPostionParam;
	private int mNormalParam;
	private int mColorParam;
	private int mModelViewProjectionParam;
	private int mLightPosParam;
	private int mModelViewParam;
	private int mModelParam;
	private int mIsFloorParam;
	private float[] mModelCube;
	private float[] mCamera;
	private float[] mView;
	private float[] mHeadView;
	private float[] mModelViewProjection;
	private float[] mModelView;
	private float[] mModelFloor;
	
	private int mScore = 0;
	private float mObjectDistance = 12f;
	private float mFloorDepth = 20f;
	
	private Vibrator vibrator;
	
	private ServerviewOverlayView mHUD;
	
	private int loadGlShader(int type, int resId){
		String code = readRawTextFile(resId);
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);
		
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		
		if(compileStatus[0] == 0){
			Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		if(shader == 0){
			throw new RuntimeException("Error compiling shader.");
		}
		return shader;
	}
	
	private static void checkGLError(String func){
		int error;
		while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
			Log.e(TAG, func + ": glError " + error);
			throw new RuntimeException(func + ": glError " + error);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
		
		mModelCube = new float[16];
		mCamera = new float[16];
	    mView = new float[16];
	    mModelViewProjection = new float[16];
	    mModelView = new float[16];
	    mModelFloor = new float[16];
	    mHeadView = new float[16];
	    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    
	    mHUD = (ServerviewOverlayView) findViewById(R.id.overlay);
	    mHUD.show3dToast("Do a thing when you see the thing");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDrawEye(EyeTransform transform) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mPostionParam = GLES20.glGetAttribLocation(mGlProgram, "a_Position");
		mNormalParam= GLES20.glGetAttribLocation(mGlProgram, "a_Normal");
		mColorParam = GLES20.glGetAttribLocation(mGlProgram, "a_Color");
		GLES20.glEnableVertexAttribArray(mPostionParam);
		GLES20.glEnableVertexAttribArray(mNormalParam);
		GLES20.glEnableVertexAttribArray(mColorParam);
		checkGLError("mColorParam");
		
		Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, mLightPosInWorldSpace, 0);
		GLES20.glUniform3f(mLightPosParam, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1],
			mLightPosInEyeSpace[2]);
		
		Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCube, 0);
		Matrix.multiplyMM(mModelViewProjection, 0, transform.getPerspective(), 
				0, mModelView, 0);
		drawCube();
		
		Matrix.multiplyMM(mModelView, 0, mView, 0, mModelFloor, 0);
		Matrix.multiplyMM(mModelViewProjection, 0, transform.getPerspective(),
				0, mModelView, 0);
		drawFloor(transform.getPerspective());
		
	}

	@Override
	public void onFinishFrame(Viewport arg0) {}

	@Override
	public void onNewFrame(HeadTransform transform) {
		GLES20.glUseProgram(mGlProgram);
		mModelViewProjectionParam = GLES20.glGetUniformLocation(mGlProgram, "u_MVP");
		mLightPosParam = GLES20.glGetUniformLocation(mGlProgram, "u_LightPos");
		mModelViewParam = GLES20.glGetUniformLocation(mGlProgram, "u_MVMatrix");
		mModelParam = GLES20.glGetUniformLocation(mGlProgram, "u_Model");
		mIsFloorParam = GLES20.glGetUniformLocation(mGlProgram, "u_IsFloor");
		
		Matrix.rotateM(mModelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
		Matrix.setLookAtM(mCamera, 0, 0f, 0f, CAMERA_Z, 0f, 0f, 0f, 0f, 1f, 0f);
		transform.getHeadView(mHeadView, 0);
		checkGLError("onNewFrame");
	}

	@Override
	public void onRendererShutdown() {
		Log.i(TAG, "renderer shutdown!");
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		Log.i(TAG, "surface changed");
	}

	@Override
	public void onSurfaceCreated(EGLConfig config) {
		Log.i(TAG, "surface created!");
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // dark background
		
		// Make the cube
		ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayout.CUBE_COORDS.length * 4);
		bbVertices.order(ByteOrder.nativeOrder());
		mCubeVertices = bbVertices.asFloatBuffer();
		mCubeVertices.put(WorldLayout.CUBE_COORDS);
		mCubeVertices.position(0);
		
		ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayout.CUBE_COLORS.length * 4);
		bbColors.order(ByteOrder.nativeOrder());
		mCubeColors = bbColors.asFloatBuffer();
		mCubeColors.put(WorldLayout.CUBE_COLORS);
		mCubeColors.position(0);
		
		ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(WorldLayout.CUBE_FOUND_COLORS.length * 4);
		bbFoundColors.order(ByteOrder.nativeOrder());
		mCubeFoundColors = bbFoundColors.asFloatBuffer();
		mCubeFoundColors.put(WorldLayout.CUBE_FOUND_COLORS);
		mCubeFoundColors.position(0);
		
		ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayout.CUBE_NORMALS.length * 4);
		bbNormals.order(ByteOrder.nativeOrder());
		mCubeNormals = bbNormals.asFloatBuffer();
		mCubeNormals.put(WorldLayout.CUBE_NORMALS);
		mCubeNormals.position(0);
		
		// Floor
		ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayout.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        mFloorVertices = bbFloorVertices.asFloatBuffer();
        mFloorVertices.put(WorldLayout.FLOOR_COORDS);
        mFloorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayout.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        mFloorNormals = bbFloorNormals.asFloatBuffer();
        mFloorNormals.put(WorldLayout.FLOOR_NORMALS);
        mFloorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayout.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        mFloorColors = bbFloorColors.asFloatBuffer();
        mFloorColors.put(WorldLayout.FLOOR_COLORS);
        mFloorColors.position(0);
		
        int vertexShader = loadGlShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGlShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        
        mGlProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGlProgram, vertexShader);
        GLES20.glAttachShader(mGlProgram, gridShader);
        GLES20.glLinkProgram(mGlProgram);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        Matrix.setIdentityM(mModelCube, 0);
        Matrix.translateM(mModelCube, 0, 0, 0, -mObjectDistance);
        Matrix.setIdentityM(mModelFloor, 0);
        Matrix.translateM(mModelFloor, 0, 0, -mFloorDepth, 0);
        
        checkGLError("onSurfaceCreated");
	
	}
	
	private String readRawTextFile(int resId){
		InputStream is = getResources().openRawResource(resId);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				sb.append(line).append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		return "";
	}
	
	public void drawCube(){
		GLES20.glUniform1f(mIsFloorParam, 0f);
		GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelCube, 0);
		GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
		GLES20.glVertexAttribPointer(mPostionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
				false, 0, mCubeVertices);
		GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false,
				mModelViewProjection, 0);
		GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false,
				0, mCubeNormals);
		if(isLookingAtObject()){
			GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0,
					mCubeFoundColors);
		}
		else{
			GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0,
					mCubeColors);
		}
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
		checkGLError("Drawing cube");
	}
	
	public void drawFloor(float[] perspective){
		GLES20.glUniform1f(mIsFloorParam, 1f);
		
		GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelFloor, 0);
		GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
		GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false,
				mModelViewProjection, 0);
		GLES20.glVertexAttribPointer(mPostionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
				false, 0, mFloorVertices);
		GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0, mFloorNormals);
		GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0, mFloorColors);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		
		checkGLError("drawFloor");		
	}
	
	@Override
	public void onCardboardTrigger(){
		Log.i(TAG, "cardboard trigger!");
		if(isLookingAtObject()){
			mScore++;
			mHUD.show3dToast("Yay! Find another.\nScore: " + mScore);
			hideObject();
		}
		else {
			mHUD.show3dToast("Keep looking!");
		}
		vibrator.vibrate(50);
	}
	
	private void hideObject() {
		float[] rotation = new float[16];
		float[] posVec = new float[4];
		
		float angleXZ = (float) Math.random() * 180 + 90;
		Matrix.setRotateM(rotation, 0, angleXZ, 0f, 1f, 0f);
		float oldDist = mObjectDistance;
		mObjectDistance = (float) Math.random() * 15 + 5;
		float objScaling = mObjectDistance / oldDist;
		Matrix.scaleM(rotation, 0, objScaling, objScaling, objScaling);
		Matrix.multiplyMV(posVec, 0, rotation, 0, mModelCube, 12);
		
		float angleY = (float) Math.random() * 80 - 40;
		angleY = (float) Math.toRadians(angleY);
		float newY = (float) Math.tan(angleY) * mObjectDistance;
		
		Matrix.setIdentityM(mModelCube, 0);
		Matrix.translateM(mModelCube, 0, posVec[0], newY, posVec[2]);
	}
	
	private boolean isLookingAtObject(){
		float[] initVec = {0, 0, 0, 1f};
		float[] objPos = new float[4];
		Matrix.multiplyMM(mModelView, 0, mHeadView, 0, mModelCube, 0);
		Matrix.multiplyMV(objPos, 0, mModelView, 0, initVec, 0);
		float pitch = (float) Math.atan2(objPos[1], -objPos[2]);
		float yaw  = (float) Math.atan2(objPos[0], -objPos[2]);
		return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
	}
}
