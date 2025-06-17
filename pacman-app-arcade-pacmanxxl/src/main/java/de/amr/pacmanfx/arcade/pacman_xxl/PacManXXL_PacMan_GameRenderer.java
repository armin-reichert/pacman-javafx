package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui._2d.VectorGraphicsGameRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class PacManXXL_PacMan_GameRenderer extends VectorGraphicsGameRenderer {

    public PacManXXL_PacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        super(spriteSheet, canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) super.spriteSheet();
    }

    @Override
    public void drawActor(Actor actor) {
        switch (actor) {
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case Bonus bonus -> drawBonus(bonus);
            default -> super.drawActor(actor);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.x(), y = levelCounter.y();
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    private void drawBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
            drawActorSprite(bonus.actor(), sprite);
        } else if (bonus.state() == Bonus.STATE_EATEN) {
            Sprite sprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
            drawActorSprite(bonus.actor(), sprite);
        }
    }

}
