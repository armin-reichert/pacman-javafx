/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import org.tinylog.Logger;

import de.amr.games.pacman.model.GameModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	public boolean debug = false;

	private Transition transition;
	private Rectangle2D[] sprites = new Rectangle2D[0];
	private int frame;
	private int frameDurationTicks;
	private boolean loop;

	public void build() {
		transition = new Transition() {
			{
				setCycleDuration(Duration.seconds(1.0 / GameModel.FPS * frameDurationTicks));
				setCycleCount(loop ? Animation.INDEFINITE : sprites.length);
				setInterpolator(Interpolator.LINEAR);
			}

			@Override
			protected void interpolate(double frac) {
				if (frac == 1.0) {
					nextFrame();
				}
			}
		};
	}

	public void reset() {
		transition.stop();
		transition.jumpTo(Duration.ZERO);
		frame = 0;
	}

	public void start() {
		transition.play();
	}

	public void stop() {
		transition.stop();
	}

	public void setSprites(Rectangle2D... sprites) {
		this.sprites = sprites;
	}

	public void setFrameDuration(int ticks) {
		frameDurationTicks = ticks;
	}

	public void repeatForever() {
		loop = true;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public Rectangle2D frame() {
		return sprites[frame];
	}

	public void nextFrame() {
		if (debug) {
			Logger.info("Begin next frame: current={}", frame);
		}
		frame++;
		if (frame == sprites.length) {
			frame = loop ? 0 : sprites.length - 1;
		}
		if (debug) {
			Logger.info("End next frame: current={}", frame);
		}
	}
}