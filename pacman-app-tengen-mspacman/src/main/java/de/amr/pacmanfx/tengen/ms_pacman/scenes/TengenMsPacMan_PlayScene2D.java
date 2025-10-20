/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.test.TestGameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.MovingGameLevelMessage;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui._2d.CanvasProvider;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GamePlayState.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode.SCROLLING;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CanvasProvider {

    private static final double CANVAS_WIDTH_UNSCALED = NES_SIZE_PX.x();
    private final DoubleProperty canvasHeightUnscaled = new SimpleDoubleProperty(NES_SIZE_PX.y());

    private final SubScene subScene;
    private final Canvas canvas = new Canvas();
    private final Rectangle clipRect = new Rectangle();

    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final PerspectiveCamera fixedCamera  = new PerspectiveCamera(false);

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_GameLevelRenderer gameLevelRenderer;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    private LevelCompletedAnimation levelCompletedAnimation;
    private final BooleanProperty mazeHighlighted = new SimpleBooleanProperty(false);

    // Testing

    private final AbstractGameAction actionCameraFollowPlayer = new AbstractGameAction("CameraFollowPlayer") {
        @Override
        public void execute(GameUI ui) {
            dynamicCamera.followPac(true);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return context().optGameLevel().isPresent();
        }
    };

    private final AbstractGameAction actionCameraTop = new AbstractGameAction("CameraTop") {
        @Override
        public void execute(GameUI ui) {
            dynamicCamera.moveTopImmediately();
        }
    };

    private final AbstractGameAction actionCameraBottom = new AbstractGameAction("CameraBottom") {
        @Override
        public void execute(GameUI ui) {
            dynamicCamera.moveBottomImmediately();
        }
    };

    private final Set<ActionBinding> testBindings = Set.of(
        new ActionBinding(actionCameraBottom,       Keyboard.control(KeyCode.B)),
        new ActionBinding(actionCameraTop,          Keyboard.control(KeyCode.T)),
        new ActionBinding(actionCameraFollowPlayer, Keyboard.control(KeyCode.F))
    );

    private class DynamicCamera extends ParallelCamera {

        private static final float MIN_CAMERA_MOVEMENT = 0.5f;
        private static final float CAMERA_SPEED = 0.02f;

        private boolean followPac;
        private int idleTicks;
        private double tgtY;
        private double minY;
        private double maxY;

        public DynamicCamera() {}

        public void setIdleTicks(int idleTicks) {
            this.idleTicks = idleTicks;
        }

        public void followPac(boolean follow) {
            followPac = follow;
        }

        public void update(GameLevel gameLevel) {
            updateRange(gameLevel);
            if (idleTicks > 0) {
                --idleTicks;
                if (idleTicks == 0) {
                    setTargetBottom();
                    followPac(true);
                }
                return;
            }
            if (followPac) {
                focusPac(gameLevel);
                move();
            }
        }

        // This is "alchemy", not science :-)
        private void updateRange(GameLevel gameLevel) {
            int numRows = gameLevel.worldMap().terrainLayer().numRows();
            if (numRows <= 30) { // MINI
                dynamicCamera.minY = -scaled(TS(3));
                dynamicCamera.maxY =  scaled(TS(2));
            }
            else if (numRows >= 42) { // BIG
                dynamicCamera.minY = -scaled(TS(9));
                dynamicCamera.maxY =  scaled(TS(8));
            }
            else { // ARCADE
                dynamicCamera.minY = -scaled(TS(6));
                dynamicCamera.maxY =  scaled(TS(5));
            }
        }

        private void move() {
            double oldCameraY = getTranslateY();
            double newCameraY = lerp(oldCameraY, tgtY, CAMERA_SPEED);
            double delta = Math.abs(oldCameraY - newCameraY);
            if (delta > MIN_CAMERA_MOVEMENT) {
                setTranslateY(newCameraY);
            }
        }

        private void focusPac(GameLevel gameLevel) {
            Pac pac = gameLevel.pac();
            double relY = pac.y() / TS(gameLevel.worldMap().terrainLayer().numRows());
            if (relY < 0.25 || relY < 0.6 && pac.moveDir() == Direction.UP) {
                setTargetTop();
            } else if (relY > 0.75 || relY > 0.4 && pac.moveDir() == Direction.DOWN) {
                setTargetBottom();
            }
        }

        public void moveTopImmediately() {
            followPac(false);
            setTranslateY(minY);
        }

        public void moveBottomImmediately() {
            followPac(false);
            setTranslateY(maxY);
        }

        public void setTargetTop() {
            tgtY = minY;
        }

        public void setTargetBottom() {
            tgtY = maxY;
        }
    }

    private class PlaySceneDebugInfoRenderer extends DefaultDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(Canvas canvas) {
            super(TengenMsPacMan_PlayScene2D.this.ui, canvas);
        }

        @Override
        public void drawDebugInfo() {
            final GameState gameState = context().gameState();
            drawTileGrid(CANVAS_WIDTH_UNSCALED, canvasHeightUnscaled.get(), Color.LIGHTGRAY);
            ctx.save();
            ctx.translate(scaled(2 * TS), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState, gameState.timer().tickCount()), 0, scaled(3 * TS));
            context().optGameLevel().ifPresent(gameLevel -> {
                drawMovingActorInfo(gameLevel.pac());
                ghostsByZ(gameLevel).forEach(this::drawMovingActorInfo);
            });
            ctx.fillText("Camera y=%.2f".formatted(dynamicCamera.getTranslateY()), scaled(11*TS), scaled(15*TS));
            ctx.restore();
        }
    }

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        var rootPane = new StackPane(canvas);
        rootPane.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));

        // Scene size gets bound to parent scene when embedded in game view, initial size doesn't matter
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE.map(mode -> mode == SCROLLING ? dynamicCamera : fixedCamera));

        canvas.widthProperty() .bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED));
        canvas.heightProperty().bind(scalingProperty().multiply(canvasHeightUnscaled));

        // All maps are 28 tiles wide but the NES screen is 32 tiles wide. To accommodate, the maps are centered
        // horizontally and 2 tiles on each side are clipped.
        final int margin = 2 * TS;
        clipRect.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(margin)));
        clipRect.yProperty().bind(canvas.translateYProperty());
        clipRect.widthProperty().bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED - 2 * margin));
        clipRect.heightProperty().bind(canvas.heightProperty());

        subScene.widthProperty().addListener((py, ov, nv) -> updateScaling());
        subScene.heightProperty().addListener((py, ov, nv) -> updateScaling());
        subScene.cameraProperty().addListener((py, ov, nv) -> updateScaling());
    }

    private void updateScaling() {
        scaling.set(switch (PROPERTY_PLAY_SCENE_DISPLAY_MODE.get()) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SIZE_PX.y();
        });
        Logger.info("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), scaling());
    }

    private void setActionsBindings(boolean demoLevel) {
        Set<ActionBinding> tengenBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        if (demoLevel) {
            actionBindingsManager.register(ACTION_QUIT_DEMO_LEVEL, tengenBindings);
        } else {
            // Pac-Man is steered with keys representing the "Joypad" buttons
            actionBindingsManager.register(ACTION_STEER_UP,    tengenBindings);
            actionBindingsManager.register(ACTION_STEER_DOWN,  tengenBindings);
            actionBindingsManager.register(ACTION_STEER_LEFT,  tengenBindings);
            actionBindingsManager.register(ACTION_STEER_RIGHT, tengenBindings);

            actionBindingsManager.register(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenBindings);
            actionBindingsManager.register(ACTION_TOGGLE_PAC_BOOSTER, tengenBindings);

            actionBindingsManager.register(ACTION_CHEAT_ADD_LIVES,        ui.actionBindings());
            actionBindingsManager.register(ACTION_CHEAT_EAT_ALL_PELLETS,  ui.actionBindings());
            actionBindingsManager.register(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindingsManager.register(ACTION_CHEAT_KILL_GHOSTS,      ui.actionBindings());

            actionBindingsManager.register(actionCameraBottom, testBindings);
            actionBindingsManager.register(actionCameraTop, testBindings);
            actionBindingsManager.register(actionCameraFollowPlayer, testBindings);

        }
        actionBindingsManager.installBindings(ui.keyboard());
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        TengenMsPacMan_UIConfig uiConfig = ui.currentConfig();
        hudRenderer       = configureRenderer(uiConfig.createHUDRenderer(canvas));
        gameLevelRenderer = configureRenderer(uiConfig.createGameLevelRenderer(canvas));
        actorRenderer     = configureRenderer(uiConfig.createActorRenderer(canvas));
        debugInfoRenderer = configureRenderer(new PlaySceneDebugInfoRenderer(canvas));
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
        dynamicCamera.moveTopImmediately();
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
            levelCompletedAnimation = null;
        }
    }

    @Override
    public void update() {
        context().optGameLevel().ifPresent(gameLevel -> {
            int numRows = gameLevel.worldMap().numRows();
            canvasHeightUnscaled.set(TS(numRows + 2)); // 2 additional rows for level counter
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
                dynamicCamera.update(gameLevel);
            }
            updateHUD();
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
        miAutopilot.selectedProperty().bindBidirectional(theGameContext().gameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(theGameContext().gameController().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            miScaledToFit,
            miScrolling,
            createContextMenuTitle("pacman", ui.preferences(), ui.assets()),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit
        );
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public Vector2i sizeInPx() {
        return context().optGameLevel().map(gameLevel -> gameLevel.worldMap().terrainLayer().sizeInPixel()).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameState state = context().gameState();
        boolean shutUp = context().gameLevel().isDemoLevel() || state instanceof TestGameState;
        if (!shutUp) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    private void initForGameLevel(GameLevel gameLevel) {
        context().game().hud().levelCounterVisible(true).livesCounterVisible(true); // is also visible in demo level!
        setActionsBindings(gameLevel.isDemoLevel());

        //TODO check if this is needed, if not, remove
        gameLevelRenderer = (TengenMsPacMan_GameLevelRenderer) ui.currentConfig().createGameLevelRenderer(canvas);
        gameLevelRenderer.scalingProperty().bind(scaling);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel(ui.gameContext().gameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        context().optGameLevel().ifPresent(this::initForGameLevel);
        dynamicCamera.followPac(true);
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.moveTopImmediately();
        dynamicCamera.setIdleTicks(90);
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case LEVEL_COMPLETE -> {
                ui.soundManager().stopAll();
                startLevelCompleteAnimation(context().gameLevel());
            }
            case GAME_OVER -> {
                ui.soundManager().stopAll();
                dynamicCamera.setTargetTop();
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
                double width = gameLevelRenderer.messageTextWidth(gameLevel, MessageType.GAME_OVER);
                movingGameLevelMessage.start(sizeInPx().x(), width);
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
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.setTargetTop();
        context().gameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        ui.soundManager().loop(SoundID.PAC_MAN_MUNCHING);
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

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTime() > 10) {
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturningToHouse = context().gameLevel()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .findAny();
        if (ghostReturningToHouse.isPresent()
            && (context().gameState() == HUNTING || context().gameState() == GamePlayState.GHOST_DYING)) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    // drawing

    @Override
    public void draw() {
        if (sceneRenderer != null) {
            sceneRenderer.clearCanvas();
        }
        context().optGameLevel().ifPresent(gameLevel -> {
            final var info = new RenderInfo();
            // this is needed for animated maze from STRANGE map category
            info.put(CommonRenderInfoKey.TICK, ui.clock().tickCount());
            info.put(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY,
                gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY));
            if (levelCompletedAnimation != null && mazeHighlighted.get()) {
                info.put(CommonRenderInfoKey.MAZE_BRIGHT, true);
                info.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, levelCompletedAnimation.flashingIndex());
            } else {
                info.put(CommonRenderInfoKey.MAZE_BRIGHT, false);
            }

            // map width is 28 tiles but NES screen width is 32 tiles: move 2 tiles right and clip one tile on each side
            canvas.setClip(clipRect);

            gameLevelRenderer.ctx().save();
            gameLevelRenderer.ctx().translate(scaled(TS(2)), 0);
            gameLevelRenderer.drawGameLevel(gameLevel, info);
            drawActors(gameLevel);
            drawHUD();
            if (debugInfoVisible.get() && debugInfoRenderer != null) {
                canvas.setClip(null); // show everything e.g. portal traversal
                gameLevelRenderer.ctx().translate(scaled(-TS(2)), 0);
                debugInfoRenderer.drawDebugInfo();
            }
            gameLevelRenderer.ctx().restore();
        });
    }

    @Override
    public void drawSceneContent() {
        // draw() is overridden and does the job
    }

    private void drawActors(GameLevel gameLevel) {
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        ghostsByZ(gameLevel).forEach(actorsInZOrder::add);
        actorsInZOrder.forEach(actor -> actorRenderer.drawActor(actor));
    }

    //TODO the ghost state should also be taken into account
    private Stream<Ghost> ghostsByZ(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }

    private void updateHUD() {
        TengenMsPacMan_GameModel game = context().game();
        GameLevel gameLevel = game.optGameLevel().orElse(null);

        if (gameLevel == null) return;

        int numLives = game.lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (context().gameState() == GamePlayState.STARTING_GAME_OR_LEVEL && !gameLevel.pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(numLives);

        game.hud().showLevelNumber(game.mapCategory() != MapCategory.ARCADE);
    }
}