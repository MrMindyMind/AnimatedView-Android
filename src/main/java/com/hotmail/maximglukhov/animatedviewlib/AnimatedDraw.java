package com.hotmail.maximglukhov.animatedviewlib;

import android.animation.TimeInterpolator;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Similar to Android's Animation class, however this is only usable with {@link AnimatedView}.
 * Together it is possible to easily and conveniently create animated views and control them.
 * @author Maxim Glukhov
 *
 */
public abstract class AnimatedDraw {

	/**
	 * Current state.
	 */
	private AnimatedDrawState mState;

	/**
	 * Animation duration in milliseconds.
	 */
	private int mDuration;
	/**
	 * Time fraction since animation has begun.
	 */
	private int mInterpolatedTime;
	/**
	 * Used for animation value interpolation.
	 */
	private TimeInterpolator mInterpolator;

	/**
	 * List of {@link AnimatedDrawStateChangeListener} listeners.
	 */
	private List<AnimatedDrawStateChangeListener> mListeners;
	
	/*
	 * Public Constructor.
	 */
	
	/**
	 * @param interpolator Interpolator to define how this animation should behave.
	 * @param durationMillis Animation duration in milliseconds.
	 */
	public AnimatedDraw(TimeInterpolator interpolator, int durationMillis) {
		mInterpolator 		= interpolator;
		mDuration 			= durationMillis;
		mInterpolatedTime 	= 0;
		mListeners 			= new ArrayList<>();
		mState				= AnimatedDrawState.NONE;
	}
	
	/*
	 * Public Methods
	 */
	
	/**
	 * Calls every invalidation time which is essentially the animation frame.
	 * @param elapsedAnimFrameTime - Amount of time passed since last frame in milliseconds.
	 */
	public void onFrame(long elapsedAnimFrameTime) {
		mInterpolatedTime += elapsedAnimFrameTime;
		float animTimeFraction = (mInterpolatedTime / (float) mDuration);
		
		if (elapsedAnimFrameTime == 0) {
			setState(AnimatedDrawState.RUNNING);
		}
		
		if (animTimeFraction > 1.0f) {
			animTimeFraction = 1.0f;
		}
		
		 onFrame(animTimeFraction);
		 
		 if (animTimeFraction == 1.0f) {
			 setState(AnimatedDrawState.ENDED);
		 }
	}

	/**
	 * @see #onFrame(long)
	 * @param interpolatedTime A value between 0.0f and 1.0f representing the animation time progression.
	 * 0.0f is relatively the start point and 1.0f is the end point.
	 */
	public abstract void onFrame(float interpolatedTime);
	
	/**
	 * Starts the animation.
	 */
	public void startAnimation() {
		switch (getState()) {
		case PAUSED:
			// Resume from where it was.
			// We set the state to Started to trigger the invalidation posting.
			setState(AnimatedDrawState.STARTED);
			break;
		case RUNNING:
			// Do nothing.
			break;
		case NONE:
		case STOPPED:
		case ENDED:
		default:
			mInterpolatedTime = 0;
			setState(AnimatedDrawState.STARTED);
			onFrame(0);
			break;
		
		}
	}
	
	/**
	 * Resets the animation.
	 */
	public void resetAnimation() {
		mInterpolatedTime = 0;
		setState(AnimatedDrawState.NONE);
		// It is very important to make sure the value here is a float
		// so the interpreter knows to call the right onFrame method.
		onFrame(0.0f);
	}
	
	/**
	 * Pauses the animation.
	 */
	public void pauseAnimation() {
		if (getState() == AnimatedDrawState.RUNNING) {
			setState(AnimatedDrawState.PAUSED);
		}
	}
	
	/**
	 * Stops the animation.
	 */
	public void stopAnimation() {
		if (getState() != AnimatedDrawState.STOPPED) {
			setState(AnimatedDrawState.STOPPED);
			mInterpolatedTime = 0;
		}
	}
	
	/**
	 * <u><b>Defines</u></b>how this AnimatedDraw should draw itself.
	 * Doesn't actually draw on its own, this method rather waits for {@link AnimatedView} to call it.
	 * @param canvas Canvas object to draw to
	 */
	public abstract void onDraw(Canvas canvas);
	
	/*
	 * Getters & Setters
	 */
	
	/**
	 * @return an {@link AnimatedDrawState} enumerator showing the current state of the animation.
	 */
	public AnimatedDrawState getState() {
		return mState;
	}
	
	/**
	 * Sets the duration of the animation,
	 * <b><u>Note:</b></u> Doesn't do anything if the animation is currently running.
	 * @param duration - Duration time in milliseconds.
	 */
	public void setDuration(int duration) {
		if (mState == AnimatedDrawState.RUNNING || mState == AnimatedDrawState.STARTED)
			return;
		
		mDuration = duration;
	}
	
	/**
	 * @return Animation duration in milliseconds.
	 */
	public int getDuration() {
		return mDuration;
	}
	
	/**
	 * @return The interpolated value. This value can be more than 1.0 for interpolators which overshoot their targets, or less than
	 * 0 for interpolators that undershoot their targets.
	 */
	public float getAnimatedValue() {
		float fractionTime = mInterpolatedTime / (float)mDuration;
		if (fractionTime > 1.0f) {
			fractionTime = 1.0f;
		}
		return mInterpolator.getInterpolation(fractionTime);
	}
	
	/**
	 * Registers a listener to the animation state changes.
	 * Supports more than one listener.
	 * @param listener Listener object to register.
	 * @throws NullPointerException when listener is null.
	 */
	public void addOnStateChangeListener(AnimatedDrawStateChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		
		if (!mListeners.contains(listener))  {
			mListeners.add(listener);
		}
	}
	
	/**
	 * Remove a state change listener.
	 * @param listener - Listener object to remove.
	 * @throws NullPointerException when listener is null.
	 */
	public void removeOnStateChangeListener(AnimatedDrawStateChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		
		if (mListeners.contains(listener))  {
			mListeners.remove(listener);
		}
	}

	/*
	 * Private methods
	 */

	/**
	 * Sets the current state of the animation.
	 * @param state - new state of the animation
	 */
	private void setState(AnimatedDrawState state) {
		mState = state;

		for (AnimatedDrawStateChangeListener listener : mListeners) {
			listener.onStateChange(this, state);
		}
	}
}
