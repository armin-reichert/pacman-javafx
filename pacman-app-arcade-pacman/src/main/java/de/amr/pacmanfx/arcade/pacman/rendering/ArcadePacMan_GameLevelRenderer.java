/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Renderer for classic Arcade Pac-Man and Pac-Man XXL game variants.
 */
public class ArcadePacMan_GameLevelRenderer extends GameLevelRenderer implements SpriteRendererMixin {

    protected final GameUI_Config uiConfig;

    public ArcadePacMan_GameLevelRenderer(GameUI_Config uiConfig, Canvas canvas) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
    }

    @Override
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        ctx().setFill(backgroundColor);
        ctx().save();
        ctx().scale(scaling(), scaling());
        if (mazeBright) {
            drawBrightGameLevel();
        } else if (gameContext.gameLevel().uneatenFoodCount() == 0) {
            drawEmptyGameLevel(gameContext.gameLevel());
        } else {
            drawGameLevelWithFood(gameContext.gameLevel(), !energizerBright);
        }
        ctx().restore();
    }

    private void drawEmptyGameLevel(GameLevel gameLevel) {
        drawSprite(spriteSheet().sprite(SpriteID.MAP_EMPTY), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        // hide doors
        gameLevel.house().ifPresent(house -> fillSquareAtTileCenter(house.leftDoorTile(), TS + 0.5));
        gameLevel.house().ifPresent(house -> fillSquareAtTileCenter(house.rightDoorTile(), TS + 0.5));
    }

    private void drawBrightGameLevel() {
        Image brightMazeImage = uiConfig.assets().image("flashing_maze");
        ctx().drawImage(brightMazeImage, 0, GameLevel.EMPTY_ROWS_OVER_MAZE * TS);
    }

    private void drawGameLevelWithFood(GameLevel gameLevel, boolean energizerDark) {
        drawSprite(spriteSheet().sprite(SpriteID.MAP_FULL), 0, TS(GameLevel.EMPTY_ROWS_OVER_MAZE), false);
        gameLevel.worldMap().tiles()
                .filter(not(gameLevel::isEnergizerPosition))
                .filter(gameLevel::tileContainsEatenFood)
                .forEach(tile -> fillSquareAtTileCenter(tile, 4));
        gameLevel.energizerPositions().stream()
                .filter(tile -> energizerDark || gameLevel.tileContainsEatenFood(tile))
                .forEach(tile -> fillSquareAtTileCenter(tile, 10));
    }

    @Override
    public void drawActor(Actor actor) {
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