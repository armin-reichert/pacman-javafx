/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui._2d.GameRenderer.fillCanvas;

/**
 * The 3D play scene of Tengen Ms. Pac-Man. Differs slightly from the Arcade version, e.g. the
 * action bindings differ or additional information is displayed (difficulty, maze category etc.)
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameUI ui) {
        super(ui);
    }

    @Override
    protected void setActionBindings() {
        // if demo level is running, allow going back to options screen
        if (gameContext.optGameLevel().isPresent() && gameContext.theGameLevel().isDemoLevel()) {
            actionBindings.bind(ACTION_QUIT_DEMO_LEVEL, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        } else {
            setPlayerSteeringActionBindings();
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_TOGGLE_PAC_BOOSTER, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        }
        actionBindings.bind(ACTION_PERSPECTIVE_PREVIOUS, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_PERSPECTIVE_NEXT, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TOGGLE_DRAW_MODE, GLOBAL_ACTION_BINDINGS);

        actionBindings.update();
    }

    @Override
    protected void setPlayerSteeringActionBindings() {
        actionBindings.bind(ACTION_STEER_UP, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_DOWN, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_LEFT, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_RIGHT, TENGEN_MS_PACMAN_ACTION_BINDINGS);
    }

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();
        var tengenGame = (TengenMsPacMan_GameModel) gameContext.theGame();
        if (!tengenGame.optionsAreInitial()) {
            ImageView infoView = createGameInfoView(tengenGame, gameContext.theGameLevel());
            gameLevel3D.getChildren().add(infoView);
        }
    }

    private ImageView createGameInfoView(TengenMsPacMan_GameModel game, GameLevel level) {
        final int infoWidth = level.worldMap().numCols() * TS, infoHeight = 2 * TS;
        final float quality = 5; // scale for better snapshot resolution

        var canvas = new Canvas(quality * infoWidth, quality * infoHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!
        fillCanvas(canvas, theUI().PY_3D_FLOOR_COLOR().get());

        var r = (TengenMsPacMan_GameRenderer) theUI().theUIConfiguration().createGameRenderer(canvas);
        r.setScaling(quality);
        r.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * infoWidth, TS + HTS);
        r.drawLevelNumberBox(level.number(), 0, 0);
        r.drawLevelNumberBox(level.number(), infoWidth - 2 * TS, 0);

        ImageView infoView = new ImageView(canvas.snapshot(null, null));
        infoView.setFitWidth(infoWidth);
        infoView.setFitHeight(infoHeight);
        infoView.setTranslateY((level.worldMap().numRows() - 2) * TS);
        infoView.setTranslateZ(-gameLevel3D.floorThickness());
        return infoView;
    }

    @Override
    protected void updateScores(GameLevel gameLevel) {
        final Score score = gameContext.theGame().score(), highScore = gameContext.theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            // if score is disabled, display "GAME OVER" using maze-specific color
            NES_ColorScheme nesColorScheme = gameLevel.worldMap().getConfigValue("nesColorScheme");
            Color color = Color.web(nesColorScheme.strokeColorRGB());
            scores3D.showTextAsScore(theUI().theAssets().text("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        gameContext.optGameLevel().flatMap(GameLevel::bonus).ifPresent(bonus -> gameLevel3D.updateBonus3D(bonus));
        theUI().theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
        theUI().theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
    }
}