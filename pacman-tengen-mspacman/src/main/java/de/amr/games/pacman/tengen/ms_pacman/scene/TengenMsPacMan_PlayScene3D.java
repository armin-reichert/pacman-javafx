/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.ScoreManager;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.level.Bonus3D;
import de.amr.games.pacman.ui3d.scene3d.PlayScene3D;
import javafx.scene.Camera;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.action.GameActions2D.bindCheatActions;
import static de.amr.games.pacman.ui2d.action.GameActions2D.bindFallbackPlayerControlActions;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;

public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    protected void replaceGameLevel3D() {
        super.replaceGameLevel3D();

        MsPacManGameTengen game = (MsPacManGameTengen) context.game();
        if (game.hasDefaultOptionValues()) {
            return;
        }

        TileMap terrain = context.level().world().map().terrain();
        float scale = 10;
        int unscaledWidth = terrain.numCols() * TS;
        int unscaledHeight = TS;
        var canvas = new Canvas(scale * unscaledWidth, scale * unscaledHeight);

        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(level3D.floorColor());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        var renderer = (TengenMsPacMan_Renderer2D) context.gameConfiguration().createRenderer(context.assets(), canvas);
        renderer.setScaling(scale);
        renderer.drawGameOptionsInfo(unscaledWidth, HTS, (MsPacManGameTengen) context.game());

        Box settingsView = new Box(unscaledWidth, unscaledHeight, 0.05);
        var texture = new PhongMaterial();
        ImageView snap = new ImageView(canvas.snapshot(null, null));
        snap.setFitWidth(unscaledWidth);
        snap.setFitHeight(unscaledHeight);
        texture.setDiffuseMap(snap.getImage());

        settingsView.setMaterial(texture);
        settingsView.setTranslateX(unscaledWidth * 0.5);
        settingsView.setTranslateY(terrain.numRows() * TS - TS);
        settingsView.setTranslateZ(-HTS);

        level3D.getChildren().add(settingsView);
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        if (context.game().isDemoLevel()) {
            bind(TengenMsPacMan_GameActions.QUIT_DEMO_LEVEL, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_START));
        }
        else {
            bind(GameActions2D.PLAYER_UP,    context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_UP));
            bind(GameActions2D.PLAYER_DOWN,  context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_DOWN));
            bind(GameActions2D.PLAYER_LEFT,  context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_LEFT));
            bind(GameActions2D.PLAYER_RIGHT, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_RIGHT));
            bind(TengenMsPacMan_GameActions.TOGGLE_PAC_BOOSTER,
                context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_A),
                context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_B));
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    protected void updateScores() {
        ScoreManager manager = context.game().scoreManager();
        Score score = manager.score(), highScore = manager.highScore();

        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (manager.isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // when score is disabled, show text "game over"
            WorldMap worldMap = context.level().world().map();
            NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
            Color color = Color.valueOf(nesColorScheme.strokeColor());
            scores3D.showTextAsScore(GAME_OVER_TEXT, color);
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.level().bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.gameConfiguration().spriteSheet()));
        context.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        context.sound().stopBonusBouncingSound();
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        context.sound().stopBonusBouncingSound();
    }
}