/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui._3d.Bonus3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_ActionBindings.TENGEN_DEFAULT_BINDING_MAP;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;

/**
 * The 3D play scene of Tengen Ms. Pac-Man. Differs slightly from the Arcade version, e.g. the
 * action bindings differ or additional information is displayed (difficulty, maze category etc.)
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    @Override
    protected void bindActions() {
        // if demo level is running, allow going back to options screen
        if (optGameLevel().isPresent() && theGameLevel().isDemoLevel()) {
            bindActionToKeys(QUIT_DEMO_LEVEL, TENGEN_DEFAULT_BINDING_MAP);
        } else {
            bindPlayerSteeringActions();
            bindActionToCommonKeys(CHEAT_EAT_ALL_PELLETS);
            bindActionToCommonKeys(CHEAT_ADD_LIVES);
            bindActionToCommonKeys(CHEAT_ENTER_NEXT_LEVEL);
            bindActionToCommonKeys(CHEAT_KILL_GHOSTS);
            bindActionToKeys(TOGGLE_PAC_BOOSTER, TENGEN_DEFAULT_BINDING_MAP);
        }
        bindActionToCommonKeys(PacManGames_Actions.PERSPECTIVE_PREVIOUS);
        bindActionToCommonKeys(PacManGames_Actions.PERSPECTIVE_NEXT);
        bindActionToCommonKeys(PacManGames_Actions.TOGGLE_DRAW_MODE);

        updateActionBindings();
    }

    @Override
    protected void bindPlayerSteeringActions() {
        bindActionToKeys(PLAYER_UP,    TENGEN_DEFAULT_BINDING_MAP);
        bindActionToKeys(PLAYER_DOWN,  TENGEN_DEFAULT_BINDING_MAP);
        bindActionToKeys(PLAYER_LEFT,  TENGEN_DEFAULT_BINDING_MAP);
        bindActionToKeys(PLAYER_RIGHT, TENGEN_DEFAULT_BINDING_MAP);
    }

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();
        var tengenGame = (TengenMsPacMan_GameModel) theGame();
        if (!tengenGame.optionsAreInitial()) {
            ImageView infoView = createGameInfoView(tengenGame, theGameLevel());
            level3D.root().getChildren().add(infoView);
        }
    }

    private ImageView createGameInfoView(TengenMsPacMan_GameModel game, GameLevel level) {
        final int infoWidth = level.worldMap().numCols() * TS, infoHeight = 2 * TS;
        final float quality = 5; // scale for better snapshot resolution

        var canvas = new Canvas(quality * infoWidth, quality * infoHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!

        var r = (TengenMsPacMan_Renderer2D) theUI().currentConfig().createRenderer(canvas);
        r.setScaling(quality);
        r.fillCanvas(level3D.floorColor());
        r.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * infoWidth, TS + HTS);
        r.drawLevelNumberBox(level.number(), 0, 0);
        r.drawLevelNumberBox(level.number(), infoWidth - 2 * TS, 0);

        ImageView infoView = new ImageView(canvas.snapshot(null, null));
        infoView.setFitWidth(infoWidth);
        infoView.setFitHeight(infoHeight);
        infoView.setTranslateY((level.worldMap().numRows() - 2) * TS);
        infoView.setTranslateZ(-level3D.floorThickness());
        return infoView;
    }

    @Override
    protected void updateScores() {
        final Score score = theGame().score(), highScore = theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER" using maze-specific color
            optGameLevel().ifPresent(level -> {
                NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                Color color = Color.web(nesColorScheme.strokeColor());
                scores3D.showTextAsScore(theAssets().text("score.game_over"), color);
            });
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        optGameLevel().flatMap(GameLevel::bonus)
                .ifPresent(bonus -> level3D.updateBonus3D(bonus, theUI().currentConfig().spriteSheet()));
        theSound().playBonusActiveSound();
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        theSound().stopBonusActiveSound();
        theSound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::expire);
        theSound().stopBonusActiveSound();
    }
}