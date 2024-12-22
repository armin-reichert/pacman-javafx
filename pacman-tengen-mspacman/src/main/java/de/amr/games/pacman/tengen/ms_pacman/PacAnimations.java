/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationCollection;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationCollection {

    public PacAnimations(MsPacManGameTengenSpriteSheet spriteSheet) {
        Globals.assertNotNull(spriteSheet);

        var msPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .loop();

        var msPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching booster mode")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .loop();

        var msPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man waving hand")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var msPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man turning away")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_TURNING_AWAY)
            .frameTicks(15)
            .end();

        var msPacmanDying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man dying")
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var mrPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .loop();

        var mrPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching booster mode")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .loop();

        var mrPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Pac-Man waving hand")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var mrPacManTurningAway = SpriteAnimation
                .spriteSheet(spriteSheet)
                .info("Mr. Pac-Man turning away")
                .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_TURNING_AWAY)
                .frameTicks(15)
                .end();

        var junior = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Junior Pac-Man")
            .sprites(MsPacManGameTengenSpriteSheet.JUNIOR_PAC_SPRITE)
            .end();

        add(Map.of(
            ANIM_PAC_DYING, msPacmanDying,
            ANIM_PAC_MUNCHING, msPacManMunching,
            ANIM_MS_PACMAN_BOOSTER, msPacManMunchingBooster,
            ANIM_MS_PACMAN_WAVING_HAND, msPacManWavingHand,
            ANIM_MS_PACMAN_TURNING_AWAY, msPacManTurningAway,

            ANIM_MR_PACMAN_MUNCHING, mrPacManMunching,
            ANIM_MR_PACMAN_BOOSTER, mrPacManMunchingBooster,
            ANIM_MR_PACMAN_WAVING_HAND, mrPacManWavingHand,
            ANIM_MR_PACMAN_TURNING_AWAY, mrPacManTurningAway,
            ANIM_JUNIOR_PACMAN, junior
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac msPacMan) {
            if (isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
                return MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(ANIM_MS_PACMAN_BOOSTER)) {
                return MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(ANIM_MR_PACMAN_MUNCHING)) {
                return ((MsPacManGameTengenSpriteSheet)spriteSheet).pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}