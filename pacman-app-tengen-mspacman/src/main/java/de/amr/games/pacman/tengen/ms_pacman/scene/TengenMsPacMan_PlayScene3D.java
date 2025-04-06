/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameAction;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui._3d.level.Bonus3D;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.*;
import static de.amr.games.pacman.uilib.Keyboard.alt;
import static de.amr.games.pacman.uilib.Keyboard.control;

public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();
        game().level().ifPresent(level -> {
            TengenMsPacMan_GameModel tengenMsPacManGame = game();
            if (!tengenMsPacManGame.hasDefaultOptionValues()) {
                addGameOptionsArea(tengenMsPacManGame, level);
            }
        });
    }

    // displays level number boxes, maze category and difficulty as in 2D view at the bottom of the 3D maze
    private void addGameOptionsArea(TengenMsPacMan_GameModel game, GameLevel level) {
        int imageWidth = level.worldMap().numCols() * TS;
        int imageHeight = 2 * TS;

        ImageView imageView = new ImageView();
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
        imageView.setTranslateY((level.worldMap().numRows() - 2) * TS);
        imageView.setTranslateZ(-level3D.floorThickness());

        float quality = 5; // scale 5x for better quality of snapshot
        var canvas = new Canvas(quality * imageWidth, quality * imageHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!
        var r2D = (TengenMsPacMan_Renderer2D) THE_UI.gameUIConfigManager().current().createRenderer(canvas);
        r2D.setScaling(quality);
        r2D.ctx().setFill(level3D.floorColor());
        r2D.ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        r2D.drawGameOptions(game, 0.5 * imageWidth, TS + HTS);
        r2D.drawLevelNumberBox(level.number(), 0, 0);
        r2D.drawLevelNumberBox(level.number(), imageWidth - 2 * TS, 0);

        imageView.setImage(canvas.snapshot(null, null));
        level3D.getChildren().add(imageView);
    }

    @Override
    public void bindGameActions() {
        JoypadKeyBinding joypad = THE_KEYBOARD.currentJoypadKeyBinding();
        bind(GameAction.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameAction.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        bind(GameAction.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        if (game().isDemoLevel()) {
            bind(TengenMsPacMan_GameAction.QUIT_DEMO_LEVEL, joypad.key(NES_JoypadButtonID.START));
        }
        else {
            bind(GameAction.PLAYER_UP,    joypad.key(NES_JoypadButtonID.UP),    control(KeyCode.UP));
            bind(GameAction.PLAYER_DOWN,  joypad.key(NES_JoypadButtonID.DOWN),  control(KeyCode.DOWN));
            bind(GameAction.PLAYER_LEFT,  joypad.key(NES_JoypadButtonID.LEFT),  control(KeyCode.LEFT));
            bind(GameAction.PLAYER_RIGHT, joypad.key(NES_JoypadButtonID.RIGHT), control(KeyCode.RIGHT));
            bind(TengenMsPacMan_GameAction.TOGGLE_PAC_BOOSTER, joypad.key(NES_JoypadButtonID.A), joypad.key(NES_JoypadButtonID.B));
            bindCheatActions();
        }
        enableActionBindings(THE_KEYBOARD);
    }

    @Override
    protected void updateScores() {
        Score score = game().scoreManager().score(), highScore = game().scoreManager().highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (game().scoreManager().isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // score is disabled, show text "GAME OVER" instead, use maze-specific color
            game().level().ifPresent(level -> {
                NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                scores3D.showTextAsScore(THE_ASSETS.text("score.game_over"), Color.web(nesColorScheme.strokeColor()));
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        game().level().flatMap(GameLevel::bonus)
                .ifPresent(bonus -> level3D.replaceBonus3D(bonus, THE_UI.gameUIConfigManager().current().spriteSheet()));
        THE_SOUND.playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        THE_SOUND.stopBonusBouncingSound();
        THE_SOUND.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        THE_SOUND.stopBonusBouncingSound();
    }
}