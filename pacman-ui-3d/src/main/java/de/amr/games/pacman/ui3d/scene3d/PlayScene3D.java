/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.assets.GameSound;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.CameraControlledView;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.level.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui2d.action.GameActions2D.*;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;
import static de.amr.games.pacman.ui2d.lib.Ufx.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.*;

/**
 * 3D play scene for Arcade game variants. Tengen 3D scene is a subclass which adapts the action bindings.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 * and <code>Alt+RIGHT</code>.</p>
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, CameraControlledView {

    //TODO localize?
    protected static final String SCORE_TEXT = "SCORE";
    protected static final String HIGH_SCORE_TEXT = "HIGH SCORE";
    protected static final String GAME_OVER_TEXT = "GAME OVER!";

    // Each 3D play scene has its own set of cameras/perspectives
    private final Map<Perspective.Name, Perspective> namePerspectiveMap = new EnumMap<>(Perspective.Name.class);
    {
        namePerspectiveMap.put(Perspective.Name.DRONE, new Perspective.Drone());
        namePerspectiveMap.put(Perspective.Name.TOTAL, new Perspective.Total());
        namePerspectiveMap.put(Perspective.Name.FOLLOWING_PLAYER, new Perspective.FollowingPlayer());
        namePerspectiveMap.put(Perspective.Name.NEAR_PLAYER, new Perspective.NearPlayer());
    }

    public final ObjectProperty<Perspective.Name> perspectiveNamePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Logger.info("Perspective named changed to {}", get());
            context.game().level().ifPresent(level -> perspective().init(level.world()));
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    protected GameContext context;

    private final SubScene fxSubScene;
    private final Group root;
    protected final Scores3D scores3D;

    protected GameLevel3D level3D;
    private Animation levelCompleteAnimation;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        var axes = new CoordinateSystem();
        scores3D = new Scores3D(SCORE_TEXT, HIGH_SCORE_TEXT);
        // last child is placeholder for level 3D
        root = new Group(scores3D, axes, ambientLight, new Group());
        // initial size is irrelevant, it is bound to parent scene later
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(Color.TRANSPARENT);
        fxSubScene.cameraProperty().bind(perspectiveNamePy.map(name -> perspective().getCamera()));
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
        axes.visibleProperty().bind(PY_3D_AXES_VISIBLE);
    }

    @Override
    public final void init() {
        context.setScoreVisible(true); //TODO check this
        scores3D.setFont(context.assets().font("font.arcade", 8));
        bindGameActions();
        registerGameActionKeyBindings(context().keyboard());
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene initialized. {}", this);
    }

    @Override
    public final void end() {
        perspectiveNamePy.unbind();
        if (levelCompleteAnimation != null) {
            levelCompleteAnimation.stop();
        }
        level3D = null;
        unregisterGameActionKeyBindings(context().keyboard());
        Logger.info("3D play scene ended. {}", this);
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        if (context.game().isDemoLevel()) {
            bind(GameActions2D.INSERT_COIN, context.arcadeKeys().key(Arcade.Button.COIN));
        }
        else {
            bindDefaultArcadeControllerActions(this, context.arcadeKeys());
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
            context.setScoreVisible(true); //TODO check this
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        GameLevel level = context.level();
        bindGameActions();
        if (!hasLevel3D()) {
            replaceGameLevel3D();
            level3D.addLevelCounter();
        } else {
            Logger.error("3D level already created?");
        }
        switch (context.gameState()) {
            case TESTING_LEVELS, TESTING_LEVEL_TEASERS -> {
                replaceGameLevel3D();
                level3D.livesCounter3D().shapesRotation().play();
                level3D.energizers3D().forEach(Energizer3D::startPumping);
                showLevelTestMessage("TEST LEVEL " + level.number);
            }
            default -> {
                if (!context.game().isDemoLevel()){
                    showReadyMessage();
                }
            }
        }
        updateScores();
        namePerspectiveMap.forEach((name, perspective) -> perspective.init(level.world()));
    }

    @Override
    public void onSceneVariantSwitch(GameScene fromScene) {
        Logger.info("{} entered from {}", getClass().getSimpleName(), fromScene.getClass().getSimpleName());

        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());

        if (!hasLevel3D()) {
            replaceGameLevel3D();
            level3D.addLevelCounter();
        }

        GameLevel level = context.level();
        level3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!level.world().hasEatenFoodAt(pellet.tile())));
        level3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!level.world().hasEatenFoodAt(energizer.tile())));
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            level3D.energizers3D().filter(energizer -> energizer.shape3D().isVisible()).forEach(Energizer3D::startPumping);
        }
        level.pac().show();
        level3D.pac3D().init();
        level.ghosts().forEach(Ghost::show);
        level3D.pac3D().update(context);

        if (context.gameState() == GameState.HUNTING) {
            if (level.powerTimer().isRunning()) {
                context.sound().playPacPowerSound();
            }
            level3D.livesCounter3D().shapesRotation().play();
        }
        updateScores();
    }

    public boolean hasLevel3D() {
        return level3D != null;
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            // Scene is visible for 1 (2?) ticks before game level has been created
            Logger.warn("Tick #{}: Cannot update PlayScene3D: game level not yet available", context.tick());
            return;
        }
        // TODO: check this
        if (!hasLevel3D()) {
            Logger.warn("Tick #{}: Cannot update 3D play scene, 3D game level not yet created?", context.tick());
            return;
        }

        GameLevel level = context.level();
        level3D.update(context);

        //TODO check if this has to de done on every tick
        if (context.game().isDemoLevel()) {
            context.game().setDemoLevelBehavior();
        }
        else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
            updateScores();
            updateSound(context.sound());
        }
        updatePerspective(level);
    }

    private void updatePerspective(GameLevel level) {
        perspective().update(level.world(), level.pac());
        Camera camera = perspective().getCamera();
        scores3D.rotationAxisProperty().set(camera.getRotationAxis());
        scores3D.rotateProperty().set(camera.getRotate());
    }

    protected void updateScores() {
        ScoreManager manager = context.game().scoreManager();
        Score score = manager.score(), highScore = manager.highScore();

        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (manager.isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // when score is disabled, show text "game over"
            String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
            Color color = context.assets().color(assetKeyPrefix + ".color.game_over_message");
            scores3D.showTextAsScore(GAME_OVER_TEXT, color);
        }
    }

    private void updateSound(GameSound sound) {
        if (context.gameState() == GameState.HUNTING && !context.level().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (context.level().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = context.level().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.level().pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
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
        this.context = assertNotNull(context);
    }

    public Perspective perspective() {
        return namePerspectiveMap.get(perspectiveNamePy.get());
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS; // irrelevant
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
            case TESTING_LEVELS -> onEnterStateTestingLevelBoni();
            case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers();
            case GAME_OVER             -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateStartingGame() {
        if (hasLevel3D()) {
            stopLevelAnimations();
            level3D.pac3D().init();
            level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
            showReadyMessage();
        }
    }

    private void onEnterStateHunting() {
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
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
        GameSpriteSheet spriteSheet = context.gameConfiguration().spriteSheet();
        RectArea[] numberSprites = spriteSheet.ghostNumberSprites();
        context.game().eventLog().killedGhosts.forEach(ghost -> {
            int victimIndex = context.level().victims().indexOf(ghost);
            var numberImage = spriteSheet.subImage(numberSprites[victimIndex]);
            level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
        });
    }

    private void onEnterStateLevelComplete() {
        context.sound().stopAll();
        // if cheat has been used to complete level, food might still exist, so eat it:
        GameWorld world = context.level().world();
        world.map().food().tiles().forEach(world::registerFoodEatenAt);
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.house3D().door3D().setVisible(false);
        playLevelCompleteAnimation(context.level());
    }

    private void onEnterStateLevelTransition() {
        context.gameState().timer().restartSeconds(3);
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        perspective().init(context.level().world());
    }

    private void onEnterStateTestingLevelBoni() {
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("TEST LEVEL" + context.level().number);
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers() {
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("PREVIEW LEVEL " + context.level().number);
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateGameOver() {
        stopLevelAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        context.showFlashMessageSec(3, context.locGameOverMessage());
        context.sound().stopAll();
        context.sound().playGameOverSound();
    }

    private void stopLevelAnimations() {
        level3D.energizers3D().forEach(Energizer3D::stopPumping);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.level().bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.gameConfiguration().spriteSheet()));
        if (context.gameVariant() == GameVariant.MS_PACMAN) {
            context.sound().playBonusBouncingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        if (context.gameVariant() == GameVariant.MS_PACMAN) {
            context.sound().stopBonusBouncingSound();
        }
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        if (context.gameVariant() == GameVariant.MS_PACMAN) {
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
    public void onGameStarted(GameEvent e) {
        boolean silent = context.game().isDemoLevel() ||
                context.gameState() == TESTING_LEVELS ||
                context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        GameWorld world = context.level().world();
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

    private void replaceGameLevel3D() {
        level3D = new GameLevel3D(context);
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3 * TS));
        Logger.info("3D game level {} created.", context.level().number);
    }

    private void showLevelTestMessage(String message) {
        TileMap terrainMap = context.level().world().map().terrain();
        double x = terrainMap.numCols() * HTS;
        double y = (terrainMap.numRows() - 2) * TS;
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage() {
        GameWorld world = context.level().world();
        Vector2i houseTopLeft = world.houseTopLeftTile();
        Vector2i houseSize = world.houseSize();
        double x = TS * (houseTopLeft.x() + 0.5 * houseSize.x());
        double y = TS * (houseTopLeft.y() +       houseSize.y());
        double seconds = context.game().isPlaying() ? 0.5 : 2.5;
        level3D.showAnimatedMessage("READY!", seconds, x, y);
    }

    private void playPacManDiesAnimation() {
        context.gameState().timer().resetIndefiniteTime();
        Animation animation = level3D.pac3D().createDyingAnimation(context.sound());
        animation.setDelay(Duration.seconds(1));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        animation.play();
    }

    private void playLevelCompleteAnimation(GameLevel level) {
        context.gameState().timer().resetIndefiniteTime(); // block game state until animation has finished
        levelCompleteAnimation = level.intermissionNumber() != 0
            ? levelCompleteAnimationBeforeIntermission(level.numFlashes())
            : levelCompleteAnimation(level.numFlashes());
        levelCompleteAnimation.setDelay(Duration.seconds(1.0));
        levelCompleteAnimation.setOnFinished(e -> context.gameState().timer().expire());
        levelCompleteAnimation.play();
    }

    private Animation levelCompleteAnimationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
            doAfterSec(1.0, () -> context.level().ghosts().forEach(Ghost::hide))
            , level3D.mazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.level().pac().hide())
        );
    }

    private Animation levelCompleteAnimation(int numFlashes) {
        return new SequentialTransition(
              now(() -> {
                  perspectiveNamePy.unbind();
                  perspectiveNamePy.set(Perspective.Name.TOTAL);
                  level3D.livesCounter3D().light().setLightOn(false);
                  context.showFlashMessageSec(3, context.locLevelCompleteMessage(context.level().number));
                  context.sound().playLevelCompleteSound();
              })
            , doAfterSec(1.0, () -> context.level().ghosts().forEach(Ghost::hide))
            , doAfterSec(1.0, level3D.mazeFlashAnimation(numFlashes))
            , doAfterSec(0.5, () -> context.level().pac().hide())
            , doAfterSec(0.5, level3D.levelRotateAnimation(1.5))
            , level3D.wallsDisappearAnimation(2.0)
            , doAfterSec(1, () -> {
                context.sound().playLevelChangedSound();
                perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu(ContextMenuEvent e) {
        ContextMenu contextMenu = new ContextMenu();

        contextMenu.getItems().add(contextMenuTitleItem(context.locText("scene_display")));

        var item = new MenuItem(context.locText("use_2D_scene"));
        item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
        contextMenu.getItems().add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(context.locText("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        contextMenu.getItems().add(miPiP);

        contextMenu.getItems().add(contextMenuTitleItem(context.locText("select_perspective")));

        // Camera perspective selection
        var perspectivesGroup = new ToggleGroup();
        for (var perspective : Perspective.Name.values()) {
            var miPerspective = new RadioMenuItem(context.locText(perspective.name()));
            miPerspective.setToggleGroup(perspectivesGroup);
            // keep global property in sync with selection
            miPerspective.selectedProperty().addListener((py, ov, selected) -> {
                if (selected) {
                    PY_3D_PERSPECTIVE.set(perspective);
                }
            });
            // keep selection in sync with global property value
            PY_3D_PERSPECTIVE.addListener((py, ov, newPerspective) -> miPerspective.setSelected(newPerspective == perspective));
            miPerspective.setSelected(perspective == PY_3D_PERSPECTIVE.get()); // == is allowed for enum comparison
            contextMenu.getItems().add(miPerspective);
            }

        // Common items
        contextMenu.getItems().add(contextMenuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(context.locText("muted"));
        miMuted.selectedProperty().bindBidirectional(context.sound().mutedProperty());
        contextMenu.getItems().add(miMuted);

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(ae -> GameActions2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        return Optional.of(contextMenu);
    }
}