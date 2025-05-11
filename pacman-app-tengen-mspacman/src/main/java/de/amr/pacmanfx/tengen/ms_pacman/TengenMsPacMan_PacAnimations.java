/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TengenMsPacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String JUNIOR = "junior";

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
            ANY_PAC_DYING, msPacmanDying,
            ANY_PAC_MUNCHING, msPacManMunching,
            MS_PAC_MAN_BOOSTER, msPacManMunchingBooster,
            MS_PAC_MAN_WAVING_HAND, msPacManWavingHand,
            MS_PAC_MAN_TURNING_AWAY, msPacManTurningAway,

            PAC_MAN_MUNCHING, mrPacManMunching,
            PAC_MAN_BOOSTER, mrPacManMunchingBooster,
            PAC_MAN_WAVING_HAND, mrPacManWavingHand,
            PAC_MAN_TURNING_AWAY, mrPacManTurningAway,
            JUNIOR, junior
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        TengenMsPacMan_SpriteSheet gss = (TengenMsPacMan_SpriteSheet) spriteSheet;
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(ANY_PAC_MUNCHING)) {
                return TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(MS_PAC_MAN_BOOSTER)) {
                return TengenMsPacMan_SpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(PAC_MAN_MUNCHING)) {
                return gss.pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}