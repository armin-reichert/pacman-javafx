/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.rendering2d;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.SpriteAnimationSet;
import de.amr.games.pacman.uilib.SpriteAnimation;
import de.amr.games.pacman.uilib.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class TengenMsPacMan_PacAnimations extends SpriteAnimationSet {

    public TengenMsPacMan_PacAnimations(TengenMsPacMan_SpriteSheet spriteSheet) {
        Globals.assertNotNull(spriteSheet);

        var msPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .loop();

        var msPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching booster mode")
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .loop();

        var msPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man waving hand")
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var msPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man turning away")
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_TURNING_AWAY)
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
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .loop();

        var mrPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching booster mode")
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .loop();

        var mrPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Pac-Man waving hand")
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var mrPacManTurningAway = SpriteAnimation
                .spriteSheet(spriteSheet)
                .info("Mr. Pac-Man turning away")
                .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_TURNING_AWAY)
                .frameTicks(15)
                .end();

        var junior = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Junior Pac-Man")
            .sprites(TengenMsPacMan_SpriteSheet.JUNIOR_PAC_SPRITE)
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
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor2D actor) {
        TengenMsPacMan_SpriteSheet gss = (TengenMsPacMan_SpriteSheet) spriteSheet;
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
                return TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(ANIM_MS_PACMAN_BOOSTER)) {
                return TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(ANIM_MR_PACMAN_MUNCHING)) {
                return gss.pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}