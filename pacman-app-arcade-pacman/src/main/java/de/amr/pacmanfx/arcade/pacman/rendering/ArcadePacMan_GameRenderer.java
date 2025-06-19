/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.StaticBonus;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_YELLOW;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class ArcadePacMan_GameRenderer extends SpriteGameRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_GameRenderer(ArcadePacMan_SpriteSheet spriteSheet, Canvas canvas) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void drawHUD(GameModel game) {
        requireNonNull(game);

        final HUD hud = game.hud();
        if (!hud.isVisible()) return;

        Vector2f sceneSize = optGameLevel().map(GameLevel::worldSizePx).orElse(ARCADE_MAP_SIZE_IN_PIXELS);

        if (hud.isScoreVisible()) {
            Font scoreFont = theAssets().arcadeFont(scaled(8));
            drawScore(game.score(), "SCORE", tiles_to_px(1), tiles_to_px(1), scoreFont, ARCADE_WHITE);
            drawScore(game.highScore(), "HIGH SCORE", tiles_to_px(14), tiles_to_px(1), scoreFont, ARCADE_WHITE);
        }

        if (hud.isLevelCounterVisible()) {
            LevelCounter levelCounter = hud.levelCounter();
            float x = sceneSize.x() - 4 * TS, y = sceneSize.y() - 2 * TS;
            for (byte symbol : levelCounter.symbols()) {
                Sprite sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[symbol];
                drawSpriteScaled(sprite, x, y);
                x -= TS * 2;
            }
        }

        if (hud.isLivesCounterVisible()) {
            LivesCounter livesCounter = hud.livesCounter();
            float x = 2 * TS, y = sceneSize.y() - 2 * TS;
            Sprite sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
            for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
                drawSpriteScaled(sprite, x + TS * (2 * i), y);
            }
            if (game.lifeCount() > livesCounter.maxLivesDisplayed()) {
                // show text indicating that more lives are available than symbols displayed (cheating may cause this)
                Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
                fillTextAtScaledPosition("(%d)".formatted(game.lifeCount()), ARCADE_YELLOW, font, x + TS * 10, y + TS);
            }
        }

        if (hud.isCreditVisible()) {
            String text = "CREDIT %2d".formatted(theCoinMechanism().numCoins());
            fillTextAtScaledPosition(text, ARCADE_WHITE, theAssets().arcadeFont(scaled(8)), 2 * TS, sceneSize.y() - 2);
        }
    }

    private void drawScore(Score score, String title, double x, double y, Font font, Color color) {
        ctx.setFont(font);
        ctx.setFill(color);
        fillTextAtScaledPosition(title, x, y);
        fillTextAtScaledPosition("%7s".formatted("%02d".formatted(score.points())), x, y + TS + 1);
        if (score.points() != 0) {
            fillTextAtScaledPosition("L" + score.levelNumber(), x + tiles_to_px(8), y + TS + 1);
        }
    }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        if (mazeHighlighted) {
            ctx.drawImage(theAssets().image("pacman.flashing_maze"), 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
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
        else super.drawActor(actor);
    }

    public void drawStaticBonus(StaticBonus bonus) {
        switch (bonus.state()) {
            case Bonus.STATE_INACTIVE -> {}
            case Bonus.STATE_EDIBLE -> {
                Sprite sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
            case Bonus.STATE_EATEN  -> {
                Sprite sprite = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
        }
    }
}