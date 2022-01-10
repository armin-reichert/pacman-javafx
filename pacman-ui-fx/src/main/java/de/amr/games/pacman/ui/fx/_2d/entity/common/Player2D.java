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

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the player (Pac-Man or Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class Player2D implements Renderable2D {

	private final Pac player;
	private final Rendering2D rendering;

	public Map<Direction, TimedSequence<Rectangle2D>> munchingAnimations;
	public TimedSequence<Rectangle2D> dyingAnimation;

	private Rectangle2D currentSprite;

	public Player2D(Pac player, Rendering2D rendering) {
		this.player = player;
		this.rendering = rendering;
		munchingAnimations = rendering.createPlayerMunchingAnimations();
		dyingAnimation = rendering.createPlayerDyingAnimation();
	}

	public void reset() {
		munchingAnimations.values().forEach(TimedSequence::reset);
		dyingAnimation.reset();
		currentSprite = munchingAnimations.get(player.dir()).frame();
	}

	@Override
	public void render(GraphicsContext g) {
		final Direction dir = player.dir();
		if (player.dead) {
			if (dyingAnimation.hasStarted()) {
				dyingAnimation.animate();
			}
			currentSprite = dyingAnimation.frame();
		} else {
			if (!player.velocity.equals(V2d.NULL) && !player.stuck) {
				munchingAnimations.get(dir).animate();
			}
			currentSprite = munchingAnimations.get(dir).frame();
		}
		rendering.renderEntity(g, player, currentSprite);
	}
}