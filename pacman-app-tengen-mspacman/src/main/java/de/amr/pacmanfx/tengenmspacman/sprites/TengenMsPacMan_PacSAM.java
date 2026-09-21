/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.LazySAM;
import de.amr.basics.ui.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.ui.assets.SpriteSheet;

import java.util.Arrays;

public class TengenMsPacMan_PacSAM extends LazySAM {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_PacSAM(SpriteAnimationContainer container) {
        setFactory(id -> switch (id) {
            case CommonSpriteAnimationID.PAC_MOUTH_SHUT -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PAC_FULL))
                .build(container);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build(container);

            case CommonSpriteAnimationID.PAC_MOUTH_MOVING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(container);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build(container);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(container);

            case TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(container);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(container);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(container);

            case TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(container);

            case TengenMsPacMan_AnimationID.ANIM_JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(container);

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