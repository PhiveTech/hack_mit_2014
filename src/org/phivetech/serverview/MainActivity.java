package org.phivetech.serverview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import static android.opengl.GLES20.*;

/**
 * The main activity of the game.
 * @author zrneely, gebhard
 *
 */
public class MainActivity extends CardboardActivity implements
			CardboardView.StereoRenderer {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	public static final float CAMERA_Z = 0.01f;
	public static final float TIME_DELTA = 0.13f;
	public static final float YAW_LIMIT = 0.12f;
	public static final float PITCH_LIMIT = 0.12f;
	public static final int COORDS_PER_VERTEX = 3;
	
	// TODO light object
	private final float[] lightInWorld = new float[]{0.0f, 2.0f, 0.0f, 1.0f};
	private final float[] lightInEyes = new float[4];
	
	private List<Cube> cubes;
	private Floor floor;
	
	// The OpenGL program
	private int programPointer;
	// Pointers!
	private int positionPointer;
	private int normalPointer;
	private int colorPointer;
	private int lightPosPointer;
	private int viewPointer;
	private int modelPointer;
	private int viewProjectionPointer;
	// TODO camera object
	private float[] mCamera;
	private float[] mView;
	private float[] headView;
	
	private int score = 0;
	private float objectDistance = 12f;
	private float floorDepth = 20f;
	
	private Vibrator vibrator;
	
	private ServerviewOverlayView hud;
	
	private int loadGlShader(int type, int resId){
		String code = readRawTextFile(resId);
		int shader = glCreateShader(type);
		glShaderSource(shader, code);
		glCompileShader(shader);
		
		final int[] compileStatus = new int[1];
		glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0);
		
		if(compileStatus[0] == 0){
			Log.e(TAG, "Error compiling shader: " + glGetShaderInfoLog(shader));
			glDeleteShader(shader);
			shader = 0;
		}
		if(shader == 0){
			throw new RuntimeException("Error compiling shader.");
		}
		return shader;
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

	/**
	 * Checks the program state; if an error occurred, throw a runtime exception.
	 * @param func The name of the function in which the error might have occurred.
	 */
	public static void checkGLError(String func){
		int error;
		while((error = glGetError()) != GL_NO_ERROR){
			Log.e(TAG, func + ": glError " + error);
			throw new RuntimeException(func + ": glError " + error);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		cubes = new ArrayList<Cube>();
		
		// Load the cardboard view
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
		
		// Set up the camera TODO move to Camera class
		mCamera = new float[16];
	    mView = new float[16];
	    headView = new float[16];

	    // Load the default vibrator (hue hue hue)
	    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    
	    // Find the Heads-Up Display
	    hud = (ServerviewOverlayView) findViewById(R.id.overlay);
	    
	    // Display the start message TODO more arguments (time, color, etc)
	    hud.show3dToast("Do a thing when you see the thing");
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
	public void onSurfaceCreated(EGLConfig config) {
		Log.i(TAG, "surface created!");
		glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // dark background
		
		// Let's add two cubes!
		cubes.add(new Cube(new int[]{Color.RED, Color.GREEN,
									 Color.BLUE, Color.CYAN,
									 Color.MAGENTA, Color.WHITE},
						   new int[]{Color.YELLOW, Color.YELLOW,
									 Color.YELLOW, Color.YELLOW,
									 Color.YELLOW, Color.YELLOW}, objectDistance));
		cubes.add(new Cube(new int[]{Color.GRAY, Color.GREEN,
									 Color.BLUE, Color.GREEN,
									 Color.DKGRAY, Color.GREEN},
						   new int[]{Color.YELLOW, Color.YELLOW,
									 Color.YELLOW, Color.YELLOW,
									 Color.YELLOW, Color.YELLOW}, objectDistance));
		// in random locations
		for(Cube cube : cubes){
			cube.randomizeLocation();
		}
		
		// Create the floor
		floor = new Floor(Color.BLUE, floorDepth);
		
		// Load the shaders
	    int vertexShader = loadGlShader(GL_VERTEX_SHADER, R.raw.light_vertex);
	    int gridShader = loadGlShader(GL_FRAGMENT_SHADER, R.raw.grid_fragment);
	    
	    // Attach them to the program
	    programPointer = glCreateProgram();
	    glAttachShader(programPointer, vertexShader);
	    glAttachShader(programPointer, gridShader);
	    glLinkProgram(programPointer);
	    glEnable(GL_DEPTH_TEST);
	    
	    checkGLError("onSurfaceCreated");
	
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		Log.i(TAG, "surface changed");
	}

	@Override
	public void onNewFrame(HeadTransform transform) {
		// Use the correct shaders on this frame
		glUseProgram(programPointer);
		
		// Find the pointers
		viewProjectionPointer = glGetUniformLocation(programPointer, "u_MVP");
		lightPosPointer = glGetUniformLocation(programPointer, "u_LightPos");
		viewPointer = glGetUniformLocation(programPointer, "u_MVMatrix");
		modelPointer = glGetUniformLocation(programPointer, "u_Model");
		
		// Slowly rotate each of the cubes
		for(Cube cube : cubes){
			cube.rotate(TIME_DELTA, 0.5f, 0.5f, 1.0f);
		}
		
		// TODO camera object
		Matrix.setLookAtM(mCamera, 0, 0f, 0f, CAMERA_Z, 0f, 0f, 0f, 0f, 1f, 0f);
		
		// Save part of the head transformation
		transform.getHeadView(headView, 0);
		
		checkGLError("onNewFrame");
	}

	@Override
	public void onDrawEye(EyeTransform transform) {
		// Clear color and depth buffers
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		positionPointer = glGetAttribLocation(programPointer, "a_Position");
		normalPointer = glGetAttribLocation(programPointer, "a_Normal");
		colorPointer = glGetAttribLocation(programPointer, "a_Color");
		
		// Enable the vertex attributes
		glEnableVertexAttribArray(positionPointer);
		glEnableVertexAttribArray(normalPointer);
		glEnableVertexAttribArray(colorPointer);
		checkGLError("onDrawEyePointer");
		
		// Calculate the view
		Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);
		
		// Place the light
		Matrix.multiplyMV(lightInEyes, 0, mView, 0, lightInWorld, 0);
		glUniform3f(lightPosPointer, lightInEyes[0], lightInEyes[1],
					lightInEyes[2]);
		
		// Draw each of the cubes
		for(Cube cube : cubes) {
			cube.draw(mView, transform.getPerspective(), programPointer, 
					modelPointer, viewPointer, viewProjectionPointer);
		}
		
		// Draw the floor
		floor.draw(mView, transform.getPerspective(), programPointer,
					modelPointer, viewPointer, viewProjectionPointer);
		
	}

	@Override
	public void onFinishFrame(Viewport arg0) {}

	@Override
	public void onRendererShutdown() {
		Log.i(TAG, "renderer shutdown!");
	}

	@Override
	public void onCardboardTrigger(){
		Log.i(TAG, "cardboard trigger!");

		for(Cube cube : cubes){
			if(cube.isLookingAt(headView, PITCH_LIMIT, YAW_LIMIT)){
				hud.show3dToast("Yay! Find another one!\nScore: " + score);
	            score++;
				cube.randomizeLocation();
				break;
			}
		}
		
		vibrator.vibrate(50);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean result;
		switch(event.getKeyCode()){
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			result = true;
			break;
		default:
			result = super.dispatchKeyEvent(event);
			break;
		}
		return result;
	}
	
}
