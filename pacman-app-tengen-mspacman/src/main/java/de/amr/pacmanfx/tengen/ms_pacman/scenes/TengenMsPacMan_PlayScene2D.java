/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.AnimationID;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUD_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PlayScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.SubSceneProvider;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.GAME_OVER_MESSAGE_TEXT;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;
import static java.util.Objects.requireNonNull;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements SubSceneProvider {

    public static final double CANVAS_WIDTH_UNSCALED = NES_SIZE_PX.x();

    private static final Font GAME_OVER_MESSAGE_FONT = Font.font(BaseRenderer.DEFAULT_ARCADE_FONT.getFamily(), TS);

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SIZE_PX.y());

    private final StackPane rootPane;
    private final SubScene subScene;

    private final PlayScene2DCamera dynamicCamera;
    private final PerspectiveCamera fixedCamera;

    private TengenMsPacMan_PlayScene2D_Renderer sceneRenderer;
    private TengenMsPacMan_HUD_Renderer hudRenderer;

    private LevelCompletedAnimation levelCompletedAnimation;

    // Context menu
    private ToggleGroup toggleGroup;
    private RadioMenuItem miScrolling;
    private RadioMenuItem miScaledToFit;

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        fixedCamera = new PerspectiveCamera(false);

        dynamicCamera = new PlayScene2DCamera();
        dynamicCamera.scalingProperty().bind(scalingProperty());

        rootPane = new StackPane();
        rootPane.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((py, ov, nv) -> updateScaling());
        subScene.heightProperty().addListener((py, ov, nv) -> updateScaling());

        scalingProperty().addListener((py, ov, nv) -> context().currentGame().optGameLevel().ifPresent(level ->
            dynamicCamera.updateRange(level.worldMap())));
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    public boolean isMazeHighlighted() {
        return levelCompletedAnimation != null && levelCompletedAnimation.highlightedProperty().get();
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
        createRenderers(canvas);
    }

    private void initForGameLevel(GameLevel level) {
        level.game().hud().levelCounterVisible(true).livesCounterVisible(true);

        dynamicCamera.updateRange(level.worldMap());

        // Action keyboard bindings
        final Set<ActionBinding> tengenBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        if (level.isDemoLevel()) {
            actionBindings.useFirst(TengenMsPacMan_Actions.ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenBindings);
            actionBindings.useFirst(ACTION_QUIT_DEMO_LEVEL, tengenBindings);
        } else {
            // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
            actionBindings.useFirst(CommonGameActions.ACTION_STEER_UP,    tengenBindings);
            actionBindings.useFirst(CommonGameActions.ACTION_STEER_DOWN,  tengenBindings);
            actionBindings.useFirst(CommonGameActions.ACTION_STEER_LEFT,  tengenBindings);
            actionBindings.useFirst(CommonGameActions.ACTION_STEER_RIGHT, tengenBindings);

            actionBindings.useFirst(TengenMsPacMan_Actions.ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenBindings);
            actionBindings.useFirst(TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER, tengenBindings);

            actionBindings.useAll(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.attach(ui.keyboard());
    }


    private void updateScaling() {
        SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
        scalingProperty().set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SIZE_PX.y();
        });
        Logger.info("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final TengenMsPacMan_UIConfig uiConfig = ui.currentConfig();

        hudRenderer = configureRendererForGameScene(uiConfig.createHUDRenderer(canvas), this);

        sceneRenderer = configureRendererForGameScene(
            new TengenMsPacMan_PlayScene2D_Renderer(this, canvas, uiConfig.spriteSheet()), this);
    }

    @Override
    public TengenMsPacMan_HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public TengenMsPacMan_PlayScene2D_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit(Game game) {
        TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game;
        game.hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(true);
        tengenGame.hud().showGameOptions(!tengenGame.allOptionsHaveDefaultValue());
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
                ui.soundManager().setEnabled(false);
            } else {
                ui.soundManager().setEnabled(true);
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

    private void handlePlaySceneDisplayModeChange(
        ObservableValue<? extends SceneDisplayMode> property, SceneDisplayMode oldMode, SceneDisplayMode newMode) {
        toggleGroup.selectToggle(newMode == SCROLLING ? miScrolling : miScaledToFit);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();

        miScaledToFit = new RadioMenuItem(ui.assets().translated("scaled_to_fit"));
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(e -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCALED_TO_FIT));

        miScrolling = new RadioMenuItem(ui.assets().translated("scrolling"));
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(e -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SCROLLING));

        toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        PROPERTY_PLAY_SCENE_DISPLAY_MODE.addListener(this::handlePlaySceneDisplayModeChange);
        Logger.info("Added listener to config propertyPlaySceneDisplayMode property");
        //TODO might interfere with onHidden event handler set elsewhere on this menu
        menu.setOnHidden(e -> {
            PROPERTY_PLAY_SCENE_DISPLAY_MODE.removeListener(this::handlePlaySceneDisplayModeChange);
            Logger.info("Removed listener from config propertyPlaySceneDisplayMode property");
        });

        var miAutopilot = new CheckMenuItem(ui.assets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(context().currentGame().usingAutopilotProperty());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(context().currentGame().immuneProperty());

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            miScaledToFit,
            miScrolling,
            createContextMenuTitle(ui.preferences(), ui.assets().translated("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit
        );
    }

    @Override
    public SubScene subScene() {
        return subScene;
    }

    @Override
    public Vector2i unscaledSize() {
        return context().currentGame().optGameLevel().map(level -> level.worldMap().terrainLayer().sizeInPixel()).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        final Game game = context().currentGame();
        StateMachine.State<Game> state = game.control().state();
        boolean shutUp = game.level().isDemoLevel() || state instanceof TestState;
        if (!shutUp) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGameContinued(GameEvent e) {
        TengenMsPacMan_GameModel game = context().currentGame();
        game.optGameLevel().ifPresent(level -> {
            resetAnimations(level);
            game.showLevelMessage(MessageType.READY);
            dynamicCamera.enterIntroMode();
        });
    }

    private void resetAnimations(GameLevel level) {
        final TengenMsPacMan_GameModel game = context().currentGame();
        level.pac().optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(game.isBoosterActive()
                ? AnimationID.ANIM_MS_PAC_MAN_BOOSTER : CommonAnimationID.ANIM_PAC_MUNCHING);
            animationManager.reset();
        });
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(CommonAnimationID.ANIM_GHOST_NORMAL);
            animationManager.reset();
        }));
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel(ui.context().currentGame().level());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
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
    public void onEnterGameState(StateMachine.State<Game> state) {
        switch (state) {
            case GameState.LEVEL_COMPLETE -> {
                ui.soundManager().stopAll();
                startLevelCompleteAnimation(context().currentGame().level());
            }
            case GameState.GAME_OVER -> {
                ui.soundManager().stopAll();
                dynamicCamera.enterManualMode();
                dynamicCamera.setToTopPosition();
                context().currentGame().level().optMessage().ifPresent(this::startGameOverMessageAnimation);
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        ui.soundManager().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
        ui.soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        context().currentGame().control().terminateCurrentGameState();
    }

    @Override
    public void onPacDying(GameEvent e) {
        dynamicCamera.enterManualMode();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        if (!ui.soundManager().isPlaying(SoundID.PAC_MAN_MUNCHING)) {
            ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
        }
    }

    @Override
    public void onPacPowerStarts(GameEvent e) {
        ui.soundManager().pauseSiren();
        ui.soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacPowerEnds(GameEvent e) {
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound(GameLevel level) {
        if (!ui.soundManager().isEnabled()) return;

        final Game game = level.game();
        if (game.control().state() == GameState.HUNTING) {
            final Pac pac = level.pac();
            if (!pac.powerTimer().isRunning()) {
                selectAndPlaySiren();
            }
            final boolean ghostReturningHome = pac.isAlive()
                && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
            if (ghostReturningHome) {
                ui.soundManager().loop(SoundID.GHOST_RETURNS);
            } else {
                ui.soundManager().stop(SoundID.GHOST_RETURNS);
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
        final SoundID sirenID = switch (sirenNumber) {
            case 1 -> SoundID.SIREN_1;
            case 2 -> SoundID.SIREN_2;
            case 3 -> SoundID.SIREN_3;
            case 4 -> SoundID.SIREN_4;
            default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
        };
        ui.soundManager().playSiren(sirenID, volume);
    }

    private void updateHUD(GameLevel level) {
        final TengenMsPacMan_GameModel game = context().currentGame();
        int numLives = game.lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (game.control().state() == GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(numLives);
        game.hud().showLevelNumber(game.mapCategory() != MapCategory.ARCADE);
    }

    private void startLevelCompleteAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry, level);
        levelCompletedAnimation.setSingleFlashMillis(333);
        // When animation ends, let state "LEVEL_COMPLETE" expire
        levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(
            e -> context().currentGame().control().terminateCurrentGameState());
        levelCompletedAnimation.playFromStart();
    }

    private void startGameOverMessageAnimation(GameLevelMessage message) {
        if (message instanceof MovingGameLevelMessage movingMessage) {
            double messageWidth = Ufx.textWidth(GAME_OVER_MESSAGE_TEXT, GAME_OVER_MESSAGE_FONT);
            movingMessage.startMovement(unscaledSize().x(), messageWidth);
        }
    }
}