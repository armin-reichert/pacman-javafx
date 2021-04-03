package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Ghost2D extends Renderable2D {

	private final Ghost ghost;
	private Map<Direction, TimedSequence<Rectangle2D>> kickingAnimations = new EnumMap<>(Direction.class);
	private TimedSequence<Rectangle2D> flashingAnimation;
	private TimedSequence<Rectangle2D> frightenedAnimation;
	private Map<Direction, TimedSequence<Rectangle2D>> returningHomeAnimations = new EnumMap<>(Direction.class);
	private Map<Integer, Rectangle2D> numberSprites;
	private boolean looksFrightened;

	public Ghost2D(Ghost ghost, GameRendering2D rendering) {
		super(rendering);
		this.ghost = ghost;
		setFlashingAnimation(rendering.createGhostFlashingAnimation());
		setFrightenedAnimation(rendering.createGhostFrightenedAnimation());
		setKickingAnimations(rendering.createGhostKickingAnimations(ghost.id));
		setReturningHomeAnimations(rendering.createGhostReturningHomeAnimations());
		setNumberSpriteMap(rendering.getBountyNumberSpritesMap());
	}

	public void setLooksFrightened(boolean looksFrightened) {
		this.looksFrightened = looksFrightened;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> getKickingAnimations() {
		return kickingAnimations;
	}

	public void setKickingAnimations(Map<Direction, TimedSequence<Rectangle2D>> kickingAnimations) {
		this.kickingAnimations = kickingAnimations;
	}

	public TimedSequence<Rectangle2D> getFlashingAnimation() {
		return flashingAnimation;
	}

	public void setFlashingAnimation(TimedSequence<Rectangle2D> flashingAnimation) {
		this.flashingAnimation = flashingAnimation;
	}

	public TimedSequence<Rectangle2D> getFrightenedAnimation() {
		return frightenedAnimation;
	}

	public void setFrightenedAnimation(TimedSequence<Rectangle2D> frightenedAnimation) {
		this.frightenedAnimation = frightenedAnimation;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> getReturningHomeAnimations() {
		return returningHomeAnimations;
	}

	public void setReturningHomeAnimations(Map<Direction, TimedSequence<Rectangle2D>> returningHomeAnimations) {
		this.returningHomeAnimations = returningHomeAnimations;
	}

	public Map<Integer, Rectangle2D> getNumberSprites() {
		return numberSprites;
	}

	public void setNumberSpriteMap(Map<Integer, Rectangle2D> numberSprites) {
		this.numberSprites = numberSprites;
	}

	@Override
	public void render(GraphicsContext g) {
		renderEntity(g, ghost, currentSprite());
	}

	private Rectangle2D currentSprite() {
		if (ghost.bounty > 0) {
			return numberSprites.get(ghost.bounty);
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