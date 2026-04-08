/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID { PAC_MAN_MUNCHING }

    private final SpriteAnimationTimer timer;
    
    public ArcadeMsPacMan_PacAnimations(SpriteAnimationTimer spriteAnimationTimer) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.timer = requireNonNull(spriteAnimationTimer);
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Pac.AnimationID.PAC_FULL -> builder(timer)
                .singleSprite(spriteSheet.sprite(SpriteID.MS_PACMAN_FULL))
                .build();

            case Pac.AnimationID.PAC_MUNCHING -> builder(timer)
                .sprites(msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build();

            case Pac.AnimationID.PAC_DYING -> builder(timer)
                .sprites(spriteSheet().sprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build();

            case AnimationID.PAC_MAN_MUNCHING -> builder(timer)
                .sprites(mrPacManMunchingSprites(Direction.LEFT))
                .frameTicks(2)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac) {
            switch (selectedID) {
                case Pac.AnimationID.PAC_MUNCHING -> currentAnimation().setSprites(msPacManMunchingSprites(pac.moveDir()));
                case AnimationID.PAC_MAN_MUNCHING -> currentAnimation().setSprites(mrPacManMunchingSprites(pac.moveDir()));
                default -> {}
            }
        }
    }

    private RectShort[] msPacManMunchingSprites(Direction dir) {
        return spriteSheet().sprites(switch (dir) {
            case RIGHT -> SpriteID.MS_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MS_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MS_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MS_PACMAN_MUNCHING_DOWN;
        });
    }

    private RectShort[] mrPacManMunchingSprites(Direction dir) {
        return spriteSheet().sprites(switch (dir) {
            case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MR_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
        });
    }
}