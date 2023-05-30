/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game spritesheets.
 * 
 * @author Armin Reichert
 */
public interface GameSpritesheet {

	Spritesheet sheet();

	Image source();

	int raster();

	Image subImage(Rectangle2D region);

	/**
	 * @return sprite used in lives counter
	 */
	Rectangle2D livesCounterSprite();

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol (cherries, strawberry, ...)
	 */
	Rectangle2D bonusSymbolSprite(int symbol);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol value (100, 300, ...)
	 */
	Rectangle2D bonusValueSprite(int symbol);

	AnimationMap createPacAnimations(Pac pac);

	AnimationMap createWorldAnimations(World world);

	Rectangle2D[] pacMunchingSprites(Direction dir);

	Rectangle2D[] pacDyingSprites();

	Rectangle2D[] normalGhostSprites(byte ghostID, Direction dir);

	Rectangle2D[] blueGhostSprites();

	Rectangle2D[] flashingGhostSprites();

	Rectangle2D[] eyesGhostSprites(Direction dir);

	Rectangle2D[] numberSprites();

}