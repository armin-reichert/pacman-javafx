/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.HTS;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimations implements PacAnimations {

	private final Pac pac;
	private GameSpritesheet gss;

	private Map<Direction, SpriteAnimation> munchingAnimationByDir;
	private SpriteAnimation dyingAnimation;
	private SpriteAnimation damagedAnimation;
	private SpriteAnimation stretchedAnimation;
	private SpriteAnimation patchedAnimation;
	private SpriteAnimation nakedAnimation;
	private SpriteAnimation bigPacManAnimation;

	private String currentAnimationName;
	private SpriteAnimation currentAnimation;

	public PacSpriteAnimations(Pac pac, GameSpritesheet gss) {
		this.pac = pac;
		this.gss = gss;
		setSpritesheet(gss);
	}

	private double s(double value) {
		return value; // TODO
	}

	public void setSpritesheet(GameSpritesheet gss) {
		this.gss = gss;
		createMunchingAnimation();
		createDyingAnimation();
		createBlinkyDamagedAnimation();
		createBlinkyStretchedAnimation();
		createBlinkyPatchedAnimation();
		createBlinkyNakedAnimation();
		createBigPacManAnimation();
	}

	private void createMunchingAnimation() {
		munchingAnimationByDir = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation();
			animation.setSprites(gss.pacMunchingSprites(dir));
			animation.setFrameDuration(1);
			animation.repeatForever();
			animation.build();
			munchingAnimationByDir.put(dir, animation);
		}
	}

	private void createDyingAnimation() {
		dyingAnimation = new SpriteAnimation();
		dyingAnimation.setSprites(gss.pacDyingSprites());
		dyingAnimation.setFrameDuration(8);
		dyingAnimation.build();
	}

	private void createBlinkyDamagedAnimation() {
		damagedAnimation = new SpriteAnimation();
		damagedAnimation.setSprites(gss.sheet().tile(8, 7), gss.sheet().tile(9, 7));
		damagedAnimation.setFrameDuration(60);
		damagedAnimation.build();
	}

	private void createBlinkyStretchedAnimation() {
		stretchedAnimation = new SpriteAnimation();
		stretchedAnimation.setSprites(gss.sheet().tilesRightOf(8, 6, 5));
		stretchedAnimation.setFrameDuration(60);
		stretchedAnimation.build();
	}

	private void createBlinkyPatchedAnimation() {
		patchedAnimation = new SpriteAnimation();
		patchedAnimation.setSprites(gss.sheet().tile(10, 7), gss.sheet().tile(11, 7));
		patchedAnimation.setFrameDuration(4);
		patchedAnimation.repeatForever();
		patchedAnimation.build();
	}

	private void createBlinkyNakedAnimation() {
		nakedAnimation = new SpriteAnimation();
		nakedAnimation.setSprites(gss.sheet().tiles(8, 8, 2, 1), gss.sheet().tiles(10, 8, 2, 1));
		nakedAnimation.setFrameDuration(4);
		nakedAnimation.repeatForever();
		nakedAnimation.build();
	}

	private void createBigPacManAnimation() {
		bigPacManAnimation = new SpriteAnimation();
		bigPacManAnimation.setSprites(gss.sheet().region(31, 15, 32, 34), gss.sheet().region(63, 15, 32, 34),
				gss.sheet().region(95, 15, 34, 34));
		bigPacManAnimation.setFrameDuration(3);
		bigPacManAnimation.repeatForever();
		bigPacManAnimation.build();
	}

	@Override
	public void select(String name) {
		if (name != currentAnimationName) {
			currentAnimationName = name;
			currentAnimation = animationByName(name);
			if (currentAnimation != null) {
				currentAnimation.setFrame(0);
			}
		}
	}

	@Override
	public void startSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingAnimationByDir.values().forEach(SpriteAnimation::start);
			} else {
				currentAnimation.start();
			}
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingAnimationByDir.values().forEach(SpriteAnimation::stop);
			} else {
				currentAnimation.stop();
			}
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation != null) {
			if (currentAnimationName.equals(PacAnimations.PAC_MUNCHING)) {
				munchingAnimationByDir.values().forEach(SpriteAnimation::reset);
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

	public void draw(GraphicsContext g) {
		if (!pac.isVisible() || currentAnimationName == null) {
			return;
		}
		if (PAC_MUNCHING.equals(currentAnimationName)) {
			currentAnimation = animationByName(PAC_MUNCHING); // update
		}
		var sprite = currentAnimation.frame();
		var x = pac.position().x() + HTS - sprite.getWidth() / 2;
		var y = pac.position().y() + HTS - sprite.getHeight() / 2;
		drawSprite(g, gss.sheet().source(), sprite, x, y);
	}

	private SpriteAnimation animationByName(String name) {
		if (PAC_MUNCHING.equals(name)) {
			return munchingAnimationByDir.get(pac.moveDir());
		}
		if (PAC_DYING.equals(name)) {
			return dyingAnimation;
		}
		if (BIG_PACMAN.equals(name)) {
			return bigPacManAnimation;
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}

	protected void drawSprite(GraphicsContext g, Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, //
					sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), //
					s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
		}
	}
}