/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * Common base class for the Pac-Man and Ms. Pac-Man game spritesheets.
 * 
 * @author Armin Reichert
 */
public abstract class GameSpritesheet extends Spritesheet {

	protected GameSpritesheet(Image image, int raster) {
		super(image, raster);
	}

	public abstract Rectangle2D livesCounterSprite();

	public abstract Rectangle2D bonusSymbolSprite(int symbol);

	public abstract Rectangle2D bonusValueSprite(int symbol);

	public abstract Rectangle2D[] pacMunchingSprites(Direction dir);

	public abstract Rectangle2D[] pacDyingSprites();

	public abstract Rectangle2D[] ghostNormalSprites(byte ghostID, Direction dir);

	public abstract Rectangle2D[] ghostFrightenedSprites();

	public abstract Rectangle2D[] ghostFlashingSprites();

	public abstract Rectangle2D[] ghostEyesSprites(Direction dir);

	public abstract Rectangle2D[] ghostNumberSprites();

}