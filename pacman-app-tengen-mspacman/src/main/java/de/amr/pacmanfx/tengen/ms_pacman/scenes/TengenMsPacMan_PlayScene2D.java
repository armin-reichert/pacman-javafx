/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.teststates.LevelMediumTestState;
import de.amr.pacmanfx.controller.teststates.LevelShortTestState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.GameOverMessage;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
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
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {

    /** Unscaled canvas width: 32 tiles (NES screen width) */
    public static final int UNSCALED_CANVAS_WIDTH = NES_TILES.x() * TS;

    /** Unscaled canvas height: 42 tiles (BIG maps height) + 2 extra rows */
    public static final int UNSCALED_CANVAS_HEIGHT = 44 * TS;

    private final ObjectProperty<SceneDisplayMode> displayModeProperty = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);
    private final BooleanProperty mazeHighlighted = new SimpleBooleanProperty(false);

    private final SubScene subScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final Rectangle clipRect = new Rectangle();

    private class TengenPlaySceneDebugInfoRenderer extends DefaultDebugInfoRenderer {

        public TengenPlaySceneDebugInfoRenderer(GameUI ui) {
            super(ui, canvas);
        }

        @Override
        public void drawDebugInfo() {
            drawTileGrid(UNSCALED_CANVAS_WIDTH, UNSCALED_CANVAS_HEIGHT, Color.LIGHTGRAY);
            ctx.save();
            ctx.translate(scaled(2 * TS), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(context().gameState(), context().gameState().timer().tickCount()), 0, scaled(3 * TS));
            if (context().optGameLevel().isPresent()) {
                drawMovingActorInfo(ctx, scaling(), context().gameLevel().pac());
                ghostsByZ(context().gameLevel()).forEach(ghost -> drawMovingActorInfo(ctx, scaling(), ghost));
            }
            ctx.restore();
        }
    }

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_GameLevelRenderer gameLevelRenderer;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        displayModeProperty.bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE);

        // Play scene uses its own canvas, not the one from the game view
        canvas = new Canvas();
        canvas.widthProperty() .bind(scalingProperty().multiply(UNSCALED_CANVAS_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_HEIGHT));

        // The maps are 28 tiles wide while the NES screen is 32 tiles wide. The map is displayed horizontally centered
        // on the NES screen and the unused 2 tiles on each side are clipped.
        clipRect.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(2 * TS)));
        clipRect.yProperty().bind(canvas.translateYProperty());
        clipRect.widthProperty().bind(canvas.widthProperty().subtract(scalingProperty().multiply(4 * TS)));
        clipRect.heightProperty().bind(canvas.heightProperty());

        var rootPane = new StackPane(canvas);
        rootPane.setBackground(null);

        // Scene size gets bound to parent scene size when embedded in game view so initial size is 88 ("doesn't matter")
        subScene = new SubScene(rootPane, 88, 88);
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(displayModeProperty.map(mode -> mode == SceneDisplayMode.SCROLLING ? dynamicCamera : fixedCamera));

        dynamicCamera.scalingProperty().bind(scalingProperty());
    }

    private void setActionsBindings(boolean demoLevel) {
        Set<ActionBinding> tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        if (demoLevel) {
            actionBindings.assign(ACTION_QUIT_DEMO_LEVEL, tengenActionBindings);
        } else {
            // Pac-Man is steered with keys representing the "Joypad" buttons
            actionBindings.assign(ACTION_STEER_UP,    tengenActionBindings);
            actionBindings.assign(ACTION_STEER_DOWN,  tengenActionBindings);
            actionBindings.assign(ACTION_STEER_LEFT,  tengenActionBindings);
            actionBindings.assign(ACTION_STEER_RIGHT, tengenActionBindings);

            actionBindings.assign(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenActionBindings);
            actionBindings.assign(ACTION_TOGGLE_PAC_BOOSTER, tengenActionBindings);

            actionBindings.assign(ACTION_CHEAT_ADD_LIVES,        ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS,  ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS,      ui.actionBindings());
        }
        actionBindings.installBindings(ui.keyboard());
    }

    @Override
    public void doInit() {
        TengenMsPacMan_UIConfig uiConfig = ui.currentConfig();

        hudRenderer       = uiConfig.createHUDRenderer(canvas);
        gameLevelRenderer = uiConfig.createGameLevelRenderer(canvas);
        actorRenderer     = uiConfig.createActorRenderer(canvas);
        debugInfoRenderer = new TengenPlaySceneDebugInfoRenderer(ui);

        bindRendererProperties(hudRenderer, gameLevelRenderer, actorRenderer, debugInfoRenderer);

        context().game().hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(true);

        dynamicCamera.targetTop();
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
        }
    }

    @Override
    public void update() {
        context().optGameLevel().ifPresent(gameLevel -> {
            if (gameLevel.isDemoLevel()) {
                ui.soundManager().setEnabled(false);
            } else {
                ui.soundManager().setEnabled(true);
                // Update moving "game over" message if present
                gameLevel.optMessage()
                    .filter(GameOverMessage.class::isInstance)
                    .map(GameOverMessage.class::cast)
                    .ifPresent(GameOverMessage::update);
                updateSound();
            }
            updateCamera(gameLevel);
            updateHUD();
        });
    }

    private void updateCamera(GameLevel gameLevel) {
        if (subScene.getCamera() == dynamicCamera) {
            //TODO check if this is correct
            dynamicCamera.setFollowTarget(context().gameState() == HUNTING);
            dynamicCamera.setVerticalRangeInTiles(gameLevel.worldMap().numRows());
            dynamicCamera.update(gameLevel.pac());
        }
    }

    // Context menu

    private ToggleGroup toggleGroup;
    private RadioMenuItem miScrolling;
    private RadioMenuItem miScaledToFit;

    private void handlePlaySceneDisplayModeChange(
        ObservableValue<? extends SceneDisplayMode> property, SceneDisplayMode oldMode, SceneDisplayMode newMode) {
        toggleGroup.selectToggle(newMode == SceneDisplayMode.SCROLLING ? miScrolling : miScaledToFit);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        SceneDisplayMode displayMode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();

        miScaledToFit = new RadioMenuItem(ui.assets().translated("scaled_to_fit"));
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(e -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCALED_TO_FIT));

        miScrolling = new RadioMenuItem(ui.assets().translated("scrolling"));
        miScrolling.setSelected(displayMode == SceneDisplayMode.SCROLLING);
        miScrolling.setOnAction(e -> PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCROLLING));

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
    public Vector2f sizeInPx() {
        return context().optGameLevel().map(GameLevel::worldSizePx).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameState state = context().gameState();
        boolean shutUp = context().gameLevel().isDemoLevel()
            || state.is(LevelShortTestState.class)
            || state.is(LevelMediumTestState.class);
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
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.setIdleTime(90);
        dynamicCamera.moveTop();
        dynamicCamera.targetBottom();
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
                dynamicCamera.targetTop();
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
            .filter(GameOverMessage.class::isInstance)
            .map(GameOverMessage.class::cast)
            .ifPresent(gameOverMessage -> {
                double width = gameLevelRenderer.messageTextWidth(gameLevel, MessageType.GAME_OVER);
                gameOverMessage.start(sizeInPx().x(), width);
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
        dynamicCamera.targetTop();
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
            int huntingPhase = context().game().huntingTimer().phaseIndex();
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
        if (pac.starvingTicks() > 10) {
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
        gameLevelRenderer.clearCanvas();
        context().optGameLevel().ifPresent(gameLevel -> {
            updateScaling(gameLevel);
            ctx().save();
            // map width is 28 tiles but NES screen width is 32 tiles: move 2 tiles right and clip one tile on each side
            ctx().translate(scaled(TS(2)), 0);
            canvas.setClip(clipRect);
            drawGameLevel(gameLevel);
            drawActors(gameLevel);
            drawHUD();
            if (debugInfoVisible.get() && debugInfoRenderer != null) {
                // debug info also used normally clipped area
                canvas.setClip(null);
                ctx().translate(scaled(-TS(2)), 0);
                debugInfoRenderer.drawDebugInfo();
            }
            ctx().restore();
        });
    }

    @Override
    public void drawHUD() {
        TengenMsPacMan_GameModel game = context().game();
        game.hud().showGameOptions(!game.optionsAreInitial());
        hudRenderer.drawHUD(context(), game.hud(), sizeInPx());
    }

    @Override
    public void drawSceneContent() {
        // draw() is overridden and does the job
    }

    private void updateScaling(GameLevel gameLevel) {
        double scaling = switch (displayModeProperty.get()) {
            case SCALED_TO_FIT -> {
                //TODO this code smells
                int tilesY = gameLevel.worldMap().numRows() + 3;
                double y = scaled((tilesY - 46) * HTS);
                fixedCamera.setTranslateY(y);
                yield subScene.getHeight() / TS(tilesY);
            }
            case SCROLLING -> subScene.getHeight() / NES_SIZE_PX.y();
        };
        setScaling(scaling);
    }

    private void drawActors(GameLevel gameLevel) {
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        ghostsByZ(gameLevel).forEach(actorsInZOrder::add);
        actorsInZOrder.forEach(actor -> actorRenderer.drawActor(actor));
    }

    private void drawGameLevel(GameLevel gameLevel) {
        TengenMsPacMan_UIConfig uiConfig = ui.currentConfig();
        RenderInfo info = new RenderInfo();
        gameLevelRenderer.applyLevelSettings(gameLevel, info); // ensure maze sprite set is stored in render info
        boolean bright = levelCompletedAnimation != null && mazeHighlighted.get();
        if (bright) {
            uiConfig.configureHighlightedMazeRenderInfo(info, gameLevel, levelCompletedAnimation.flashingIndex());
        } else {
            uiConfig.configureNormalMazeRenderInfo(info, context().game(), gameLevel, ui.clock().tickCount());
        }
        gameLevelRenderer.drawGameLevel(gameLevel, info);
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
        numLives = Math.min(numLives, game.maxLivesDisplayed());
        game.hud().setVisibleLifeCount(numLives);

        game.hud().showLevelNumber(game.mapCategory() != MapCategory.ARCADE);
    }
}