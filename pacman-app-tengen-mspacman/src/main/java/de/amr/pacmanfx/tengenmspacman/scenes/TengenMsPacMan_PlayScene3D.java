/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.ActionBindingsManagerImpl;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
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
    protected void decorateGameLevel3D(GameLevel3D level3D) {
        final Game game = gameContext().game();
        if (!(game instanceof TengenMsPacMan_GameModel tengenGame)) {
            throw new IllegalStateException("Cannot use Tengen play scene 3D in game of class %s"
                .formatted(game.getClass().getSimpleName()));
        }
        // If any of the default level settings has been changed, display the level info
        tengenGame.optGameLevel().ifPresent(level -> {
            if (!tengenGame.allOptionsDefault()) {
                final ImageView levelInfo = new ImageView();
                final double infoWidth = TS(level.worldMap().numCols());
                final double infoHeight = TS(2);
                levelInfo.setFitWidth(infoWidth);
                levelInfo.setFitHeight(infoHeight);
                levelInfo.imageProperty().bind(PROPERTY_3D_FLOOR_COLOR.map(
                    color -> createLevelInfoImage(tengenGame, level.number(), infoWidth, infoHeight, color)));
                // Display the level info at front side of floor just over the surface
                final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
                levelInfo.setTranslateY(maze3D.floor().getHeight() - levelInfo.getFitHeight());
                levelInfo.setTranslateZ(-maze3D.floor().getDepth());
                level3D.getChildren().add(levelInfo);
            }
        });
    }

    private Image createLevelInfoImage(TengenMsPacMan_GameModel game, int levelNumber, double width, double height, Color backgroundColor) {
        final double quality = 6;
        final var canvas = new Canvas(quality * width, quality * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setScaling(quality);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBoosterMode(), 0.5 * width, TS(1.5f));

        return canvas.snapshot(null, null);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings = new ActionBindingsManagerImpl();
        if (level.isDemoLevel()) {
            // In demo level, allow going back to options screen
            actionBindings.registerOne(ACTION_QUIT_DEMO_LEVEL, TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS);
        } else {
            actionBindings.registerAll(TengenMsPacMan_ActionBindings.STEERING_BINDINGS);
            actionBindings.registerOne(ACTION_TOGGLE_PAC_BOOSTER, TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS);
            actionBindings.registerAll(GameUI.CHEAT_ACTION_BINDINGS);
        }
        bindPlaySceneActions();

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