/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGamePacAnimations extends SpriteAnimationCollection {

    public TengenMsPacManGamePacAnimations(GameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var munching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .loop();

        var munchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching booster mode")
            .sprites(TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .loop();

        var dying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man dying")
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var husbandMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching")
            .sprites(spriteSheet.pacManMunchingSprites(Direction.LEFT))
            .frameTicks(2)
            .loop();

        add(Map.of(
            Pac.ANIM_MUNCHING, munching,
            Pac.ANIM_MUNCHING_BOOSTER, munchingBooster,
            Pac.ANIM_DYING, dying,
            Pac.ANIM_HUSBAND_MUNCHING, husbandMunching
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac msPacMan) {
            if (currently(Pac.ANIM_MUNCHING)) {
                return TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (currently(Pac.ANIM_MUNCHING_BOOSTER)) {
                return TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (currently(Pac.ANIM_HUSBAND_MUNCHING)) {
                return spriteSheet.pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}