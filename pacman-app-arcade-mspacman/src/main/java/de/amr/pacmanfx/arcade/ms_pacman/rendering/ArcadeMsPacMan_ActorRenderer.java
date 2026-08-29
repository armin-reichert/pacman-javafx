/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.arcade.ms_pacman.entities.clapperboard.ClapperboardAnimationSystem;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

/**
 * Implements the rendering for all actor types occurring in the Arcade Ms. Pac-Man game.
 */
public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    private final ActorSpriteAnimController animController;

    public ArcadeMsPacMan_ActorRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(GameEntity actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        final Vector2f center = actor.pos().bodyCenter();
        switch (actor) {
            case Pac pac                   -> drawSpriteCentered(computeSprite(pac),    center);
            case Ghost ghost               -> drawSpriteCentered(computeSprite(ghost),  center);
            case GhostPoints points        -> drawSpriteCentered(computeSprite(points), center);
            case Bonus bonus               -> drawSpriteCentered(computeSprite(bonus),  center);
            case BonusPoints points        -> drawSpriteCentered(computeSprite(points), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default                        -> drawSpriteCentered(animController.currentSprite(actor), center);
        }
    }

    private RectShort computeSprite(Ghost ghost) {
        RectShort sprite;
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            sprite = spriteOrDefault(sprites, animController.currentFrame(ghost));
        }
        else if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            sprite = spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            sprite = animController.currentSprite(ghost);
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    private RectShort computeSprite(Pac pac) {
        RectShort sprite;
        if (animController.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final RectShort[] sprites = spriteSheet().msPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else if (animController.isSelected(pac, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().mrPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else {
            sprite = animController.currentSprite(pac);
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    // TODO decouple symbol code from sprite index
    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.bonusState()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN ->  spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES), bonus.data().symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = switch (bonusPoints.points().number()) {
            case 100 -> 0;
            case 200 -> 1;
            case 500 -> 2;
            case 700 -> 3;
            case 1000 -> 4;
            case 2000 -> 5;
            case 5000 -> 6;
            default -> throw new IllegalArgumentException("Illegal bonus points number: " + bonusPoints.points().number());
        };
        return spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES), index);
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = switch (ghostPoints.points().number()) {
            case 200 -> 0;
            case 400 -> 1;
            case 800 -> 2;
            case 1600 -> 3;
            default -> throw new IllegalArgumentException("Illegal points value: " + ghostPoints.points());
        };
        return spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS), index);
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) return;
        ClapperboardAnimationSystem.sprite(clapperboard).ifPresent(sprite -> {
            drawSpriteCentered(sprite, clapperboard.pos().bodyCenter());

            // Draw number and title
            final String number = String.valueOf(clapperboard.inscription().number());
            final String text = clapperboard.inscription().text();
            final double numberX = scaled(clapperboard.pos().x() + sprite.width() - 25);
            final double textX = scaled(clapperboard.pos().x() + sprite.width());
            final double y = scaled(clapperboard.pos().y() + 18);
            ctx.setFont(arcadeFont8());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(number, numberX, y);
            ctx.fillText(text, textX, y);
        });
    }
}