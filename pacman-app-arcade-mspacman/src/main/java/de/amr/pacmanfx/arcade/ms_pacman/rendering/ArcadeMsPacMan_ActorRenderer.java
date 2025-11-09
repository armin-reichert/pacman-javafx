/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements ActorRenderer {

    protected final GameUI_Config uiConfig;

    public ArcadeMsPacMan_ActorRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;

        switch (actor) {
            case Bonus bonus -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default -> actor.optAnimationManager()
                    .map(animations -> animations.currentSprite(actor))
                    .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));

        }
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        RectShort[] sprites = spriteSheet().spriteSequence(SpriteID.CLAPPERBOARD);
        int index = clapperboard.state();
        if (0 <= index && index < sprites.length) {
            RectShort sprite = sprites[index];
            drawSpriteCentered(clapperboard.center(), sprite);
            // Draw number and title
            double numberX = scaled(clapperboard.x() + sprite.width() - 25);
            double textX = scaled(clapperboard.x() + sprite.width());
            double y = scaled(clapperboard.y() + 18);
            ctx.setFont(clapperboard.font());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(clapperboard.number(), numberX, y);
            ctx.fillText(clapperboard.text(), textX, y);
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> {
                ctx.save();
                ctx.translate(0, bonus.verticalElongation());
                drawBonusSprite(bonus, spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS), bonus.symbol());
                ctx.restore();
            }
            case EATEN -> drawBonusSprite(bonus, spriteSheet().spriteSequence(SpriteID.BONUS_VALUES), bonus.symbol());
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