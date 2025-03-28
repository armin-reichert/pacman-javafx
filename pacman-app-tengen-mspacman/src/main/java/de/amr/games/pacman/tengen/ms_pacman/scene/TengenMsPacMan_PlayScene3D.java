/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.ScoreManager;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._3d.GameActions3D;
import de.amr.games.pacman.ui._3d.level.Bonus3D;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.uilib.Keyboard.alt;

public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();
        TengenMsPacMan_GameModel game = THE_GAME_CONTROLLER.game();
        game.level().ifPresent(level -> {
            if (!game.hasDefaultOptionValues()) {
                addGameOptionsArea(game, level);
            }
        });
    }

    // displays level number boxes, maze category and difficulty as in 2D view at the bottom of the 3D maze
    private void addGameOptionsArea(TengenMsPacMan_GameModel game, GameLevel level) {
        WorldMap worldMap = level.worldMap();
        int imageWidth = worldMap.numCols() * TS;
        int imageHeight = 2 * TS;

        ImageView imageView = new ImageView();
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
        imageView.setTranslateY((worldMap.numRows() - 2) * TS);
        imageView.setTranslateZ(-level3D.floorThickness());
        level3D.getChildren().add(imageView);

        float quality = 5; // scale 5x for better quality of snapshot
        var canvas = new Canvas(quality * imageWidth, quality * imageHeight);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important!

        var renderer = (TengenMsPacMan_Renderer2D) THE_UI.configurations().current().createRenderer(canvas);
        renderer.setScaling(quality);
        renderer.ctx().setFill(level3D.floorColor());
        renderer.ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderer.drawGameOptionsInfoCenteredAt(0.5 * imageWidth, TS+HTS, game);
        renderer.drawLevelNumberBox(level.number(), 0, 0);
        renderer.drawLevelNumberBox(level.number(), imageWidth - 2*TS, 0);

        imageView.setImage(canvas.snapshot(null, null));
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        bind(GameActions3D.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        if (THE_GAME_CONTROLLER.game().isDemoLevel()) {
            bind(TengenMsPacMan_GameActions.QUIT_DEMO_LEVEL, THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.START));
        }
        else {
            bind(GameActions2D.PLAYER_UP,    THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.UP));
            bind(GameActions2D.PLAYER_DOWN,  THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.DOWN));
            bind(GameActions2D.PLAYER_LEFT,  THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.LEFT));
            bind(GameActions2D.PLAYER_RIGHT, THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.RIGHT));
            bind(TengenMsPacMan_GameActions.TOGGLE_PAC_BOOSTER,
                THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.A),
                THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.B));
            bindAlternativePlayerControlActions();
            bindCheatActions();
            bindAlternativePlayerControlActions();
            bindCheatActions();
        }
        enableActionBindings();
    }

    @Override
    protected void updateScores() {
        ScoreManager manager = THE_GAME_CONTROLLER.game().scoreManager();
        Score score = manager.score(), highScore = manager.highScore();

        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (manager.isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // when score is disabled, show text "game over"
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                Color color = Color.web(nesColorScheme.strokeColor());
                scores3D.showTextAsScore(TEXT_GAME_OVER, color);
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        THE_GAME_CONTROLLER.game().level().flatMap(GameLevel::bonus)
                .ifPresent(bonus -> level3D.replaceBonus3D(bonus, THE_UI.configurations().current().spriteSheet()));
        THE_UI.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        THE_UI.sound().stopBonusBouncingSound();
        THE_UI.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        THE_UI.sound().stopBonusBouncingSound();
    }
}