/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
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

    private final SpriteAnimSystem animSystem;

    public ArcadeMsPacMan_ActorRenderer(SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    @Override
    public SpriteAnimSystem animSystem() {
        return animSystem;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(GameEntity actor) {
        requireNonNull(actor);
        if (!actor.visibility().isVisible()) return;
        final Vector2f center = WorldNavigationSystem.computeCenter(actor);
        switch (actor) {
            case Pac pac                   -> drawSpriteCentered(computePacSprite(pac),     center);
            case Ghost ghost               -> drawSpriteCentered(computeGhostSprite(ghost), center);
            case Bonus bonus               -> drawSpriteCentered(computeBonusSprite(bonus), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default                        -> drawSpriteCentered(animSystem().currentSprite(actor), center);
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        RectShort sprite;
        if (animSystem().isSelected(ghost, ActorAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            sprite = spriteOrDefault(sprites, animSystem().currentFrame(ghost));
        }
        else if (animSystem().isSelected(ghost, ActorAnimationID.GHOST_EYES)) {
            sprite = spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            sprite = animSystem().currentSprite(ghost);
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    private RectShort computePacSprite(Pac pac) {
        RectShort sprite;
        if (animSystem().isSelected(pac, ActorAnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().msPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = spriteOrDefault(sprites, animSystem().currentFrame(pac));
        }
        else if (animSystem().isSelected(pac, ActorAnimationID.MR_PAC_MAN_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().mrPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = spriteOrDefault(sprites, animSystem().currentFrame(pac));
        }
        else {
            sprite = animSystem().currentSprite(pac);
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        final RectShort[] sprites = spriteSheet().findSprites(SpriteID.CLAPPERBOARD);
        final int spriteIndex = clapperboard.state(); //TODO decouple state and index in sprite sheet
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            final RectShort sprite = sprites[spriteIndex];
            drawSpriteCentered(sprite, WorldNavigationSystem.computeCenter(clapperboard));
            // Draw number and title
            final double numberX = scaled(clapperboard.pos().x() + sprite.width() - 25);
            final double textX = scaled(clapperboard.pos().x() + sprite.width());
            final double y = scaled(clapperboard.pos().y() + 18);
            ctx.setFont(clapperboard.font());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(clapperboard.number(), numberX, y);
            ctx.fillText(clapperboard.text(), textX, y);
        }
    }

    // TODO decouple symbol code from sprite index
    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.bonusState()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS), bonus.symbolCode());
            case EATEN ->  spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_VALUES), bonus.symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }
}