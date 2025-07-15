/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameUI ui) {
        super(ui);
    }

    @Override
    protected void setActionBindings() {
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        // if demo level, allow going back to options screen
        if (gameContext().optGameLevel().isPresent() && gameContext().theGameLevel().isDemoLevel()) {
            actionBindings.bind(config.ACTION_QUIT_DEMO_LEVEL, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        } else {
            setPlayerSteeringActionBindings();
            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            // Tengen only:
            actionBindings.bind(config.ACTION_TOGGLE_PAC_BOOSTER, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        }
        actionBindings.bind(ACTION_PERSPECTIVE_PREVIOUS, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_PERSPECTIVE_NEXT, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TOGGLE_DRAW_MODE, GLOBAL_ACTION_BINDINGS);

        actionBindings.updateKeyboard();
    }

    @Override
    protected void setPlayerSteeringActionBindings() {
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        actionBindings.bind(ACTION_STEER_UP,    config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_DOWN,  config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_LEFT,  config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_RIGHT, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
    }

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();
        TengenMsPacMan_GameModel game = gameContext().theGame();
        if (!game.optionsAreInitial()) {
            // show info about category, difficulty, booster etc
            ImageView infoView = createGameInfoView(gameContext().theGameLevel(), ui.property3DFloorColor().get());
            infoView.setTranslateX(0);
            infoView.setTranslateY((gameContext().theGameLevel().worldMap().numRows() - 2) * TS);
            infoView.setTranslateZ(-gameLevel3D.floorThickness());
            gameLevel3D.getChildren().add(infoView);
        }
    }

    private ImageView createGameInfoView(GameLevel gameLevel, Color floorColor) {
        var game = gameContext().<TengenMsPacMan_GameModel>theGame();

        final int infoWidth = gameLevel.worldMap().numCols() * TS, infoHeight = 2 * TS;
        final float quality = 5; // scale for better snapshot resolution

        var canvas = new Canvas(quality * infoWidth, quality * infoHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!
        GameRenderer.fillCanvas(canvas, floorColor);

        var r = (TengenMsPacMan_GameRenderer) ui.theConfiguration().createGameRenderer(canvas);
        r.setScaling(quality);
        r.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * infoWidth, TS + HTS);
        r.drawLevelNumberBox(gameLevel.number(), 0, 0);
        r.drawLevelNumberBox(gameLevel.number(), infoWidth - 2 * TS, 0);

        ImageView infoView = new ImageView(canvas.snapshot(null, null));
        infoView.setFitWidth(infoWidth);
        infoView.setFitHeight(infoHeight);

        return infoView;
    }

    @Override
    protected void updateScores(GameLevel gameLevel) {
        final Score score = gameContext().theGame().score(), highScore = gameContext().theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            // if score is disabled, display "GAME OVER" using maze-specific color
            NES_ColorScheme nesColorScheme = gameLevel.worldMap().getConfigValue("nesColorScheme");
            Color color = Color.web(nesColorScheme.strokeColorRGB());
            scores3D.showTextForScore(ui.theAssets().text("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}