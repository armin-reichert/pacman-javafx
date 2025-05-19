/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationSet;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class TengenMsPacMan_PacAnimationSet extends SpriteAnimationSet {

    public static final String ANIM_MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String ANIM_MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String ANIM_MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String ANIM_PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String ANIM_PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String ANIM_PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String ANIM_PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String ANIM_JUNIOR = "junior";

    public TengenMsPacMan_PacAnimationSet(TengenMsPacMan_SpriteSheet ss) {
        super(ss);
        add(ANIM_ANY_PAC_DYING,           from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        add(ANIM_ANY_PAC_MUNCHING,        from(ss).take(MS_PAC_MUNCHING_SPRITES_LEFT).endless());

        add(ANIM_MS_PAC_MAN_BOOSTER,      from(ss).take(MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).endless());
        add(ANIM_MS_PAC_MAN_WAVING_HAND,  from(ss).take(MS_PAC_WAVING_HAND).frameTicks(8).endless());
        add(ANIM_MS_PAC_MAN_TURNING_AWAY, from(ss).take(MS_PAC_TURNING_AWAY).frameTicks(15).end());

        add(ANIM_PAC_MAN_MUNCHING,        from(ss).take(MR_PAC_MUNCHING_SPRITES_LEFT).frameTicks(2).endless());
        add(ANIM_PAC_MAN_BOOSTER,         from(ss).take(MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).frameTicks(2).endless());
        add(ANIM_PAC_MAN_WAVING_HAND,     from(ss).take(MR_PAC_WAVING_HAND).frameTicks(8).endless());
        add(ANIM_PAC_MAN_TURNING_AWAY,    from(ss).take(MR_PAC_TURNING_AWAY).frameTicks(15).end());

        add(ANIM_JUNIOR,                  from(ss).take(JUNIOR_PAC_SPRITE).end());
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac msPacMan) {
            var gss = (TengenMsPacMan_SpriteSheet) spriteSheet;
            if (isCurrentAnimationID(ANIM_ANY_PAC_MUNCHING)) {
                currentAnimation().setSprites(MS_PAC_MUNCHING_SPRITES_LEFT);
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER);
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(gss.pacManMunchingSprites(msPacMan.moveDir()));
            }
        }
    }
}