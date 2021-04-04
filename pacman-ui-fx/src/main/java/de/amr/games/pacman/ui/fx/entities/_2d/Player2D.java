package de.amr.games.pacman.ui.fx.entities._2d;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the player (Pac-Man or Ms. Pac-Man).
 * <p>
 * The animations can be changed so that both, Ms. Pac-Man and Pac-Man can be rendered using this
 * class.
 * 
 * @author Armin Reichert
 */
public class Player2D extends Renderable2D {

	private final Pac player;
	private Map<Direction, TimedSequence<Rectangle2D>> munchingAnimations;
	private TimedSequence<Rectangle2D> dyingAnimation;

	public Player2D(Pac pac, GameRendering2D rendering) {
		super(rendering);
		this.player = pac;
		dyingAnimation = rendering.createPlayerDyingAnimation();
		setMunchingAnimations(rendering.createPlayerMunchingAnimations());
	}

	public TimedSequence<Rectangle2D> getDyingAnimation() {
		return dyingAnimation;
	}

	public void setMunchingAnimations(Map<Direction, TimedSequence<Rectangle2D>> munchingAnimations) {
		this.munchingAnimations = munchingAnimations;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> getMunchingAnimations() {
		return munchingAnimations;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = currentSprite();
		if (player.visible) {
			renderEntity(g, player, sprite);
		}
	}

	private Rectangle2D currentSprite() {
		if (player.dead) {
			return dyingAnimation.hasStarted() ? dyingAnimation.animate() : munchingAnimations.get(player.dir).frame();
		}
		if (player.speed == 0) {
			return munchingAnimations.get(player.dir).frame(0);
		}
		if (player.stuck) {
			return munchingAnimations.get(player.dir).frame(1);
		}
		return munchingAnimations.get(player.dir).animate();
	}
}