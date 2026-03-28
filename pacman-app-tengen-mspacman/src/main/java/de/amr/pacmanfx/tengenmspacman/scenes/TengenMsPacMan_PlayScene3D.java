/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.action.ActionBindingsManagerImpl;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.Maze3D;
import de.amr.pacmanfx.ui.d3.MazeFloor3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_FLOOR_COLOR;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    protected GameLevel3D createGameLevel3D(GameLevel level, UIConfig uiConfig) {
        if (!(level.game() instanceof TengenMsPacMan_GameModel game)) {
            throw new IllegalArgumentException("Game must be Tengen Ms. Pac-Man");
        }

        // Common stuff
        final var newLevel3D = new GameLevel3D(level, uiConfig, soundEffects, ui.localizedTexts());
        final Maze3D maze3D = newLevel3D.entities().first(Maze3D.class).orElseThrow();
        final MazeFloor3D floor3D = maze3D.floor();
        final Pac3D pac3D = newLevel3D.pac3D().orElseThrow();

        pac3D.init(level);
        newLevel3D.ghostAppearances3DInOrder().forEach(ghost3D -> ghost3D.init(level));
        newLevel3D.startTrackingPac();

        // Tengen-specific stuff: level info
        if (!game.allOptionsDefault()) {
            final double width = TS(level.worldMap().numCols());
            final double height = TS(2);
            final ImageView levelInfo = new ImageView();
            levelInfo.setFitWidth(width);
            levelInfo.setFitHeight(height);
            levelInfo.imageProperty().bind(PROPERTY_3D_FLOOR_COLOR
                .map(color -> createLevelInfo(game, level.number(), width, height, color)));

            // Display the level info at front side of floor just over the surface
            levelInfo.setTranslateY(floor3D.getHeight() - levelInfo.getFitHeight());
            levelInfo.setTranslateZ(-floor3D.getDepth());
            newLevel3D.getChildren().add(levelInfo);
        }

        return newLevel3D;
    }

    private Image createLevelInfo(TengenMsPacMan_GameModel game, int levelNumber, double width, double height, Color backgroundColor) {
        final double quality = 6;
        final var canvas = new Canvas(quality * width, quality * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setScaling(quality);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(), 0.5 * width, TS(1.5f));

        return canvas.snapshot(null, null);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings = new ActionBindingsManagerImpl();
        if (level.isDemoLevel()) {
            // In demo level, allow going back to options screen
            actionBindings.registerAnyFrom(ACTION_QUIT_DEMO_LEVEL, TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS);
        } else {
            actionBindings.registerAllFrom(TengenMsPacMan_ActionBindings.STEERING_BINDINGS);
            actionBindings.registerAnyFrom(ACTION_TOGGLE_PAC_BOOSTER, TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS);
            actionBindings.registerAllFrom(GameUI.CHEAT_BINDINGS);
        }
        bindSceneActions();

        actionBindings.addAll(GameUI.KEYBOARD);
    }

    @Override
    public void updateHUD3D(GameLevel level) {
        final Score score = level.game().score(), highScore = level.game().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            scores3D.showTextForScore(ui.translate("score.game_over"), Color.valueOf(NES_Palette.rgbColor(0x16)));
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}