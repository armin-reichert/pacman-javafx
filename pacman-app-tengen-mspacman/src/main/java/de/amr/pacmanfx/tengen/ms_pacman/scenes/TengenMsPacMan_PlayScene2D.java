/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_LevelCounter;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColorSchemedSprite;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.uilib.Ufx.menuTitleItem;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D {
    // 32 tiles (NES screen width)
    private static final int UNSCALED_CANVAS_WIDTH = NES_TILES.x() * TS;
    // 42 tiles (BIG maps height) + 2 extra rows
    private static final int UNSCALED_CANVAS_HEIGHT = 44 * TS;

    private static final int MESSAGE_MOVEMENT_DELAY = 120;

    private final ObjectProperty<SceneDisplayMode> displayModeProperty = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private final SubScene subScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final Rectangle canvasClipArea = new Rectangle();

    private MessageMovement messageMovement;
    private LevelCompletedAnimation levelCompletedAnimation;

    private final List<Actor> actorsByZ = new ArrayList<>();

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        // use own canvas, not the shared canvas from the game view
        canvas = new Canvas();
        canvas.widthProperty() .bind(scalingProperty().multiply(UNSCALED_CANVAS_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_HEIGHT));

        // The maps are only 28 tiles wide. To avoid seeing the actors outside the map e.g. when going through portals,
        // 2 tiles on each side of the canvas are clipped. and not drawn.
        canvasClipArea.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(2 * TS)));
        canvasClipArea.yProperty().bind(canvas.translateYProperty());
        canvasClipArea.widthProperty().bind(canvas.widthProperty().subtract(scalingProperty().multiply(4 * TS)));
        canvasClipArea.heightProperty().bind(canvas.heightProperty());

        var root = new StackPane(canvas);
        root.setBackground(Background.EMPTY);

        subScene = new SubScene(root, 88, 88); // size gets bound to parent scene size when embedded in game view
        subScene.setFill(backgroundColor());
        subScene.cameraProperty().bind(displayModeProperty()
            .map(displayMode -> displayMode == SceneDisplayMode.SCROLLING ? dynamicCamera : fixedCamera));

        dynamicCamera.scalingProperty().bind(scalingProperty());
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModeProperty;
    }

    private void setActionsBindings() {
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        if (gameContext().theGameLevel().isDemoLevel()) {
            actionBindings.bind(config.ACTION_QUIT_DEMO_LEVEL, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        } else {
            // Steer Pac-Man using current "Joypad" settings
            actionBindings.bind(ACTION_STEER_UP,    config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_DOWN,  config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_LEFT,  config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_RIGHT, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);

            actionBindings.bind(config.ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(config.ACTION_TOGGLE_PAC_BOOSTER, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);

            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
        }
        actionBindings.updateKeyboard();
    }

    @Override
    public void doInit() {
        setGameRenderer(ui.theConfiguration().createGameRenderer(canvas));

        messageMovement = new MessageMovement();
        levelCompletedAnimation = new LevelCompletedAnimation(animationManager);

        gameContext().theGame().theHUD().showScore(true);
        gameContext().theGame().theHUD().showLevelCounter(true);
        gameContext().theGame().theHUD().showLivesCounter(true);

        dynamicCamera.moveTop();
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            animationManager.destroyAnimation(levelCompletedAnimation);
        }
    }

    @Override
    public void update() {
        gameContext().optGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                ui.theSound().setEnabled(false);
            } else {
                messageMovement.update();
                ui.theSound().setEnabled(true);
                updateSound();
            }
            if (subScene.getCamera() == dynamicCamera) {
                if (gameContext().theGameState() == GameState.HUNTING) {
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
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        SceneDisplayMode displayMode = config.propertyPlaySceneDisplayMode.get();

        miScaledToFit = new RadioMenuItem(ui.theAssets().text("scaled_to_fit"));
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(e -> config.propertyPlaySceneDisplayMode.set(SceneDisplayMode.SCALED_TO_FIT));

        miScrolling = new RadioMenuItem(ui.theAssets().text("scrolling"));
        miScrolling.setSelected(displayMode == SceneDisplayMode.SCROLLING);
        miScrolling.setOnAction(e -> config.propertyPlaySceneDisplayMode.set(SceneDisplayMode.SCROLLING));

        toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        config.propertyPlaySceneDisplayMode.addListener(this::handlePlaySceneDisplayModeChange);
        Logger.info("Added listener to config propertyPlaySceneDisplayMode property");
        //TODO might interfere with onHidden event handler set elsewhere on this menu
        menu.setOnHidden(e -> {
            config.propertyPlaySceneDisplayMode.removeListener(this::handlePlaySceneDisplayModeChange);
            Logger.info("Removed listener from config propertyPlaySceneDisplayMode property");
        });

        var miAutopilot = new CheckMenuItem(ui.theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(theGameContext().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(theGameContext().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(ui.propertyMuted());

        var miQuit = new MenuItem(ui.theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            miScaledToFit,
            miScrolling,
            menuTitleItem(ui.theAssets().text("pacman")),
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
        return gameContext().optGameLevel().map(GameLevel::worldSizePx).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean shutUp = gameContext().theGameLevel().isDemoLevel()
            || gameContext().theGameState() == TESTING_LEVELS_SHORT
            || gameContext().theGameState() == TESTING_LEVELS_MEDIUM;
        if (!shutUp) {
            ui.theSound().play(SoundID.GAME_READY);
        }
    }

    private void initForGameLevel(GameLevel gameLevel) {
        gameContext().theGame().theHUD().showLevelCounter(true);
        gameContext().theGame().theHUD().showLivesCounter(true); // is also visible in demo level!
        setActionsBindings();
        //TODO needed?
        setGameRenderer(ui.theConfiguration().createGameRenderer(canvas));
        renderer().ensureRenderingHintsAreApplied(gameLevel);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel(theGameContext().theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        if (gameContext().optGameLevel().isPresent()) {
            initForGameLevel(gameContext().theGameLevel());
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
                ui.theSound().stopAll();
                levelCompletedAnimation.setGameLevel(gameContext().theGameLevel());
                levelCompletedAnimation.setSingleFlashMillis(333);
                levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> gameContext().theGameController().letCurrentGameStateExpire());
                levelCompletedAnimation.playFromStart();
            }
            case GAME_OVER -> {
                // After some delay, the "game over" message moves from the center to the right border, wraps around,
                // appears at the left border and moves to the center again (for non-Arcade maps)
                if (gameContext().<TengenMsPacMan_GameModel>theGame().mapCategory() != MapCategory.ARCADE) {
                    gameContext().theGameLevel().house().ifPresent(house -> {
                        float startX = house.centerPositionUnderHouse().x();
                        float wrappingX = sizeInPx().x();
                        messageMovement.start(MESSAGE_MOVEMENT_DELAY, startX, wrappingX);
                    });
                }
                dynamicCamera.moveTop();
            }
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        ui.theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        ui.theSound().stop(SoundID.BONUS_ACTIVE);
        ui.theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        ui.theSound().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        ui.theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        gameContext().optGameLevel().ifPresent(level -> level.showMessage(GameLevel.MESSAGE_READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
        gameContext().theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        ui.theSound().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        ui.theSound().pauseSiren();
        ui.theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        ui.theSound().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound() {
        final Pac pac = gameContext().theGameLevel().pac();

        //TODO check in simulator when exactly which siren plays
        boolean pacChased = gameContext().theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext().theGame().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> ui.theSound().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> ui.theSound().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> ui.theSound().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> ui.theSound().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTicks() > 10) {
            ui.theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturningToHouse = gameContext().theGameLevel()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .findAny();
        if (ghostReturningToHouse.isPresent()
            && (gameContext().theGameState() == GameState.HUNTING || gameContext().theGameState() == GameState.GHOST_DYING)) {
            ui.theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    // drawing

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        clear();
        if (gameContext().optGameLevel().isEmpty()) {
            return; // Scene is drawn already 2 ticks before level has been created
        }
        final GameLevel gameLevel = gameContext().theGameLevel();

        // compute current scene scaling
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

        renderer().setScaling(scaling);
        renderer().ensureRenderingHintsAreApplied(gameContext().theGameLevel());

        ctx().save();

        if (debugInfoVisibleProperty.get()) {
            canvas.setClip(null);
            drawSceneContent();
            drawDebugInfo();
        } else {
            canvas.setClip(canvasClipArea);
            drawSceneContent();
        }
        // NES screen is 32 tiles wide but mazes are only 28 tiles wide, so shift HUD right:
        ctx().translate(scaled(2 * TS), 0);
        renderer().drawHUD(gameContext(), gameContext().theGame().theHUD(), sizeInPx(), ui.theGameClock().tickCount());

        ctx().restore();
    }

    @Override
    public void drawSceneContent() {
        ctx().save();
        ctx().translate(scaled(2 * TS), 0);

        if (levelCompletedAnimation.isRunning()) {
            if (levelCompletedAnimation.isHighlighted()) {
                // get the current flashing maze "animation frame"
                int frameIndex = levelCompletedAnimation.flashingIndex();
                ColorSchemedSprite flashingMazeSprite = renderer().mazeConfig().flashingMazeSprites().get(frameIndex);
                renderer().drawLevelWithMaze(gameContext(), gameContext().theGameLevel(), flashingMazeSprite.image(), flashingMazeSprite.sprite());
            } else {
                renderer().drawLevel(gameContext(), gameContext().theGameLevel(), null, false, false, ui.theGameClock().tickCount());
            }
        }
        else {
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            renderer().drawLevelMessage(ui.theConfiguration(), gameContext().theGameLevel(), currentMessagePosition(), scaledArcadeFont8());
            renderer().drawLevel(gameContext(), gameContext().theGameLevel(), null, false, false, ui.theGameClock().tickCount());
        }

        actorsByZ.clear();
        gameContext().theGameLevel().bonus().ifPresent(actorsByZ::add);
        actorsByZ.add(gameContext().theGameLevel().pac());
        ghostsByZ(gameContext().theGameLevel()).forEach(actorsByZ::add);
        renderer().drawActors(actorsByZ);

        ctx().restore();
    }

    @Override
    protected void drawDebugInfo() {
        renderer().drawTileGrid(UNSCALED_CANVAS_WIDTH, UNSCALED_CANVAS_HEIGHT, Color.LIGHTGRAY);
        ctx().save();
        ctx().translate(scaled(2 * TS), 0);
        ctx().setFill(DEBUG_TEXT_FILL);
        ctx().setFont(DEBUG_TEXT_FONT);
        ctx().fillText("%s %d".formatted(gameContext().theGameState(), gameContext().theGameState().timer().tickCount()), 0, scaled(3 * TS));
        if (gameContext().optGameLevel().isPresent()) {
            renderer().drawMovingActorInfo(gameContext().theGameLevel().pac());
            ghostsByZ(gameContext().theGameLevel()).forEach(renderer()::drawMovingActorInfo);
        }
        ctx().restore();
    }

    private Stream<Ghost> ghostsByZ(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }

    private Vector2f currentMessagePosition() {
        House house = gameContext().theGameLevel().house().orElse(null);
        if (house == null) {
            Logger.error("No house in game level!");
            return Vector2f.ZERO; //TODO
        }
        Vector2f center = house.centerPositionUnderHouse();
        return messageMovement != null && messageMovement.isRunning()
            ? new Vector2f(messageMovement.currentX(), center.y())
            : center;
    }

    private void updateHUD() {
        TengenMsPacMan_HUD hud = gameContext().<TengenMsPacMan_GameModel>theGame().theHUD();
        int numLives = gameContext().theGame().lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (gameContext().theGameState() == GameState.STARTING_GAME && !gameContext().theGameLevel().pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, hud.theLivesCounter().maxLivesDisplayed());
        hud.theLivesCounter().setVisibleLifeCount(numLives);

        //TODO check demo level behavior in emulator. Are there demo levels for non-ARCADE maps at all?
        TengenMsPacMan_LevelCounter levelCounter = hud.theLevelCounter();
        if (gameContext().<TengenMsPacMan_GameModel>theGame().mapCategory() == MapCategory.ARCADE
            || gameContext().optGameLevel().isEmpty()
            || gameContext().theGameLevel().isDemoLevel()) {
            levelCounter.setDisplayedLevelNumber(0); // no level number boxes for ARCADE maps or when level not yet created
        } else {
            levelCounter.setDisplayedLevelNumber(gameContext().theGameLevel().number());
        }
    }
}