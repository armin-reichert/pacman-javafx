/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Arrays;

import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;

public class TengenMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public TengenMsPacMan_PacAnimations() {
        super(TengenMsPacMan_SpriteSheet.instance());
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> builder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PAC_FULL))
                .build();

            case Pac.AnimationID.PAC_DYING -> builder()
                .sprites(pacDyingSprites())
                .frameTicks(8)
                .build();

            case Pac.AnimationID.PAC_MUNCHING -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING))
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_BOOSTER -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_WAVING_HAND -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_TURNING_AWAY -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case ANIM_PAC_MAN_MUNCHING -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build();

            case ANIM_PAC_MAN_BOOSTER -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING_BOOSTER))
                .frameTicks(2)
                .repeated()
                .build();

            case ANIM_PAC_MAN_WAVING_HAND -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case ANIM_PAC_MAN_TURNING_AWAY -> builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case ANIM_JUNIOR -> builder()
                .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                .stopped()
                .build();

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
            if (isSelected(Pac.AnimationID.PAC_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING));
            }
            if (isSelected(ANIM_MS_PAC_MAN_BOOSTER)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER));
            }
            if (isSelected(ANIM_MS_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MS_PAC_TURNING_AWAY));
            }
            if (isSelected(ANIM_MS_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MS_PAC_WAVING_HAND));
            }
            if (isSelected(ANIM_PAC_MAN_MUNCHING)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING));
            }
            if (isSelected(ANIM_PAC_MAN_TURNING_AWAY)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MR_PAC_TURNING_AWAY));
            }
            if (isSelected(ANIM_PAC_MAN_WAVING_HAND)) {
                currentAnimation().setSprites(spriteSheet.sprites(SpriteID.MR_PAC_WAVING_HAND));
            }
        }
    }

    private RectShort[] pacDyingSprites() {
        // TODO: reconsider renderer rotating single sprite to create animation effect
        final var sprites = new RectShort[11];
        final RectShort munchingOpen = spriteSheet().sprites(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, munchingOpen);
        return sprites;
    }
}