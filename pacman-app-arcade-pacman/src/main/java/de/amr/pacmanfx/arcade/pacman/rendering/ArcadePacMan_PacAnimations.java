/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID implements SpriteAnimationID {
        ANIM_BIG_PAC_MAN,
    }

    private final SpriteAnimationContainer container;

    public ArcadePacMan_PacAnimations(SpriteAnimationContainer container, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        this.container = requireNonNull(container);
    }

    @Override
    protected SpriteAnimation createAnimation(SpriteAnimationID animationID) {
        return switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet.sprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build(container);

            // Renderer draws sprites depending on Pac-Man move direction!
            case ArcadePacMan_AnimationID.PAC_MUNCHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.PAC_DYING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case AnimationID.ANIM_BIG_PAC_MAN -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.PACMAN_BIG))
                .frameTicks(3)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }
}