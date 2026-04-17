/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.GameLevelMessageType;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.STEERING_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ActionBindings.TENGEN_SPECIFIC_BINDINGS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static java.util.Objects.requireNonNull;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {

    public static final Vector2i DEFAULT_SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SCREEN_HEIGHT);

    private final StackPane rootPane;
    private final SubScene subScene;

    private final PlayScene2DCamera dynamicCamera;
    private final PerspectiveCamera fixedCamera;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D() {
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

        scalingProperty().addListener((_, _, _) -> gameContext().game().optGameLevel().ifPresent(level ->
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
    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        canvas.widthProperty() .bind(scalingProperty().multiply(NES_SCREEN_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));
        rootPane.getChildren().setAll(canvas);
    }

    @Override
    public void onStart() {
        final TengenMsPacMan_GameModel game = gameContext().game();
        game.hud().score(true).levelCounter(true).livesCounter(true).show();
        game.hud().gameOptions(!game.allOptionsDefault());
        updateScaling();
        dynamicCamera.enterManualMode();
        dynamicCamera.setToTopPosition();
    }

    @Override
    public void onEnd() {
        dynamicCamera.enterManualMode();
    }

    @Override
    public void onTick(long tick) {
        final TengenMsPacMan_GameModel game = gameContext().game();
        game.optGameLevel().ifPresent(level -> {
            final int numRows = level.worldMap().terrainLayer().numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter below maze
            if (!level.isDemoLevel()) {
                // Update moving "game over" message if present
                level.optMessage()
                    .filter(MovingGameLevelMessage.class::isInstance)
                    .map(MovingGameLevelMessage.class::cast)
                    .ifPresent(MovingGameLevelMessage::updateMovement);
            }
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(TS(level.worldMap().numRows()), level.pac());
            }
            updateHUD(level);
            soundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!level.isDemoLevel());
                soundEffects.playLevelRunningSound(level);
            });
        });
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        final SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();

        final var menu = new GameUI_ContextMenu(ui);

        final RadioMenuItem miScaledToFit = menu.addLocalizedRadioButton("scaled_to_fit");
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(_ -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCALED_TO_FIT));

        final RadioMenuItem miScrolling = menu.addLocalizedRadioButton("scrolling");
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(_ -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SCROLLING));

        final ToggleGroup toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        menu.addLocalizedTitleItem("pacman");
        menu.addLocalizedCheckBox(game.cheats().usingAutopilotProperty(), "autopilot");
        menu.addLocalizedCheckBox(game.cheats().immuneProperty(), "immunity");
        menu.addSeparator();
        menu.addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        menu.addLocalizedActionItem(ACTION_QUIT_GAME_SCENE, "quit");
        return Optional.of(menu);
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public Vector2i unscaledSize() {
        return gameContext().game().optGameLevel()
            .map(level -> level.worldMap().terrainLayer().sizeInPixel())
            .orElse(DEFAULT_SIZE);
    }

    // Game event handlers

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        final TengenMsPacMan_GameModel game = gameContext().game();
        game.optGameLevel().ifPresent(level -> {
            resetAnimations(level);
            game.showMessage(level, GameLevelMessageType.READY);
            dynamicCamera.playIntroSequence();
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final Game game = e.game();
        final boolean silent = game.isDemoLevelRunning() || game.flow().state() instanceof TestState;
        if (!silent) {
            soundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = gameContext().game();
        switch (e.newState()) {
            case TengenMsPacMan_GameState.LEVEL_COMPLETE -> {
                final GameLevel level = game.optGameLevel().orElseThrow();
                soundEffects().ifPresent(GameSoundEffects::stopAll);
                playLevelCompleteAnimation(level);
            }
            case TengenMsPacMan_GameState.GAME_OVER -> {
                final GameLevel level = game.optGameLevel().orElseThrow();
                soundEffects().ifPresent(GameSoundEffects::stopAll);
                dynamicCamera.enterManualMode();
                dynamicCamera.setToTopPosition();
                level.optMessage().ifPresent(this::startGameOverMessageAnimation);
            }
            default -> {}
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        acceptGameLevel(e.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent e) {
        e.game().optGameLevel().ifPresent(this::resetAnimations);
        dynamicCamera.playIntroSequence();
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        e.game().flow().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        dynamicCamera.enterManualMode();
        soundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameContext().clock().tickCount();
        soundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        soundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    @Override
    public void onEnteredFrom3DScene() {
        final Game game = gameContext().game();
        game.hud().levelCounter(true).livesCounter(true).show();
        game.optGameLevel().ifPresent(this::acceptGameLevel);
    }

    // private

    private void acceptGameLevel(GameLevel level) {
        dynamicCamera.enterTrackingMode();
        dynamicCamera.updateRange(level.worldMap());

        // Action keyboard bindings
        if (level.isDemoLevel()) {
            actionBindings.bindOne(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TENGEN_SPECIFIC_BINDINGS);
            actionBindings.bindOne(ACTION_QUIT_DEMO_LEVEL, TENGEN_SPECIFIC_BINDINGS);
        } else {
            // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
            actionBindings.bindAll(STEERING_BINDINGS);
            actionBindings.bindAll(GameUI.CHEAT_ACTION_BINDINGS);
            actionBindings.bindOne(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TENGEN_SPECIFIC_BINDINGS);
            actionBindings.bindOne(ACTION_TOGGLE_PAC_BOOSTER, TENGEN_SPECIFIC_BINDINGS);
        }
        Input.instance().joypad.setBindings(actionBindings);
        actionBindings.pluginKeyboard();
    }

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
        final TengenMsPacMan_GameModel game = gameContext().game();
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = game.flow().state() == TengenMsPacMan_GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int displayedLifeCount = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        game.hud().setVisibleLifeCount(Math.clamp(displayedLifeCount, 0, game.hud().maxLivesDisplayed()));
        game.hud().levelNumber(game.mapCategory() != MapCategory.ARCADE);
    }

    private void playLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> level.game().flow().state().expire());
        levelCompletedAnimation.play();
    }

    private void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            double messageWidth = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, Font.font(BaseRenderer.arcadeFont().getFamily(), TS));
            movingMessage.startMovement(unscaledSize().x(), messageWidth);
        }
    }

    private void resetAnimations(GameLevel level) {
        final TengenMsPacMan_GameModel game = gameContext().game();
        level.pac().selectAnimation(game.isBoosterActive()
            ? TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_BOOSTER
            : Pac.AnimationID.PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }
}