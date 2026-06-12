/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import java.util.Arrays;

import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationContainer container;

    public TengenMsPacMan_PacAnimations(SpriteAnimationContainer container) {
        super(TengenMsPacMan_SpriteSheet.instance());
        this.container = requireNonNull(container);
    }

    @Override
    public SpriteAnimationContainer container() {
        return container;
    }

    @Override
    protected SpriteAnimation createAnimation(Identifier animationID) {
        final SpriteAnimation animation = switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PAC_FULL))
                .build();

            case ArcadePacMan_AnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(pacDyingSprites(spriteSheet))
                .frameTicks(8)
                .build();

            case ArcadePacMan_AnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING))
                .repeated()
                .build();

            case MS_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER))
                .repeated()
                .build();

            case MS_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case MS_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MS_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING))
                .frameTicks(2)
                .repeated()
                .build();

            case ANIM_PAC_MAN_BOOSTER -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_MUNCHING_BOOSTER))
                .frameTicks(2)
                .repeated()
                .build();

            case MR_PAC_MAN_WAVING_HAND -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_WAVING_HAND))
                .frameTicks(8)
                .repeated()
                .build();

            case MR_PAC_MAN_TURNING_AWAY -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.sprites(SpriteID.MR_PAC_TURNING_AWAY))
                .frameTicks(15)
                .build();

            case ANIM_JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };

        animation.setContainer(container);
        return animation;
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