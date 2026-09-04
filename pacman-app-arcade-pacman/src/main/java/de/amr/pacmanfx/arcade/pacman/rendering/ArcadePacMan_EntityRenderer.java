/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_EntityRenderer extends BaseRenderer implements SpriteRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

    private final ActorSpriteAnimController animController;

    public ArcadePacMan_EntityRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameEntity gameEntity)) {
            return;
        }
        if (!gameEntity.isVisible()) return;

        final Vector2f center = gameEntity.pos().bodyCenter();

        switch (r) {
            case Pac pac -> drawSpriteCentered(computeSprite(pac), center);
            case Ghost ghost -> drawSpriteCentered(computeSprite(ghost), center);
            case GhostPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Bonus bonus -> drawSpriteCentered(computeSprite(bonus), center);
            case BonusPoints bonusPoints -> drawSpriteCentered(computeSprite(bonusPoints), center);

            default -> {
                if (gameEntity.hasComp(SpriteAnimationComp.class)) {
                    drawSpriteCentered(animController.currentSprite(gameEntity), center);
                }
            }
        }
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

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    //TODO: decouple symbol code from index in sprite array
    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.state().enumValue()) {
            case EDIBLE   -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE  -> RectShort.NULL_RECTANGLE;
        };
    }

}