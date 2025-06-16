/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Arrays;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createAnimation;

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
        set(ANIM_PAC_DYING,               createAnimation().ofSprites(pacDyingSprites()).frameTicks(8).end());
        set(ANIM_PAC_MUNCHING,            createAnimation().ofSprites(ss.spriteSeq(SpriteID.MS_PAC_MUNCHING)).endless());

        set(ANIM_MS_PAC_MAN_BOOSTER,      createAnimation().ofSprites(ss.spriteSeq(SpriteID.MS_PAC_MUNCHING_BOOSTER)).endless());
        set(ANIM_MS_PAC_MAN_WAVING_HAND,  createAnimation().ofSprites(ss.spriteSeq(SpriteID.MS_PAC_WAVING_HAND)).frameTicks(8).endless());
        set(ANIM_MS_PAC_MAN_TURNING_AWAY, createAnimation().ofSprites(ss.spriteSeq(SpriteID.MS_PAC_TURNING_AWAY)).frameTicks(15).end());

        set(ANIM_PAC_MAN_MUNCHING,        createAnimation().ofSprites(ss.spriteSeq(SpriteID.MR_PAC_MUNCHING)).frameTicks(2).endless());
        set(ANIM_PAC_MAN_BOOSTER,         createAnimation().ofSprites(ss.spriteSeq(SpriteID.MR_PAC_MUNCHING_BOOSTER)).frameTicks(2).endless());
        set(ANIM_PAC_MAN_WAVING_HAND,     createAnimation().ofSprites(ss.spriteSeq(SpriteID.MR_PAC_WAVING_HAND)).frameTicks(8).endless());
        set(ANIM_PAC_MAN_TURNING_AWAY,    createAnimation().ofSprites(ss.spriteSeq(SpriteID.MR_PAC_TURNING_AWAY)).frameTicks(15).end());

        set(ANIM_JUNIOR,                  createAnimation().ofSprite(ss.sprite(SpriteID.JUNIOR_PAC)).end());
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac) {
            if (isCurrentAnimationID(ANIM_PAC_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_MUNCHING));
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_MUNCHING_BOOSTER));
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet().spriteSeq(SpriteID.MR_PAC_MUNCHING));
            }
        }
    }

    private Sprite[] pacDyingSprites() {
        // TODO this is nuts
        // renderer rotates single sprite to create animation effect
        var sprites = new Sprite[11];
        Sprite munchingOpen = spriteSheet().spriteSeq(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, munchingOpen);
        return sprites;
    }
}