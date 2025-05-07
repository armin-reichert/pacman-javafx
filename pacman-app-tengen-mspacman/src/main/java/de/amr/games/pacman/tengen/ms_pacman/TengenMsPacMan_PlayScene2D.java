/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.JoypadButtonID;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.LevelCompleteAnimation;
import de.amr.games.pacman.uilib.CameraControlledView;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.input.Keyboard;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameAction.QUIT_DEMO_LEVEL;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.games.pacman.ui.Globals.*;
import static de.amr.games.pacman.uilib.input.Keyboard.control;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CameraControlledView {

    // (NES screen width, BIG map height (42 tiles) + 2 extra tile rows)
    private static final Vector2i UNSCALED_CANVAS_SIZE = Vector2i.of(NES_SIZE.x(), 44 * TS);
    private static final float CAM_SPEED = 0.03f;
    private static final int MOVING_MESSAGE_DELAY = 120;

    private class MovingCamera extends ParallelCamera {
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
            setTranslateY(Math.clamp(y, camMinY(), camMaxY()));
            Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, camMinY(), camMaxY());
        }
    }

    private final SubScene fxSubScene;
    private final MovingCamera movingCamera;
    private final ParallelCamera fixedCamera;
    private final ObjectProperty<SceneDisplayMode> displayModePy = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private MessageMovement messageMovement;
    private LevelCompleteAnimation levelCompleteAnimation;

    public TengenMsPacMan_PlayScene2D() {
        movingCamera = new MovingCamera();
        fixedCamera = new ParallelCamera();

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
        root.setBackground(Background.EMPTY);

        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setFill(PY_CANVAS_BG_COLOR.get());
        fxSubScene.cameraProperty().bind(displayModeProperty()
            .map(mode -> mode == SceneDisplayMode.SCROLLING ? movingCamera : fixedCamera));
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        // Switching scene display mode
        var miScaledToFit = new RadioMenuItem(THE_ASSETS.text("scaled_to_fit"));
        miScaledToFit.selectedProperty().addListener(
            (py,ov,nv) -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCALED_TO_FIT:SceneDisplayMode.SCROLLING));
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScaledToFit.setSelected(nv == SceneDisplayMode.SCALED_TO_FIT));
        items.add(miScaledToFit);

        var miScrolling = new RadioMenuItem(THE_ASSETS.text("scrolling"));
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
        items.add(Ufx.contextMenuTitleItem(THE_ASSETS.text("pacman")));

        var miAutopilot = new CheckMenuItem(THE_ASSETS.text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(THE_ASSETS.text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(THE_ASSETS.text("muted"));
        miMuted.selectedProperty().bindBidirectional(THE_SOUND.mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(THE_ASSETS.text("quit"));
        miQuit.setOnAction(ae -> GameAction.QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    @Override
    public void setCanvas(Canvas canvas) { /* ignore */ }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModePy;
    }

    protected void updateScaling() {
        SceneDisplayMode displayMode = displayModePy.get();
        double unscaledHeight = displayMode == SceneDisplayMode.SCROLLING ? NES_SIZE.y() : sizeInPx().y() + 3*TS;
        setScaling(viewPortHeightProperty().get() / unscaledHeight);
    }

    private void updateCameraPosition(double scaling) {
        int worldTilesY = game().level().map(level -> level.worldMap().numRows()).orElse(NES_TILES.y());
        double dy = scaling * (worldTilesY - 43) * HTS;
        fixedCamera.setTranslateY(dy);
    }

    private void setJoypadKeyBindings(GameLevel level) {
        if (level.isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, THE_JOYPAD.key(JoypadButtonID.START));
        } else {
            bind(GameAction.PLAYER_UP,    THE_JOYPAD.key(JoypadButtonID.UP),    control(KeyCode.UP));
            bind(GameAction.PLAYER_DOWN,  THE_JOYPAD.key(JoypadButtonID.DOWN),  control(KeyCode.DOWN));
            bind(GameAction.PLAYER_LEFT,  THE_JOYPAD.key(JoypadButtonID.LEFT),  control(KeyCode.LEFT));
            bind(GameAction.PLAYER_RIGHT, THE_JOYPAD.key(JoypadButtonID.RIGHT), control(KeyCode.RIGHT));
            bind(TengenMsPacMan_GameAction.TOGGLE_PAC_BOOSTER,
                THE_JOYPAD.key(JoypadButtonID.A), THE_JOYPAD.key(JoypadButtonID.B));
            bindCheatActions();
        }
        enableActionBindings();
    }

    @Override
    public void bindActions() {
        bind(TengenMsPacMan_GameAction.TOGGLE_DISPLAY_MODE, Keyboard.alt(KeyCode.C));
    }

    @Override
    public void doInit() {
        messageMovement = new MessageMovement();
        game().scoreManager().setScoreVisible(true);
        setGameRenderer(THE_UI_CONFIGS.current().createRenderer(canvas));
        movingCamera.focusTopOfScene();
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopAll();
    }

    @Override
    public void update() {
        game().level().ifPresent(level -> {
            if (level.isDemoLevel()) {
                game().assignDemoLevelBehavior(level.pac());
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                messageMovement.update();
                updateSound(level);
            }
            if (gameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.tick();
            }
            if (fxSubScene.getCamera() == movingCamera) {
                if (gameState() == GameState.HUNTING) {
                    movingCamera.focusPlayer(true);
                }
                movingCamera.setVerticalRangeTiles(level.worldMap().numRows());
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
        return levelSizeInTilesOrElse(NES_TILES).toVector2f().scaled(TS);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameLevel level = game().level().orElseThrow();
        boolean silent = level.isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_SOUND.playGameReadySound();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        GameLevel level = game().level().orElseThrow();
        setJoypadKeyBindings(level);
        if (level.isDemoLevel()) {
            level.pac().setImmune(false);
        } else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
        }
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        movingCamera.setCameraToTopOfScene();
        movingCamera.focusBottomOfScene();
        movingCamera.setIdleTicks(90);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        GameLevel level = game().level().orElseThrow();
        setJoypadKeyBindings(level);
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> movingCamera.focusPlayer(true);

            case LEVEL_COMPLETE ->
                game().level().ifPresent(level -> {
                    THE_SOUND.stopAll();
                    levelCompleteAnimation = new LevelCompleteAnimation(level);
                    levelCompleteAnimation.setActionOnFinished(THE_GAME_CONTROLLER::letCurrentStateExpire);
                    levelCompleteAnimation.start();
                });

            case GAME_OVER ->
                game().level().ifPresent(level -> {
                    TengenMsPacMan_GameModel game = game();
                    if (game.mapCategory() != MapCategory.ARCADE) {
                        float belowHouse = centerPosBelowHouse(level).x();
                        messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, sizeInPx().x());
                    }
                    movingCamera.focusTopOfScene();
                });

            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        THE_SOUND.playBonusActiveSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        THE_SOUND.stopBonusActiveSound();
        THE_SOUND.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        THE_SOUND.stopBonusActiveSound();
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        THE_SOUND.playExtraLifeSound();
    }

    @Override
    public void onGameContinued(GameEvent e) {
        game().level().ifPresent(level -> level.showMessage(GameLevel.Message.READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_SOUND.playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        movingCamera.focusTopOfScene();
        THE_GAME_CONTROLLER.letCurrentStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        THE_SOUND.playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        THE_SOUND.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        THE_SOUND.stopSiren();
        THE_SOUND.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        THE_SOUND.stopPacPowerSound();
    }

    private void updateSound(GameLevel level) {
        if (gameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2; // TODO check how this works in original game
            THE_SOUND.selectSiren(sirenNumber);
            THE_SOUND.playSiren();
        }
        if (level.pac().starvingTicks() > 5) { // TODO not sure how to do this right
            THE_SOUND.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            THE_SOUND.playGhostReturningHomeSound();
        } else {
            THE_SOUND.stopGhostReturningHomeSound();
        }
    }

    // drawing

    @Override
    public void draw() {
        final var tr = (TengenMsPacMan_Renderer2D) gr;
        tr.fillCanvas(tr.backgroundColorProperty().get());
        updateScaling();
        updateCameraPosition(scaling());
        drawSceneContent();
    }

    @Override
    protected void drawSceneContent() {
        final GameLevel level = game().level().orElse(null);
        if (level == null) {
            // Scene is drawn already 2 ticks before level has been created
            Logger.warn("Tick {}: Game level not yet available, scene content not drawn", THE_CLOCK.tickCount());
            return;
        }
        final TengenMsPacMan_GameModel tgame = game();
        final var tr = (TengenMsPacMan_Renderer2D) gr;
        final int mazeTopY = 3 * TS;

        tr.ctx().save();

        // Tengen screen width is 32 tiles, each maze is 28 tiles wide, so reserve 2 tiles on each side
        tr.ctx().translate(scaled(2 * TS), 0);

        tr.setScaling(scaling());

        tr.drawScores(tgame.scoreManager(), nesPaletteColor(0x20), arcadeFontScaledTS());

        final boolean flashing = levelCompleteAnimation != null && levelCompleteAnimation.inFlashingPhase();
        if (flashing) {
            if (levelCompleteAnimation.inHighlightPhase()) {
                tr.drawHighlightedWorld(level, 0, mazeTopY, levelCompleteAnimation.flashingIndex());
            } else {
                tr.drawWorld(level, 0, mazeTopY);
                tr.drawFood(level); // this also hides eaten food!
            }
        }
        else {
            tr.drawWorld(level, 0, mazeTopY);
            tr.drawFood(level);
            level.bonus().ifPresent(tr::drawBonus);
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            tr.drawLevelMessage(level, level.isDemoLevel(), currentMessagePosition(level), arcadeFontScaledTS());
            tr.drawAnimatedActor(level.pac());
            ghostsInZOrder(level).forEach(tr::drawAnimatedActor);
        }

        // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
        int numLivesDisplayed = gameState() == GameState.STARTING_GAME && !level.pac().isVisible()
            ? tgame.lives() : tgame.lives() - 1;
        tr.drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - TS);

        if (level.isDemoLevel() || tgame.mapCategory() == MapCategory.ARCADE) {
            tr.drawLevelCounter(tgame.levelCounter(), sizeInPx());
        } else {
            tr.drawLevelCounterWithLevelNumbers(level.number(), tgame.levelCounter(), sizeInPx());
        }

        if (debugInfoVisiblePy.get()) {
            tr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(tr::drawAnimatedCreatureInfo);
            drawDebugInfo();
        }

        tr.ctx().restore();
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST_ID, CYAN_GHOST_ID, PINK_GHOST_ID, RED_GHOST_ID).map(level::ghost);
    }

    private Vector2f currentMessagePosition(GameLevel level) {
        Vector2f center = centerPosBelowHouse(level);
        if (messageMovement != null && messageMovement.isRunning()) {
            return new Vector2f(messageMovement.currentX(), center.y());
        }
        return center;
    }

    private Vector2f centerPosBelowHouse(GameLevel level) {
        return level.houseMinTile().plus(0.5f * level.houseSizeInTiles().x(), level.houseSizeInTiles().y() + 1).scaled(TS);
    }
}