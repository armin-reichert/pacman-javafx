/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplay.SCROLLING;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SCREEN_HEIGHT);

    private final StackPane rootPane = new StackPane();
    private final SubScene subScene;

    private final PerspectiveCamera fixedCamera = new PerspectiveCamera(false);
    private final PlayScene2DCamera dynamicCamera;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(Game game) {
        super(game);

        dynamicCamera = new PlayScene2DCamera();
        dynamicCamera.scalingProperty().bind(scalingProperty());

        rootPane.backgroundProperty().bind(game.ui().viewModel().d2.canvasBackgroundColorProperty.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter.
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(game.ui().viewModel().d2.canvasBackgroundColorProperty);
        subScene.heightProperty().addListener((_, _, _) -> updateScaling());

        final var uiSettings = game().extensions().get(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

        subScene.cameraProperty().bind(uiSettings.playSceneDisplay.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((_, _, _) -> updateScaling());

        scalingProperty().addListener((_, _, _) -> gameContext().optCurrentLevel().ifPresent(level ->
            dynamicCamera.updateRange(level.worldMap().terrainLayer())));

        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        // Default height. Varies with map size.
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        setGameEventHandler(new TengenMsPacMan_PlayScene2DGameEventHandler(this));
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
    public TengenMsPacMan_GameModel gameModel() {
        return (TengenMsPacMan_GameModel) super.gameModel();
    }

    @Override
    public void onEnteredFrom3DScene() {
        gameModel().hud().levelCounterOn().livesCounterOn().show();
        gameModel().optGameLevel().ifPresent(this::acceptGameLevel);
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        canvas.widthProperty() .bind(scalingProperty().multiply(NES_SCREEN_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));
        rootPane.getChildren().setAll(canvas);
    }

    @Override
    public void onActivate() {
        final TengenMsPacMan_HUDState hud = gameModel().hud();
        hud.scoreOn().levelCounterOn().livesCounterOn().show();
        if (gameModel().allOptionsDefault()) {
            hud.gameOptionsOff();
        } else {
            hud.gameOptionsOn();
        }

        updateScaling();

        dynamicCamera.enterManualMode();
        dynamicCamera.setToTopPosition();
    }

    @Override
    public void onDeactivate() {
        dynamicCamera.enterManualMode();
    }

    @Override
    public void onTick(long tick) {
        gameModel().optGameLevel().ifPresent(level -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final int numRows = terrain.numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter below maze
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(TS(terrain.numRows()), level.entities().pac());
            }
            updateDemoLevelMessage(level);
            updateHUD(level);
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!level.isDemoLevel());
                soundEffects.playAmbientGameLevelSound(gameContext(), level);
            });
        });
    }

    @Override
    public void handleQuit(Game game) {
        if (gameModel().isPlaying()) {
            gameContext().optCurrentLevel().ifPresent(level -> gameModel().onGameOver(gameContext(), level));
        }
        gameModel().cheats().clear();
        gameModel().lives().setCount(0);
        onDeactivate();
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final var uiSettings = game().extensions().get(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

        final TranslationManager translations = game().ui().translations();
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
        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacImmuneProperty(), "context_menu.immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, game().ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(contextMenu, translations, game().actions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public void acceptGameLevel(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i terrainSize = terrain.sizeInPixel();

        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        dynamicCamera.enterTrackingMode();
        dynamicCamera.updateRange(terrain);

        if (level.isDemoLevel()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel();
        }

        Logger.info(actionBindings());
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void acceptNormalLevel() {
        game().ui().sounds().setEnabled(true); //TODO needed?

        final var actions = game().extensions().get(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
        actionBindings().registerAllBindings(actions.steeringBindings());
        actionBindings().registerAllBindings(game().actions().cheatActions().bindings());
        actionBindings().selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
    }

    private void acceptDemoLevel() {
        game().ui().sounds().setEnabled(false); //TODO needed?

        final var actions = game().extensions().get(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        actionBindings().selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
    }

    private void updateScaling() {
        final var uiSettings = game().extensions().get(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();

        scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SCREEN_HEIGHT;
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    private void updateHUD(GameLevel level) {
        final TengenMsPacMan_HUDState hud = gameModel().hud();

        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameState())
            && !level.entities().pac().isVisible();
        final int displayed = oneExtra ? gameModel().lives().count() : gameModel().lives().count() - 1;

        final int visibleLives = Math.clamp(displayed, 0, gameModel().hud().maxLivesDisplayed());
        hud.setVisibleLifeCount(visibleLives);
        if (gameModel().mapCategory() == MapCategory.ARCADE) {
            hud.levelNumberOff();
        } else {
            hud.levelNumberOn();
        }
    }

    private void updateDemoLevelMessage(GameLevel level) {
        if (level.isDemoLevel()) {
            level.optMessage()
                .filter(MovingGameLevelMessage.class::isInstance)
                .map(MovingGameLevelMessage.class::cast)
                .ifPresent(MovingGameLevelMessage::updateMovement);
        }
    }

    void playLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> gameState().expire());
        levelCompletedAnimation.play();
    }

    void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            final Font font = Font.font(BaseRenderer.ARCADE_FONT.getFamily(), TS);
            final double width = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, font);
            movingMessage.startMovement(unscaledWidth(), width);
        }
    }

    void resetAnimations(GameLevel level) {
        final Pac pac = level.entities().pac();

        pac.animations().select(gameModel().isBoosterActive()
            ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();

        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().resetSelected();
        });
    }
}