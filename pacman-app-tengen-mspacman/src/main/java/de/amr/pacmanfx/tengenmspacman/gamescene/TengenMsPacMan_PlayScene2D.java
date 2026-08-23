/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay.SCROLLING;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene implements TengenMsPacMan_PlayScene2D_GameEventHandler
{
    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SCREEN_HEIGHT);

    private final StackPane rootPane = new StackPane();
    private final SubScene subScene;

    private final PerspectiveCamera fixedCamera = new PerspectiveCamera(false);
    private final PlayScene2DCamera dynamicCamera;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(GameAppContext app) {
        super(app);
        components().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());

        dynamicCamera = new PlayScene2DCamera();

        rootPane.backgroundProperty().bind(app.ui().viewModel().common2D.canvasBackgroundColorProperty.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter.
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(app.ui().viewModel().common2D.canvasBackgroundColorProperty);
        subScene.heightProperty().addListener((_, _, _) -> updateScaling());

        final var uiSettings = uiSettings();

        subScene.cameraProperty().bind(uiSettings.playSceneDisplay.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((_, _, _) -> updateScaling());
    }

    @Override
    public TengenMsPacMan_PlayScene2D gameScene() {
        return this;
    }

    public double canvasHeightUnscaled() {
        return canvasHeightUnscaled.get();
    }

    public PlayScene2DCamera dynamicCamera() {
        return dynamicCamera;
    }

    public Optional<LevelCompletedAnimation> optLevelCompletedAnimation() {
        return Optional.ofNullable(levelCompletedAnimation);
    }

    @Override
    public void onEnteredFrom3DScene() {
        final GameSession session = game().session();
        session.hud().showLevelCounter().showLivesCounter().show();
        session.optLevel().ifPresent(level -> acceptGameLevel(session, level));
    }

    @Override
    public void onActivate() {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();
        final GameSession session = game().session();
        final HUDState hud = session.hud();

        hud.showScore().showLevelCounter().showLivesCounter().show();

        if (gamePlay.allOptionsHaveDefaultValue(session)) {
            hud.hideGameOptions();
        } else {
            hud.showGameOptions();
        }

        resetRendering2D();
        updateScaling();

        dynamicCamera.enterManualMode();
        dynamicCamera.setToTopPosition();
    }

    @Override
    public void onDeactivate() {
        dynamicCamera.enterManualMode();
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final int numRows = terrain.numRows();
            canvasHeightUnscaled.set(tilesPx(numRows + 2)); // 2 additional rows for level counter below maze
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(tilesPx(terrain.numRows()), level.entities().pac());
            }
            ensureActorAnimationsCreated(session, level);
            updateHUD(session, level);
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!session.isAttractMode());
                soundEffects.playAmbientGameLevelSound(game(), level);
            });
        });
    }

    @Override
    public void handleQuit(GameAppContext app) {
        onDeactivate();
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final var uiSettings = uiSettings();

        final TranslationManager translations = app().ui().translations();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();
        final var contextMenu = new ContextMenu();

        final RadioMenuItem miScaledToFit = addLocalizedRadioButton(contextMenu, translations, "context_menu.scaled_to_fit");
        miScaledToFit.setSelected(displayMode == SceneDisplay.SCALED_TO_FIT);
        miScaledToFit.setOnAction(_ -> uiSettings.playSceneDisplay.set(SceneDisplay.SCALED_TO_FIT));

        final RadioMenuItem miScrolling = addLocalizedRadioButton(contextMenu, translations, "context_menu.scrolling");
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(_ -> uiSettings.playSceneDisplay.set(SCROLLING));

        final ToggleGroup toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacImmuneProperty(), "context_menu.immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, app().ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(app(), contextMenu, translations, app().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public void acceptGameLevel(GameSession session, GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i size = terrain.sizeInPixel();

        reqCanvasRendering().unscaledWidthProperty().set(size.x());
        reqCanvasRendering().unscaledHeightProperty().set(size.y());

        dynamicCamera.enterTrackingMode();
        dynamicCamera.updateRange(terrain);

        if (session.isAttractMode()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel();
        }

        Logger.info(actionBindingsSupport());
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // private area, do NOT enter!

    private final ChangeListener<? super Number> scalingListener = (_, _, _) ->
        game().session().optLevel()
            .ifPresent(level -> dynamicCamera().updateRange(level.worldMap().terrainLayer()));

    private final ChangeListener<? super Canvas> canvasListener = (_, oldCanvas, newCanvas) -> {
        if (oldCanvas != null) {
            oldCanvas.widthProperty().unbind();
            oldCanvas.heightProperty().unbind();
            rootPane.getChildren().remove(oldCanvas);
        }
        newCanvas.widthProperty() .bind(reqCanvasRendering().scalingProperty().multiply(NES_SCREEN_WIDTH));
        newCanvas.heightProperty().bind(reqCanvasRendering().scalingProperty().multiply(canvasHeightUnscaled));
        rootPane.getChildren().add(newCanvas);
    };

    private void resetRendering2D() {
        // Replace component. TODO: Check why necessary
        components().removeComp(CanvasRenderingComp.class);
        components().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());

        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        // Default height. Varies with map size.
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        reqCanvasRendering().scalingProperty().addListener(scalingListener);
        reqCanvasRendering().canvasProperty().addListener(canvasListener);

        dynamicCamera.scalingProperty().bind(reqCanvasRendering().scalingProperty());
    }

    private TengenMsPacMan_Actions actions() {
        return app().currentGameVariantUIConfig()
            .extensionValue(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);
    }

    private TengenMsPacMan_UISettings uiSettings() {
        return app().currentGameVariantUIConfig()
            .extensionValue(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    private void acceptNormalLevel() {
        app().ui().sounds().setEnabled(true); //TODO needed?

        final var actions = actions();

        // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
        final var bindingsMap = actionBindingsSupport().bindingsMap();

        bindingsMap.registerAllBindings(actions.steeringBindings());
        bindingsMap.registerAllBindings(app().commonActions().cheatActions().bindings());

        bindingsMap.selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
    }

    private void acceptDemoLevel() {
        app().ui().sounds().setEnabled(false); //TODO needed?

        final var actions = actions();

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
    }

    private void updateScaling() {
        final var uiSettings = uiSettings();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();

        reqCanvasRendering().scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SCREEN_HEIGHT;
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), reqCanvasRendering().scaling());
    }

    private void updateHUD(GameSession session, GameLevel level) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game().variant().gamePlay();
        final HUDState hud = session.hud();
        final LivesCounter livesCounter = session.livesCounter();

        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = CommonGameStateID.GAME_OR_LEVEL_STARTING.hasSameNameAs(game().state())
            && !level.entities().pac().isVisible();
        final int numLives = livesCounter.data().numLives();
        final int displayed = oneExtra ? numLives : numLives - 1;

        final int visibleLives = Math.clamp(displayed, 0, hud.maxLivesShown());
        hud.setLivesCount(visibleLives);
        if (gamePlay.mapCategory(session) == MapCategory.ARCADE) {
            hud.hideLevelNumber();
        } else {
            hud.showLevelNumber();
        }
    }

    void playLevelCompleteAnimation(GameLevel level, int numFlashes) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> game().state().triggerTimeout());
        levelCompletedAnimation.play(numFlashes);
    }

    private void ensureActorAnimationsCreated(GameSession session, GameLevel level) {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.config().systems().actorSpriteAnimController();

        final Pac pac = level.entities().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, renderConfig.createPacAnimations(animContainer));
            resetPacAnimation(animController, session, pac);
        }

        level.entities().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost, renderConfig.createGhostAnimations(animContainer, ghost.personality()));
                resetGhostAnimation(animController, ghost);
            }
        });
    }
}