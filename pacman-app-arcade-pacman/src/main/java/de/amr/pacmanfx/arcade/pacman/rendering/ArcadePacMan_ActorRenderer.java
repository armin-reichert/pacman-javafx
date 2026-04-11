/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.*;
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
            case Pac pac     -> drawSpriteCentered(computePacSprite(pac), pac.center());
            case Ghost ghost -> drawSpriteCentered(computeGhostSprite(ghost), ghost.center());
            case Bonus bonus -> drawBonus(bonus);
            default          -> drawSpriteCentered(actor.animations().currentSprite(), actor.center());
        }
    }

    private RectShort computePacSprite(Pac pac) {
        final AnimationManager animations = pac.animations();
        if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(pac.moveDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final AnimationManager animations = ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = spriteSheet().ghostEyesSprites(ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> {
                //TODO: decouple symbol code from index in sprite array
                final int index = bonus.symbol();
                drawSpriteCentered(spriteSheet().sprites(SpriteID.BONUS_SYMBOLS)[index], bonus.center());
            }
            case EATEN -> {
                //TODO: decouple symbol code from index in sprite array
                final int index = bonus.symbol();
                drawSpriteCentered(spriteSheet().sprites(SpriteID.BONUS_VALUES)[index], bonus.center());
            }
            case INACTIVE -> {}
        }
    }
}