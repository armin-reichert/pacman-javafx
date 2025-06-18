/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Arrays;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

public class TengenMsPacMan_PacAnimationMap extends SpriteAnimationMap {

    public static final String ANIM_MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String ANIM_MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String ANIM_MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String ANIM_PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String ANIM_PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String ANIM_PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String ANIM_PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String ANIM_JUNIOR = "junior";

    public TengenMsPacMan_PacAnimationMap(TengenMsPacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case ANIM_PAC_DYING               -> SpriteAnimation.createAnimation().ofSprites(pacDyingSprites()).frameTicks(8).end();
            case ANIM_PAC_MUNCHING            -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_MUNCHING)).endless();
            case ANIM_MS_PAC_MAN_BOOSTER      -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_MUNCHING_BOOSTER)).endless();
            case ANIM_MS_PAC_MAN_WAVING_HAND  -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_WAVING_HAND)).frameTicks(8).endless();
            case ANIM_MS_PAC_MAN_TURNING_AWAY -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MS_PAC_TURNING_AWAY)).frameTicks(15).end();
            case ANIM_PAC_MAN_MUNCHING        -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MR_PAC_MUNCHING)).frameTicks(2).endless();
            case ANIM_PAC_MAN_BOOSTER         -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MR_PAC_MUNCHING_BOOSTER)).frameTicks(2).endless();
            case ANIM_PAC_MAN_WAVING_HAND     -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MR_PAC_WAVING_HAND)).frameTicks(8).endless();
            case ANIM_PAC_MAN_TURNING_AWAY    -> SpriteAnimation.createAnimation().ofSprites(spriteSheet().spriteSeq(SpriteID.MR_PAC_TURNING_AWAY)).frameTicks(15).end();
            case ANIM_JUNIOR                  -> SpriteAnimation.createAnimation().ofSprite(spriteSheet().sprite(SpriteID.JUNIOR_PAC)).end();
            default -> throw new IllegalArgumentException("Illegal animation ID " + id);
        };
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