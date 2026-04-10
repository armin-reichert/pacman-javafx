/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    public ArcadePacMan_ActorRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        switch (actor) {
            case Bonus bonus -> drawBonus(bonus);
            case Pac pac -> drawPac(pac);
            case Ghost ghost -> drawGhost(ghost);
            default -> drawSpriteCentered(actor.center(), actor.animations().currentSprite());
        }
    }

    private void drawPac(Pac pac) {
        drawSpriteCentered(pac.center(), computePacSprite(pac));
    }

    private RectShort computePacSprite(Pac pac) {
        final AnimationManager animations = pac.animations();
        if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = ArcadePacMan_PacAnimations.munchingSprites(spriteSheet(), pac.moveDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private void drawGhost(Ghost ghost) {
        drawSpriteCentered(ghost.center(), computeGhostSprite(ghost));
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final SpriteAnimationMap<?> animations = (SpriteAnimationMap<?>) ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = ArcadePacMan_GhostAnimations.ghostNormalSprites(
                spriteSheet(), ghost.personality(), ghost.wishDir());
            return sprites[ghost.animations().frameIndex()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = ArcadePacMan_GhostAnimations.ghostEyesSprites(spriteSheet(), ghost.wishDir());
            return sprites[ghost.animations().frameIndex()];
        }
        else {
            return ghost.animations().currentSprite();
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> // symbol code is index in sprite array
                drawBonusSprite(bonus, spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            case EATEN -> // symbol code is index in sprite array
                drawBonusSprite(bonus, spriteSheet().sprites(SpriteID.BONUS_VALUES), bonus.symbol());
            case INACTIVE -> {}
        }
    }

    private void drawBonusSprite(Bonus bonus, RectShort[] sprites, int index) {
        if (0 <= index && index < sprites.length) {
            drawSpriteCentered(bonus.center(), sprites[index]);
        }
    }
}