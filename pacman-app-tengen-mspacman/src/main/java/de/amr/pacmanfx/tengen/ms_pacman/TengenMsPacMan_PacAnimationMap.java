/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

public class TengenMsPacMan_PacAnimationMap extends SpriteAnimationMap<RectArea> {

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
        set(ANIM_PAC_DYING,               createAnimation().sprites(ss.pacDyingSprites()).frameTicks(8).end());
        set(ANIM_PAC_MUNCHING,            createAnimation().sprites(getSprites(SpriteID.MS_PAC_MUNCHING)).endless());

        set(ANIM_MS_PAC_MAN_BOOSTER,      createAnimation().sprites(getSprites(SpriteID.MS_PAC_MUNCHING_BOOSTER)).endless());
        set(ANIM_MS_PAC_MAN_WAVING_HAND,  createAnimation().sprites(getSprites(SpriteID.MS_PAC_WAVING_HAND)).frameTicks(8).endless());
        set(ANIM_MS_PAC_MAN_TURNING_AWAY, createAnimation().sprites(getSprites(SpriteID.MS_PAC_TURNING_AWAY)).frameTicks(15).end());

        set(ANIM_PAC_MAN_MUNCHING,        createAnimation().sprites(getSprites(SpriteID.MR_PAC_MUNCHING)).frameTicks(2).endless());
        set(ANIM_PAC_MAN_BOOSTER,         createAnimation().sprites(getSprites(SpriteID.MR_PAC_MUNCHING_BOOSTER)).frameTicks(2).endless());
        set(ANIM_PAC_MAN_WAVING_HAND,     createAnimation().sprites(getSprites(SpriteID.MR_PAC_WAVING_HAND)).frameTicks(8).endless());
        set(ANIM_PAC_MAN_TURNING_AWAY,    createAnimation().sprites(getSprites(SpriteID.MR_PAC_TURNING_AWAY)).frameTicks(15).end());

        set(ANIM_JUNIOR,                  createAnimation().sprites(getSprite(SpriteID.JUNIOR_PAC)).end());
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac) {
            if (isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
                currentAnimation().setSprites(getSprites(SpriteID.MS_PAC_MUNCHING));
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(getSprites(SpriteID.MS_PAC_MUNCHING_BOOSTER));
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(getSprites(SpriteID.MR_PAC_MUNCHING));
            }
        }
    }
}