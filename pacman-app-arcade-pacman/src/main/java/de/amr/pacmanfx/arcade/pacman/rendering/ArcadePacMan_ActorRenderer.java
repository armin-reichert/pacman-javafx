/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements ActorRenderer {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_ActorRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
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
            case EDIBLE -> {
                RectShort[] sprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
                // symbol code is index in sprite array
                byte index = bonus.symbol();
                if (0 <= index && index < sprites.length) {
                    drawSpriteCentered(bonus.center(), sprites[bonus.symbol()]);
                } else {
                    Logger.error("Cannot render bonus with symbol code {}", index);
                }
            }
            case EATEN -> {
                RectShort[] sprites = spriteSheet().spriteSequence(SpriteID.BONUS_VALUES);
                // symbol code is index in sprite array
                byte index = bonus.symbol();
                if (0 <= index && index < sprites.length) {
                    drawSpriteCentered(bonus.center(), sprites[index]);
                } else {
                    Logger.error("Cannot render bonus with symbol code {}", index);
                }
            }
            case INACTIVE -> {}
        }
    }
}