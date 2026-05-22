/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimator;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationContainer;

import java.util.Arrays;

import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PacAnimations extends SpriteAnimationContainer<SpriteID> {

    private final SpriteAnimator animator;

    public TengenMsPacMan_PacAnimations(SpriteAnimator animator) {
        super(TengenMsPacMan_SpriteSheet.instance());
        this.animator = requireNonNull(animator);
    }

    @Override
    protected SpriteAnimation createAnimation(AnimationIdentifier animationID) {
        return switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PAC_FULL))
                .build(animator);

            case ArcadePacMan_AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build(animator);

            case ArcadePacMan_AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING))
                .repeated()
                .build(animator);

            case MS_PAC_MAN_BOOSTER -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build(animator);

            case MS_PAC_MAN_WAVING_HAND -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(animator);

            case MS_PAC_MAN_TURNING_AWAY -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(animator);

            case MR_PAC_MAN_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(animator);

            case ANIM_PAC_MAN_BOOSTER -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING_BOOSTER))
                .frameTicks(2)
                .repeated()
                .build(animator);

            case MR_PAC_MAN_WAVING_HAND -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(animator);

            case MR_PAC_MAN_TURNING_AWAY -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(animator);

            case ANIM_JUNIOR -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(animator);

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) super.spriteSheet();
    }

    //TODO rethink this
    public static RectShort[] pacDyingSprites(SpriteSheet<SpriteID> spriteSheet) {
        final var sprites = new RectShort[11];
        final RectShort mouthOpen = spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, mouthOpen);
        return sprites;
    }
}