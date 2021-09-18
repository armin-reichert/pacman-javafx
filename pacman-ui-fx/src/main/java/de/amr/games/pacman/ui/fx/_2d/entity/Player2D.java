package de.amr.games.pacman.ui.fx._2d.entity;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_Common;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the player (Pac-Man or Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class Player2D implements Renderable2D {

	private final Pac player;
	private final Rendering2D_Common rendering;

	public Map<Direction, TimedSequence<Rectangle2D>> munchingAnimations;
	public TimedSequence<Rectangle2D> dyingAnimation;

	public Player2D(Pac player, Rendering2D_Common rendering) {
		this.player = player;
		this.rendering = rendering;
		munchingAnimations = rendering.createPlayerMunchingAnimations();
		dyingAnimation = rendering.createPlayerDyingAnimation();
	}

	@Override
	public void render(GraphicsContext g) {
		if (player.isVisible()) {
			rendering.renderEntity(g, player, currentSprite());
		}
	}

	private Rectangle2D currentSprite() {
		final Direction dir = player.dir();
		if (player.dead) {
			return dyingAnimation.hasStarted() ? dyingAnimation.animate() : munchingAnimations.get(dir).frame();
		}
		if (player.speed() == 0) {
			return munchingAnimations.get(dir).frame(0);
		}
		if (player.stuck) {
			return munchingAnimations.get(dir).frame(1);
		}
		return munchingAnimations.get(dir).animate();
	}
}