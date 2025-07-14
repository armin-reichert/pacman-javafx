/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.StaticBonus;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer implements SpriteGameRenderer {

    protected final AssetStorage assets;
    protected GraphicsContext ctx;
    protected ArcadePacMan_SpriteSheet spriteSheet;
    protected final FloatProperty scalingPy = new SimpleFloatProperty(1);

    public ArcadePacMan_GameRenderer(AssetStorage assets, ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        this.assets = requireNonNull(assets);
        this.ctx = requireNonNull(canvas).getGraphicsContext2D();
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public PacManGames_Assets assets() {
        return (PacManGames_Assets) assets;
    }

    @Override
    public void destroy() {
        ctx = null;
        spriteSheet = null;
        scalingPy.unbind();
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public void drawHUD(GameContext gameContext, HUD hud, Vector2f sceneSize, long tick) {
        requireNonNull(hud);
        if (!hud.isVisible()) return;

        if (hud.isScoreVisible()) {
            ctx.setFont(assets().arcadeFont(scaled(8)));
            ctx.setFill((ARCADE_WHITE));
            drawScore(gameContext.theGame().score(), "SCORE", tiles_to_px(1), tiles_to_px(1));
            drawScore(gameContext.theGame().highScore(), "HIGH SCORE", tiles_to_px(14), tiles_to_px(1));
        }

        if (hud.isLevelCounterVisible()) {
            LevelCounter levelCounter = hud.levelCounter();
            float x = sceneSize.x() - 4 * TS, y = sceneSize.y() - 2 * TS;
            for (byte symbol : levelCounter.symbols()) {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[symbol];
                drawSpriteScaled(sprite, x, y);
                x -= TS * 2;
            }
        }

        if (hud.isLivesCounterVisible()) {
            LivesCounter livesCounter = hud.livesCounter();
            float x = 2 * TS, y = sceneSize.y() - 2 * TS;
            RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSpriteScaled(sprite, x + TS * (2 * i), y);
            }
            if (gameContext.theGame().lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillTextAtScaledPosition("(%d)".formatted(gameContext.theGame().lifeCount()), ARCADE_YELLOW, font, x + TS * 10, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            String text = "CREDIT %2d".formatted(gameContext.theCoinMechanism().numCoins());
            fillTextAtScaledPosition(text, ARCADE_WHITE, assets().arcadeFont(scaled(8)), 2 * TS, sceneSize.y() - 2);
        }
    }

    private void drawScore(Score score, String title, double x, double y) {
        fillTextAtScaledPosition(title, x, y);
        fillTextAtScaledPosition("%7s".formatted("%02d".formatted(score.points())), x, y + TS + 1);
        if (score.points() != 0) {
            fillTextAtScaledPosition("L" + score.levelNumber(), x + tiles_to_px(8), y + TS + 1);
        }
    }

    @Override
    public void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick)
    {
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (mazeHighlighted) {
            String assetNamespace = theUI().theUIConfiguration().assetNamespace();
            ctx.drawImage(assets.image(assetNamespace + ".flashing_maze"), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else if (level.uneatenFoodCount() == 0) {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_EMPTY), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
        }
        else {
            drawSprite(spriteSheet.sprite(SpriteID.MAP_FULL), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
            ctx.setFill(backgroundColor);
            level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::tileContainsEatenFood)
                    .forEach(tile -> fillSquareAtTileCenter(tile, 4));
            level.energizerTiles()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    @Override
    public void drawActor(Actor actor) {
        if (actor instanceof StaticBonus staticBonus) {
            drawStaticBonus(staticBonus);
        }
        else SpriteGameRenderer.super.drawActor(actor);
    }

    public void drawStaticBonus(StaticBonus bonus) {
        switch (bonus.state()) {
            case Bonus.STATE_INACTIVE -> {}
            case Bonus.STATE_EDIBLE -> {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
            case Bonus.STATE_EATEN  -> {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
        }
    }
}