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
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColoredSpriteImage;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.MazeSpriteSet;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
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
import java.util.Map;
import java.util.Optional;
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
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {
    // 32 tiles (NES screen width)
    private static final int UNSCALED_CANVAS_WIDTH = NES_TILES.x() * TS;
    // 42 tiles (BIG maps height) + 2 extra rows
    private static final int UNSCALED_CANVAS_HEIGHT = 44 * TS;

    private final ObjectProperty<SceneDisplayMode> displayModeProperty = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private final SubScene subScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final Rectangle contentClipArea = new Rectangle();

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_GameLevelRenderer gameLevelRenderer;
    private ActorSpriteRenderer actorSpriteRenderer;

    private final BooleanProperty mazeHighlighted = new SimpleBooleanProperty(false);

    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        displayModeProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE);

        // use own canvas, not the shared canvas from the game view
        canvas = new Canvas();
        canvas.widthProperty() .bind(scalingProperty().multiply(UNSCALED_CANVAS_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_HEIGHT));

        // The maps are only 28 tiles wide. To avoid seeing the actors outside the map e.g. when going through portals,
        // 2 tiles on each side of the canvas are clipped. and not drawn.
        contentClipArea.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(2 * TS)));
        contentClipArea.yProperty().bind(canvas.translateYProperty());
        contentClipArea.widthProperty().bind(canvas.widthProperty().subtract(scalingProperty().multiply(4 * TS)));
        contentClipArea.heightProperty().bind(canvas.heightProperty());

        var root = new StackPane(canvas);
        root.setBackground(null);

        subScene = new SubScene(root, 88, 88); // size gets bound to parent scene size when embedded in game view
        subScene.fillProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        subScene.cameraProperty().bind(displayModeProperty()
            .map(displayMode -> displayMode == SceneDisplayMode.SCROLLING ? dynamicCamera : fixedCamera));

        dynamicCamera.scalingProperty().bind(scalingProperty());
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModeProperty;
    }

    private void setActionsBindings() {
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        if (context().gameLevel().isDemoLevel()) {
            actionBindings.assign(ACTION_QUIT_DEMO_LEVEL, tengenActionBindings);
        } else {
            // Steer Pac-Man using current "Joypad" settings
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
        GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = (TengenMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        gameLevelRenderer = (TengenMsPacMan_GameLevelRenderer) uiConfig.createGameLevelRenderer(canvas);
        actorSpriteRenderer = uiConfig.createActorSpriteRenderer(canvas);
        debugInfoRenderer = new PlaySceneDebugInfoRenderer(ui);

        bindRendererProperties(hudRenderer, gameLevelRenderer, actorSpriteRenderer, debugInfoRenderer);

        context().game().hudData().score(true).levelCounter(true).livesCounter(true);

        dynamicCamera.moveTop();
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
        }
    }

    @Override
    public void update() {
        context().optGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                ui.soundManager().setEnabled(false);
            } else {
                level.optMessage()
                    .filter(GameOverMessage.class::isInstance)
                    .map(GameOverMessage.class::cast)
                    .ifPresent(GameOverMessage::update);
                ui.soundManager().setEnabled(true);
                updateSound();
            }
            if (subScene.getCamera() == dynamicCamera) {
                if (context().gameState() == HUNTING) {
                    dynamicCamera.setFocussingActor(true);
                }
                dynamicCamera.setVerticalRangeInTiles(level.worldMap().numRows());
                dynamicCamera.update(level.pac());
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

    private void initForGameLevel() {
        context().game().hudData().showLevelCounter(true);
        context().game().hudData().showLivesCounter(true); // is also visible in demo level!
        setActionsBindings();

        //TODO check if this is needed, if not, remove
        gameLevelRenderer = (TengenMsPacMan_GameLevelRenderer) ui.currentConfig().createGameLevelRenderer(canvas);
        gameLevelRenderer.scalingProperty().bind(scaling);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel();
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        if (context().optGameLevel().isPresent()) {
            initForGameLevel();
        }
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.setIdleTime(90);
        dynamicCamera.setCameraTopOfScene();
        dynamicCamera.moveBottom();
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> dynamicCamera.setFocussingActor(true);
            case LEVEL_COMPLETE -> {
                ui.soundManager().stopAll();
                levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry);
                mazeHighlighted.bind(levelCompletedAnimation.highlightedProperty());
                levelCompletedAnimation.setGameLevel(context().gameLevel());
                levelCompletedAnimation.setSingleFlashMillis(333);
                levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(e -> context().gameController().letCurrentGameStateExpire());
                levelCompletedAnimation.playFromStart();
            }
            case GAME_OVER -> {
                ui.soundManager().stopAll();
                context().gameLevel().optMessage()
                    .filter(GameOverMessage.class::isInstance)
                    .map(GameOverMessage.class::cast)
                    .ifPresent(gameOverMessage -> {
                        double width = gameLevelRenderer.messageTextWidth(context().gameLevel(), MessageType.GAME_OVER);
                        gameOverMessage.start(sizeInPx().x(), width);
                    });
                dynamicCamera.moveTop();
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
    public void onGameContinued(GameEvent e) {
        context().optGameLevel().ifPresent(gameLevel -> context().game().showMessage(gameLevel, MessageType.READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
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
        clear();
        context().optGameLevel().ifPresent(gameLevel -> {
            updateScaling(gameLevel);
            ctx().save();
            // map width is 28 tiles but NES screen width is 32 tiles: move 2 tiles right and clip one tile on each side
            ctx().translate(scaled(TS(2)), 0);
            canvas.setClip(contentClipArea);
            drawGameLevel(context().gameLevel());
            drawActors();
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
        hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
    }

    @Override
    public void drawSceneContent() {
        // draw() is overridden and does the job
    }

    private void updateScaling(GameLevel gameLevel) {
        double scaling = switch (displayModeProperty.get()) {
            case SCALED_TO_FIT -> { //TODO this code smells
                int tilesY = gameLevel.worldMap().numRows() + 3;
                double camY = scaled((tilesY - 46) * HTS);
                fixedCamera.setTranslateY(camY);
                yield (subScene.getHeight() / (tilesY * TS));
            }
            case SCROLLING -> (subScene.getHeight() / NES_SIZE_PX.y());
        };
        setScaling(scaling);
    }

    private void drawActors() {
        actorsInZOrder.clear();
        context().gameLevel().bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(context().gameLevel().pac());
        ghostsByZ(context().gameLevel()).forEach(actorsInZOrder::add);
        actorsInZOrder.forEach(actor -> actorSpriteRenderer.drawActor(actor));
    }

    private void drawGameLevel(GameLevel gameLevel) {
        if (levelCompletedAnimation != null && levelCompletedAnimation.isRunning()) {
            if (mazeHighlighted.get()) {
                MazeSpriteSet recoloredMaze = gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY);
                // get the current maze flashing "animation frame"
                int frame = levelCompletedAnimation.flashingIndex();
                ColoredSpriteImage flashingMazeSprite = recoloredMaze.flashingMazeImages().get(frame);

                RenderInfo info = RenderInfo.build(Map.of(
                    "mazeBright", false,
                    "blinkingPhaseOn", false,
                    "mazeImage", flashingMazeSprite.spriteSheetImage(),
                    "mazeSprite", flashingMazeSprite.sprite()
                ));
                gameLevelRenderer.drawGameLevel(context(), info);
            } else {
                RenderInfo info = RenderInfo.build(Map.of(
                    "mazeBright", false,
                    "blinkingPhaseOn", false
                ));
                gameLevelRenderer.drawGameLevel(context(), info);
            }
        }
        else {
            RenderInfo info = RenderInfo.build(Map.of(
                "mazeBright", false,
                "blinkingPhaseOn", false
            ));
            gameLevelRenderer.drawGameLevel(context(), info);
        }
    }

    private Stream<Ghost> ghostsByZ(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }

    private void updateHUD() {
        TengenMsPacMan_HUDData hud = context().<TengenMsPacMan_GameModel>game().hudData();
        int numLives = context().game().lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (context().gameState() == GamePlayState.STARTING_GAME && !context().gameLevel().pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, hud.theLivesCounter().maxLivesDisplayed());
        hud.theLivesCounter().setVisibleLifeCount(numLives);

        //TODO check demo level behavior in emulator. Are there demo levels for non-ARCADE maps at all?
        TengenMsPacMan_LevelCounter levelCounter = hud.theLevelCounter();
        if (context().<TengenMsPacMan_GameModel>game().mapCategory() == MapCategory.ARCADE
            || context().optGameLevel().isEmpty()
            || context().gameLevel().isDemoLevel()) {
            levelCounter.setDisplayedLevelNumber(0); // no level number boxes for ARCADE maps or when level not yet created
        } else {
            levelCounter.setDisplayedLevelNumber(context().gameLevel().number());
        }
    }

    private class PlaySceneDebugInfoRenderer extends DefaultDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(GameUI ui) {
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
}