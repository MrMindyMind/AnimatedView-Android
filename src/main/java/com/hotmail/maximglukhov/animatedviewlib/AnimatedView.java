package com.hotmail.maximglukhov.animatedviewlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * View class that supports animating.
 * @author Maxim Glukhov
 *
 */
public abstract class AnimatedView extends View {

	/**
	 * List of {@link AnimatedDraw} objects in this view.
	 */
	private List<AnimatedDraw> mAnimatedDraws;

	/**
	 * Amount of currently running animations.
	 */
	private int mRunningAnimationsCount;

	/**
	 * Last time of animation frame drawing.
	 */
	private long mLastAnimTime = -1;
	/**
	 * Time between two frames.
	 */
	private long mElapsedAnimFrameTime = 0;

	/**
	 * Amount of frames-per-second.
	 */
	private static final int DEFAULT_FPS = 120;

	/**
	 * {@link Handler} to post invalidation requests.
	 */
	private Handler mHandler = new Handler();
	/**
	 * {@link Runnable} that executes invalidation requests.
	 */
	private AnimatorRunnable mAnimator = new AnimatorRunnable(DEFAULT_FPS);

	/**
	 * Keeps track of {@link AnimatedDraw} state.
	 */
	private AnimatedDrawStateChangeListener mStateChangeListener = new AnimatedDrawStateChangeListener() {
		@Override
		public void onStateChange(AnimatedDraw animated,
								  AnimatedDrawState newState) {
			// Handle animation started state.
			// Only started state is handled in this listener to kick-start the animation.
			// The rest of the states are handled
			if (newState == AnimatedDrawState.STARTED) {
				postInvalidate();
			}
		}
	};

	/*
	 * Public constructors.
	 */

	public AnimatedView(Context context) {
		this(context, null, 0);
	}

	public AnimatedView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnimatedView(Context context, AttributeSet attrs,
						int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(21)
	public AnimatedView(Context context, AttributeSet attrs,
						int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	/*
	 * Public methods.
	 */

	/**
	 * Registers an {@link AnimatedDraw} object.
	 * This is necessary for the view to know about the existence of this object so it can operate on it later.
	 * @param animatedDraw {@link AnimatedDraw} object that needs to be animated sometime in the future.
	 */
	public void addAnimated(AnimatedDraw animatedDraw) {
		if (animatedDraw != null && !mAnimatedDraws.contains(animatedDraw)) {
			mAnimatedDraws.add(animatedDraw);
			animatedDraw.addOnStateChangeListener(mStateChangeListener);
		}
	}

	/**
	 * Removes an {@link AnimatedDraw} object.
	 * @param animatedDraw {@link AnimatedDraw} object to remove
	 */
	public void removeAnimated(AnimatedDraw animatedDraw) {
		if (animatedDraw != null && mAnimatedDraws.contains(animatedDraw)) {
			animatedDraw.removeOnStateChangeListener(mStateChangeListener);
			mAnimatedDraws.remove(animatedDraw);
		}
	}

	/**
	 *
	 * @return {@link List} of registered {@link AnimatedDraw} objects.
	 */
	public List<AnimatedDraw> getAnimatedDraws() {
		return mAnimatedDraws;
	}

	/**
	 * Gets the amount of currently running animations.
	 * @return value of amount of current running animations.
     */
	public int getRunningAnimationsCount() {
		return mRunningAnimationsCount;
	}

	/**
	 * Set amount of frames-per-second to run the animator.
	 * Consider that actual FPS might be different than specified depending on system and application performance.
	 * @param fps Amount of frames-per-second.
	 */
	public void setFPS(int fps) {
		mAnimator.setFPS(fps);
	}

	/**
	 * @return Frames-per-second as integer.
	 */
	public int getFPS() {
		return mAnimator.getFPS();
	}

	/*
	 * Protected methods
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		onDrawStatics(canvas);
		onDrawDynamics(canvas);
	}

	/**
	 * Handles all non-animating drawings.
	 * @param canvas Canvas object to draw to.
	 */
	protected abstract void onDrawStatics(Canvas canvas);

	/**
	 * Handles all animated drawings (using {@link AnimatedDraw} objects).
	 * @param canvas - Canvas object to draw to.
	 */
	protected void onDrawDynamics(Canvas canvas) {
		mRunningAnimationsCount = 0;

		for (AnimatedDraw animated : mAnimatedDraws) {
			if (isAnimating(animated)) {
				animated.onFrame(mElapsedAnimFrameTime);
				mRunningAnimationsCount++;
			}

			animated.onDraw(canvas);
		}

		/*
		 * Draw whatever needs to be drawn regardless of the state.
		 * This makes sure no matter at what state the animation is currently
		 * the user can implement the on draw according to his needs.
		 */
		drawNextFrame(mRunningAnimationsCount);
	}

	/*
	 * Private methods.
	 */

	/**
	 * Handles internal initializing.
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 * @param defStyleRes
	 */
	private void init(Context context, AttributeSet attrs, int defStyleAttr,
					  int defStyleRes) {
		mAnimatedDraws = new ArrayList<>();

		if (attrs != null) {
			TypedArray styledAttrsArr = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.AnimatedView, defStyleAttr, defStyleRes);
			try {
				int fps = styledAttrsArr.getInt(R.styleable.AnimatedView_fps, DEFAULT_FPS);
				mAnimator.setFPS(fps);
			} finally {
				styledAttrsArr.recycle();
			}
		}
	}

	/**
	 * Doesn't really draw anything, rather posts invalidate requests to draw the next frame.
	 * @param animatingCount - Number of animations that are currently running. If higher than 0
	 * invalidate request will be posted.
	 */
	private void drawNextFrame(int animatingCount) {
		if (animatingCount > 0) {
			mHandler.removeCallbacks(mAnimator);
			mHandler.postDelayed(mAnimator, mAnimator.getDelayTime());
		} else {
			resetAnimationTime();
		}
	}

	/**
	 * Checks the states of all registered {@link AnimatedDraw} objects.
	 * @param animatedDraw {@link AnimatedDraw} object to check.
	 * @return true if this {@link AnimatedDraw} requires further animating, false otherwise.
	 */
	private boolean isAnimating(AnimatedDraw animatedDraw) {
		if (animatedDraw == null)
			return false;

		AnimatedDrawState state = animatedDraw.getState();
		if (state == AnimatedDrawState.RUNNING
				|| state == AnimatedDrawState.STARTED) {
			return true;
		}

		return false;
	}

	private void resetAnimationTime() {
		mLastAnimTime = -1;
		mElapsedAnimFrameTime = 0;
	}

	private class AnimatorRunnable implements Runnable {

		private int mDelayTime;

		public AnimatorRunnable(int fps) {
			setFPS(fps);
		}

		@Override
		public void run() {
			long now = AnimationUtils.currentAnimationTimeMillis();

			// Calibrate animation start time reference point.
			if (mLastAnimTime == -1) {
				mElapsedAnimFrameTime = 0;
			} else {
				mElapsedAnimFrameTime = now - mLastAnimTime;
			}
			mLastAnimTime = now;

			invalidate();
		}

		public void setFPS(int fps) {
			mDelayTime = calculateDelayTime(fps);
		}

		public int getFPS() {
			return (int) Math.floor(1000.0 / mDelayTime);
		}

		public int getDelayTime() {
			return mDelayTime;
		}

		private int calculateDelayTime(int fps) {
			return (int) Math.floor(1000.0 / fps);
		}
	}

}
