/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationCollection {

    public PacAnimations(PacManGameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);
        add(Map.of(
            GameModel.ANIM_PAC_MUNCHING,
            SpriteAnimation.spriteSheet(spriteSheet).info("Pac-Man munching").sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).loop(),

            GameModel.ANIM_PAC_DYING,
            SpriteAnimation.spriteSheet(spriteSheet).info("Pac-Man dying").sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            PacManArcadeGame.ANIM_PAC_BIG,
            SpriteAnimation.spriteSheet(spriteSheet).info("BIG Pac-Man munching").sprites(spriteSheet.bigPacManSprites()).frameTicks(3).loop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac pac) {
            if (isCurrentAnimationID(GameModel.ANIM_PAC_MUNCHING)) {
                return spriteSheet.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}