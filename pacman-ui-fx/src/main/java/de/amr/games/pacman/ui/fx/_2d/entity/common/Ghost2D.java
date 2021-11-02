/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D implements Renderable2D {

	private final Ghost ghost;
	private final Rendering2D rendering;
	public Map<Direction, TimedSequence<Rectangle2D>> kickingAnimations;
	public Map<Direction, TimedSequence<Rectangle2D>> returningHomeAnimations;
	public TimedSequence<Rectangle2D> flashingAnimation;
	public TimedSequence<Rectangle2D> frightenedAnimation;
	private boolean looksFrightened;

	public Ghost2D(Ghost ghost, Rendering2D rendering) {
		this.ghost = ghost;
		this.rendering = rendering;
		flashingAnimation = rendering.createGhostFlashingAnimation();
		frightenedAnimation = rendering.createGhostFrightenedAnimation();
		kickingAnimations = rendering.createGhostKickingAnimations(ghost.id);
		returningHomeAnimations = rendering.createGhostReturningHomeAnimations();
	}

	public void setLooksFrightened(boolean looksFrightened) {
		this.looksFrightened = looksFrightened;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, ghost, currentSprite());
	}

	private Rectangle2D currentSprite() {
		if (ghost.bounty > 0) {
			return rendering.getBountyNumberSprites().get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return returningHomeAnimations.get(ghost.dir()).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return flashingAnimation.isRunning() ? flashingAnimation.animate() : frightenedAnimation.animate();
		}
		if (ghost.is(LOCKED) && looksFrightened) {
			return frightenedAnimation.animate();
		}
		if (ghost.velocity().equals(V2d.NULL)) {
			return kickingAnimations.get(ghost.wishDir()).frame();
		}
		return kickingAnimations.get(ghost.wishDir()).animate(); // Looks towards wish dir!
	}
}