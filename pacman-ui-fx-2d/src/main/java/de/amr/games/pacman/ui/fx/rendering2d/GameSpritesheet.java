/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game spritesheets.
 * 
 * @author Armin Reichert
 */
public interface GameSpritesheet {

	Spritesheet sheet();

	Rectangle2D livesCounterSprite();

	Rectangle2D bonusSymbolSprite(int symbol);

	Rectangle2D bonusValueSprite(int symbol);

	Rectangle2D[] pacMunchingSprites(Direction dir);

	Rectangle2D[] pacDyingSprites();

	Rectangle2D[] ghostNormalSprites(byte ghostID, Direction dir);

	Rectangle2D[] ghostFrightenedSprites();

	Rectangle2D[] ghostFlashingSprites();

	Rectangle2D[] ghostEyesSprites(Direction dir);

	Rectangle2D[] ghostNumberSprites();

}