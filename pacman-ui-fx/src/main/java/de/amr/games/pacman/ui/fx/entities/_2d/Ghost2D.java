package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D<RENDERING extends Rendering2D> implements Renderable2D<RENDERING> {

	private final RENDERING rendering;
	private final Ghost ghost;
	private Map<Direction, TimedSequence<Rectangle2D>> kickingAnimations;
	private Map<Direction, TimedSequence<Rectangle2D>> returningHomeAnimations;
	private TimedSequence<Rectangle2D> flashingAnimation;
	private TimedSequence<Rectangle2D> frightenedAnimation;
	private Map<Integer, Rectangle2D> numberSpritesMap;
	private boolean looksFrightened;

	public Ghost2D(Ghost ghost, RENDERING rendering) {
		this.rendering = rendering;
		this.ghost = ghost;
		flashingAnimation = rendering.createGhostFlashingAnimation();
		frightenedAnimation = rendering.createGhostFrightenedAnimation();
		kickingAnimations = rendering.createGhostKickingAnimations(ghost.id);
		returningHomeAnimations = rendering.createGhostReturningHomeAnimations();
		numberSpritesMap = rendering.getBountyNumberSpritesMap();
	}

	public void setLooksFrightened(boolean looksFrightened) {
		this.looksFrightened = looksFrightened;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> getKickingAnimations() {
		return kickingAnimations;
	}

	public TimedSequence<Rectangle2D> getFlashingAnimation() {
		return flashingAnimation;
	}

	public TimedSequence<Rectangle2D> getFrightenedAnimation() {
		return frightenedAnimation;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> getReturningHomeAnimations() {
		return returningHomeAnimations;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, ghost, currentSprite());
	}

	private Rectangle2D currentSprite() {
		if (ghost.bounty > 0) {
			return numberSpritesMap.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return returningHomeAnimations.get(ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return flashingAnimation.isRunning() ? flashingAnimation.animate() : frightenedAnimation.animate();
		}
		if (ghost.is(LOCKED) && looksFrightened) {
			return frightenedAnimation.animate();
		}
		if (ghost.speed == 0) {
			return kickingAnimations.get(ghost.wishDir).frame();
		}
		return kickingAnimations.get(ghost.wishDir).animate(); // Looks towards wish dir!
	}
}