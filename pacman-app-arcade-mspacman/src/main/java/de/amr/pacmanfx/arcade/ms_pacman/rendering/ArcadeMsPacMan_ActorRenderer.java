/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

/**
 * Implements the rendering for all actor types occurring in the Arcade Ms. Pac-Man game.
 */
public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRendererMixin, ActorRenderer {

    public ArcadeMsPacMan_ActorRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        switch (actor) {
            case Pac pac                   -> drawSpriteCentered(computePacSprite(pac), pac.computeCenter());
            case Ghost ghost               -> drawSpriteCentered(computeGhostSprite(ghost), ghost.computeCenter());
            case Bonus bonus               -> drawSpriteCentered(computeBonusSprite(bonus), bonus.computeCenter());
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default                        -> drawSpriteCentered(actor.animations().currentSprite(), actor.computeCenter());
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final SpriteAnimationAccessor animationManager = ghost.animations();
        if (animationManager.isSelected(ArcadePacMan_AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir());
            return spriteOrDefault(sprites, animationManager.currentFrame());
        }
        else if (animationManager.isSelected(ArcadePacMan_AnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.wishDir());
        }
        else {
            return animationManager.currentSprite();
        }
    }

    private RectShort computePacSprite(Pac pac) {
        final SpriteAnimationAccessor animations = pac.animations();
        RectShort sprite = null;
        if (animations.isSelected(ArcadePacMan_AnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().msPacManMunchingSprites(pac.moveDir());
            sprite = spriteOrDefault(sprites, animations.currentFrame());
        }
        else if (animations.isSelected(ArcadeMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().mrPacManMunchingSprites(pac.moveDir());
            sprite = spriteOrDefault(sprites, animations.currentFrame());
        }
        else {
            sprite = animations.currentSprite();
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.CLAPPERBOARD);
        final int spriteIndex = clapperboard.state(); //TODO decouple
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            final RectShort sprite = sprites[spriteIndex];
            drawSpriteCentered(sprite, clapperboard.computeCenter());
            // Draw number and title
            final double numberX = scaled(clapperboard.x() + sprite.width() - 25);
            final double textX = scaled(clapperboard.x() + sprite.width());
            final double y = scaled(clapperboard.y() + 18);
            ctx.setFont(clapperboard.font());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(clapperboard.number(), numberX, y);
            ctx.fillText(clapperboard.text(), textX, y);
        }
    }

    // TODO decouple symbol code from sprite index
    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.state()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbolCode());
            case EATEN ->  spriteOrDefault(spriteSheet().sprites(SpriteID.BONUS_VALUES), bonus.symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }
}