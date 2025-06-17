package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui._2d.VectorGraphicsMapRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;

//TODO avoid code duplication with Arcade Ms. Pac-Man renderer
public class PacManXXL_MsPacMan_GameRenderer extends VectorGraphicsMapRenderer implements SpriteGameRenderer {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet;

    public PacManXXL_MsPacMan_GameRenderer(ArcadeMsPacMan_SpriteSheet spriteSheet, Canvas canvas) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawActor(Actor actor) {
        switch (actor) {
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case Marquee marquee -> drawMarquee(marquee);
            case MovingBonus movingBonus -> drawMovingBonus(movingBonus);
            default -> SpriteGameRenderer.super.drawActor(actor);
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

    private void drawMovingBonus(MovingBonus bonus) {
        if (!bonus.isVisible()) {
            return;
        }
        ctx().save();
        ctx().setImageSmoothing(false);
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> {
                Sprite sprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
            case Bonus.STATE_EATEN  -> {
                Sprite sprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
        }
        ctx().restore();
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        if (!clapperboard.isVisible()) {
            return;
        }
        Sprite sprite = spriteSheet().spriteSeq(SpriteID.CLAPPERBOARD)[clapperboard.state()];
        float numberX = scaled(clapperboard.x() + sprite.width() - 25);
        float numberY = scaled(clapperboard.y() + 18);
        float textX = scaled(clapperboard.x() + sprite.width());
        drawSpriteScaledCenteredAt(sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
        ctx().setFont(clapperboard.font());
        ctx().setFill(ARCADE_WHITE);
        ctx().fillText(clapperboard.number(), numberX, numberY);
        ctx().fillText(clapperboard.text(), textX, numberY);
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    private void drawMarquee(Marquee marquee) {
        long tick = marquee.timer().tickCount();
        ctx().setFill(marquee.bulbOffColor());
        for (int bulbIndex = 0; bulbIndex < marquee.totalBulbCount(); ++bulbIndex) {
            drawBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx().setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx().setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawBulb(marquee, bulbIndex);
        }
    }

    private void drawBulb(Marquee marquee, int bulbIndex) {
        final double minX = marquee.x(), minY = marquee.y();
        final double maxX = marquee.x() + marquee.size().getWidth(), maxY = marquee.y() + marquee.size().getHeight();
        double x, y;
        if (bulbIndex <= 33) { // lower edge left-to-right
            x = minX + 4 * bulbIndex;
            y = maxY;
        }
        else if (bulbIndex <= 48) { // right edge bottom-to-top
            x = maxX;
            y = 4 * (70 - bulbIndex);
        }
        else if (bulbIndex <= 81) { // upper edge right-to-left
            x = 4 * (marquee.totalBulbCount() - bulbIndex);
            y = minY;
        }
        else { // left edge top-to-bottom
            x = minX;
            y = 4 * (bulbIndex - 59);
        }
        ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}
