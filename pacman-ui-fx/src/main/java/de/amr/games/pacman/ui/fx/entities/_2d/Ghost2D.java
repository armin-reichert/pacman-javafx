package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Common;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D implements Renderable2D {

	private final Ghost ghost;
	private final Rendering2D_Common rendering;
	public Map<Direction, TimedSequence<Rectangle2D>> kickingAnimations;
	public Map<Direction, TimedSequence<Rectangle2D>> returningHomeAnimations;
	public TimedSequence<Rectangle2D> flashingAnimation;
	public TimedSequence<Rectangle2D> frightenedAnimation;
	private boolean looksFrightened;

	public Ghost2D(Ghost ghost, Rendering2D_Common rendering) {
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
		if (ghost.isVisible()) {
			rendering.renderEntity(g, ghost, currentSprite());
		}
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
		if (ghost.speed() == 0) {
			return kickingAnimations.get(ghost.wishDir()).frame();
		}
		return kickingAnimations.get(ghost.wishDir()).animate(); // Looks towards wish dir!
	}
}