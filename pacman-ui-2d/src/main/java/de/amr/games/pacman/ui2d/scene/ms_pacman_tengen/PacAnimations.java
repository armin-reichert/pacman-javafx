/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationCollection {

    public PacAnimations(TengenMsPacManGameSpriteSheet spriteSheet) {
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
            .sprites(TengenMsPacManGameSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .loop();

        var husbandMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching booster mode")
            .sprites(TengenMsPacManGameSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .loop();

        add(Map.of(
            GameModel.ANIM_PAC_MUNCHING, munching,
            TengenMsPacManGame.ANIM_PAC_MUNCHING_BOOSTER, munchingBooster,
            GameModel.ANIM_PAC_DYING, dying,
            MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING, husbandMunching,
            TengenMsPacManGame.ANIM_PAC_HUSBAND_MUNCHING_BOOSTER, husbandMunchingBooster
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac msPacMan) {
            if (isCurrentAnimationID(GameModel.ANIM_PAC_MUNCHING)) {
                return TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(TengenMsPacManGame.ANIM_PAC_MUNCHING_BOOSTER)) {
                return TengenMsPacManGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING)) {
                return ((TengenMsPacManGameSpriteSheet)spriteSheet).pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}