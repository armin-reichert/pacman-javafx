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
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui._3d.Bonus3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameAction.TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.alt;
import static de.amr.pacmanfx.uilib.input.Keyboard.control;

public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    @Override
    public void bindActions() {
        bind(GameAction.PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT));
        bind(GameAction.PERSPECTIVE_NEXT, alt(KeyCode.RIGHT));
        bind(GameAction.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        if (optionalGameLevel().isPresent() && theGameLevel().isDemoLevel()) {
            bind(TengenMsPacMan_GameAction.QUIT_DEMO_LEVEL, theJoypad().key(JoypadButton.START));
        }
        else {
            bind(GameAction.PLAYER_UP,    theJoypad().key(JoypadButton.UP),    control(KeyCode.UP));
            bind(GameAction.PLAYER_DOWN,  theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN));
            bind(GameAction.PLAYER_LEFT,  theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT));
            bind(GameAction.PLAYER_RIGHT, theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT));
            bind(TOGGLE_PAC_BOOSTER, theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B));
            bindCheatActions();
        }
        enableActionBindings();
    }

    @Override
    protected void replaceGameLevel3D(GameLevel level) {
        super.replaceGameLevel3D(level);

        // At the bottom of the 3D maze, display level number boxes, maze category and difficulty as in 2D view
        var tengenGame = (TengenMsPacMan_GameModel) theGame();
        if (!tengenGame.optionsAreInitial()) {
            int imgWidth = level.worldMap().numCols() * TS, imgHeight = 2 * TS;

            ImageView imageView = new ImageView();
            imageView.setFitWidth(imgWidth);
            imageView.setFitHeight(imgHeight);
            imageView.setTranslateY((level.worldMap().numRows() - 2) * TS);
            imageView.setTranslateZ(-level3D.floorThickness());

            float quality = 5; // scale 5x for better quality of snapshot
            var canvas = new Canvas(quality * imgWidth, quality * imgHeight);
            canvas.getGraphicsContext2D().setImageSmoothing(false); // important!

            var r = (TengenMsPacMan_Renderer2D) theUIConfig().current().createRenderer(canvas);
            r.setScaling(quality);
            r.ctx().setFill(level3D.floorColor());
            r.ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            r.drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(), 0.5 * imgWidth, TS + HTS);
            r.drawLevelNumberBox(level.number(), 0, 0);
            r.drawLevelNumberBox(level.number(), imgWidth - 2 * TS, 0);

            imageView.setImage(canvas.snapshot(null, null));
            level3D.root().getChildren().add(imageView);
        }
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
            optionalGameLevel().ifPresent(level -> {
                NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                scores3D.showTextAsScore(theAssets().text("score.game_over"), Color.web(nesColorScheme.strokeColor()));
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        optionalGameLevel().flatMap(GameLevel::bonus)
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