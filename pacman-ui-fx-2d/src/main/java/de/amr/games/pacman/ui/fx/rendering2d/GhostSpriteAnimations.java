/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostSpriteAnimations implements GhostAnimations<SpriteAnimation> {

	private final Ghost ghost;
	private GameSpritesheet gss;
	private Map<Direction, SpriteAnimation> normalAnimationByDir;
	private SpriteAnimation frightenedAnimation;
	private SpriteAnimation flashingAnimation;
	private Map<Direction, SpriteAnimation> eyesAnimationByDir;
	private SpriteAnimation numberAnimation;

	private SpriteAnimation damagedAnimation;
	private SpriteAnimation stretchedAnimation;
	private SpriteAnimation patchedAnimation;
	private SpriteAnimation nakedAnimation;

	private String currentAnimationName;
	private SpriteAnimation currentAnimation;

	public GhostSpriteAnimations(Ghost ghost, GameSpritesheet gss) {
		this.ghost = ghost;
		this.gss = gss;
		setSpritesheet(gss);
	}

	public void setSpritesheet(GameSpritesheet gss) {
		this.gss = gss;
		createNormalAnimation();
		createFrightenedAnimation();
		createFlashingAnimation();
		createEyesAnimation();
		createNumberAnimation();

		if (gss instanceof PacManGameSpritesheet) {
			createBlinkyDamagedAnimation((PacManGameSpritesheet) gss);
			createBlinkyStretchedAnimation((PacManGameSpritesheet) gss);
			createBlinkyPatchedAnimation((PacManGameSpritesheet) gss);
			createBlinkyNakedAnimation((PacManGameSpritesheet) gss);
		}

		// TODO check this
		for (var dir : Direction.values()) {
			normalAnimationByDir.get(dir).start();
			eyesAnimationByDir.get(dir).start();
		}
		frightenedAnimation.start();
		flashingAnimation.start();
	}

	private void createNormalAnimation() {
		normalAnimationByDir = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation();
			animation.setSprites(gss.ghostNormalSprites(ghost.id(), dir));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animation.build();
			normalAnimationByDir.put(dir, animation);
		}
	}

	private void createFrightenedAnimation() {
		frightenedAnimation = new SpriteAnimation();
		frightenedAnimation.setSprites(gss.ghostFrightenedSprites());
		frightenedAnimation.setFrameDuration(8);
		frightenedAnimation.repeatForever();
		frightenedAnimation.build();
	}

	private void createFlashingAnimation() {
		flashingAnimation = new SpriteAnimation();
		flashingAnimation.setSprites(gss.ghostFlashingSprites());
		flashingAnimation.setFrameDuration(6);
		flashingAnimation.repeatForever();
		flashingAnimation.build();
	}

	private void createEyesAnimation() {
		eyesAnimationByDir = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation();
			animation.setSprites(gss.ghostEyesSprites(dir));
			animation.build();
			eyesAnimationByDir.put(dir, animation);
		}
	}

	private void createNumberAnimation() {
		numberAnimation = new SpriteAnimation();
		numberAnimation.setSprites(gss.ghostNumberSprites());
		numberAnimation.setFrameDuration(Integer.MAX_VALUE);
		numberAnimation.build();
	}

	// Pac-Man only

	private void createBlinkyDamagedAnimation(PacManGameSpritesheet ss) {
		damagedAnimation = new SpriteAnimation();
		damagedAnimation.setSprites(ss.blinkyDamagedSprites());
		damagedAnimation.setFrameDuration(60);
		damagedAnimation.build();
	}

	private void createBlinkyStretchedAnimation(PacManGameSpritesheet ss) {
		stretchedAnimation = new SpriteAnimation();
		stretchedAnimation.setSprites(ss.blinkyStretchedSprites());
		stretchedAnimation.setFrameDuration(60);
		stretchedAnimation.build();
	}

	private void createBlinkyPatchedAnimation(PacManGameSpritesheet ss) {
		patchedAnimation = new SpriteAnimation();
		patchedAnimation.setSprites(ss.blinkyPatchedSprites());
		patchedAnimation.setFrameDuration(4);
		patchedAnimation.repeatForever();
		patchedAnimation.build();
	}

	private void createBlinkyNakedAnimation(PacManGameSpritesheet ss) {
		nakedAnimation = new SpriteAnimation();
		nakedAnimation.setSprites(ss.blinkyNakedSprites());
		nakedAnimation.setFrameDuration(4);
		nakedAnimation.repeatForever();
		nakedAnimation.build();
	}

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
	}

	@Override
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public void select(String name, Object... args) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = animationByName(name);
			if (currentAnimation != null) {
				if (currentAnimation == numberAnimation) {
					numberAnimation.setFrame((Integer) args[0]);
				}
				currentAnimation.setFrame(0);
			}
		}
	}

	@Override
	public void startSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(GhostAnimations.GHOST_NORMAL)) {
				normalAnimationByDir.values().forEach(SpriteAnimation::start);
			} else if (currentAnimationName.equals(GhostAnimations.GHOST_EYES)) {
				eyesAnimationByDir.values().forEach(SpriteAnimation::start);
			} else {
				currentAnimation.start();
			}
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(GhostAnimations.GHOST_NORMAL)) {
				normalAnimationByDir.values().forEach(SpriteAnimation::stop);
			} else if (currentAnimationName.equals(GhostAnimations.GHOST_EYES)) {
				eyesAnimationByDir.values().forEach(SpriteAnimation::stop);
			} else {
				currentAnimation.stop();
			}
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(GhostAnimations.GHOST_NORMAL)) {
				normalAnimationByDir.values().forEach(SpriteAnimation::reset);
			} else if (currentAnimationName.equals(GhostAnimations.GHOST_EYES)) {
				eyesAnimationByDir.values().forEach(SpriteAnimation::reset);
			} else {
				currentAnimation.reset();
			}
		}
	}

	public SpriteAnimation getStretchedAnimation() {
		return stretchedAnimation;
	}

	public SpriteAnimation getDamagedAnimation() {
		return damagedAnimation;
	}

	public Rectangle2D currentSprite() {
		if (!ghost.isVisible() || currentAnimationName == null) {
			return null;
		}
		if (GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation = animationByName(GHOST_NORMAL); // update
		} else if (GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation = animationByName(GHOST_EYES); // update
		}
		return currentAnimation.frame();
	}

	private SpriteAnimation animationByName(String name) {
		if (GHOST_NORMAL.equals(name)) {
			return normalAnimationByDir.get(ghost.wishDir());
		}
		if (GHOST_FRIGHTENED.equals(name)) {
			return frightenedAnimation;
		}
		if (GHOST_FLASHING.equals(name)) {
			return flashingAnimation;
		}
		if (GHOST_EYES.equals(name)) {
			return eyesAnimationByDir.get(ghost.wishDir());
		}
		if (GHOST_NUMBER.equals(name)) {
			return numberAnimation;
		}
		if (BLINKY_DAMAGED.equals(name)) {
			return damagedAnimation;
		}
		if (BLINKY_NAKED.equals(name)) {
			return nakedAnimation;
		}
		if (BLINKY_PATCHED.equals(name)) {
			return patchedAnimation;
		}
		if (BLINKY_STRETCHED.equals(name)) {
			return stretchedAnimation;
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}
}