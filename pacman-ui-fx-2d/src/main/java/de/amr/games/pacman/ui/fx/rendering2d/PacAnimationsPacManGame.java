/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacAnimationsPacManGame extends SpriteAnimations implements PacAnimations<SpriteAnimation, Rectangle2D> {

	private final Pac pac;
	private final SpritesheetPacManGame spritesheet;

	public PacAnimationsPacManGame(Pac pac, SpritesheetPacManGame spritesheet) {
		Globals.checkNotNull(pac);
		Globals.checkNotNull(spritesheet);
		this.pac = pac;
		this.spritesheet = spritesheet;
		//@formatter:off
		var munching = SpriteAnimation
			.begin()
				.sprites(spritesheet.pacMunchingSprites(Direction.LEFT))
				.loop()
			.end();
		
		var dying = SpriteAnimation
			.begin()
				.sprites(spritesheet.pacDyingSprites())
				.frameTicks(8)
			.end();
		
		var bigPacMan = SpriteAnimation
			.begin()
				.sprites(spritesheet.bigPacManSprites())
				.frameTicks(3)
				.loop()
			.end();
		//@formatter:on
		animationsByName.put(PacAnimations.MUNCHING, munching);
		animationsByName.put(PacAnimations.DYING, dying);
		animationsByName.put(PacAnimations.BIG_PACMAN, bigPacMan);
	}

	@Override
	public void updateCurrentAnimation() {
		if (MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.pacMunchingSprites(pac.moveDir()));
		}
	}
}