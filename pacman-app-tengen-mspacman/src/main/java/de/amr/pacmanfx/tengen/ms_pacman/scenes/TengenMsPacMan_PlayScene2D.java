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
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUDData;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_LevelCounter;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColoredMazeSpriteSet;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.RecoloredSpriteImage;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
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
import static de.amr.pacmanfx.controller.GamePlayState.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
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

    private static final int MESSAGE_MOVEMENT_DELAY = 120;

    private final ObjectProperty<SceneDisplayMode> displayModeProperty = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private final SubScene subScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final Rectangle canvasClipArea = new Rectangle();

    private TengenMsPacMan_SpriteSheet spriteSheet;

    private MessageMovement messageMovement;
    private LevelCompletedAnimation levelCompletedAnimation;

    private final List<Actor> actorsByZ = new ArrayList<>();

    public TengenMsPacMan_PlayScene2D(GameUI ui) {
        super(ui);

        displayModeProperty().bind(PROPERTY_PLAY_SCENE_DISPLAY_MODE);

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
        var tengenBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenMsPacManBindings();
        if (gameContext().gameLevel().isDemoLevel()) {
            actionBindings.assign(ACTION_QUIT_DEMO_LEVEL, tengenBindings);
        } else {
            // Steer Pac-Man using current "Joypad" settings
            actionBindings.assign(ACTION_STEER_UP,    tengenBindings);
            actionBindings.assign(ACTION_STEER_DOWN,  tengenBindings);
            actionBindings.assign(ACTION_STEER_LEFT,  tengenBindings);
            actionBindings.assign(ACTION_STEER_RIGHT, tengenBindings);

            actionBindings.assign(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, tengenBindings);
            actionBindings.assign(ACTION_TOGGLE_PAC_BOOSTER, tengenBindings);

            actionBindings.assign(ACTION_CHEAT_ADD_LIVES,        ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS,  ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS,      ui.actionBindings());
        }
        actionBindings.installBindings(ui.keyboard());
    }

    @Override
    public void doInit() {
        spriteSheet = (TengenMsPacMan_SpriteSheet) ui.currentConfig().spriteSheet();
        setGameRenderer(ui.currentConfig().createGameRenderer(canvas));

        messageMovement = new MessageMovement();
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry);

        gameContext().game().hudData().showScore(true);
        gameContext().game().hudData().showLevelCounter(true);
        gameContext().game().hudData().showLivesCounter(true);

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
        gameContext().optGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                ui.soundManager().setEnabled(false);
            } else {
                messageMovement.update();
                ui.soundManager().setEnabled(true);
                updateSound();
            }
            if (subScene.getCamera() == dynamicCamera) {
                if (gameContext().gameState() == HUNTING) {
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
            createContextMenuTitle("pacman", ui.uiPreferences(), ui.assets()),
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
        GameState state = gameContext().gameState();
        boolean shutUp = gameContext().gameLevel().isDemoLevel()
            || state.is(LevelShortTestState.class)
            || state.is(LevelMediumTestState.class);
        if (!shutUp) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    private void initForGameLevel() {
        gameContext().game().hudData().showLevelCounter(true);
        gameContext().game().hudData().showLivesCounter(true); // is also visible in demo level!
        setActionsBindings();
        //TODO check if this is needed?
        setGameRenderer(ui.currentConfig().createGameRenderer(canvas));
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initForGameLevel();
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        // Switch might occur just during the few ticks when level is not yet available!
        if (gameContext().optGameLevel().isPresent()) {
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
                levelCompletedAnimation.setGameLevel(gameContext().gameLevel());
                levelCompletedAnimation.setSingleFlashMillis(333);
                levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(e -> gameContext().gameController().letCurrentGameStateExpire());
                levelCompletedAnimation.playFromStart();
            }
            case GAME_OVER -> {
                ui.soundManager().stopAll();
                // After some delay, the "game over" message moves from the center to the right border, wraps around,
                // appears at the left border and moves to the center again (for non-Arcade maps)
                if (gameContext().<TengenMsPacMan_GameModel>game().mapCategory() != MapCategory.ARCADE) {
                    gameContext().gameLevel().house().ifPresent(house -> {
                        float startX = house.centerPositionUnderHouse().x();
                        float wrappingX = sizeInPx().x();
                        messageMovement.start(MESSAGE_MOVEMENT_DELAY, startX, wrappingX);
                    });
                }
                dynamicCamera.moveTop();
            }
            default -> {
                //TODO
            }
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
        gameContext().optGameLevel().ifPresent(level -> level.showMessage(GameLevel.MESSAGE_READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
        gameContext().gameController().letCurrentGameStateExpire();
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
        final Pac pac = gameContext().gameLevel().pac();

        //TODO check in simulator when exactly which siren plays
        boolean pacChased = gameContext().gameState() == HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext().game().huntingTimer().phaseIndex();
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
        var ghostReturningToHouse = gameContext().gameLevel()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .findAny();
        if (ghostReturningToHouse.isPresent()
            && (gameContext().gameState() == HUNTING || gameContext().gameState() == GamePlayState.GHOST_DYING)) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
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
        final GameLevel gameLevel = gameContext().gameLevel();

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

        gameRenderer.ctx().save();

        if (debugInfoVisibleProperty.get()) {
            canvas.setClip(null);
            drawSceneContent();
            drawDebugInfo();
        } else {
            canvas.setClip(canvasClipArea);
            drawSceneContent();
        }
        // NES screen is 32 tiles wide but mazes are only 28 tiles wide, so shift HUD right:
        gameRenderer.ctx().translate(scaled(2 * TS), 0);
        renderer().drawHUD(gameContext(), gameContext().game().hudData(), sizeInPx(), ui.clock().tickCount());

        gameRenderer.ctx().restore();
    }

    @Override
    public void drawSceneContent() {
        gameRenderer.applyLevelSettings(gameContext());
        gameRenderer.ctx().save();
        gameRenderer.ctx().translate(scaled(2 * TS), 0);
        GameLevel gameLevel = gameContext().gameLevel();
        if (levelCompletedAnimation.isRunning()) {
            if (levelCompletedAnimation.isHighlighted()) {
                ColoredMazeSpriteSet recoloredMaze = gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.RECOLORED_MAZE_PROPERTY);
                // get the current maze flashing "animation frame"
                int frame = levelCompletedAnimation.flashingIndex();
                RecoloredSpriteImage flashingMazeSprite = recoloredMaze.flashingMazeSprites().get(frame);
                renderer().drawLevelWithMaze(gameContext(), gameLevel, flashingMazeSprite.image(), flashingMazeSprite.sprite());
            } else {
                renderer().drawLevel(gameContext(), null, false, false, ui.clock().tickCount());
            }
        }
        else {
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            renderer().drawLevelMessage(gameLevel, currentMessagePosition(), scaledArcadeFont8());
            renderer().drawLevel(gameContext(), null, false, false, ui.clock().tickCount());
        }

        actorsByZ.clear();
        gameLevel.bonus().ifPresent(actorsByZ::add);
        actorsByZ.add(gameLevel.pac());
        ghostsByZ(gameLevel).forEach(actorsByZ::add);
        actorsByZ.forEach(actor -> gameRenderer.drawActor(actor, spriteSheet.sourceImage()));

        gameRenderer.ctx().restore();
    }

    @Override
    protected void drawDebugInfo() {
        renderer().drawTileGrid(UNSCALED_CANVAS_WIDTH, UNSCALED_CANVAS_HEIGHT, Color.LIGHTGRAY);
        gameRenderer.ctx().save();
        gameRenderer.ctx().translate(scaled(2 * TS), 0);
        gameRenderer.ctx().setFill(debugTextFill);
        gameRenderer.ctx().setFont(debugTextFont);
        gameRenderer.ctx().fillText("%s %d".formatted(gameContext().gameState(), gameContext().gameState().timer().tickCount()), 0, scaled(3 * TS));
        if (gameContext().optGameLevel().isPresent()) {
            gameRenderer.drawMovingActorInfo(gameRenderer.ctx(), scaling(), gameContext().gameLevel().pac());
            ghostsByZ(gameContext().gameLevel())
                    .forEach(ghost -> gameRenderer.drawMovingActorInfo(gameRenderer.ctx(), scaling(), ghost));
        }
        gameRenderer.ctx().restore();
    }

    private Stream<Ghost> ghostsByZ(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }

    private Vector2f currentMessagePosition() {
        House house = gameContext().gameLevel().house().orElse(null);
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
        TengenMsPacMan_HUDData hud = gameContext().<TengenMsPacMan_GameModel>game().hudData();
        int numLives = gameContext().game().lifeCount() - 1;
        // As long as Pac-Man is still invisible on start, he is shown as an additional entry in the lives counter
        if (gameContext().gameState() == GamePlayState.STARTING_GAME && !gameContext().gameLevel().pac().isVisible()) {
            numLives += 1;
        }
        numLives = Math.min(numLives, hud.theLivesCounter().maxLivesDisplayed());
        hud.theLivesCounter().setVisibleLifeCount(numLives);

        //TODO check demo level behavior in emulator. Are there demo levels for non-ARCADE maps at all?
        TengenMsPacMan_LevelCounter levelCounter = hud.theLevelCounter();
        if (gameContext().<TengenMsPacMan_GameModel>game().mapCategory() == MapCategory.ARCADE
            || gameContext().optGameLevel().isEmpty()
            || gameContext().gameLevel().isDemoLevel()) {
            levelCounter.setDisplayedLevelNumber(0); // no level number boxes for ARCADE maps or when level not yet created
        } else {
            levelCounter.setDisplayedLevelNumber(gameContext().gameLevel().number());
        }
    }
}