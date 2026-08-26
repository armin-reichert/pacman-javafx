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
            case Pac pac                   -> drawSpriteCentered(computePacSprite(pac),     center);
            case Ghost ghost               -> drawSpriteCentered(computeGhostSprite(ghost), center);
            case Bonus bonus               -> drawSpriteCentered(computeBonusSprite(bonus), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default                        -> drawSpriteCentered(animController.currentSprite(actor), center);
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
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

    private RectShort computePacSprite(Pac pac) {
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

    // TODO decouple symbol code from sprite index
    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.bonusState()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN ->  spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES), bonus.data().symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }
}