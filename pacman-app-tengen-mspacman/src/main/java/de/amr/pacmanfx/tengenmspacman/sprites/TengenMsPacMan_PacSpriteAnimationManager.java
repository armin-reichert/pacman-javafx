/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.spriteanim.*;

import java.util.Arrays;

public class TengenMsPacMan_PacSpriteAnimationManager extends LazySpriteAnimationManager {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_PacSpriteAnimationManager(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> switch (id) {
            case CommonSpriteAnimationID.PAC_MOUTH_SHUT -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PAC_FULL))
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.PAC_MOUTH_MOVING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(spriteAnimationSystem);

            case TengenMsPacMan_AnimationID.ANIM_JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(spriteAnimationSystem);

            default -> throw new IllegalArgumentException("Illegal animation ID " + id);
        });
    }

    //TODO rethink this
    public static RectShort[] pacDyingSprites(SpriteSheet<SpriteID> spriteSheet) {
        final var sprites = new RectShort[11];
        final RectShort mouthOpen = spriteSheet.findSprite(SpriteID.MS_PAC_MUNCHING);
        Arrays.fill(sprites, mouthOpen);
        return sprites;
    }
}