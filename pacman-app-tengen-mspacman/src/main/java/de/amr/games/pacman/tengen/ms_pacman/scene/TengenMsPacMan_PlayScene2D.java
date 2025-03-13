/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapCategory;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.MessageMovement;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui.CameraControlledView;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.LevelCompleteAnimation;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions.QUIT_DEMO_LEVEL;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameUIConfig3D.*;
import static de.amr.games.pacman.ui._2d.GameActions2D.bindCheatActions;
import static de.amr.games.pacman.ui._2d.GameActions2D.bindFallbackPlayerControlActions;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_AUTOPILOT;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_IMMUNITY;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CameraControlledView {

    // (NES screen width, BIG map height (42 tiles) + 2 extra tile rows)
    private static final Vector2i UNSCALED_CANVAS_SIZE = vec_2i(NES_SIZE.x(), 44 * TS);

    private static final int MOVING_MESSAGE_DELAY = 120;

    private static class MovingCamera extends ParallelCamera {
        private static final float CAM_SPEED = 0.03f;

        private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);

        private int idleTicks;
        private int verticalRangeTiles;
        private double targetY;
        private boolean focusPlayer;

        public void setIdleTicks(int ticks) {
            idleTicks = ticks;
        }

        public void setVerticalRangeTiles(int numTiles) {
            verticalRangeTiles = numTiles;
        }

        public DoubleProperty scalingProperty() {
            return scalingPy;
        }

        public void setCameraToTopOfScene() {
            setTranslateY(camMinY());
        }

        public void focusTopOfScene() {
            targetY = camMinY();
            focusPlayer = false;
        }

        public void focusBottomOfScene() {
            targetY = camMaxY();
            focusPlayer = false;
        }

        public double camMinY() {
            return scalingPy.get() * (-9 * TS);
        }

        public double camMaxY() {
            return scalingPy.get() * (verticalRangeTiles - 35) * TS;
        }

        public void focusPlayer(boolean focus) {
            focusPlayer = focus;
        }

        public void update(Pac pac) {
            if (idleTicks > 0) {
                --idleTicks;
                return;
            }
            if (focusPlayer) {
                double frac = (double) pac.tile().y() / verticalRangeTiles;
                if (frac < 0.4) { frac = 0; } else if (frac > 0.6) { frac = 1.0; }
                targetY = lerp(camMinY(), camMaxY(), frac);
            }
            double y = lerp(getTranslateY(), targetY, CAM_SPEED);
            setTranslateY(clamp(y, camMinY(), camMaxY()));
            Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, camMinY(), camMaxY());
        }
    }

    private final SubScene fxSubScene;
    private final Canvas canvas;
    private final MovingCamera movingCamera;
    private final ParallelCamera fixedCamera;
    private final ObjectProperty<SceneDisplayMode> displayModePy = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private MessageMovement messageMovement;
    private LevelCompleteAnimation levelCompleteAnimation;

    public TengenMsPacMan_PlayScene2D() {
        canvas = new Canvas();
        canvas.widthProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.x()));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.y()));

        // maze is drawn centered inside canvas: clip left and right vertical stripes (2 tiles wide each)
        var clip = new Rectangle();
        int stripeWidth = 2 * TS;
        clip.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(stripeWidth)));
        clip.yProperty().bind(canvas.translateYProperty());
        clip.widthProperty().bind(canvas.widthProperty().subtract(scalingPy.multiply(2 * stripeWidth)));
        clip.heightProperty().bind(canvas.heightProperty());
        canvas.setClip(clip);

        var root = new StackPane(canvas);
        root.setBackground(null);

        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setFill(nesPaletteColor(0x0f));

        movingCamera = new MovingCamera();
        movingCamera.scalingProperty().bind(scalingProperty());

        fixedCamera = new ParallelCamera();

        fxSubScene.cameraProperty().bind(displayModeProperty()
            .map(mode -> mode == SceneDisplayMode.SCROLLING ? movingCamera : fixedCamera));
    }

    @Override
    public void setCanvas(Canvas canvas) {
        // use our own canvas
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModePy;
    }

    protected void updateScaling() {
        SceneDisplayMode displayMode = displayModePy.get();
        double unscaledHeight = displayMode == SceneDisplayMode.SCROLLING ? NES_SIZE.y() : sizeInPx().y() + 3*TS;
        setScaling(viewPortHeightProperty().get() / unscaledHeight);
    }

    private void updateCameraPosition(double scaling) {
        int worldTilesY = context.game().level().map(level -> level.map().numRows()).orElse(NES_TILES.y());
        double dy = scaling * (worldTilesY - 43) * HTS;
        fixedCamera.setTranslateY(dy);
    }

    @Override
    public void bindGameActions() {
        bind(TengenMsPacMan_GameActions.TOGGLE_DISPLAY_MODE, Keyboard.alt(KeyCode.C));
    }

    @Override
    public void doInit() {
        messageMovement = new MessageMovement();
        context.joypadKeyBinding().register(context.keyboard());
        context.setScoreVisible(true);
        setGameRenderer(context.gameConfiguration().createRenderer(context.assets(), canvas));
        movingCamera.focusTopOfScene();
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.joypadKeyBinding().unregister(context.keyboard());
    }

    @Override
    public void update() {
        context.game().level().ifPresent(level -> {
            if (context.game().isDemoLevel()) {
                context.game().assignDemoLevelBehavior(level);
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                messageMovement.update();
                updateSound(level);
            }
            if (context.gameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.update();
            }
            if (fxSubScene.getCamera() == movingCamera) {
                if (context.gameState() == GameState.HUNTING) {
                    movingCamera.focusPlayer(true);
                }
                movingCamera.setVerticalRangeTiles(level.map().numRows());
                movingCamera.update(level.pac());
            }
        });
    }

    @Override
    public DoubleProperty viewPortWidthProperty() {
        return fxSubScene.widthProperty();
    }

    @Override
    public DoubleProperty viewPortHeightProperty() {
        return fxSubScene.heightProperty();
    }

    @Override
    public Node viewPort() {
        return fxSubScene;
    }

    @Override
    public Camera camera() {
        return fxSubScene.getCamera();
    }

    @Override
    public Vector2f sizeInPx() {
        return context.worldSizeInTilesOrElse(NES_TILES).toVector2f().scaled(TS);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.game().isDemoLevel() ||
            context.gameState() == TESTING_LEVELS ||
            context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        context.game().level().ifPresent(level -> {
            context.joypadKeyBinding().register(context.keyboard());
            setKeyBindings();
            if (context.game().isDemoLevel()) {
                level.pac().setImmune(false);
            } else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
            }
            createLevelCompleteAnimation(level);
            gr.setWorldMap(level.map());
        });
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        movingCamera.setCameraToTopOfScene();
        movingCamera.focusBottomOfScene();
        movingCamera.setIdleTicks(90);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        context.joypadKeyBinding().register(context.keyboard());
        setKeyBindings();
        context.game().level().map(GameLevel::map).ifPresent(worldMap -> gr.setWorldMap(worldMap));
    }

    private void setKeyBindings() {
        if (context.game().isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, context.joypadKeyBinding().key(NES_JoypadButton.BTN_START));
        } else {
            bind(GameActions2D.PLAYER_UP,    context.joypadKeyBinding().key(NES_JoypadButton.BTN_UP));
            bind(GameActions2D.PLAYER_DOWN,  context.joypadKeyBinding().key(NES_JoypadButton.BTN_DOWN));
            bind(GameActions2D.PLAYER_LEFT,  context.joypadKeyBinding().key(NES_JoypadButton.BTN_LEFT));
            bind(GameActions2D.PLAYER_RIGHT, context.joypadKeyBinding().key(NES_JoypadButton.BTN_RIGHT));
            bind(TengenMsPacMan_GameActions.TOGGLE_PAC_BOOSTER,
                context.joypadKeyBinding().key(NES_JoypadButton.BTN_A),
                context.joypadKeyBinding().key(NES_JoypadButton.BTN_B));
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> movingCamera.focusPlayer(true);
            case LEVEL_COMPLETE -> {
                context.game().level().ifPresent(level -> {
                    if (levelCompleteAnimation == null) {
                        // if 3D scene was active when level has been created, the animation has not been created!
                        createLevelCompleteAnimation(level);
                    }
                    levelCompleteAnimation.start();
                });
            }
            case GAME_OVER -> {
                TengenMsPacMan_GameModel game = context.game();
                game.level().ifPresent(level -> {
                    if (game.mapCategory() != MapCategory.ARCADE) {
                        float belowHouse = centerPosBelowHouse(level).x();
                        messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, sizeInPx().x());
                    }
                    movingCamera.focusTopOfScene();
                });
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        context.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sound().stopBonusBouncingSound();
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        context.sound().stopBonusBouncingSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sound().playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        movingCamera.focusTopOfScene();
    }

    @Override
    public void onPacDying(GameEvent e) {
        context.sound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        context.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        context.sound().stopSiren();
        context.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        context.sound().stopPacPowerSound();
    }

    private void updateSound(GameLevel level) {
        GameSound sound = context.sound();
        if (context.gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            HuntingTimer huntingControl = context.game().huntingTimer();
            int sirenNumber = 1 + huntingControl.phaseIndex() / 2; // TODO check how this works in original game
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
        }
    }

    // drawing

    @Override
    public void draw() {
        // do this here because it should be run also when game is paused
        updateScaling();
        updateCameraPosition(scaling());

        var r = (TengenMsPacMan_Renderer2D) gr;
        r.setScaling(scaling());
        r.clearCanvas();
        context.game().level().ifPresent(level -> {
            r.ctx().save();
            r.ctx().translate(scaled(2 * TS), 0);
            drawSceneContent();
            r.ctx().restore();
        });
    }

    @Override
    protected void drawSceneContent() {
        var r = (TengenMsPacMan_Renderer2D) gr;
        TengenMsPacMan_GameModel game = context.game();
        GameLevel level = game.level().orElse(null);
        if (level == null) {
            Logger.warn("Cannot draw scene content, no game level exists");
            return;
        }
        if (context.isScoreVisible()) {
            r.drawScores(context);
        }
        Vector2f messageCenterPosition = centerPosBelowHouse(level);
        if (messageMovement != null) {
            r.setMessagePosition(messageMovement.isRunning()
                    ? new Vector2f(messageMovement.currentX(), messageCenterPosition.y())
                    : messageCenterPosition
            );
        } else {
            r.setMessagePosition(messageCenterPosition);
        }

        r.setBlinking(level.blinking().isOn());
        boolean flashing = levelCompleteAnimation != null && levelCompleteAnimation.isFlashing();
        if (flashing && levelCompleteAnimation.isInHighlightPhase()) {
            r.drawWorldHighlighted(context, level, 0, 3 * TS, levelCompleteAnimation.flashingIndex());
        } else {
            //TODO in the original game, the message is draw under the maze image but over the pellets!
            r.drawWorld(context, level, 0,  3 * TS);
            r.drawFood(level);
            r.drawLevelMessage(context.gameConfiguration().assetNamespace(), level, game.isDemoLevel());
        }

        level.bonus().ifPresent(r::drawBonus);

        r.drawAnimatedActor(level.pac());
        ghostsInZOrder(level).forEach(r::drawAnimatedActor);

        int livesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !level.pac().isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        r.drawLivesCounter(livesCounterEntries, 5, 2 * TS, sizeInPx().y() - TS);
        r.setLevelNumberBoxesVisible(!game.isDemoLevel() && game.mapCategory() != MapCategory.ARCADE);
        r.drawLevelCounter(context, sizeInPx().x() - 2 * TS, sizeInPx().y() - TS);

        if (debugInfoVisiblePy.get()) {
            r.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(r::drawAnimatedCreatureInfo);
            drawDebugInfo();
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(canvas.getWidth(), canvas.getHeight());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(GameRenderer.DEBUG_FONT);
        GameState state = context().gameState();
        gr.ctx().fillText("%s %d".formatted(state, state.timer().tickCount()), 0, scaled(3 * TS));
    }

    private Vector2f centerPosBelowHouse(GameLevel level) {
        return level.houseMinTile().plus(0.5f * level.houseSizeInTiles().x(), level.houseSizeInTiles().y() + 1).scaled(TS);
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(level::ghost);
    }

    private void createLevelCompleteAnimation(GameLevel level) {
        levelCompleteAnimation = new LevelCompleteAnimation(level.numFlashes(), 10);
        levelCompleteAnimation.setOnHideGhosts(() -> level.ghosts().forEach(Ghost::hide));
        levelCompleteAnimation.setOnFinished(() -> context.gameState().timer().expire());
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        // Switching scene display mode
        var miScaledToFit = new RadioMenuItem(context.locText("scaled_to_fit"));
        miScaledToFit.selectedProperty().addListener(
                (py,ov,nv) -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCALED_TO_FIT:SceneDisplayMode.SCROLLING));
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScaledToFit.setSelected(nv == SceneDisplayMode.SCALED_TO_FIT));
        items.add(miScaledToFit);

        var miScrolling = new RadioMenuItem(context.locText("scrolling"));
        miScrolling.selectedProperty().addListener(
                (py,ov,nv) -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCROLLING:SceneDisplayMode.SCALED_TO_FIT));
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScrolling.setSelected(nv == SceneDisplayMode.SCROLLING));
        items.add(miScrolling);

        ToggleGroup exclusion = new ToggleGroup();
        miScaledToFit.setToggleGroup(exclusion);
        miScrolling.setToggleGroup(exclusion);
        if (PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get() == SceneDisplayMode.SCALED_TO_FIT) {
            miScaledToFit.setSelected(true);
        } else {
            miScrolling.setSelected(true);
        }
        items.add(Ufx.contextMenuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(context.locText("muted"));
        miMuted.selectedProperty().bindBidirectional(context.sound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(ae -> GameActions2D.SHOW_START_PAGE.execute(context));
        items.add(miQuit);

        return items;
    }
}