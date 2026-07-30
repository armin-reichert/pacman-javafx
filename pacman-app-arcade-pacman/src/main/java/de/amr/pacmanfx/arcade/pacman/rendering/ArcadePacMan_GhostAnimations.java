/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import java.util.Objects;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final GhostPersonality personality;

    public ArcadePacMan_GhostAnimations(SpriteAnimationContainer container, GhostPersonality personality) {
        super(ArcadePacMan_SpriteSheet.instance());
        this.personality = Objects.requireNonNull(personality);
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {

        return switch (animationID) {
            case ActorAnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ActorAnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ActorAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case ActorAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ActorAnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            case ActorAnimationID.BLINKY_DAMAGED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build(container);

            case ActorAnimationID.BLINKY_PATCHED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build(container);

            case ActorAnimationID.BLINKY_NAKED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Identifier animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ActorAnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ActorAnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}