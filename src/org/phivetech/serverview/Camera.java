package org.phivetech.serverview;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;

import android.opengl.Matrix;

/**
 * A camera which accounts for both Eye-Independent and Eye-Dependent vr corrections.
 * @author zrneely
 *
 */
public class Camera {
	
	private float[] camera;
	private float[] view;
	private float[] correctedView;

	public Camera(){
		camera = new float[16];
		view = new float[16];
		correctedView = new float[16];
	}
	
	/**
	 * Points the camera.
	 * @param eX Eye X
	 * @param eY Eye Y
	 * @param eZ Eye Z
	 * @param cX Center X
	 * @param cY Center Y
	 * @param cZ Center Z
	 * @param uX Up X
	 * @param uY Up Y
	 * @param uZ Up Z
	 */
	public void pointAt(float eX, float eY, float eZ, float cX, float cY, float cZ,
			float uX, float uY, float uZ){
		Matrix.setLookAtM(camera, 0, eX, eY, eZ, cX, cY, cZ, uX, uY, uZ);
	}
	
	/**
	 * Set the correction independent of eye.
	 * @param transform The transform.
	 */
	public void setHeadCorrection(HeadTransform transform){
		transform.getHeadView(correctedView, 0);
	}
	
	/**
	 * Get the view, corrected independently of eye (what the user sees).
	 * @return The head-corrected but eye-independent view.
	 */
	public float[] getHeadCorrectedView(){
		return correctedView;
	}
	
	/**
	 * Gets the view, corrected for an eye
	 * @param transform The eye transform. 
	 * @return the corrected view
	 */
	public float[] getView(EyeTransform transform){
		float[] tempView = new float[16];
		Matrix.multiplyMM(tempView, 0, transform.getEyeView(), 0, camera, 0);
		return tempView;
	}
	
	/**
	 * Gets the view, uncorrected.
	 * @return The uncorrected view.
	 */
	public float[] getView(){
		return view;
	}

}
