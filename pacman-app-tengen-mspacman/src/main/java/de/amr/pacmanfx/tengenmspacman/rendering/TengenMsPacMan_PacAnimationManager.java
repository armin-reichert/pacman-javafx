/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.Arrays;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;

public class TengenMsPacMan_PacAnimationManager extends SpriteAnimationManager<SpriteID> {

    public TengenMsPacMan_PacAnimationManager() {
        super(TengenMsPacMan_SpriteSheet.INSTANCE);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case ANIM_PAC_FULL                -> SpriteAnimation.builder().ofSprite(spriteSheet.sprite(SpriteID.MS_PAC_FULL)).once();
            case ANIM_PAC_DYING               -> SpriteAnimation.builder().fromSprites(pacDyingSprites()).ticksPerFrame(8).once();
            case ANIM_PAC_MUNCHING            -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING)).endless();
            case ANIM_MS_PAC_MAN_BOOSTER      -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER)).endless();
            case ANIM_MS_PAC_MAN_WAVING_HAND  -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_WAVING_HAND)).ticksPerFrame(8).endless();
            case ANIM_MS_PAC_MAN_TURNING_AWAY -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_TURNING_AWAY)).ticksPerFrame(15).once();
            case ANIM_PAC_MAN_MUNCHING        -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING)).ticksPerFrame(2).endless();
            case ANIM_PAC_MAN_BOOSTER         -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING_BOOSTER)).ticksPerFrame(2).endless();
            case ANIM_PAC_MAN_WAVING_HAND     -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_WAVING_HAND)).ticksPerFrame(8).endless();
            case ANIM_PAC_MAN_TURNING_AWAY    -> SpriteAnimation.builder().fromSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_TURNING_AWAY)).ticksPerFrame(15).once();
            case ANIM_JUNIOR                  -> SpriteAnimation.builder().ofSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).once();
            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac) {
            if (isSelected(ANIM_PAC_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING));
            }
            if (isSelected(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER));
            }
            if (isSelected(ANIM_MS_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_TURNING_AWAY));
            }
            if (isSelected(ANIM_MS_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MS_PAC_WAVING_HAND));
            }
            if (isSelected(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_MUNCHING));
            }
            if (isSelected(ANIM_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_TURNING_AWAY));
            }
            if (isSelected(ANIM_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.spriteSequence(SpriteID.MR_PAC_WAVING_HAND));
            }
        }
    }

    private RectShort[] pacDyingSprites() {
        // TODO: reconsider renderer rotating single sprite to create animation effect
        final var sprites = new RectShort[11];
        final RectShort munchingOpen = spriteSheet().spriteSequence(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, munchingOpen);
        return sprites;
    }
}