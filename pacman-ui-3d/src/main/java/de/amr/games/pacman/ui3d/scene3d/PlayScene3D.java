/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Globals;
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
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
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

import java.util.*;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui2d.action.GameActions2D.*;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;
import static de.amr.games.pacman.ui2d.lib.Ufx.contextMenuTitleItem;
import static de.amr.games.pacman.ui2d.lib.Ufx.doAfterSec;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_AXES_VISIBLE;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_PERSPECTIVE;

/**
 * 3D play scene. Provides different camera perspectives that can be stepped
 * through using keys <code>Alt+LEFT</code> and <code>Alt+RIGHT</code>.
 *
 * @author Armin Reichert
 */
public class PlayScene3D extends Group implements GameScene, CameraControlledView {

    public static final String TEXT_SCORE = "SCORE";
    public static final String TEXT_HIGH_SCORE = "HIGH SCORE";
    public static final String TEXT_GAME_OVER = "GAME OVER!";

    protected final ObjectProperty<Perspective.Name> perspectiveNamePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            context.game().level().ifPresent(level -> perspective().init(level.world()));
        }
    };

    protected final Map<Perspective.Name, Perspective> perspectives = new EnumMap<>(Perspective.Name.class);
    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    protected final SubScene fxSubScene;
    protected final Scores3D scores3D;
    protected GameContext context;
    protected GameLevel3D level3D;
    protected Animation levelCompleteAnimation;

    // When constructor is called, context is not yet available!
    public PlayScene3D() {
        var axes = new CoordinateSystem();
        axes.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D(TEXT_SCORE, TEXT_HIGH_SCORE);

        // last child is placeholder for level 3D
        getChildren().addAll(scores3D, axes, new Group());

        var camera = new PerspectiveCamera(true);
        perspectives.put(Perspective.Name.DRONE, new Perspective.Drone(camera));
        perspectives.put(Perspective.Name.TOTAL, new Perspective.Total(camera));
        perspectives.put(Perspective.Name.TRACK_PLAYER, new Perspective.TrackingPlayer(camera));
        perspectives.put(Perspective.Name.NEAR_PLAYER, new Perspective.StalkingPlayer(camera));

        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        // initial size is irrelevant, gets bound to parent scene size later
        fxSubScene = new SubScene(this, 88, 88, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(Color.TRANSPARENT);
        fxSubScene.setCamera(camera);
    }

    @Override
    public void init() {
        bindGameActions();
        registerGameActionKeyBindings(context().keyboard());
        context.setScoreVisible(true);
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        scores3D.setFont(context.assets().font("font.arcade", 8));
    }

    @Override
    public final void end() {
        unregisterGameActionKeyBindings(context().keyboard());
        perspectiveNamePy.unbind();
        if (levelCompleteAnimation != null) {
            levelCompleteAnimation.stop();
        }
        level3D = null;
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        if (context.game().isDemoLevel()) {
            bind(GameActions2D.INSERT_COIN, context.arcadeKeys().key(Arcade.Button.COIN));
        } else {
            bindDefaultArcadeControllerActions(this, context.arcadeKeys());
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        bindGameActions(); //TODO check this
        GameLevel level = context.level();
        if (!hasLevel3D()) {
            replaceGameLevel3D();
            level3D.addLevelCounter();
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
        perspectives.values().forEach(perspective -> perspective.init(level.world()));
    }

    @Override
    public void onSceneVariantSwitch(GameScene fromScene) {
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
        perspective().update(level.world(), level.pac());
    }

    protected Perspective perspective() {
        return perspectives.get(perspectiveNamePy.get());
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
            scores3D.showTextAsScore(TEXT_GAME_OVER, color);
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
            case TESTING_LEVELS        -> onEnterStateTestingLevelBoni();
            case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers();
            case GAME_OVER             -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateStartingGame() {
        if (hasLevel3D()) {
            level3D.stopAnimations();
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
        world.map().food().tiles().filter(world::hasFoodAt).forEach(world::registerFoodEatenAt);
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.door3D().setVisible(false);
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
        level3D.stopAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        if (Globals.randomInt(0, 100) < 25) {
            context.showFlashMessageSec(3, context.locGameOverMessage());
        }
        context.sound().stopAll();
        context.sound().playGameOverSound();
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
        Vector2i tile = event.tile().orElse(null);
        if (tile == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            level3D.pellets3D().forEach(Pellet3D::onEaten);
        } else {
            Optional<Energizer3D> energizer3D = level3D.energizer3D(tile);
            if (energizer3D.isPresent()) {
                energizer3D.get().onEaten();
            } else {
                level3D.pellets3D()
                    .filter(pellet3D -> tile.equals(pellet3D.tile()))
                    .findFirst()
                    .ifPresent(Pellet3D::onEaten);
            }
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

    protected void replaceGameLevel3D() {
        level3D = new GameLevel3D(context);
        int lastIndex = getChildren().size() - 1;
        getChildren().set(lastIndex, level3D);
        scores3D.translateXProperty().bind(level3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.translateZProperty().subtract(3.5 * TS));
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
        return new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                perspectiveNamePy.unbind();
                perspectiveNamePy.set(Perspective.Name.TOTAL);
                level3D.livesCounter3D().light().setLightOn(false);
                if (Globals.randomInt(1, 100) < 25) {
                    context.showFlashMessageSec(3, context.locLevelCompleteMessage(context.level().number));
                }
            }),
            new KeyFrame(Duration.seconds(1.0), e -> context.level().ghosts().forEach(Ghost::hide)),
            new KeyFrame(Duration.seconds(1.5), e -> level3D.mazeFlashAnimation(numFlashes).play()),
            new KeyFrame(Duration.seconds(4.5), e -> context.level().pac().hide()),
            new KeyFrame(Duration.seconds(5.0), e -> level3D.levelRotateAnimation(1.5).play()),
            new KeyFrame(Duration.seconds(7.0), e -> {
                level3D.wallsDisappearAnimation(2.0).play();
                context.sound().playLevelCompleteSound();
            }),
            new KeyFrame(Duration.seconds(9.5), e -> {
                context.sound().playLevelChangedSound();
                perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();

        items.add(contextMenuTitleItem(context.locText("scene_display")));

        var item = new MenuItem(context.locText("use_2D_scene"));
        item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
        items.add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(context.locText("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        items.add(miPiP);

        items.add(contextMenuTitleItem(context.locText("select_perspective")));

        // Camera perspective radio buttons
        var radioButtonGroup = new ToggleGroup();
        for (var perspective : Perspective.Name.values()) {
            var miPerspective = new RadioMenuItem(context.locText(perspective.name()));
            miPerspective.setToggleGroup(radioButtonGroup);
            miPerspective.setUserData(perspective);
            if (perspective == PY_3D_PERSPECTIVE.get())  { // == allowed for enum values
                miPerspective.setSelected(true);
            }
            items.add(miPerspective);
        }
        // keep radio button group in sync with global property value
        radioButtonGroup.selectedToggleProperty().addListener((py, ov, radioButton) -> {
            if (radioButton != null) {
                PY_3D_PERSPECTIVE.set((Perspective.Name) radioButton.getUserData());
            }
        });
        PY_3D_PERSPECTIVE.addListener((py, ov, name) -> {
            for (Toggle toggle : radioButtonGroup.getToggles()) {
                if (toggle.getUserData() == name) { // == allowed for enum values
                    radioButtonGroup.selectToggle(toggle);
                }
            }
        });

        // Common items
        items.add(contextMenuTitleItem(context.locText("pacman")));

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