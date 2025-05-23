/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.ui._3d.Bonus3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameAction.QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameAction.TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * The 3D play scene of Tengen Ms. Pac-Man. Differs slightly from the Arcade version, e.g. the
 * action bindings differ or additional information is displayed (difficulty, maze category etc.)
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    @Override
    protected void bindActions() {
        // if demo level is running, allow going back to options screen
        if (optGameLevel().isPresent() && theGameLevel().isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, theJoypad().key(JoypadButton.START));
        } else {
            bindPlayerSteeringActions();
            bindCheatActions();
            bind(TOGGLE_PAC_BOOSTER, theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B));
        }
        bindScene3DActions();

        updateActionBindings();
    }

    @Override
    protected void bindPlayerSteeringActions() { bindJoypadPlayerSteeringActions(); }

    @Override
    protected void replaceGameLevel3D(GameLevel level) {
        super.replaceGameLevel3D(level);
        var tengenGame = (TengenMsPacMan_GameModel) theGame();
        if (!tengenGame.optionsAreInitial()) {
            ImageView infoView = createGameInfoView(tengenGame, level);
            level3D.root().getChildren().add(infoView);
        }
    }

    private ImageView createGameInfoView(TengenMsPacMan_GameModel game, GameLevel level) {
        final int infoWidth = level.worldMap().numCols() * TS, infoHeight = 2 * TS;
        final float quality = 5; // scale for better snapshot resolution

        var canvas = new Canvas(quality * infoWidth, quality * infoHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!

        var r = (TengenMsPacMan_Renderer2D) theUIConfig().current().createRenderer(canvas);
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
        ScoreManager scoreManager = theGame().scoreManager();
        Score score = scoreManager.score(), highScore = scoreManager.highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // score is disabled, show text "GAME OVER" instead, use maze-specific color
            optGameLevel().ifPresent(level -> {
                NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                scores3D.showTextAsScore(theAssets().text("score.game_over"), Color.web(nesColorScheme.strokeColor()));
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        optGameLevel().flatMap(GameLevel::bonus)
                .ifPresent(bonus -> level3D.updateBonus3D(bonus, theUIConfig().current().spriteSheet()));
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