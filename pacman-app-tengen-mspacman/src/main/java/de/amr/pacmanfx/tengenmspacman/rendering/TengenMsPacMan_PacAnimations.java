/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Arrays;

import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;

public class TengenMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public TengenMsPacMan_PacAnimations() {
        super(TengenMsPacMan_SpriteSheet.instance());
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PAC_FULL))
                .build();

            case Pac.AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build();

            case Pac.AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING))
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_BOOSTER -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_WAVING_HAND -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case ANIM_MS_PAC_MAN_TURNING_AWAY -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case ANIM_MR_PAC_MAN_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build();

            case ANIM_PAC_MAN_BOOSTER -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING_BOOSTER))
                .frameTicks(2)
                .repeated()
                .build();

            case ANIM_MR_PAC_MAN_WAVING_HAND -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case ANIM_MR_PAC_MAN_TURNING_AWAY -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case ANIM_JUNIOR -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build();

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