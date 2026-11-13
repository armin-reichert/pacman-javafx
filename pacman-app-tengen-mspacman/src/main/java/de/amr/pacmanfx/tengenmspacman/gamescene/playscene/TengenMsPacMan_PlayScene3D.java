/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.input.Joypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.ui.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.playscene.GameLevelView3D;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import de.amr.pacmanfx.ui.gamescene.playscene.WorldMapView3D;
import de.amr.pacmanfx.ui.viewmodel.Maze3DSettingsViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.basics.Tile.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.streamOfViews;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    private static final double UPSCALING = 6;

    public TengenMsPacMan_PlayScene3D() {}

    @Override
    public void replaceActionBindings(GameSession session, GameLevel level) {
        final var actions = TengenMsPacMan_Actions.instance();
        final Joypad joypad = engine.input().joypad();

        actionBindingsRegistry().dispose();

        if (session.booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
            // In demo level, allow going back to options screen
            actionBindingsRegistry().selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings(joypad));
        }
        else {
            actionBindingsRegistry().registerAllBindings(actions.steeringBindings(joypad));
            actionBindingsRegistry().selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings(joypad));
            actionBindingsRegistry().registerAllBindings(CommonGameActions.instance().cheatActions().bindings());
        }
        registerActionBindings();

        Logger.info(actionBindingsRegistry());
    }

    @Override
    protected void addAdditional3DLevelElements(GameLevelView3D level3D) {
        engine().optCurrentGame().ifPresent(game -> game.session().optLevel().ifPresent(level -> {
            if (!gameOptionValues(game.session()).areInitial()) {
                final Maze3DSettingsViewModel maze3DSettingsViewModel = engine.ui().viewModel().maze3DSettings();
                final ImageView imageView = createLevelInfoView(
                    game,
                    level,
                    level3D.maze3D(),
                    maze3DSettingsViewModel.floorColorProperty(),
                    uiConfig().renderConfig()
                );
                level3D.root().getChildren().add(imageView);
            }
        }));
    }

    private static ImageView createLevelInfoView(
        GameContext game,
        GameLevel level,
        WorldMapView3D maze3D,
        ObjectProperty<Color> floorColorProperty,
        GameVariantRenderConfig renderConfig)
    {
        final int width = level.worldMap().numCols() * TS;
        final int height = 2 * TS;

        final ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        imageView.imageProperty().bind(floorColorProperty.map(
            color -> createLevelInfoImage(
                level.number(),
                game.session(),
                width,
                height,
                color,
                renderConfig,
                game.systems().actorSpriteAnimController()))
        );

        // Display the level info at front side of floor just over the surface
        imageView.setTranslateY(maze3D.floor3D().getHeight() - imageView.getFitHeight());
        imageView.setTranslateZ(-maze3D.floor3D().getDepth());

        return imageView;
    }

    private static Image createLevelInfoImage(
        int levelNumber,
        GameSession session,
        double width,
        double height,
        Color backgroundColor,
        GameVariantRenderConfig renderConfig,
        ActorSpriteAnimController animController)
    {
        final var canvas = new Canvas(UPSCALING * width, UPSCALING * height);
        canvas.getGraphicsContext2D().setImageSmoothing(false); // important for crisp image!

        final HUD hud = new HUD();

        final GameOptionsDisplay optionsDisplay = new GameOptionsDisplay();
        optionsDisplay.options().setBoosterMode(gameOptionValues(session).boosterMode());
        optionsDisplay.options().setDifficulty(gameOptionValues(session).difficulty());
        optionsDisplay.options().setMapCategory(gameOptionValues(session).mapCategory());
        optionsDisplay.position().set(0.5 * width, 1.5f * TS);
        optionsDisplay.show();

        final LevelNumberDisplay leftNumberDisplay = new LevelNumberDisplay();
        leftNumberDisplay.levelNumber().setNumber(levelNumber);
        leftNumberDisplay.position().set(0, 0);
        leftNumberDisplay.show();

        final LevelNumberDisplay rightNumberDisplay = new LevelNumberDisplay();
        rightNumberDisplay.levelNumber().setNumber(levelNumber);
        rightNumberDisplay.position().set(width - 2 * TS, 0);
        rightNumberDisplay.show();

        hud.additionalEntities().addAll(optionsDisplay, leftNumberDisplay, rightNumberDisplay);

        final Renderer renderer = renderConfig.createVariantRenderer(animController, canvas);
        renderer.setScaling(UPSCALING);
        renderer.fillCanvas(backgroundColor);

        // Note: the HUD entities above do not need a HUD style so we don't set one
        streamOfViews(hud.allEntities(), RenderingLayer.HUD).forEach(view -> renderer.render(view, 0));

        return canvas.snapshot(null, null);
    }
}