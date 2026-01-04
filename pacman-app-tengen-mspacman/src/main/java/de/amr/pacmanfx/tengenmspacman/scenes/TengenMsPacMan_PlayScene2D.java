/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.GameLevelMessageType;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HeadsUpDisplay;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.SoundID;
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
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.JOYPAD;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static java.util.Objects.requireNonNull;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {

    public static final double CANVAS_WIDTH_UNSCALED = NES_SIZE_PX.x();

    private static final Font GAME_OVER_MESSAGE_FONT = Font.font(BaseRenderer.DEFAULT_ARCADE_FONT.getFamily(), TS);

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SIZE_PX.y());

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

        scalingProperty().addListener((_, _, _) -> context().currentGame().optGameLevel().ifPresent(level ->
            dynamicCamera.updateRange(level.worldMap())));
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    public boolean isMazeHighlighted() {
        return levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
    }

    public double canvasHeightUnscaled() {
        return canvasHeightUnscaled.get();
    }

    public PlayScene2DCamera dynamicCamera() {
        return dynamicCamera;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        canvas.widthProperty() .bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));
        rootPane.getChildren().setAll(canvas);
    }

    private void initForGameLevel(GameLevel level) {
        level.game().hud().levelCounter(true).livesCounter(true).show();

        dynamicCamera.updateRange(level.worldMap());

        // Action keyboard bindings
        if (level.isDemoLevel()) {
            actionBindings.useAnyBinding(TengenMsPacMan_Actions.ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
            actionBindings.useAnyBinding(ACTION_QUIT_DEMO_LEVEL, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
        } else {
            // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
            actionBindings.useAllBindings(TengenMsPacMan_UIConfig.STEERING_BINDINGS);
            actionBindings.useAllBindings(GameUI.CHEAT_BINDINGS);
            actionBindings.useAnyBinding(TengenMsPacMan_Actions.ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
            actionBindings.useAnyBinding(TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER, TengenMsPacMan_UIConfig.ACTION_BINDINGS);
        }
        JOYPAD.setBindings(actionBindings);
        actionBindings.activateBindings(GameUI.KEYBOARD);
    }

    private void updateScaling() {
        SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
        scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SIZE_PX.y();
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    @Override
    public void doInit(Game game) {
        TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game;
        game.hud().score(true).levelCounter(true).livesCounter(true).show();
        tengenGame.hud().gameOptions(!tengenGame.allOptionsDefault());
        updateScaling();
        dynamicCamera.enterManualMode();
        dynamicCamera.setToTopPosition();
    }

    @Override
    protected void doEnd(Game game) {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
            levelCompletedAnimation = null;
        }
        dynamicCamera.enterManualMode();
    }

    @Override
    public void update(Game game) {
        game.optGameLevel().ifPresent(level -> {
            int numRows = level.worldMap().numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter below maze
            if (level.isDemoLevel()) {
                soundManager().setEnabled(false);
            } else {
                soundManager().setEnabled(true);
                // Update moving "game over" message if present
                level.optMessage()
                    .filter(MovingGameLevelMessage.class::isInstance)
                    .map(MovingGameLevelMessage.class::cast)
                    .ifPresent(MovingGameLevelMessage::updateMovement);
                updateSound(level);
            }
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(TS(level.worldMap().numRows()), level.pac());
            }
            updateHUD(level);
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
        menu.addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot");
        menu.addLocalizedCheckBox(game.immuneProperty(), "immunity");
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
        return context().currentGame().optGameLevel().map(level -> level.worldMap().terrainLayer().sizeInPixel()).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarts(GameEvent e) {
        final Game game = context().currentGame();
        StateMachine.State<Game> state = game.control().state();
        boolean shutUp = game.level().isDemoLevel() || state instanceof TestState;
        if (!shutUp) {
            soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGameContinues(GameEvent e) {
        TengenMsPacMan_GameModel game = context().currentGame();
        game.optGameLevel().ifPresent(level -> {
            resetAnimations(level);
            game.showLevelMessage(GameLevelMessageType.READY);
            dynamicCamera.enterIntroMode();
        });
    }

    private void resetAnimations(GameLevel level) {
        final TengenMsPacMan_GameModel game = context().currentGame();
        level.pac().optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(game.isBoosterActive()
                ? TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_BOOSTER : CommonAnimationID.PAC_MUNCHING);
            animationManager.reset();
        });
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(CommonAnimationID.GHOST_NORMAL);
            animationManager.reset();
        }));
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel(ui.context().currentGame().level());
    }

    @Override
    public void onLevelStarts(GameEvent e) {
        TengenMsPacMan_GameModel game = context().currentGame();
        dynamicCamera.enterIntroMode();
        game.optGameLevel().ifPresent(this::resetAnimations);
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        context().currentGame().optGameLevel().ifPresent(this::initForGameLevel);
        dynamicCamera.enterTrackingMode();
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        switch (e.newState()) {
            case GameState.LEVEL_COMPLETE -> {
                soundManager().stopAll();
                startLevelCompleteAnimation(context().currentGame().level());
            }
            case GameState.GAME_OVER -> {
                soundManager().stopAll();
                dynamicCamera.enterManualMode();
                dynamicCamera.setToTopPosition();
                context().currentGame().level().optMessage().ifPresent(this::startGameOverMessageAnimation);
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        soundManager().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        soundManager().stop(SoundID.BONUS_ACTIVE);
        soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpires(GameEvent e) {
        soundManager().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        context().currentGame().control().terminateCurrentGameState();
    }

    @Override
    public void onPacDying(GameEvent e) {
        dynamicCamera.enterManualMode();
        soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFindsFood(GameEvent e) {
        if (!soundManager().isPlaying(SoundID.PAC_MAN_MUNCHING)) {
            soundManager().play(SoundID.PAC_MAN_MUNCHING);
        }
    }

    @Override
    public void onPacPowerBegins(GameEvent e) {
        soundManager().stopSiren();
        soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacPowerEnds(GameEvent e) {
        soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound(GameLevel level) {
        if (!soundManager().isEnabled()) return;

        final Game game = level.game();
        if (game.control().state() == GameState.HUNTING) {
            final Pac pac = level.pac();
            if (!pac.powerTimer().isRunning()) {
                selectAndPlaySiren();
            }
            final boolean ghostReturningHome = pac.isAlive()
                && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
            if (ghostReturningHome) {
                if (!soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                    soundManager().loop(SoundID.GHOST_RETURNS);
                }
            } else {
                soundManager().stop(SoundID.GHOST_RETURNS);
            }
        }
    }

    //TODO move this logic into game model as it depends on the currently played game variant
    private int selectSirenNumber(int huntingPhase) {
        // siren numbers are 1..4, hunting phase index = 0..7
        return 1 + huntingPhase / 2;
    }

    //TODO fix volume in audio file
    private void selectAndPlaySiren() {
        final float volume = 0.33f;
        final int sirenNumber = selectSirenNumber(context().currentGame().level().huntingTimer().phaseIndex());
        soundManager().playSiren(sirenNumber, volume);
    }

    private void updateHUD(GameLevel level) {
        final TengenMsPacMan_GameModel game = context().currentGame();
        final TengenMsPacMan_HeadsUpDisplay hud = game.hud();
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        final boolean oneExtra = game.control().state() == GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int displayedLifeCount = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        hud.setVisibleLifeCount(Math.clamp(displayedLifeCount, 0, hud.maxLivesDisplayed()));
        hud.levelNumber(game.mapCategory() != MapCategory.ARCADE);
    }

    private void startLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry, level);
        levelCompletedAnimation.setSingleFlashMillis(333);
        // When animation ends, let state "LEVEL_COMPLETE" expire
        levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(
            _ -> context().currentGame().control().terminateCurrentGameState());
        levelCompletedAnimation.playFromStart();
    }

    private void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            double messageWidth = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, GAME_OVER_MESSAGE_FONT);
            movingMessage.startMovement(unscaledSize().x(), messageWidth);
        }
    }
}