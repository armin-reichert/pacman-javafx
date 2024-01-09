/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	public static class Builder {

		private final SpriteAnimation animation = new SpriteAnimation();

		public Builder frameTicks(int ticks) {
			animation.frameTicks = ticks;
			return this;
		}

		public Builder fps(int fps) {
			animation.fps = fps;
			return this;
		}

		public Builder loop() {
			animation.loop = true;
			return this;
		}

		public Builder sprites(Rectangle2D... sprites) {
			animation.sprites = sprites;
			return this;
		}

		public SpriteAnimation end() {
			animation.transition = createTransition(animation);
			return animation;
		}
	}

	public static Builder begin() {
		return new Builder();
	}

	private static Transition createTransition(SpriteAnimation sa) {
		return new Transition() {
			{
				setCycleDuration(Duration.seconds(1.0 / sa.fps * sa.frameTicks));
				setCycleCount(sa.loop ? Animation.INDEFINITE : sa.sprites.length);
				setInterpolator(Interpolator.LINEAR);
			}

			@Override
			protected void interpolate(double frac) {
				if (frac == 1.0) {
					sa.nextFrame();
				}
			}
		};
	}

	private Rectangle2D[] sprites = new Rectangle2D[0];
	private boolean loop;
	private int frameTicks = 1;
	private int fps = 60;
	private Transition transition;
	private int frameIndex;

	public void setSprites(Rectangle2D[] sprites) {
		this.sprites = sprites;
		// TODO what about frame index?
	}

	public Rectangle2D[] getSprites() {
		return sprites;
	}

	public void reset() {
		transition.stop();
		transition.jumpTo(Duration.ZERO);
		frameIndex = 0;
	}

	public void setFrameTicks(int ticks) {
		if (ticks != frameTicks) {
			boolean wasRunning = transition.getStatus() == Status.RUNNING;
			transition.stop();
			frameTicks = ticks;
			transition = createTransition(this);
			if (wasRunning) {
				start();
			}
		}
	}

	public void start() {
		transition.play();
	}

	public void stop() {
		transition.stop();
	}

	public boolean isRunning() {
		return transition.getStatus() == Status.RUNNING;
	}

	public void setDelay(Duration delay) {
		transition.setDelay(delay);
	}

	public void setFrameIndex(int frame) {
		this.frameIndex = frame;
	}

	public int frameIndex() {
		return frameIndex;
	}

	public Rectangle2D currentSprite() {
		return sprites[frameIndex];
	}

	public void nextFrame() {
		frameIndex++;
		if (frameIndex == sprites.length) {
			frameIndex = transition.getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
		}
	}
}