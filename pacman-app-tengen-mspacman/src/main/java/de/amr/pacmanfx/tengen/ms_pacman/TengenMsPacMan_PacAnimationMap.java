/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public class TengenMsPacMan_PacAnimationMap extends SpriteAnimationMap {

    public static final String ANIM_MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String ANIM_MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String ANIM_MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String ANIM_PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String ANIM_PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String ANIM_PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String ANIM_PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String ANIM_JUNIOR = "junior";

    public TengenMsPacMan_PacAnimationMap(TengenMsPacMan_SpriteSheet ss) {
        super(ss);
        set(ANIM_ANY_PAC_DYING,           createSpriteAnimation().sprites(ss.pacDyingSprites()).frameTicks(8).end());
        set(ANIM_ANY_PAC_MUNCHING,        createSpriteAnimation().sprites(MS_PAC_MUNCHING_SPRITES_LEFT).endless());

        set(ANIM_MS_PAC_MAN_BOOSTER,      createSpriteAnimation().sprites(MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).endless());
        set(ANIM_MS_PAC_MAN_WAVING_HAND,  createSpriteAnimation().sprites(MS_PAC_WAVING_HAND).frameTicks(8).endless());
        set(ANIM_MS_PAC_MAN_TURNING_AWAY, createSpriteAnimation().sprites(MS_PAC_TURNING_AWAY).frameTicks(15).end());

        set(ANIM_PAC_MAN_MUNCHING,        createSpriteAnimation().sprites(MR_PAC_MUNCHING_SPRITES_LEFT).frameTicks(2).endless());
        set(ANIM_PAC_MAN_BOOSTER,         createSpriteAnimation().sprites(MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER).frameTicks(2).endless());
        set(ANIM_PAC_MAN_WAVING_HAND,     createSpriteAnimation().sprites(MR_PAC_WAVING_HAND).frameTicks(8).endless());
        set(ANIM_PAC_MAN_TURNING_AWAY,    createSpriteAnimation().sprites(MR_PAC_TURNING_AWAY).frameTicks(15).end());

        set(ANIM_JUNIOR,                  createSpriteAnimation().sprites(JUNIOR_PAC_SPRITE).end());
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(ANIM_ANY_PAC_MUNCHING)) {
                currentAnimation().setSprites(MS_PAC_MUNCHING_SPRITES_LEFT);
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER);
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet().pacManMunchingSprites(msPacMan.moveDir()));
            }
        }
    }
}