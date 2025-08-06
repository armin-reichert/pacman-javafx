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
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer extends GameRenderer {

    protected ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_GameRenderer(PacManGames_Assets assets, ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        super(assets);
        this.ctx = requireNonNull(canvas).getGraphicsContext2D();
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() { return Optional.of(spriteSheet); }

    @Override
    public void dispose() {
        ctx = null;
        spriteSheet = null;
        scalingProperty.unbind();
    }

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
            LevelCounter levelCounter = hud.theLevelCounter();
            float x = sceneSize.x() - 4 * TS, y = sceneSize.y() - 2 * TS + 2;
            for (byte symbol : levelCounter.symbols()) {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[symbol];
                drawSpriteScaled(sprite, x, y);
                x -= TS * 2;
            }
        }

        if (hud.isLivesCounterVisible()) {
            LivesCounter livesCounter = hud.theLivesCounter();
            float x = 2 * TS, y = sceneSize.y() - 2 * TS;
            RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSpriteScaled(sprite, x + TS * (2 * i), y);
            }
            if (gameContext.theGame().lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillTextAtScaledPosition("%d".formatted(gameContext.theGame().lifeCount()), ARCADE_YELLOW, font,
                    x - 14, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            String text = "CREDIT %2d".formatted(gameContext.theCoinMechanism().numCoins());
            fillTextAtScaledPosition(text, ARCADE_WHITE, assets().arcadeFont(scaled(8)), 2 * TS, sceneSize.y());
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
            String assetNamespace = theUI().theConfiguration().assetNamespace();
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
            level.energizerPositions().stream()
                    .filter(tile -> !energizerHighlighted || level.tileContainsEatenFood(tile))
                    .forEach(tile -> fillSquareAtTileCenter(tile, 10));
        }
        ctx.restore();
    }

    @Override
    public void drawActor(Actor actor) {
        if (actor instanceof Bonus bonus) {
            drawBonus(bonus);
        }
        else super.drawActor(actor);
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawActorSpriteCentered(bonus, spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[bonus.symbol()]);
            case EATEN  -> drawActorSpriteCentered(bonus, spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[bonus.symbol()]);
            case INACTIVE -> {}
        }
    }
}