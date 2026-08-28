/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    private final ActorSpriteAnimController animController;

    public ArcadePacMan_ActorRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(GameEntity actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        drawSpriteCentered(computeSprite(actor), actor.pos().bodyCenter());
    }

    private RectShort computeSprite(GameEntity actor) {
        return switch (actor) {
            case Pac pac -> computeSprite(pac);
            case Ghost ghost -> computeSprite(ghost);
            case GhostPoints points -> computeSprite(points);
            case Bonus bonus -> computeSprite(bonus);
            case BonusPoints bonusPoints -> computeSprite(bonusPoints);
            default -> animController.currentSprite(actor);
        };
    }

    private RectShort computeSprite(Pac pac) {
        if (animController.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final Direction dir = pac.worldNavigation().moveDir();
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(dir);
            return spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else {
            return animController.currentSprite(pac);
        }
    }

    private RectShort computeSprite(Ghost ghost) {
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return spriteOrDefault(sprites, animController.currentFrame(ghost));
        }
        else if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            return animController.currentSprite(ghost);
        }
    }

    private RectShort computeSprite(GhostPoints points) {
        final int index = switch (points.points().number()) {
            case 200 -> 0;
            case 400 -> 1;
            case 800 -> 2;
            case 1600 -> 3;
            default -> throw new IllegalArgumentException("Illegal points value: " + points.points());
        };
        return spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS), index);
    }

    private RectShort computeSprite(Bonus bonus) {
        //TODO: decouple symbol code from index in sprite array
        return switch (bonus.bonusState()) {
            case EDIBLE   -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE  -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = switch (bonusPoints.points().value()) {
            case 100 -> 0;
            case 300 -> 1;
            case 500 -> 2;
            case 700 -> 3;
            case 1000 -> 4;
            case 2000 -> 5;
            case 5000 -> 6;
            default -> throw new IllegalArgumentException("Illegal points value: " + bonusPoints.points());
        };
        return spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES), index);
    }
}