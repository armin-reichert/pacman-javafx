/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final GhostPersonality personality;

    public ArcadeMsPacMan_GhostAnimations(SpriteAnimationContainer container, GhostPersonality personality) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.personality = requireNonNull(personality);
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
                .sprites(spriteSheet().findSprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ActorAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case ActorAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ActorAnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Identifier animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ActorAnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ActorAnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}