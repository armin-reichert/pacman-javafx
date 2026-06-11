/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID implements Named {
        ANIM_BIG_PAC_MAN,
    }

    private final SpriteAnimationSet container;

    public ArcadePacMan_PacAnimations(SpriteAnimationSet container, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        this.container = requireNonNull(container);
    }

    @Override
    public SpriteAnimationSet container() {
        return container;
    }

    @Override
    protected SpriteAnimation createAnimation(Named animationID) {
        final SpriteAnimation animation = switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build();

            // Renderer draws sprites depending on Pac-Man move direction!
            case ArcadePacMan_AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build();

            case ArcadePacMan_AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.ANIM_BIG_PAC_MAN -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_BIG))
                .frameTicks(3)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };

        animation.setContainer(container);
        return animation;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }
}