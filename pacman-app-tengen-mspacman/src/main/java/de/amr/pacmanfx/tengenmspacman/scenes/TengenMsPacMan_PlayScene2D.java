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
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.game.GlobalActionBindings;
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

import static de.amr.pacmanfx.core.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.STEERING_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.game.GameConstants.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SCREEN_HEIGHT);

    private final StackPane rootPane;
    private final SubScene subScene;

    private final PlayScene2DCamera dynamicCamera;
    private final PerspectiveCamera fixedCamera;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(Game game) {
        super(game);

        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        setGameEventHandler(new TengenMsPacMan_PlayScene2DGameEventHandler(this));

        fixedCamera = new PerspectiveCamera(false);

        dynamicCamera = new PlayScene2DCamera();
        dynamicCamera.scalingProperty().bind(scalingProperty());

        rootPane = new StackPane();
        rootPane.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((_, _, _) -> updateScaling());
        subScene.heightProperty().addListener((_, _, _) -> updateScaling());

        scalingProperty().addListener((_, _, _) -> gameContext().optCurrentLevel().ifPresent(level ->
            dynamicCamera.updateRange(level.worldMap())));
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
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameModel();
        final TengenMsPacMan_HUDState hud = gameModel.hud();

        hud.scoreOn().levelCounterOn().livesCounterOn().show();
        if (gameModel.allOptionsDefault()) {
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
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameModel();
        gameModel.optGameLevel().ifPresent(level -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final int numRows = terrain.numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter below maze
            if (!level.isDemoLevel()) {
                // Update moving "game over" message if present
                level.optMessage()
                    .filter(MovingGameLevelMessage.class::isInstance)
                    .map(MovingGameLevelMessage.class::cast)
                    .ifPresent(MovingGameLevelMessage::updateMovement);
            }
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(TS(terrain.numRows()), level.entities().pac());
            }
            updateHUD(level);
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!level.isDemoLevel());
                soundEffects.playAmbientGameLevelSound(gameContext(), level);
            });
        });
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final TranslationManager translations = game().ui().translations();
        final SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
        final var contextMenu = new ContextMenu();

        final RadioMenuItem miScaledToFit = addLocalizedRadioButton(contextMenu, translations, "scaled_to_fit");
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(_ -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCALED_TO_FIT));

        final RadioMenuItem miScrolling = addLocalizedRadioButton(contextMenu, translations, "scrolling");
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(_ -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SCROLLING));

        final ToggleGroup toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        addLocalizedTitleItem(contextMenu, translations, "pacman");
        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacUsingAutopilotProperty(), "autopilot");
        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacImmuneProperty(), "immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, GameConstants.PROPERTY_MUTED, "muted");
        addLocalizedActionItem(contextMenu, game(), translations, ACTION_QUIT_GAME_SCENE, "quit");

        return Optional.of(contextMenu);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public void acceptGameLevel(GameLevel level) {
        dynamicCamera.enterTrackingMode();
        dynamicCamera.updateRange(level.worldMap());

        game().ui().sounds().setEnabled(!level.isDemoLevel()); //TODO is this needed?

        if (level.isDemoLevel()) {
            actionBindings().selectAnyMatchingBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TENGEN_SPECIFIC_BINDINGS);
            actionBindings().selectAnyMatchingBinding(ACTION_QUIT_DEMO_LEVEL, TENGEN_SPECIFIC_BINDINGS);
        } else {
            // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
            actionBindings().registerAllBindings(STEERING_BINDINGS);
            actionBindings().registerAllBindings(GlobalActionBindings.CHEAT_ACTION_BINDINGS);
            actionBindings().selectAnyMatchingBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TENGEN_SPECIFIC_BINDINGS);
            actionBindings().selectAnyMatchingBinding(ACTION_TOGGLE_PAC_BOOSTER, TENGEN_SPECIFIC_BINDINGS);
        }
        Logger.info(actionBindings());

        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // private

    private void updateScaling() {
        final SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
        scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SCREEN_HEIGHT;
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    private void updateHUD(GameLevel level) {
        final var gameModel = (TengenMsPacMan_GameModel) gameModel();
        final TengenMsPacMan_HUDState hud = gameModel.hud();

        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameState())
            && !level.entities().pac().isVisible();
        final int displayed = oneExtra ? gameModel.lives().count() : gameModel.lives().count() - 1;

        final int visibleLives = Math.clamp(displayed, 0, gameModel.hud().maxLivesDisplayed());
        hud.setVisibleLifeCount(visibleLives);
        if (gameModel.mapCategory() == MapCategory.ARCADE) {
            hud.levelNumberOff();
        } else {
            hud.levelNumberOn();
        }
    }

    protected void playLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> gameState().expire());
        levelCompletedAnimation.play();
    }

    protected void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            final Font font = Font.font(BaseRenderer.ARCADE_FONT.getFamily(), TS);
            final double width = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, font);
            movingMessage.startMovement(unscaledWidth(), width);
        }
    }

    protected void resetAnimations(GameLevel level) {
        final var gameModel = (TengenMsPacMan_GameModel) gameModel();
        final Pac pac = level.entities().pac();

        pac.animations().select(gameModel.isBoosterActive()
            ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();

        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().resetSelected();
        });
    }
}