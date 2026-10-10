/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.playscene.GameLevelView3D;
import de.amr.pacmanfx.ui.gamescene.playscene.WorldMapView3D;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.TileDimension.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.streamOfViews;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    protected void addAdditional3DLevelElements(GameLevelView3D level3D) {
        final GameSession session = engineContext().currentGame().session();
        session.optLevel().ifPresent(_ -> {
            if (!gameOptionValues(session).areInitial()) {
                final ImageView levelInfo = createLevelInfoView(level3D);
                level3D.root().getChildren().add(levelInfo);
            }
        });
    }

    private ImageView createLevelInfoView(GameLevelView3D level3D) {
        final GameSession session = engineContext().currentGame().session();
        final GameLevel level = session.level();

        final ImageView levelInfo = new ImageView();
        final double infoWidth = tilesPx(level.worldMap().numCols());
        final double infoHeight = tilesPx(2);
        levelInfo.setFitWidth(infoWidth);
        levelInfo.setFitHeight(infoHeight);
        levelInfo.imageProperty().bind(ui().viewModel().maze3DSettings().floorColorProperty().map(
            color -> createLevelInfoImage(level.number(), session, infoWidth, infoHeight, color))
        );

        // Display the level info at front side of floor just over the surface
        final WorldMapView3D maze3D = level3D.maze3D();
        levelInfo.setTranslateY(maze3D.floor3D().getHeight() - levelInfo.getFitHeight());
        levelInfo.setTranslateZ(-maze3D.floor3D().getDepth());

        return levelInfo;
    }

    private Image createLevelInfoImage(
        int levelNumber,
        GameSession session,
        double width,
        double height,
        Color backgroundColor)
    {
        final double quality = 6;
        final var canvas = new Canvas(quality * width, quality * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final HUD hud = new HUD();

        final GameOptionsDisplay optionsDisplay = new GameOptionsDisplay();
        optionsDisplay.options().setBoosterMode(gameOptionValues(session).boosterMode());
        optionsDisplay.options().setDifficulty(gameOptionValues(session).difficulty());
        optionsDisplay.options().setMapCategory(gameOptionValues(session).mapCategory());
        optionsDisplay.pos().set(0.5 * width, 1.5f * TS);
        optionsDisplay.show();

        final LevelNumberDisplay leftNumberDisplay = new LevelNumberDisplay();
        leftNumberDisplay.levelNumber().setNumber(levelNumber);
        leftNumberDisplay.pos().set(0, 0);
        leftNumberDisplay.show();

        final LevelNumberDisplay rightNumberDisplay = new LevelNumberDisplay();
        rightNumberDisplay.levelNumber().setNumber(levelNumber);
        rightNumberDisplay.pos().set(width - 2 * TS, 0);
        rightNumberDisplay.show();

        hud.additionalEntities().addAll(optionsDisplay, leftNumberDisplay, rightNumberDisplay);

        final ActorSpriteAnimController animController = engineContext().currentGame().playConfig().systems().actorSpriteAnimController();
        final var renderer = engineContext().gameVariantManager().currentRuntime().uiConfig().renderConfig().createVariantRenderer(animController, canvas);
        renderer.setScaling(quality);
        renderer.fillCanvas(backgroundColor);

        // Note: the HUD entities above do not need a HUD style so we don't set one
        streamOfViews(hud.allEntities(), RenderingLayer.HUD).forEach(view -> renderer.render(view, 0));

        return canvas.snapshot(null, null);
    }

    @Override
    public void replaceActionBindings(GameSession session, GameLevel level) {
        actionBindingsRegistry().dispose();

        final var actions = engineContext().gameVariantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        if (session.isAttractMode()) {
            // In demo level, allow going back to options screen
            actionBindingsRegistry().selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
        } else {
            actionBindingsRegistry().registerAllBindings(actions.steeringBindings());
            actionBindingsRegistry().selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
            actionBindingsRegistry().registerAllBindings(CommonGameActions.instance().cheatActions().bindings());
        }
        registerActionBindings();

        Logger.info(actionBindingsRegistry());
    }

    @Override
    public void updateHUD3D(GameContext game) {
        optScoresView().ifPresent(scores3D -> {
            final GameSession session = game.session();
            final Score score = session.hud().gameScore(), highScore = session.hud().highScore();
            if (score.data().isEnabled()) {
                scores3D.showScore(score.data().points(), score.data().levelNumber());
            } else {
                scores3D.showTextForScore(engineContext().translationManager().translate("score.game_over"),
                    Color.valueOf(NES_Palette.rgb(0x16)));
            }
            // Always show high score
            scores3D.showHighScore(highScore.data().points(), highScore.data().levelNumber());
        });
    }
}