/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.score.Score;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d3.GameLevel3D;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.gamescene.d3.entities.Maze3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameAppContext actionContext) {
        super(actionContext);
    }

    @Override
    public TengenMsPacMan_GameModel gameModel() {
        final GameModel gameModel = super.gameModel();
        if (!(gameModel instanceof TengenMsPacMan_GameModel tengenModel)) {
            throw new IllegalStateException("Expected Tengen game model, but found class %s"
                .formatted(gameModel.getClass().getSimpleName()));
        }
        return tengenModel;
    }

    @Override
    protected void decorate(GameLevel3D level3D) {
        // If any of the default level settings has been changed, display the level info
        gameModel().optLevel().ifPresent(_ -> {
            if (!gameModel().allOptionsHaveDefaultValue()) {
                final ImageView levelInfo = createLevelInfoView(level3D);
                level3D.getChildren().add(levelInfo);
            }
        });
    }

    private ImageView createLevelInfoView(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final ImageView levelInfo = new ImageView();
        final double infoWidth = tilesPx(level.worldMap().numCols());
        final double infoHeight = tilesPx(2);
        levelInfo.setFitWidth(infoWidth);
        levelInfo.setFitHeight(infoHeight);
        levelInfo.imageProperty().bind(actionContext().ui().viewModel().maze3D.floorColorProperty.map(
            color -> createLevelInfoImage(level.number(), infoWidth, infoHeight, color)));
        // Display the level info at front side of floor just over the surface
        final Maze3D maze3D = level3D.maze3D();
        levelInfo.setTranslateY(maze3D.floor().getHeight() - levelInfo.getFitHeight());
        levelInfo.setTranslateZ(-maze3D.floor().getDepth());
        return levelInfo;
    }

    private Image createLevelInfoImage(int levelNumber, double width, double height, Color backgroundColor) {
        final TengenMsPacMan_GameModel gameModel = gameModel();

        final double quality = 6;
        final var canvas = new Canvas(quality * width, quality * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final var hudRenderer = new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setScaling(quality);
        hudRenderer.fillCanvas(backgroundColor);
        hudRenderer.drawLevelNumberBox(levelNumber, 0, 0);
        hudRenderer.drawLevelNumberBox(levelNumber, width - 2 * TS, 0);
        hudRenderer.drawGameOptions(
            gameModel.mapCategory(),
            gameModel.difficulty(),
            gameModel.pacBoosterMode(),
            0.5 * width, tilesPx(1.5f)
        );

        return canvas.snapshot(null, null);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings().dispose();

        final var actions = actionContext().getExtensionValue(
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        if (level.isDemoLevel()) {
            // In demo level, allow going back to options screen
            actionBindings().selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
        } else {
            actionBindings().registerAllBindings(actions.steeringBindings());
            actionBindings().selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
            actionBindings().registerAllBindings(actionContext().commonActions().cheatActions().bindings());
        }
        bindActions();

        Logger.info(actionBindings());
    }

    @Override
    public void updateHUD3D(GameLevel level) {
        optScores3D().ifPresent(scores3D -> {
            final Score score = level.gameModel().score(), highScore = level.gameModel().highScore();
            if (score.isEnabled()) {
                scores3D.showScore(score.points(), score.levelNumber());
            } else {
                scores3D.showTextForScore(actionContext().ui().translations().translate("score.game_over"),
                    Color.valueOf(NES_Palette.rgb(0x16)));
            }
            // Always show high score
            scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        });
    }
}