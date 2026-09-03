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
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import java.util.Arrays;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

/**
 * Implements the rendering for all actor types occurring in the Arcade Ms. Pac-Man game.
 */
public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 5000 };

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
    public void render(Object r) {
        if (!(r instanceof GameEntity actor)) {
            return;
        }
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
            throw new IllegalStateException("Could not determine ghost sprite");
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
        return switch (bonus.state().enumValue()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        ClapperboardAnimationSystem.sprite(clapperboard).ifPresent(sprite -> {
            drawSpriteCentered(sprite, clapperboard.pos().bodyCenter());

            // Draw number and title
            final String number = clapperboard.inscription().number();
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