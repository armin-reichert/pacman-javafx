/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_Actor_Renderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    public ArcadePacMan_Actor_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;

        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else {
            actor.optAnimationManager()
                .map(animations -> animations.currentSprite(actor))
                .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> // symbol code is index in sprite array
                drawBonusSprite(bonus, spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            case EATEN -> // symbol code is index in sprite array
                drawBonusSprite(bonus, spriteSheet().spriteSequence(SpriteID.BONUS_VALUES), bonus.symbol());
            case INACTIVE -> {}
        }
    }

    private void drawBonusSprite(Bonus bonus, RectShort[] sprites, int index) {
        if (0 <= index && index < sprites.length) {
            drawSpriteCentered(bonus.center(), sprites[index]);
        } else {
            Logger.error("Cannot render bonus with symbol code {}", index);
        }
    }
}