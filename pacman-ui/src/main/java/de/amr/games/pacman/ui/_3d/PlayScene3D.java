/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.uilib.Action;
import de.amr.games.pacman.uilib.CameraControlledView;
import javafx.animation.Animation;
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

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.*;
import static de.amr.games.pacman.uilib.input.Keyboard.alt;
import static de.amr.games.pacman.uilib.input.Keyboard.naked;
import static de.amr.games.pacman.uilib.Ufx.contextMenuTitleItem;

/**
 * 3D play scene. Provides different camera perspectives that can be stepped
 * through using keys <code>Alt+LEFT</code> and <code>Alt+RIGHT</code>.
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, CameraControlledView {

    protected final ObjectProperty<PerspectiveID> perspectiveNamePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            game().level().ifPresent(level -> perspective().init(fxSubScene, level));
        }
    };

    protected final Map<PerspectiveID, Perspective> perspectives = new EnumMap<>(PerspectiveID.class);
    protected final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();
    protected final Group root = new Group();
    protected final SubScene fxSubScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final Scores3D scores3D;

    protected GameLevel3D level3D;

    public PlayScene3D() {
        var axes = new CoordinateSystem();
        axes.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D(THE_ASSETS.text("score.score"), THE_ASSETS.text("score.high_score"));

        // last child is placeholder for level 3D
        root.getChildren().addAll(scores3D, axes, new Group());

        // initial size is irrelevant, gets bound to parent scene size later
        fxSubScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(Color.TRANSPARENT);
        fxSubScene.setCamera(camera);

        perspectives.put(PerspectiveID.DRONE, new Perspective.Drone());
        perspectives.put(PerspectiveID.TOTAL, new Perspective.Total());
        perspectives.put(PerspectiveID.TRACK_PLAYER, new Perspective.TrackingPlayer());
        perspectives.put(PerspectiveID.NEAR_PLAYER, new Perspective.StalkingPlayer());

        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());
    }

    @Override
    public void init() {
        bindActions();
        enableActionBindings(THE_KEYBOARD);
        game().scoreVisibleProperty().set(true);
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        scores3D.setFont(THE_ASSETS.font("font.arcade", 8));
    }

    @Override
    public final void end() {
        disableActionBindings(THE_KEYBOARD);
        perspectiveNamePy.unbind();
        level3D.stopAnimations();
        level3D = null;
    }

    @Override
    public void bindActions() {
        bind(GameAction.PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT));
        bind(GameAction.PERSPECTIVE_NEXT, alt(KeyCode.RIGHT));
        bind(GameAction.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        if (game().isDemoLevel()) {
            bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
        } else {
            bindDefaultArcadeActions();
            bindCheatActions();
        }
        enableActionBindings(THE_KEYBOARD);
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        game().level().ifPresent(level -> {
            bindActions(); //TODO check if this is necessary
            if (level3D == null) {
                replaceGameLevel3D();
                level3D.addLevelCounter();
            }
            switch (gameState()) {
                case TESTING_LEVELS, TESTING_LEVEL_TEASERS -> {
                    replaceGameLevel3D();
                    level3D.playLivesCounterAnimation();
                    level3D.energizers3D().forEach(Energizer3D::startPumping);
                    showLevelTestMessage(level, "TEST LEVEL " + level.number());
                }
                default -> {
                    if (!game().isDemoLevel()) {
                        showReadyMessage(level);
                    }
                }
            }
            updateScores();
            perspective().init(fxSubScene, level);
        });
    }

    @Override
    public void onSceneVariantSwitch(GameScene fromScene) {
        game().level().ifPresent(level -> {
            bindActions();
            enableActionBindings(THE_KEYBOARD);
            if (level3D == null) {
                replaceGameLevel3D();
                level3D.addLevelCounter();
            }
            level3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!level.hasEatenFoodAt(pellet.tile())));
            level3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!level.hasEatenFoodAt(energizer.tile())));
            if (oneOf(gameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
                level3D.energizers3D().filter(energizer -> energizer.shape3D().isVisible()).forEach(Energizer3D::startPumping);
            }
            level.pac().show();
            level.ghosts().forEach(Ghost::show);
            level3D.pac3D().init();
            level3D.pac3D().update();
            if (gameState() == GameState.HUNTING) {
                if (level.powerTimer().isRunning()) {
                    THE_SOUND.playPacPowerSound();
                }
                level3D.playLivesCounterAnimation();
            }
            updateScores();
        });
    }

    @Override
    public void update() {
        GameLevel level = game().level().orElse(null);
        if (level == null) {
            // Scene is already visible for 3(?) ticks before game level has been created
            Logger.warn("Tick #{}: Cannot update PlayScene3D: game level not yet available", THE_CLOCK.tickCount());
            return;
        }
        // TODO: may this happen?
        if (level3D == null) {
            Logger.warn("Tick #{}: Cannot update 3D play scene, 3D game level not yet created?", THE_CLOCK.tickCount());
            return;
        }
        level3D.update();

        //TODO how to avoid calling this on every tick?
        if (game().isDemoLevel()) {
            game().assignDemoLevelBehavior(level.pac());
        }
        else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
            updateScores();
            updateSound(level);
        }
        perspective().update(fxSubScene, level, level.pac());
    }

    protected Perspective perspective() {
        return perspectives.get(perspectiveNamePy.get());
    }

    protected void updateScores() {
        Score score = game().scoreManager().score(), highScore = game().scoreManager().highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (game().scoreManager().isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // score is disabled, show text "GAME OVER"
            String assetNamespace = THE_UI_CONFIGS.current().assetNamespace();
            Color color = THE_ASSETS.color(assetNamespace + ".color.game_over_message");
            scores3D.showTextAsScore(THE_ASSETS.text("score.game_over"), color);
        }
    }

    private void updateSound(GameLevel level) {
        if (gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            int sirenNumber = 1 + game().huntingTimer().phaseIndex() / 2;
            THE_SOUND.selectSiren(sirenNumber);
            THE_SOUND.playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            THE_SOUND.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            THE_SOUND.playGhostReturningHomeSound();
        } else {
            THE_SOUND.stopGhostReturningHomeSound();
        }
    }

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
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
    public Vector2f sizeInPx() {
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
            case TESTING_LEVELS        -> onEnterStateTestingLevels();
            case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers();
            case GAME_OVER             -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateStartingGame() {
        if (level3D != null) {
            level3D.stopAnimations();
            level3D.pac3D().init();
            level3D.ghosts3D().forEach(Ghost3DAppearance::init);
            game().level().ifPresent(this::showReadyMessage);
        }
    }

    private void onEnterStateHunting() {
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(Ghost3DAppearance::init);
        level3D.energizers3D().forEach(Energizer3D::startPumping);
        level3D.playLivesCounterAnimation();
    }

    private void onEnterStatePacManDying() {
        level3D.stopAnimations();
        THE_SOUND.stopAll();
        // last update before dying animation
        level3D.pac3D().update();
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        game().level().ifPresent(level -> {
            GameSpriteSheet spriteSheet = THE_UI_CONFIGS.current().spriteSheet();
            RectArea[] numberSprites = spriteSheet.ghostNumberSprites();
            game().eventLog().killedGhosts.forEach(ghost -> {
                int victimIndex = level.victims().indexOf(ghost);
                var numberImage = spriteSheet.crop(numberSprites[victimIndex]);
                level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
            });
        });
    }

    private void onEnterStateLevelComplete() {
        game().level().ifPresent(level -> {
            THE_SOUND.stopAll();
            // if cheat has been used to complete level, food might still exist, so eat it:
            level.worldMap().tiles().filter(level::hasFoodAt).forEach(level::registerFoodEatenAt);
            level3D.pellets3D().forEach(Pellet3D::onEaten);
            level3D.energizers3D().forEach(Energizer3D::onEaten);
            level3D.maze3D().door3D().setVisible(false);
            level3D.stopAnimations();
            level3D.playLevelCompleteAnimation(level, 2.0,
                () -> {
                    perspectiveNamePy.unbind();
                    perspectiveNamePy.set(PerspectiveID.TOTAL);
                },
                () -> perspectiveNamePy.bind(PY_3D_PERSPECTIVE));
        });
    }

    private void onEnterStateLevelTransition() {
        gameState().timer().restartSeconds(3);
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        game().level().ifPresent(level -> perspective().init(fxSubScene, level));
    }

    private void onEnterStateTestingLevels() {
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(Ghost3DAppearance::init);
        game().level().ifPresent(level -> showLevelTestMessage(level, "TEST LEVEL" + level.number()));
        PY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers() {
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(Ghost3DAppearance::init);
        game().level().ifPresent(level -> showLevelTestMessage(level, "PREVIEW LEVEL " + level.number()));
        PY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
    }

    private void onEnterStateGameOver() {
        level3D.stopAnimations();
        // delay state exit for 3 seconds
        gameState().timer().restartSeconds(3);
        if (!game().isDemoLevel() && randomInt(0, 100) < 25) {
            THE_UI.showFlashMessageSec(3, THE_ASSETS.localizedGameOverMessage());
        }
        THE_SOUND.stopAll();
        THE_SOUND.playGameOverSound();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        game().level().flatMap(GameLevel::bonus).ifPresent(
                bonus -> level3D.replaceBonus3D(bonus, THE_UI_CONFIGS.current().spriteSheet()));
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_SOUND.playBonusBouncingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_SOUND.stopBonusBouncingSound();
        }
        THE_SOUND.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_SOUND.stopBonusBouncingSound();
        }
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        THE_SOUND.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_SOUND.playGhostEatenSound();
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = game().isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_SOUND.playGameReadySound();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        Vector2i tile = event.tile().orElse(null);
        if (tile == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            level3D.pellets3D().forEach(Pellet3D::onEaten);
        } else {
            Energizer3D energizer3D = level3D.energizers3D()
                .filter(e3D -> tile.equals(e3D.tile()))
                .findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                level3D.pellets3D()
                    .filter(pellet3D -> tile.equals(pellet3D.tile()))
                    .findFirst()
                    .ifPresent(Pellet3D::onEaten);
            }
            THE_SOUND.playMunchingSound();
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPowerMode(true);
        level3D.maze3D().playMaterialAnimation();
        THE_SOUND.stopSiren();
        THE_SOUND.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPowerMode(false);
        level3D.maze3D().stopMaterialAnimation();
        THE_SOUND.stopPacPowerSound();
    }

    protected void replaceGameLevel3D() {
        level3D = new GameLevel3D(game());
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3.5 * TS));
    }

    private void showLevelTestMessage(GameLevel level, String message) {
        WorldMap worldMap = level.worldMap();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage(GameLevel level) {
        Vector2i houseTopLeft = level.houseMinTile();
        Vector2i houseSize = level.houseSizeInTiles();
        double x = TS * (houseTopLeft.x() + 0.5 * houseSize.x());
        double y = TS * (houseTopLeft.y() +       houseSize.y());
        double seconds = game().isPlaying() ? 0.5 : 2.5;
        level3D.showAnimatedMessage("READY!", seconds, x, y);
    }

    private void playPacManDiesAnimation() {
        gameState().timer().resetIndefiniteTime();
        Animation animation = level3D.pac3D().createDyingAnimation();
        animation.setDelay(Duration.seconds(1));
        animation.setOnFinished(e -> THE_GAME_CONTROLLER.terminateCurrentState());
        animation.play();
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();

        items.add(contextMenuTitleItem(THE_ASSETS.text("scene_display")));

        var item = new MenuItem(THE_ASSETS.text("use_2D_scene"));
        item.setOnAction(ae -> GameAction.TOGGLE_PLAY_SCENE_2D_3D.execute());
        items.add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(THE_ASSETS.text("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        items.add(miPiP);

        items.add(contextMenuTitleItem(THE_ASSETS.text("select_perspective")));

        // Camera perspective radio buttons
        var radioButtonGroup = new ToggleGroup();
        for (var perspective : PerspectiveID.values()) {
            var miPerspective = new RadioMenuItem(THE_ASSETS.text(perspective.name()));
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
                PY_3D_PERSPECTIVE.set((PerspectiveID) radioButton.getUserData());
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
        items.add(contextMenuTitleItem(THE_ASSETS.text("pacman")));

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
        miQuit.setOnAction(ae -> GameAction.SHOW_START_VIEW.execute());
        items.add(miQuit);

        return items;
    }
}