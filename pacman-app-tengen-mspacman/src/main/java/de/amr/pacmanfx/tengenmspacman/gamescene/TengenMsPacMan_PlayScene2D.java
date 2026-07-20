/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessage;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
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

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay.SCROLLING;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends AbstractGameScene2D
    implements GameEventHandlerMixin
{
    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SCREEN_HEIGHT);

    private final StackPane rootPane = new StackPane();
    private final SubScene subScene;

    private final PerspectiveCamera fixedCamera = new PerspectiveCamera(false);
    private final PlayScene2DCamera dynamicCamera;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(GameAppContext appContext) {
        super(appContext);

        dynamicCamera = new PlayScene2DCamera();
        dynamicCamera.scalingProperty().bind(scalingProperty());

        rootPane.backgroundProperty().bind(appContext.ui().viewModel().common2D.canvasBackgroundColorProperty.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter.
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(appContext.ui().viewModel().common2D.canvasBackgroundColorProperty);
        subScene.heightProperty().addListener((_, _, _) -> updateScaling());

        final var uiSettings = tengenUISettings();

        subScene.cameraProperty().bind(uiSettings.playSceneDisplay.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((_, _, _) -> updateScaling());

        scalingProperty().addListener((_, _, _) -> gameModel().optLevel().ifPresent(level ->
            dynamicCamera.updateRange(level.worldMap().terrainLayer())));

        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        // Default height. Varies with map size.
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
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
    public TengenMsPacMan_GameModel gameModel() {
        return (TengenMsPacMan_GameModel) super.gameModel();
    }

    @Override
    public void onEnteredFrom3DScene() {
        gameContext().hudState().showLevelCounter().showLivesCounter().show();
        gameModel().optLevel().ifPresent(this::acceptGameLevel);
    }

    @Override
    public void setCanvas(Canvas canvas) {
        super.setCanvas(canvas);
        canvas.widthProperty() .bind(scalingProperty().multiply(NES_SCREEN_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));
        rootPane.getChildren().setAll(canvas);
    }

    @Override
    public void onActivate() {
        final HUDState hud = gameContext().hudState();
        hud.showScore().showLevelCounter().showLivesCounter().show();
        if (gameModel().allOptionsHaveDefaultValue()) {
            hud.hideGameOptions();
        } else {
            hud.showGameOptions();
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
    public void onTick(FrameContext frame) {
        gameModel().optLevel().ifPresent(level -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final int numRows = terrain.numRows();
            canvasHeightUnscaled.set(tilesPx(numRows + 2)); // 2 additional rows for level counter below maze
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(tilesPx(terrain.numRows()), level.entities().pac());
            }
            if (!level.isDemoLevel()) {
                updateLevelMessage(level);
            }
            ensureActorAnimationsCreated(level);
            updateHUD(level);
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!level.isDemoLevel());
                soundEffects.playAmbientGameLevelSound(gameContext(), level);
            });
        });
    }

    @Override
    public void handleQuit(GameAppContext appContext) {
        final GameContext gameContext = gameContext();
        onDeactivate();
        gameFlow().enterState(gameContext, GameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final var uiSettings = tengenUISettings();

        final TranslationManager translations = appContext().ui().translations();
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
        addLocalizedCheckBox(contextMenu, translations, gameContext().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(contextMenu, translations, gameContext().cheats().pacImmuneProperty(), "context_menu.immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, appContext().ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(contextMenu, translations, appContext().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public void acceptGameLevel(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i size = terrain.sizeInPixel();

        unscaledWidthProperty().set(size.x());
        unscaledHeightProperty().set(size.y());

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

    private TengenMsPacMan_Actions tengenActions() {
        return appContext().getExtensionValue(
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);
    }

    private TengenMsPacMan_UISettings tengenUISettings() {
        return appContext().getExtensionValue(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    private void acceptNormalLevel() {
        appContext().ui().sounds().setEnabled(true); //TODO needed?

        final var actions = tengenActions();

        // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
        actionBindings().registerAllBindings(actions.steeringBindings());
        actionBindings().registerAllBindings(appContext().commonActions().cheatActions().bindings());
        actionBindings().selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionTogglePacBooster(), actions.localBindings());
    }

    private void acceptDemoLevel() {
        appContext().ui().sounds().setEnabled(false); //TODO needed?

        final var actions = tengenActions();
        actionBindings().selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
    }

    private void updateScaling() {
        final var uiSettings = tengenUISettings();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();

        scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SCREEN_HEIGHT;
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    private void updateHUD(GameLevel level) {
        final HUDState hud = gameContext().hudState();

        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameState())
            && !level.entities().pac().isVisible();
        final int displayed = oneExtra ? gameModel().lifeCount() : gameModel().lifeCount() - 1;

        final int visibleLives = Math.clamp(displayed, 0, hud.maxLivesShown());
        hud.setLivesCount(visibleLives);
        if (gameModel().mapCategory() == MapCategory.ARCADE) {
            hud.hideLevelNumber();
        } else {
            hud.showLevelNumber();
        }
    }

    private void updateLevelMessage(GameLevel level) {
        if (level.optMessage().isPresent() && level.optMessage().get() instanceof MovingGameLevelMessage message) {
            message.updateMovement();
        }
    }

    void playLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> gameState().triggerTimeout());
        levelCompletedAnimation.play();
    }

    void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            final Font font = Font.font(BaseRenderer.ARCADE_FONT.getFamily(), TS);
            final double width = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, font);
            movingMessage.startMovement(unscaledWidth(), width);
        }
    }

    private void ensureActorAnimationsCreated(GameLevel level) {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext().ui().sprites().animations();

        final Pac pac = level.entities().pac();
        if (pac.animations().isEmpty()) {
            pac.setAnimations(renderConfig.createPacAnimations(animationContainer));
            resetPacAnimation(pac);
        }

        level.entities().ghosts().forEach(ghost -> {
            if (ghost.animations().isEmpty()) {
                ghost.setAnimations(renderConfig.createGhostAnimations(animationContainer, ghost.personality()));
                resetGhostAnimation(ghost);
            }
        });
    }

    void resetActorAnimations(GameLevel level) {
        resetPacAnimation(level.entities().pac());
        level.entities().ghosts().forEach(this::resetGhostAnimation);
    }

    private void resetPacAnimation(Pac pac) {
        pac.animations().select(gameModel().isBoosterActive()
            ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER
            : ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();
    }

    private void resetGhostAnimation(Ghost ghost) {
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        ghost.animations().resetSelected();
    }
}