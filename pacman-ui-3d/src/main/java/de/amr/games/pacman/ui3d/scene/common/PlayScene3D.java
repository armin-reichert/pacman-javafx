/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene.common;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.level.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 * and <code>Alt+RIGHT</code>.</p>
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, CameraControlledGameScene {

    // Each 3D play scene has its own set of cameras/perspectives
    private final Map<Perspective.Name, Perspective> perspectiveMap = new EnumMap<>(Perspective.Name.class);
    {
        perspectiveMap.put(Perspective.Name.DRONE, new Perspective.DronePerspective());
        perspectiveMap.put(Perspective.Name.TOTAL, new Perspective.TotalPerspective());
        perspectiveMap.put(Perspective.Name.FOLLOWING_PLAYER, new Perspective.FollowingPlayerPerspective());
        perspectiveMap.put(Perspective.Name.NEAR_PLAYER, new Perspective.NearPlayerPerspective());
    }

    public final ObjectProperty<Perspective.Name> perspectiveNamePy = new SimpleObjectProperty<>(Perspective.Name.TOTAL) {
        @Override
        protected void invalidated() {
            Perspective.Name name = get();
            Perspective perspective = perspectiveMap.get(name);
            fxSubScene.setCamera(perspective.getCamera());
            perspective.init(context.game().world());
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private GameContext context;

    private final SubScene fxSubScene;
    private final Group root = new Group();
    private final Scores3D scores3D;

    private GameLevel3D level3D;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var coordSystem = new CoordinateSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D("SCORE", "HIGH SCORE");

        // initial size is irrelevant as it is bound to parent scene later
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(null); // transparent

        // last child is placeholder for level 3D
        root.getChildren().setAll(scores3D, coordSystem, ambientLight, new Group());
    }

    @Override
    public final void init() {
        doInit();
        bindGameActions();
        registerGameActionKeyBindings(context().keyboard());
    }

    @Override
    public final void end() {
        doEnd();
        unregisterGameActionKeyBindings(context().keyboard());
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        if (context.game().isDemoLevel()) {
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                bind(TengenGameActions.QUIT_DEMO_LEVEL, context.joypad().start());
            } else {
                // TODO create Arcade controller/"joypad"
                bind(GameActions2D.ADD_CREDIT, only(KeyCode.DIGIT5), only(KeyCode.NUMPAD5));
            }
        }
        else {
            GameActions2D.setPlaySceneCheatActions(this);
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                TengenGameActions.setDefaultJoypadActions(this, context.joypad());
            } else {
                // TODO create Arcade controller/"joypad"
                GameActions2D.setDefaultPlayerControlActions(this);
            }
            GameActions2D.setFallbackPlayerControlActions(this);
        }
    }

    protected void doInit() {
        context.setScoreVisible(true);
        scores3D.fontPy.set(context.assets().font("font.arcade", 8));
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene initialized. {}", this);
    }

    protected void doEnd() {
        perspectiveNamePy.unbind();
        level3D = null;
        Logger.info("3D play scene ended. {}", this);
    }

    @Override
    public void update() {
        var game = context.game();
        if (game.currentLevelNumber() == 0 || game.world() == null) {
            Logger.warn("Cannot update 3D play scene, no game level available");
            return;
        }
        if (level3D == null) {
            Logger.warn("Cannot update 3D play scene, 3D game level not yet created?");
            return;
        }
        level3D.update(context);

        // Update camera and rotate the scores such that the viewer always sees them frontally
        perspective().update(game.world(), game.pac());
        scores3D.setRotationAxis(perspective().getCamera().getRotationAxis());
        scores3D.setRotate(perspective().getCamera().getRotate());

        if (context.game().isDemoLevel()) {
            context.game().pac().setUsingAutopilot(true);
            context.game().pac().setImmune(false);
        } else {
            context.setScoreVisible(true);
            context.game().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.game().pac().setImmune(PY_IMMUNITY.get());
        }

        // Scores
        scores3D.showHighScore(game.scoreManager().highScore().points(), game.scoreManager().highScore().levelNumber());
        if (context.game().scoreManager().isScoreEnabled()) {
            scores3D.showScore(game.scoreManager().score().points(), game.scoreManager().score().levelNumber());
        } else { // demo level or "game over" state
            String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                    color = Color.web(context.game().currentMapColorScheme().get("stroke"));
                }
            }
            scores3D.showTextAsScore("GAME OVER!", color);
        }

        // Sound
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            context.sound().selectSiren(sirenNumber);
            context.sound().playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            context.sound().stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            context.sound().playGhostReturningHomeSound();
        } else {
            context.sound().stopGhostReturningHomeSound();
        }
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
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
    public SubScene viewPort() {
        return fxSubScene;
    }

    @Override
    public Camera camera() {
        return fxSubScene.getCamera();
    }

    @Override
    public GameContext context() {
        return context;
    }

    @Override
    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    public Perspective perspective() {
        return perspectiveMap.get(perspectiveNamePy.get());
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS; // irrelevant
    }

    @Override
    public double scaling() {
        return 1; // irrelevant
    }

    @Override
    public void onEnterGameState(GameState state) {
        Logger.info("Entering game state {}", state);
        switch (state) {
            case STARTING_GAME         -> onEnterStateStartingGame();
            case HUNTING               -> onEnterStateHunting();
            case PACMAN_DYING          -> onEnterStatePacManDying();
            case GHOST_DYING           -> onEnterStateGhostDying();
            case LEVEL_COMPLETE        -> onEnterStateLevelComplete();
            case LEVEL_TRANSITION      -> onEnterStateLevelTransition();
            case TESTING_LEVEL_BONI    -> onEnterStateTestingLevelBoni();
            case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers();
            case GAME_OVER             -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateStartingGame() {
        if (level3D != null) {
            stopLevelAnimations();
            level3D.pac3D().init();
            level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
            showReadyMessage();
        }
    }

    private void onEnterStateHunting() {
        level3D.livesCounter3D().shapesRotation().play();
        level3D.energizers3D().forEach(Energizer3D::startPumping);
    }

    private void onEnterStatePacManDying() {
        context.sound().stopAll();
        // last update before dying animation
        level3D.pac3D().update(context);
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        RectArea[] numberSprites = spriteSheet.ghostNumberSprites();
        context.game().eventLog().killedGhosts.forEach(ghost -> {
            int victimIndex = context.game().victims().indexOf(ghost);
            var numberImage = spriteSheet.subImage(numberSprites[victimIndex]);
            level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
        });
    }

    private void onEnterStateLevelComplete() {
        context.sound().stopAll();
        // if cheat has been used to complete level, food might still exist, so eat it:
        GameWorld world = context.game().world();
        world.map().food().tiles().forEach(world::registerFoodEatenAt);
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.house3D().door3D().setVisible(false);
        playLevelCompleteAnimation();
    }

    private void onEnterStateLevelTransition() {
        context.gameState().timer().restartSeconds(3);
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        perspective().init(context.game().world());
    }

    private void onEnterStateTestingLevelBoni() {
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("BONI LEVEL" + context.game().currentLevelNumber());
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers() {
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("PREVIEW LEVEL " + context.game().currentLevelNumber());
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateGameOver() {
        stopLevelAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        context.showFlashMessageSeconds(3, context.locGameOverMessage());
        context.sound().stopAll();
        context.sound().playGameOverSound();
    }

    private void stopLevelAnimations() {
        level3D.energizers3D().forEach(Energizer3D::stopPumping);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this.getClass().getSimpleName(), oldScene.getClass().getSimpleName());
        if (level3D == null) {
            replaceGameLevel3D(true);
        }
        level3D.pellets3D().forEach(
            pellet3D -> pellet3D.shape3D().setVisible(!context.game().world().hasEatenFoodAt(pellet3D.tile()))
        );
        level3D.energizers3D().forEach(
            energizer3D -> energizer3D.shape3D().setVisible(!context.game().world().hasEatenFoodAt(energizer3D.tile()))
        );
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
            level3D.energizers3D().filter(energizer3D -> energizer3D.shape3D().isVisible()).forEach(Energizer3D::startPumping);
        }
        context.game().pac().show();
        context.game().ghosts().forEach(Ghost::show);
        level3D.pac3D().init();
        level3D.pac3D().update(context);

        if (context.gameState() == GameState.HUNTING) {
            if (context.game().powerTimer().isRunning()) {
                context.sound().playPacPowerSound();
            }
            level3D.livesCounter3D().shapesRotation().play();
        }
        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.game().bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.currentGameSceneConfig().spriteSheet()));
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().playBonusBouncingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().stopBonusBouncingSound();
        }
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().stopBonusBouncingSound();
        }
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
    public void onLevelCreated(GameEvent event) {
        if (level3D == null) {
            replaceGameLevel3D(false); // level counter in model not yet initialized
        } else {
            Logger.error("3D level already created?");
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        addLevelCounter();
        if (context.game().currentLevelNumber() == 1
                || context.gameState() == GameState.TESTING_LEVEL_BONI
                || context.gameState() == GameState.TESTING_LEVEL_TEASERS) {
            switch (context.gameState()) {
                case TESTING_LEVEL_BONI -> {
                    replaceGameLevel3D(false);
                    showLevelTestMessage("BONI LEVEL " + context.game().currentLevelNumber());
                }
                case TESTING_LEVEL_TEASERS -> {
                    replaceGameLevel3D(false);
                    showLevelTestMessage("PREVIEW LEVEL " + context.game().currentLevelNumber());
                }
                default -> {
                    if (!context.game().isDemoLevel()){
                        showReadyMessage();
                    }
                }
            }
        }
        perspective().init(context.game().world());
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        GameWorld world = context.game().world();
        if (event.tile().isEmpty()) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            // In that case, ensure that the 3D representations are in sync with the game model.
            world.map().food().tiles()
                .filter(world::hasEatenFoodAt)
                .map(level3D::pellet3D)
                .flatMap(Optional::stream)
                .forEach(Eatable3D::onEaten);
        } else {
            Vector2i tile = event.tile().get();
            level3D.energizer3D(tile).ifPresent(Energizer3D::onEaten);
            level3D.pellet3D(tile).ifPresent(Pellet3D::onEaten);
        }
        context.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPowerMode(true);
        context.sound().stopSiren();
        context.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPowerMode(false);
        context.sound().stopPacPowerSound();
    }

    private void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = context.game().world().map().terrain().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = GameLevel3D.createLevelCounter3D(context.currentGameSceneConfig().spriteSheet(),
                context.game().levelCounter(), x, y);
        level3D.root().getChildren().add(levelCounter3D);
    }

    private void replaceGameLevel3D(boolean createLevelCounter) {
        level3D = new GameLevel3D(context);
        if (createLevelCounter) {
            addLevelCounter();
        }
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3 * TS));

        Logger.info("3D game level {} created.", context.game().currentLevelNumber());
    }

    private void showLevelTestMessage(String message) {
        TileMap terrainMap = context.game().world().map().terrain();
        double x = terrainMap.numCols() * HTS;
        double y = (terrainMap.numRows() - 2) * TS;
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage() {
        GameWorld world = context.game().world();
        Vector2i houseTopLeft = world.houseTopLeftTile();
        Vector2i houseSize = world.houseSize();
        double x = TS * (houseTopLeft.x() + 0.5 * houseSize.x());
        double y = TS * (houseTopLeft.y() +       houseSize.y());
        double seconds = context.game().isPlaying() ? 0.5 : 2.5;
        level3D.showAnimatedMessage("READY!", seconds, x, y);
    }

    private void playPacManDiesAnimation() {
        context.gameState().timer().resetIndefinitely();
        Animation animation = level3D.pac3D().createDyingAnimation();
        animation.setDelay(Duration.seconds(1));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        animation.play();
    }

    private void playLevelCompleteAnimation() {
        Animation animation = context.game().intermissionNumberAfterLevel() != 0
            ? levelCompleteAnimationBeforeIntermission(context.game().numFlashes())
            : levelCompleteAnimation(context.game().numFlashes());
        animation.setDelay(Duration.seconds(1.0));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        context.gameState().timer().resetIndefinitely(); // block game state until animation has finished
        animation.play();
    }

    private Animation levelCompleteAnimationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
            pauseSec(1)
            , level3D.mazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.game().pac().hide())
        );
    }

    private Animation levelCompleteAnimation(int numFlashes) {
        return new SequentialTransition(
              now(() -> {
                  perspectiveNamePy.unbind();
                  perspectiveNamePy.set(Perspective.Name.TOTAL);
                  level3D.livesCounter3D().light().setLightOn(false);
                  context.showFlashMessageSeconds(3, context.locLevelCompleteMessage());
              })
            , doAfterSec(2, level3D.mazeFlashAnimation(numFlashes))
            , doAfterSec(1, () -> {
                context.game().pac().hide();
                context.sound().playLevelCompleteSound();
            })
            , doAfterSec(0.5, level3D.levelRotateAnimation(1.5))
            , level3D.wallsDisappearAnimation(2.0)
            , doAfterSec(1, () -> {
                context.sound().playLevelChangedSound();
                perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }
}