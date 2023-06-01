/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimationsMsPacManGame extends PacSpriteAnimationsCommon {

	protected Map<Direction, SpriteAnimation> husbandMunchingMap;

	public PacSpriteAnimationsMsPacManGame(Pac pac, SpritesheetMsPacManGame spritesheet) {
		super(pac, spritesheet);
		husbandMunchingMap = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation.Builder() //
					.frameDurationTicks(2) //
					.loop() //
					.sprites(spritesheet.pacManMunchingSprites(dir)) //
					.build();
			husbandMunchingMap.put(dir, animation);
		}
	}

	@Override
	public SpritesheetMsPacManGame spritesheet() {
		return (SpritesheetMsPacManGame) spritesheet;
	}

	@Override
	protected Rectangle2D[] pacMunchingSprites(Direction dir) {
		return spritesheet().pacMunchingSprites(dir);
	}

	@Override
	protected Rectangle2D[] pacDyingSprites() {
		return spritesheet().pacDyingSprites();
	}

	@Override
	protected SpriteAnimation animation(String name, Direction dir) {
		if (HUSBAND_MUNCHING.equals(name)) {
			return husbandMunchingMap.get(dir);
		}
		return super.animation(name, dir);
	}

	@Override
	protected Optional<Map<Direction, SpriteAnimation>> checkIfAnimationMap(String name) {
		if (PacAnimations.HUSBAND_MUNCHING.equals(name)) {
			return Optional.of(husbandMunchingMap);
		}
		return super.checkIfAnimationMap(name);
	}
}