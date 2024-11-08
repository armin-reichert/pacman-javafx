/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.ui2d.GameActions2D.bindCheatActions;
import static de.amr.games.pacman.ui2d.GameActions2D.bindFallbackPlayerControlActions;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions.QUIT_DEMO_LEVEL;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions.bindDefaultJoypadActions;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.paletteColor;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.*;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenPlayScene2D extends GameScene2D implements CameraControlledGameScene {

    private final SubScene fxSubScene;
    private final ParallelCamera cam = new ParallelCamera();
    private final Canvas canvas = new Canvas(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    private int camDelay;
    private final GameOverMessageAnimation gameOverMessageAnimation = new GameOverMessageAnimation();
    private final MazeFlashingAnimation mazeFlashingAnimation = new MazeFlashingAnimation();

    public TengenPlayScene2D() {
        canvas.widthProperty() .bind(scalingProperty().map(s -> s.doubleValue() * size().x()));
        canvas.heightProperty().bind(scalingProperty().map(s -> s.doubleValue() * size().y()));
        Pane root = new StackPane(canvas);
        root.setBackground(null);
        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setCamera(cam);
    }

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.enableJoypad();
        context.setScoreVisible(true);
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.disableJoypad();
    }

    @Override
    public void update() {
        var game = (TengenMsPacManGame) context.game();
        if (game.level().isEmpty()) {
            // Scene is already visible for 2 ticks before game level gets created
            Logger.warn("Tick #{}: Cannot update TengenPlayScene2D: game level not yet available", context.tick());
            return;
        }

        if (context.level().isDemoLevel()) {
            game.setDemoLevelBehavior();
        }
        else {
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
            if (context.gameState() == GameState.GAME_OVER && game.mapCategory() != MapCategory.ARCADE) {
                gameOverMessageAnimation.update();
            }
        }

        if (camDelay > 0) {
            --camDelay;
        }
        else {
            double msPacManY = scaled(context.level().pac().center().y());
            double r = cameraRadius();
            double y = lerp(cam.getTranslateY(), msPacManY - r, 0.02);
            cam.setTranslateY(clamp(y, dontAskItsMagic(-r), dontAskItsMagic(r)));
        }
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
        return cam;
    }

    private void initCamera(int delayTicks) {
        camDelay = delayTicks;
        cam.setTranslateY(dontAskItsMagic(-cameraRadius()));
    }

    private double cameraRadius() {
        return 0.5 * scaled(size().y() - 24);
    }

    private double dontAskItsMagic(double radius) {
        double height = size().y();
        if (height >= 40 * TS) return 0.38 * radius;
        if (height >= 35 * TS) return 0.30 * radius;
        return 0.1 * radius;
    }

    @Override
    public Vector2f size() {
        Vector2i defaultSize = new Vector2i(NES_TILES_X, NES_TILES_Y);
        if (context == null) {
            return defaultSize.toVector2f();
        }
        //TODO: change maps instead of inventing 2 rows?
        return context.worldSizeInTilesOrElse(defaultSize).plus(0, 2).scaled(TS).toVector2f();
    }

    @Override
    public void draw(GameRenderer renderer) {
        renderer.setCanvas(canvas);
        renderer.setScaling(scaling());
        renderer.setBackgroundColor(backgroundColor());
        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer);
        }
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        if (context.game().level().isEmpty()) {
            return;
        }
        final GameWorld world = context.level().world();
        final var game = (TengenMsPacManGame) context.game();
        final var r = (TengenMsPacManGameRenderer) renderer;
        final Pac msPacMan = context.level().pac();

        if (world == null) { // This happens on level start
            Logger.warn("Tick #{}: Cannot draw scene content, game world not yet available!", context.tick());
            return;
        }

        r.setBlinkingOn(context.level().blinking().isOn());

        // Draw level message centered under ghost house
        Vector2i houseTopLeft = world.houseTopLeftTile(), houseSize = world.houseSize();
        double cx = TS * (houseTopLeft.x() + houseSize.x() * 0.5);
        double y  = TS * (houseTopLeft.y() + houseSize.y() + 1);
        drawLevelMessage(renderer, cx, y);

        if (Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"))) {
            mazeFlashingAnimation.update(context.tick());
            r.drawEmptyMap(world.map(), mazeFlashingAnimation.currentColorScheme());
        } else {
            r.drawWorld(context, world, 0,  3 * TS);
        }

        r.drawAnimatedEntity(msPacMan);
        ghostsInZOrder().forEach(r::drawAnimatedEntity);

        // Debug mode info
        if (debugInfoPy.get()) {
            r.drawAnimatedCreatureInfo(msPacMan);
            ghostsInZOrder().forEach(r::drawAnimatedCreatureInfo);
        }

        int livesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !msPacMan.isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        r.drawLivesCounter(livesCounterEntries, 5, size());

        r.setLevelNumberBoxesVisible(!context.level().isDemoLevel() && game.mapCategory() != MapCategory.ARCADE);
        r.drawLevelCounter(context, size());
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(context.level()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer, double cx, double y) {
        AssetStorage assets = context.assets();
        String assetPrefix = assetPrefix(GameVariant.MS_PACMAN_TENGEN);
        GameState state = context.gameState();
        GameModel game = context.game();
        GameLevel level = game.level().orElseThrow();
        if (level.isDemoLevel()) {
            Color color = Color.web(level.mapConfig().colorScheme().get("stroke"));
            drawText(renderer, "GAME  OVER", cx, y, color);
        } else if (state == GameState.GAME_OVER) {
            Color color = assets.color(assetPrefix + ".color.game_over_message");
            drawText(renderer, "GAME  OVER", gameOverMessageAnimation.currentX(), y, color);
        } else if (state == GameState.STARTING_GAME) {
            Color color = assets.color(assetPrefix + ".color.ready_message");
            drawText(renderer, "READY!", cx, y, color);
        } else if (state == GameState.TESTING_LEVEL_BONI) {
            drawText(renderer, "TEST L%02d".formatted(level.number), cx, y, paletteColor(0x28));
        }
    }

    private void drawText(GameRenderer renderer, String text, double cx, double y, Color color) {
        double x = (cx - text.length() * 0.5 * TS);
        renderer.drawText(text, color, renderer.scaledArcadeFont(TS), x, y);
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.YELLOW);
        renderer.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        renderer.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        context.updateRenderer();
        setKeyBindings();
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case LEVEL_COMPLETE -> mazeFlashingAnimation.init((TengenMsPacManGame) context.game());
            case GAME_OVER -> {
                GameWorld world = context.level().world();
                double houseCenterX = TS * (world.houseTopLeftTile().x() + 0.5 * world.houseSize().x());
                gameOverMessageAnimation.start(houseCenterX, size().x());
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
    public void onLevelCreated(GameEvent e) {
        if (context.level().isDemoLevel()) {
            context.level().pac().setImmune(false);
        } else {
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
        }
        setKeyBindings();
        context.updateRenderer();
    }

    private void setKeyBindings() {
        JoypadKeyAdapter joypad = context.joypad();
        if (context.level().isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, joypad.mapControToKey(NES.Joypad.START));
        } else {
            bindCheatActions(this);
            bindDefaultJoypadActions(this, joypad);
            bindFallbackPlayerControlActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        context.updateRenderer();
        initCamera(90);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.level().isDemoLevel() ||
                context.gameState() == TESTING_LEVEL_BONI ||
                context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
        initCamera(30);
    }

    @Override
    public void onPacDied(GameEvent e) {
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

    private void updatePlaySceneSound() {
        GameSound sounds = context.sound();
        if (context.gameState() == GameState.HUNTING && !context.level().powerTimer().isRunning()) {
            HuntingControl huntingControl = context.game().huntingControl();
            int sirenNumber = 1 + huntingControl.phaseIndex() / 2; // TODO check how this works in original game
            sounds.selectSiren(sirenNumber);
            sounds.playSiren();
        }
        if (context.level().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sounds.stopMunchingSound();
        }
        boolean ghostsReturning = context.level().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.level().pac().isAlive() && ghostsReturning) {
            sounds.playGhostReturningHomeSound();
        } else {
            sounds.stopGhostReturningHomeSound();
        }
    }
}