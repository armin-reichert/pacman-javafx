/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements SpriteRendererMixin, ActorRenderer {

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
        drawSpriteCentered(computeSprite(actor), actor.center());
    }

    private RectShort computeSprite(Actor actor) {
        return switch (actor) {
            case Pac pac -> computePacSprite(pac);
            case Ghost ghost -> computeGhostSprite(ghost);
            case Bonus bonus -> computeBonusSprite(bonus);
            default -> actor.animations().currentSprite();
        };
    }

    private RectShort computePacSprite(Pac pac) {
        final AnimationSet animations = pac.animations();
        if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(pac.moveDir());
            return sprites[animations.currentFrame()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final AnimationSet animations = ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir());
            return sprites[animations.currentFrame()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = spriteSheet().ghostEyesSprites(ghost.wishDir());
            return sprites[animations.currentFrame()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        //TODO: decouple symbol code from index in sprite array
        return switch (bonus.state()) {
            case EDIBLE   -> spriteSheet().sprites(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
            case EATEN    -> spriteSheet().sprites(SpriteID.BONUS_VALUES)[bonus.symbol()];
            case INACTIVE -> RectShort.ZERO;
        };
    }
}