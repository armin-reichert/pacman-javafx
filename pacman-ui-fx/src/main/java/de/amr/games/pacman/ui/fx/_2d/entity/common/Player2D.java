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

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the player (Pac-Man or Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class Player2D extends GameEntity2D {

	public final Pac player;
	public Map<Direction, SpriteAnimation> animMunching;
	private SpriteAnimation animDying;

	public Player2D(Pac player, GameModel game) {
		super(game);
		this.player = player;
	}

	public Player2D createAnimations(Rendering2D r2D) {
		animMunching = r2D.createPlayerMunchingAnimations();
		animDying = r2D.createPlayerDyingAnimation();
		return this;
	}

	public void resetAnimations() {
		if (animMunching != null) {
			for (Direction dir : Direction.values()) {
				animMunching.get(dir).reset();
			}
		}
		if (animDying != null) {
			animDying.reset();
		}
	}

	public void startDyingAnimation(boolean sound) {
		animDying.restart();
		if (sound) {
			SoundManager.get().play(GameSound.PACMAN_DEATH);
		}
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		Rectangle2D sprite = null;
		if (player.killed) {
			sprite = animDying.animate();
		} else {
			var munching = animMunching.get(player.moveDir());
			sprite = munching.frame();
			if (!player.velocity.equals(V2d.NULL) && !player.stuck) {
				munching.advance();
			}
		}
		r2D.drawEntity(g, player, sprite);
	}
}