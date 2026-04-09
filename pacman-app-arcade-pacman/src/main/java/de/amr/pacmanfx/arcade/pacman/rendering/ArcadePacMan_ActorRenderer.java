/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Pac;
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

        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else if (actor instanceof Pac pac && pac.animations().selectedAnimationID() == Pac.AnimationID.PAC_MUNCHING) {
            // Select munching sprite depending on Pac-Man's current move direction
            final RectShort[] sprites = switch (pac.moveDir()) {
                case RIGHT -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_RIGHT);
                case LEFT  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_LEFT);
                case UP    -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_UP);
                case DOWN  -> spriteSheet().sprites(SpriteID.PACMAN_MUNCHING_DOWN);
            };
            final RectShort sprite = sprites[pac.animations().frameIndex()];
            drawSpriteCentered(actor.center(), sprite);
        }
        else {
            final RectShort sprite = actor.animations().currentSprite(actor);
            if (sprite != null) {
                drawSpriteCentered(actor.center(), sprite);
            }
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