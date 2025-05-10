/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TengenMsPacMan_PacAnimations extends SpriteAnimationSet implements PacAnimations {

    public static final String ANIM_MS_PACMAN_BOOSTER      = "ms_pacman_booster";
    public static final String ANIM_MR_PACMAN_BOOSTER      = "pacman_booster";
    public static final String ANIM_MS_PACMAN_WAVING_HAND  = "ms_pacman_waving_hand";
    public static final String ANIM_MR_PACMAN_WAVING_HAND  = "mr_pacman_waving_hand";
    public static final String ANIM_MS_PACMAN_TURNING_AWAY = "ms_pacman_turning_away";
    public static final String ANIM_MR_PACMAN_TURNING_AWAY = "mr_pacman_turning_away";
    public static final String ANIM_JUNIOR_PACMAN          = "junior";

    public TengenMsPacMan_PacAnimations(TengenMsPacMan_SpriteSheet spriteSheet) {
        requireNonNull(spriteSheet);

        var msPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .endLoop();

        var msPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .endLoop();

        var msPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_WAVING_HAND)
            .frameTicks(8)
            .endLoop();

        var msPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MS_PAC_TURNING_AWAY)
            .frameTicks(15)
            .end();

        var msPacmanDying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var mrPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .endLoop();

        var mrPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .endLoop();

        var mrPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_WAVING_HAND)
            .frameTicks(8)
            .endLoop();

        var mrPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(TengenMsPacMan_SpriteSheet.MR_PAC_TURNING_AWAY)
            .frameTicks(15)
            .end();

        var junior = SpriteAnimation
            .spriteSheet(spriteSheet)
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
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
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