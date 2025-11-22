/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.controller.test.TestState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PlayScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.SubSceneProvider;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.controller.PacManGamesState.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui._2d.GameScene2DRenderer.configureRendererForGameScene;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements SubSceneProvider {

    public static final float CONTENT_INDENT = TS(2);
    private static final double CANVAS_WIDTH_UNSCALED = NES_SIZE_PX.x();

    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SIZE_PX.y());
    public final BooleanProperty mazeHighlighted = new SimpleBooleanProperty(false);

    private final StackPane rootPane;
    private final SubScene subScene;

    private final PlayScene2DCamera dynamicCamera;
    private final PerspectiveCamera fixedCamera;

    private TengenMsPacMan_PlayScene2D_Renderer sceneRenderer;
    private TengenMsPacMan_HUDRenderer hudRenderer;

    private Rectangle clipRect;
    public LevelCompletedAnimation levelCompletedAnimation;

    public class PlaySceneDebugInfoRenderer extends BaseDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
            super(TengenMsPacMan_PlayScene2D.this, canvas, spriteSheet);
        }

        @Override
        public void drawDebugInfo() {
            final FsmState<GameContext> gameState = context().gameState();
            drawTileGrid(CANVAS_WIDTH_UNSCALED, canvasHeightUnscaled.get(), Color.LIGHTGRAY);
            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState, gameState.timer().tickCount()), 0, scaled(3 * TS));
            context().optGameLevel().ifPresent(gameLevel -> {
                drawMovingActorInfo(gameLevel.pac());
                gameLevel.ghosts().forEach(this::drawMovingActorInfo);
            });
            ctx.fillText("Camera y=%.2f".formatted(dynamicCamera.getTranslateY()), scaled(11*TS), scaled(15*TS));
            ctx.restore();
        }
    }

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        fixedCamera = new PerspectiveCamera(false);

        dynamicCamera = new PlayScene2DCamera();
        dynamicCamera.scalingProperty().bind(scaling);
        scaling.addListener((py, ov, nv) -> context().optGameLevel().ifPresent(gameLevel ->
            dynamicCamera.updateRange(gameLevel.worldMap().terrainLayer().numRows())));

        rootPane = new StackPane();
        rootPane.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));
        subScene.cameraProperty().addListener((py, ov, nv) -> updateScaling());
        subScene.heightProperty().addListener((py, ov, nv) -> updateScaling());
    }

    @Override
    public void setCanvas(Canvas canvas) {
        super.setCanvas(canvas);

        canvas.widthProperty() .bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));

        // All maps are 28 tiles wide but the NES screen is 32 tiles wide. To accommodate, the maps are centered
        // horizontally and 2 tiles on each side are clipped.
        clipRect = new Rectangle();
        clipRect.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(CONTENT_INDENT)));
        clipRect.yProperty().bind(canvas.translateYProperty());
        clipRect.widthProperty().bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED - 2 * CONTENT_INDENT));
        clipRect.heightProperty().bind(canvas.heightProperty());

        rootPane.getChildren().setAll(canvas);
    }

    private void initForGameLevel(GameLevel gameLevel) {
        context().game().hud().levelCounterVisible(true).livesCounterVisible(true); // is also visible in demo level!
        setActionsBindings(gameLevel.isDemoLevel());
        dynamicCamera.updateRange(gameLevel.worldMap().numRows());
    }

    private void updateScaling() {
        SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
        scaling.set(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SIZE_PX.y();
        });
        Logger.info("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    private void setActionsBindings(boolean demoLevel) {
        Set<ActionBinding> tengenBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        if (demoLevel) {
            actionBindings.bind(ACTION_QUIT_DEMO_LEVEL, tengenBindings);
        } else {
            // Pac-Man is steered with keys representing the "Joypad" buttons
            actionBindings.bind(ACTION_STEER_UP,    tengenBindings);
            actionBindings.bind(ACTION_STEER_DOWN,  tengenBindings);
            actionBindings.bind(ACTION_STEER_LEFT,  tengenBindings);
            actionBindings.bind(ACTION_STEER_RIGHT, tengenBindings);

            actionBindings.bind(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenBindings);
            actionBindings.bind(ACTION_TOGGLE_PAC_BOOSTER, tengenBindings);

            actionBindings.bind(CheatActions.ACTION_ADD_LIVES,        ui.actionBindings());
            actionBindings.bind(CheatActions.ACTION_EAT_ALL_PELLETS,  ui.actionBindings());
            actionBindings.bind(CheatActions.ACTION_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.bind(CheatActions.ACTION_KILL_GHOSTS,      ui.actionBindings());
        }
        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final TengenMsPacMan_UIConfig uiConfig = ui.currentConfig();

        hudRenderer = configureRendererForGameScene(
            uiConfig.createHUDRenderer(canvas), this);

        debugInfoRenderer = configureRendererForGameScene(
            new PlaySceneDebugInfoRenderer(canvas, uiConfig.spriteSheet()), this);

        sceneRenderer = configureRendererForGameScene(
            new TengenMsPacMan_PlayScene2D_Renderer(this, canvas, uiConfig.spriteSheet()), this);
    }

    @Override
    public TengenMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        final TengenMsPacMan_GameModel game = context().game();
        game.hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(true);
        game.hud().showGameOptions(!game.optionsAreInitial());
        updateScaling();
        dynamicCamera.enterManualMode();
        dynamicCamera.setToTopPosition();
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
            levelCompletedAnimation = null;
        }
        dynamicCamera.enterManualMode();
    }

    @Override
    public void update() {
        context().optGameLevel().ifPresent(gameLevel -> {
            int numRows = gameLevel.worldMap().numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter below maze
            if (gameLevel.isDemoLevel()) {
                ui.soundManager().setEnabled(false);
            } else {
                ui.soundManager().setEnabled(true);
                // Update moving "game over" message if present
                gameLevel.optMessage()
                    .filter(MovingGameLevelMessage.class::isInstance)
                    .map(MovingGameLevelMessage.class::cast)
                    .ifPresent(MovingGameLevelMessage::update);
                updateSound();
            }
            if (subScene.getCamera() == dynamicCamera) {
                dynamicCamera.update(TS(gameLevel.worldMap().numRows()), gameLevel.pac());
            }
            updateHUD(gameLevel);
        });
    }

    // Context menu

    private ToggleGroup toggleGroup;
    private RadioMenuItem miScrolling;
    private RadioMenuItem miScaledToFit;

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
        miAutopilot.selectedProperty().bindBidirectional(context().gameController().usingAutopilotProperty());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(context().gameController().immunityProperty());

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
    public Vector2i sizeInPx() {
        return context().optGameLevel().map(gameLevel -> gameLevel.worldMap().terrainLayer().sizeInPixel()).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        FsmState<GameContext> state = context().gameState();
        boolean shutUp = context().gameLevel().isDemoLevel() || state instanceof TestState;
        if (!shutUp) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel(ui.gameContext().gameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        context().optGameLevel().ifPresent(this::initForGameLevel);
        dynamicCamera.enterTrackingMode();
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.enterIntroMode();
    }

    @Override
    public void onEnterGameState(FsmState<GameContext> state) {
        switch (state) {
            case LEVEL_COMPLETE -> {
                ui.soundManager().stopAll();
                startLevelCompleteAnimation(context().gameLevel());
            }
            case GAME_OVER -> {
                ui.soundManager().stopAll();
                dynamicCamera.enterManualMode();
                dynamicCamera.setToTopPosition();
                startGameOverMessageAnimation(context().gameLevel());
            }
            default -> {}
        }
    }

    private void startLevelCompleteAnimation(GameLevel gameLevel) {
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry);
        levelCompletedAnimation.setGameLevel(gameLevel);
        levelCompletedAnimation.setSingleFlashMillis(333);
        levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(e -> {
            mazeHighlighted.unbind();
            context().gameController().letCurrentGameStateExpire();
        });
        mazeHighlighted.bind(levelCompletedAnimation.highlightedProperty());
        levelCompletedAnimation.playFromStart();
    }

    private void startGameOverMessageAnimation(GameLevel gameLevel) {
        gameLevel.optMessage()
            .filter(MovingGameLevelMessage.class::isInstance)
            .map(MovingGameLevelMessage.class::cast)
            .ifPresent(movingGameLevelMessage -> {
                Font font = Font.font(BaseRenderer.DEFAULT_ARCADE_FONT.getFamily(), TS);
                movingGameLevelMessage.start(sizeInPx().x(), Ufx.textWidth("GAME OVER", font));
            });
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
    public void onGameContinued(GameEvent e) {
        context().optGameLevel().ifPresent(gameLevel -> context().game().showMessage(gameLevel, MessageType.READY));
        dynamicCamera.enterIntroMode();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        context().gameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        dynamicCamera.enterManualMode();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        ui.soundManager().pauseSiren();
        ui.soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound() {
        final Pac pac = context().gameLevel().pac();

        //TODO check in simulator when exactly which siren plays
        boolean pacChased = context().gameState() == HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = context().gameLevel().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> ui.soundManager().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> ui.soundManager().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> ui.soundManager().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> ui.soundManager().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturningToHouse = context().gameLevel()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .findAny();
        if (ghostReturningToHouse.isPresent()
            && (context().gameState() == HUNTING || context().gameState() == PacManGamesState.GHOST_DYING)) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    // drawing

    @Override
    public void drawSceneContent() {
        // Overridden draw() method does the job
    }

    @Override
    public void draw() {
        context().optGameLevel().ifPresent(gameLevel -> {
            canvas.setClip(clipRect);
            sceneRenderer.draw();
            drawHUD();
            if (debugInfoVisible.get() && debugInfoRenderer != null) {
                canvas.setClip(null); // also show normally clipped region (to see how Pac-Man travels through portals)
                debugInfoRenderer.drawDebugInfo();
            }
        });
    }

    private void updateHUD(GameLevel gameLevel) {
        final TengenMsPacMan_GameModel game = context().game();
        int numLives = game.lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (context().gameState() == PacManGamesState.STARTING_GAME_OR_LEVEL && !gameLevel.pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(numLives);
        game.hud().showLevelNumber(game.mapCategory() != MapCategory.ARCADE);
    }
}