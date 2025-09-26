/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.Arrays;

import static de.amr.pacmanfx.model.actors.Actor.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.Actor.ANIM_PAC_MUNCHING;

public class TengenMsPacMan_PacAnimationManager extends SpriteAnimationManager<SpriteID> {

    public static final String ANIM_MS_PAC_MAN_BOOSTER = "ms_pac_man_booster";
    public static final String ANIM_MS_PAC_MAN_WAVING_HAND = "ms_pac_man_waving_hand";
    public static final String ANIM_MS_PAC_MAN_TURNING_AWAY = "ms_pac_man_turning_away";
    public static final String ANIM_PAC_MAN_MUNCHING = "pac_man_munching";
    public static final String ANIM_PAC_MAN_BOOSTER = "pac_man_booster";
    public static final String ANIM_PAC_MAN_WAVING_HAND = "pac_man_waving_hand";
    public static final String ANIM_PAC_MAN_TURNING_AWAY = "pac_man_turning_away";
    public static final String ANIM_JUNIOR = "junior";

    public TengenMsPacMan_PacAnimationManager(TengenMsPacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
    }

    @Override
    protected SpriteAnimation createAnimation(String id) {
        return switch (id) {
            case ANIM_PAC_DYING               -> SpriteAnimation.build().of(pacDyingSprites()).frameTicks(8).once();
            case ANIM_PAC_MUNCHING            -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING)).forever();
            case ANIM_MS_PAC_MAN_BOOSTER      -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER)).forever();
            case ANIM_MS_PAC_MAN_WAVING_HAND  -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MS_PAC_WAVING_HAND)).frameTicks(8).forever();
            case ANIM_MS_PAC_MAN_TURNING_AWAY -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MS_PAC_TURNING_AWAY)).frameTicks(15).once();
            case ANIM_PAC_MAN_MUNCHING        -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING)).frameTicks(2).forever();
            case ANIM_PAC_MAN_BOOSTER         -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING_BOOSTER)).frameTicks(2).forever();
            case ANIM_PAC_MAN_WAVING_HAND     -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MR_PAC_WAVING_HAND)).frameTicks(8).forever();
            case ANIM_PAC_MAN_TURNING_AWAY    -> SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.MR_PAC_TURNING_AWAY)).frameTicks(15).once();
            case ANIM_JUNIOR                  -> SpriteAnimation.build().ofSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).once();
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
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING));
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER));
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_TURNING_AWAY));
            }
            if (isCurrentAnimationID(ANIM_MS_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_WAVING_HAND));
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING));
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_TURNING_AWAY));
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_WAVING_HAND));
            }
        }
    }

    private RectShort[] pacDyingSprites() {
        // TODO this is nuts: renderer rotates single sprite to create animation effect
        var sprites = new RectShort[11];
        RectShort munchingOpen = spriteSheet().spriteSequence(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, munchingOpen);
        return sprites;
    }
}