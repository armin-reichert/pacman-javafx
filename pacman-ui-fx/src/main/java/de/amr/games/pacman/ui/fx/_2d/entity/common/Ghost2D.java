/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.entity.common;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimationKey;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D {

	public static final long FLASHING_TIME = TickTimer.sec_to_ticks(2); // TODO not sure

	public final Ghost ghost;
	public final GhostAnimations animations;

	public Ghost2D(Ghost ghost, Rendering2D r2D) {
		this.ghost = ghost;
		this.animations = new GhostAnimations(ghost.id, r2D);
		animations.select(GhostAnimationKey.ANIM_COLOR);
	}

	public void updateAnimations(boolean startFlashing, boolean stopFlashing, int numFlashes) {
		// this is to keep feet still when locked
		if (ghost.velocity.length() == 0) {
			animations.color.stop();
		} else {
			animations.color.run();
		}
		switch (ghost.state) {
		case DEAD -> {
			animations.select(ghost.killIndex == -1 ? GhostAnimationKey.ANIM_EYES : GhostAnimationKey.ANIM_VALUE);
		}
		case FRIGHTENED, LOCKED -> {
			if (startFlashing) {
				startFlashing(numFlashes);
			} else if (stopFlashing) {
				ensureFlashingStopped();
			}
		}
		case LEAVING_HOUSE -> {
			animations.select(GhostAnimationKey.ANIM_COLOR);
		}
		default -> {
		}
		}
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		r2D.drawEntity(g, ghost, animations.currentSprite(ghost));
	}

	private void startFlashing(int numFlashes) {
		long frameDuration = FLASHING_TIME / (numFlashes * animations.flashing.numFrames());
		animations.flashing.frameDuration(frameDuration);
		animations.flashing.repeat(numFlashes);
		animations.flashing.restart();
		animations.select(GhostAnimationKey.ANIM_FLASHING);
	}

	private void ensureFlashingStopped() {
		if (animations.selectedKey() == GhostAnimationKey.ANIM_FLASHING) {
			animations.select(GhostAnimationKey.ANIM_COLOR);
		}
	}
}