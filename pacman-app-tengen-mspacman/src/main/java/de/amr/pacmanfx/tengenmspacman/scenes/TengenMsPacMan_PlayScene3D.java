/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.GameLevel3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_FLOOR_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.control;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    protected GameLevel3D createGameLevel3D(GameLevel level) {
        if (!(level.game() instanceof TengenMsPacMan_GameModel tengenGame)) {
            throw new IllegalArgumentException("Game must be Tengen Ms. Pac-Man");
        }

        // Note: member variable "gameLevel3D" is only set later in replaceGameLevel3D()
        final GameLevel3D newLevel3D = super.createGameLevel3D(level);

        if (!tengenGame.allOptionsDefault()) {
            final double width = TS(level.worldMap().numCols());
            final double height = TS(2);
            final ImageView levelInfo = new ImageView();
            levelInfo.setFitWidth(width);
            levelInfo.setFitHeight(height);
            levelInfo.imageProperty().bind(PROPERTY_3D_FLOOR_COLOR
                .map(color -> createLeveInfo(tengenGame, level.number(), width, height, color)));

            // Display the level info at front side of floor just over the surface
            levelInfo.setTranslateY(newLevel3D.maze3D().mazeFloor3D().getHeight() - levelInfo.getFitHeight());
            levelInfo.setTranslateZ(-newLevel3D.maze3D().mazeFloor3D().getDepth());
            newLevel3D.getChildren().add(levelInfo);
        }
        return newLevel3D;
    }

    private Image createLeveInfo(TengenMsPacMan_GameModel game, int levelNumber, double width, double height, Color backgroundColor) {
        final double scaling = 6;
        final var canvas = new Canvas(scaling * width, scaling * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.scalingProperty().set(scaling);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * width, TS(1.5f));

        return canvas.snapshot(null, null);
    }

    @Override
    protected void setActionBindings(GameLevel level) {
        actionBindings.releaseBindings(GameUI.KEYBOARD);
        actionBindings.useAllBindings(GameUI.PLAY_3D_BINDINGS);
        if (level.isDemoLevel()) {
            // In demo level, allow going back to options screen
            actionBindings.useAnyBinding(ACTION_QUIT_DEMO_LEVEL, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
        } else {
            actionBindings.useAllBindings(TengenMsPacMan_UIConfig.STEERING_BINDINGS);
            actionBindings.useAnyBinding(ACTION_TOGGLE_PAC_BOOSTER, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
            actionBindings.useAllBindings(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.setKeyCombination(actionDroneClimb, control(KeyCode.MINUS));
        actionBindings.setKeyCombination(actionDroneDescent, control(KeyCode.PLUS));
        actionBindings.activateBindings(GameUI.KEYBOARD);
    }

    @Override
    protected void updateHUD(Game game) {
        final Score score = game.score(), highScore = game.highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            scores3D.showTextForScore(ui.translate("score.game_over"), Color.valueOf(NES_Palette.color(0x16)));
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}