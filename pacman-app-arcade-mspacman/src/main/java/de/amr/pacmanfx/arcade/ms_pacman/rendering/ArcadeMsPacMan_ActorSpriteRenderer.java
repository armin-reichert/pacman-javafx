package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_ActorSpriteRenderer extends ActorSpriteRenderer {

    protected final GameUI_Config uiConfig;

    private final RectShort[] bonusSymbols;
    private final RectShort[] bonusValues;

    public ArcadeMsPacMan_ActorSpriteRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        bonusSymbols = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        bonusValues = spriteSheet().spriteSequence(SpriteID.BONUS_VALUES);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Bonus bonus -> drawBonus(bonus);
                default -> actor.animations()
                        .map(animations -> animations.currentSprite(actor))
                        .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));
            }
        }
    }

    public void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) {
            return;
        }
        RectShort sprite = spriteSheet().spriteSequence(SpriteID.CLAPPERBOARD)[clapperboard.state()];
        double numberX = scaled(clapperboard.x() + sprite.width() - 25);
        double numberY = scaled(clapperboard.y() + 18);
        double textX = scaled(clapperboard.x() + sprite.width());
        drawSpriteCentered(clapperboard.center(), sprite);
        ctx().setFont(clapperboard.font());
        ctx().setFill(ARCADE_WHITE);
        ctx().fillText(clapperboard.number(), numberX, numberY);
        ctx().fillText(clapperboard.text(), textX, numberY);
    }

    public void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> {
                ctx().save();
                ctx().translate(0, bonus.jumpHeight());
                drawSpriteCentered(bonus.center(), bonusSymbols[bonus.symbol()]);
                ctx().restore();
            }
            case EATEN  -> drawSpriteCentered(bonus.center(), bonusValues[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }
}
