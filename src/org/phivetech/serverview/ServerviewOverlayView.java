package org.phivetech.serverview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A HUD for the Serverview.
 * @author zrneely
 *
 */
public class ServerviewOverlayView extends LinearLayout {
	
	@SuppressWarnings("unused")
	private static final String TAG = ServerviewOverlayView.class.getSimpleName();
	
	private final ServerviewOverlayEyeView mLeftView;
	private final ServerviewOverlayEyeView mRightView;
	private AlphaAnimation mTextFadeAnimation;

	public ServerviewOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(HORIZONTAL);
		
		// Fullscreen
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
		params.setMargins(0, 0, 0, 0);
		
		mLeftView = new ServerviewOverlayEyeView(context, attrs);
		mLeftView.setLayoutParams(params);
		addView(mLeftView);
		
		mRightView = new ServerviewOverlayEyeView(context, attrs);
		mRightView.setLayoutParams(params);
		addView(mRightView);
		
		setDepthOffset(0.016f);
		setColor(Color.rgb(150, 255, 180));
		setVisibility(View.VISIBLE);
		
		mTextFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
		mTextFadeAnimation.setDuration(5000);
		
	}
	
	/**
	 * Displays a 3d toast for 5 seconds.
	 * @param message
	 */
	public void show3dToast(String message){
		setText(message);
		setTextAlpha(1f);
		mTextFadeAnimation.setAnimationListener(new EndAnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				setTextAlpha(0f);
			}
		});
		startAnimation(mTextFadeAnimation);
	}
	
	private void setDepthOffset(float offset){
		mLeftView.setOffset(offset);
		mRightView.setOffset(-offset);
	}
	
	private void setText(String text){
		mLeftView.setText(text);
		mRightView.setText(text);
	}
	
	private void setTextAlpha(float alpha){
		mLeftView.setTextViewAlpha(alpha);
		mRightView.setTextViewAlpha(alpha);
	}
	
	private void setColor(int color){
		mLeftView.setColor(color);
		mRightView.setColor(color);
	}
	
	private abstract class EndAnimationListener implements Animation.AnimationListener {
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationStart(Animation animation) {}
	}
	
	
	private class ServerviewOverlayEyeView extends ViewGroup {
		
		private final ImageView image;
		private final TextView text;
		private float offset;
		
		public ServerviewOverlayEyeView(Context context, AttributeSet attrs) {
			super(context, attrs);
			image = new ImageView(context, attrs);
			image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			image.setAdjustViewBounds(true);
			
			text = new TextView(context, attrs);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
			text.setTypeface(text.getTypeface(), Typeface.BOLD);
			text.setGravity(Gravity.CENTER);
			text.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
			addView(text);
		}
		
		public void setColor(int color){
			image.setColorFilter(color);
			text.setTextColor(color);
		}
		
		public void setText(String text){
			this.text.setText(text);
		}
		
		public void setTextViewAlpha(float alpha){
			text.setAlpha(alpha);
		}
		
		public void setOffset(float offset){
			this.offset = offset;
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			final int width = r - l;
			final int height = b - t;
			
			final float imageSize = 0.12f;
			final float vertImgOffset = -0.07f;
			final float vertTextPos = 0.52f;
			
			float imgMargin = (1.0f - imageSize) / 2.0f;
			int leftMargin = (int) (width * (imgMargin + offset));
			int topMargin = (int) (height * (imgMargin + vertImgOffset));
			image.layout(leftMargin, topMargin, 
					(int) (leftMargin + width * imageSize),
					(int) (topMargin + height * imageSize));
			leftMargin = (int) (offset * width);
			topMargin = (int) (height * vertTextPos);
			text.layout(leftMargin, topMargin, 
					(int) (leftMargin + width),
					(int) (topMargin + height * (1.0f - vertTextPos)));
		}
		
	}
	
}
