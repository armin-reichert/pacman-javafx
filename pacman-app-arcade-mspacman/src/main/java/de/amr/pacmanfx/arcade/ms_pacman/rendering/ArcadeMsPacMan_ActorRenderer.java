/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

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
            case Pac pac                   -> drawSpriteCentered(pac.center(), computePacSprite(pac));
            case Ghost ghost               -> drawSpriteCentered(ghost.center(), computeGhostSprite(ghost));
            case Bonus bonus               -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default                        -> drawSpriteCentered(actor.center(), actor.animations().currentSprite());
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final AnimationManager animations = ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = ArcadeMsPacMan_GhostAnimations.ghostNormalSprites(
                spriteSheet(), ghost.personality(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = ArcadeMsPacMan_GhostAnimations.ghostEyesSprites(spriteSheet(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private RectShort computePacSprite(Pac pac) {
        final AnimationManager animations = pac.animations();
        if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = ArcadeMsPacMan_PacAnimations.msPacManMunchingSprites(spriteSheet(), pac.moveDir());
            return sprites[animations.frameIndex()];
        }
        else if (animations.isSelected(ArcadeMsPacMan_PacAnimations.AnimationID.PAC_MAN_MUNCHING)) {
            final RectShort[] sprites = ArcadeMsPacMan_PacAnimations.mrPacManMunchingSprites(spriteSheet(), pac.moveDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.CLAPPERBOARD);
        final int spriteIndex = clapperboard.state(); //TODO decouple
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            final RectShort sprite = sprites[spriteIndex];
            drawSpriteCentered(clapperboard.center(), sprite);
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

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawSpriteCentered(bonus.center().plus(0, bonus.verticalElongation()),
                spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            case EATEN -> drawSpriteCentered(bonus.center(),
                spriteSheet().sprites(SpriteID.BONUS_VALUES), bonus.symbol());
            case INACTIVE -> {}
        }
    }

    private void drawSpriteCentered(Vector2f center, RectShort[] sprites, int spriteIndex) {
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            drawSpriteCentered(center, sprites[spriteIndex]);
        }
    }
}