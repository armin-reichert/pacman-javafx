/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Arrays;

import static de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID.*;

public class TengenMsPacMan_PacSAM extends LazySAM {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_PacSAM(SpriteAnimContainer container) {
        factory = id -> switch (id) {
            case CommonSpriteAnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PAC_FULL))
                .build(container);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build(container);

            case CommonSpriteAnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MS_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(container);

            case MS_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build(container);

            case MS_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(container);

            case MS_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(container);

            case MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build(container);

            case ANIM_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PAC_MUNCHING_BOOSTER))
                .frameTicks(2)
                .repeated()
                .build(container);

            case MR_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build(container);

            case MR_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build(container);

            case ANIM_JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID " + id);
        };
    }

    //TODO rethink this
    public static RectShort[] pacDyingSprites(SpriteSheet<SpriteID> spriteSheet) {
        final var sprites = new RectShort[11];
        final RectShort mouthOpen = spriteSheet.findSprites(SpriteID.MS_PAC_MUNCHING)[0];
        Arrays.fill(sprites, mouthOpen);
        return sprites;
    }
}