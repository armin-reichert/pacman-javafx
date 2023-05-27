/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game renderers.
 * 
 * @author Armin Reichert
 */
public abstract class GameSpritesheet {

	protected final Spritesheet spritesheet;

	protected GameSpritesheet(Spritesheet spritesheet) {
		checkNotNull(spritesheet);
		this.spritesheet = spritesheet;
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	/**
	 * @return sprite used in lives counter
	 */
	public abstract Rectangle2D livesCounterSprite();

	/**
	 * @return sprite showing ghost value (200, 400, 800, 1600)
	 */
	public abstract Rectangle2D ghostValueSprite(int index);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol (cherries, strawberry, ...)
	 */
	public abstract Rectangle2D bonusSymbolSprite(int symbol);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol value (100, 300, ...)
	 */
	public abstract Rectangle2D bonusValueSprite(int symbol);

	public abstract AnimationMap createPacAnimations(Pac pac);

	public abstract AnimationMap createGhostAnimations(Ghost ghost);

	public abstract AnimationMap createWorldAnimations(World world);
}