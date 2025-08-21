package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorSpriteRenderer extends ActorSpriteRenderer {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_ActorSpriteRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawActor(Actor actor) {
        if (!actor.isVisible()) return;

        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else {
            actor.animations()
                    .map(animations -> animations.currentSprite(actor))
                    .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawSpriteCentered(bonus.center(),
                    spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()]);
            case EATEN  -> drawSpriteCentered(bonus.center(),
                    spriteSheet().spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }
}
