/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class ArcadePacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID implements Identifier {
        ANIM_BIG_PAC_MAN,
    }

    public ArcadePacMan_PacAnimations(SpriteAnimationContainer container, ArcadePacMan_SpriteSheet spriteSheet) {
        super(spriteSheet);
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {

        return switch (animationID) {
            case ArcadePacMan_AnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build(container);

            // Renderer draws sprites depending on Pac-Man move direction!
            case ArcadePacMan_AnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case AnimationID.ANIM_BIG_PAC_MAN -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.PACMAN_BIG))
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