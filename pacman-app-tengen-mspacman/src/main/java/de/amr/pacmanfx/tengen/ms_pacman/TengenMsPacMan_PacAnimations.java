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
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String JUNIOR = "junior";

    public TengenMsPacMan_PacAnimations(TengenMsPacMan_SpriteSheet ss) {
        requireNonNull(ss);
        add(ANY_PAC_DYING,           from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        add(ANY_PAC_MUNCHING,        from(ss).take(MS_PAC_MUNCHING_SPRITES_LEFT).endless());

        add(MS_PAC_MAN_BOOSTER,      from(ss).take(MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).endless());
        add(MS_PAC_MAN_WAVING_HAND,  from(ss).take(MS_PAC_WAVING_HAND).frameTicks(8).endless());
        add(MS_PAC_MAN_TURNING_AWAY, from(ss).take(MS_PAC_TURNING_AWAY).frameTicks(15).end());

        add(PAC_MAN_MUNCHING,        from(ss).take(MR_PAC_MUNCHING_SPRITES_LEFT).frameTicks(2).endless());
        add(PAC_MAN_BOOSTER,         from(ss).take(MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).frameTicks(2).endless());
        add(PAC_MAN_WAVING_HAND,     from(ss).take(MR_PAC_WAVING_HAND).frameTicks(8).endless());
        add(PAC_MAN_TURNING_AWAY,    from(ss).take(MR_PAC_TURNING_AWAY).frameTicks(15).end());

        add(JUNIOR,                  from(ss).take(JUNIOR_PAC_SPRITE).end());
    }

    @Override
    protected RectArea[] updateActorSprites(SpriteSheet spriteSheet, Actor actor) {
        if (actor instanceof Pac msPacMan) {
            var gss = (TengenMsPacMan_SpriteSheet) spriteSheet;
            if (isCurrentAnimationID(ANY_PAC_MUNCHING)) {
                return MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(MS_PAC_MAN_BOOSTER)) {
                return MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(PAC_MAN_MUNCHING)) {
                return gss.pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return null;
    }
}