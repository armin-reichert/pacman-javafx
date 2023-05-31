/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimations implements PacAnimations {

	private final Pac pac;
	private GameSpritesheet gss;

	private Map<Direction, SpriteAnimation> munchingMap;
	private SpriteAnimation dyingAnimation;

	// Pac-Man specific
	private SpriteAnimation bigPacManAnimation;

	// Ms. Pac-Man specific
	private Map<Direction, SpriteAnimation> husbandMunchingMap;

	private String currentAnimationName;
	private SpriteAnimation currentAnimation;

	public PacSpriteAnimations(Pac pac, GameSpritesheet gss) {
		this.pac = pac;
		this.gss = gss;
		setSpritesheet(gss);
	}

	public void setSpritesheet(GameSpritesheet gss) {
		this.gss = gss;
		createMunchingAnimation();
		createDyingAnimation();
		if (gss instanceof MsPacManGameSpritesheet) {
			createHusbandMunchingAnimation((MsPacManGameSpritesheet) gss);
		} else {
			createBigPacManAnimation((PacManGameSpritesheet) gss);
		}
	}

	private void createMunchingAnimation() {
		munchingMap = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation();
			animation.setSprites(gss.pacMunchingSprites(dir));
			animation.setFrameDuration(1);
			animation.repeatForever();
			animation.build();
			munchingMap.put(dir, animation);
		}
	}

	private void createHusbandMunchingAnimation(MsPacManGameSpritesheet gss) {
		husbandMunchingMap = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation();
			animation.setSprites(gss.pacManMunchingSprites(dir));
			animation.setFrameDuration(2);
			animation.repeatForever();
			animation.build();
			husbandMunchingMap.put(dir, animation);
		}
	}

	private void createDyingAnimation() {
		dyingAnimation = new SpriteAnimation();
		dyingAnimation.setSprites(gss.pacDyingSprites());
		dyingAnimation.setFrameDuration(8);
		dyingAnimation.build();
	}

	private void createBigPacManAnimation(PacManGameSpritesheet gss) {
		bigPacManAnimation = new SpriteAnimation();
		bigPacManAnimation.setSprites(gss.bigPacManSprites());
		bigPacManAnimation.setFrameDuration(3);
		bigPacManAnimation.repeatForever();
		bigPacManAnimation.build();
	}

	@Override
	public void select(String name) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = animation(name, pac.moveDir());
			if (currentAnimation != null) {
				currentAnimation.setFrame(0);
			}
		}
	}

	@Override
	public void startSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingMap.values().forEach(SpriteAnimation::start);
			} else if (currentAnimationName.equals(PacAnimations.HUSBAND_MUNCHING)) {
				husbandMunchingMap.values().forEach(SpriteAnimation::start);
			} else {
				currentAnimation.start();
			}
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingMap.values().forEach(SpriteAnimation::stop);
			} else if (currentAnimationName.equals(PacAnimations.HUSBAND_MUNCHING)) {
				husbandMunchingMap.values().forEach(SpriteAnimation::stop);
			} else {
				currentAnimation.stop();
			}
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingMap.values().forEach(SpriteAnimation::reset);
			} else if (currentAnimationName.equals(PacAnimations.HUSBAND_MUNCHING)) {
				husbandMunchingMap.values().forEach(SpriteAnimation::reset);
			} else {
				currentAnimation.reset();
			}
		}
	}

	public Rectangle2D currentSprite() {
		if (!pac.isVisible() || currentAnimationName == null) {
			return null;
		}
		if (PAC_MUNCHING.equals(currentAnimationName)) {
			currentAnimation = animation(PAC_MUNCHING, pac.moveDir()); // update
		}
		if (HUSBAND_MUNCHING.equals(currentAnimationName)) {
			currentAnimation = animation(HUSBAND_MUNCHING, pac.moveDir()); // update
		}
		return currentAnimation.frame();
	}

	private SpriteAnimation animation(String name, Direction dir) {
		if (PAC_MUNCHING.equals(name)) {
			return munchingMap.get(dir);
		}
		if (PAC_DYING.equals(name)) {
			return dyingAnimation;
		}
		if (BIG_PACMAN.equals(name)) {
			return bigPacManAnimation;
		}
		if (HUSBAND_MUNCHING.equals(name)) {
			return husbandMunchingMap.get(dir);
		}
		throw new IllegalArgumentException("Illegal animation (name, dir) value: " + name + "," + dir);
	}
}