/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.CameraControlledView;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._3d.GameActions3D;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.ui._3d.level.*;
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
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_PERSPECTIVE;
import static de.amr.games.pacman.uilib.Keyboard.alt;
import static de.amr.games.pacman.uilib.Ufx.contextMenuTitleItem;

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
            game().level().ifPresent(level -> perspective().init(fxSubScene, level));
        }
    };

    protected final Map<Perspective.Name, Perspective> perspectives = new EnumMap<>(Perspective.Name.class);
    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    protected final SubScene fxSubScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final Scores3D scores3D;

    protected GameLevel3D level3D;

    public PlayScene3D() {
        var axes = new CoordinateSystem();
        axes.visibleProperty().bind(GlobalProperties3d.PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D(TEXT_SCORE, TEXT_HIGH_SCORE);

        // last child is placeholder for level 3D
        getChildren().addAll(scores3D, axes, new Group());

        // initial size is irrelevant, gets bound to parent scene size later
        fxSubScene = new SubScene(this, 88, 88, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(Color.TRANSPARENT);
        fxSubScene.setCamera(camera);

        perspectives.put(Perspective.Name.DRONE, new Perspective.Drone());
        perspectives.put(Perspective.Name.TOTAL, new Perspective.Total());
        perspectives.put(Perspective.Name.TRACK_PLAYER, new Perspective.TrackingPlayer());
        perspectives.put(Perspective.Name.NEAR_PLAYER, new Perspective.StalkingPlayer());

        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());
    }

    @Override
    public void init() {
        bindGameActions();
        enableActionBindings();
        THE_UI.setScoreVisible(true);
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        scores3D.setFont(THE_UI.assets().font("font.arcade", 8));
    }

    @Override
    public final void end() {
        disableActionBindings();
        perspectiveNamePy.unbind();
        level3D.stopAnimations();
        level3D = null;
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        bind(GameActions3D.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        if (game().isDemoLevel()) {
            bind(GameActions2D.INSERT_COIN, THE_UI.keyboard().currentArcadeKeyBinding().key(Arcade.Button.COIN));
        } else {
            bindDefaultArcadeControllerActions(THE_UI.keyboard().currentArcadeKeyBinding());
            bindAlternativePlayerControlActions();
            bindCheatActions();
        }
        enableActionBindings();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        game().level().ifPresent(level -> {
            bindGameActions(); //TODO check if this is necessary
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
            bindGameActions();
            enableActionBindings();
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
                    THE_UI.sound().playPacPowerSound();
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
            Logger.warn("Tick #{}: Cannot update PlayScene3D: game level not yet available", THE_UI.clock().tickCount());
            return;
        }
        // TODO: may this happen?
        if (level3D == null) {
            Logger.warn("Tick #{}: Cannot update 3D play scene, 3D game level not yet created?", THE_UI.clock().tickCount());
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
            String assetNamespace = THE_UI.configurations().current().assetNamespace();
            Color color = THE_UI.assets().color(assetNamespace + ".color.game_over_message");
            scores3D.showTextAsScore(TEXT_GAME_OVER, color);
        }
    }

    private void updateSound(GameLevel level) {
        if (gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            int sirenNumber = 1 + game().huntingTimer().phaseIndex() / 2;
            THE_UI.sound().selectSiren(sirenNumber);
            THE_UI.sound().playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            THE_UI.sound().stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            THE_UI.sound().playGhostReturningHomeSound();
        } else {
            THE_UI.sound().stopGhostReturningHomeSound();
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
        THE_UI.sound().stopAll();
        // last update before dying animation
        level3D.pac3D().update();
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        game().level().ifPresent(level -> {
            GameSpriteSheet spriteSheet = THE_UI.configurations().current().spriteSheet();
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
            THE_UI.sound().stopAll();
            // if cheat has been used to complete level, food might still exist, so eat it:
            level.worldMap().tiles().filter(level::hasFoodAt).forEach(level::registerFoodEatenAt);
            level3D.pellets3D().forEach(Pellet3D::onEaten);
            level3D.energizers3D().forEach(Energizer3D::onEaten);
            level3D.maze3D().door3D().setVisible(false);
            level3D.stopAnimations();
            level3D.playLevelCompleteAnimation(level, 2.0,
                () -> {
                    perspectiveNamePy.unbind();
                    perspectiveNamePy.set(Perspective.Name.TOTAL);
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
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers() {
        replaceGameLevel3D();
        level3D.addLevelCounter();
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(Ghost3DAppearance::init);
        game().level().ifPresent(level -> showLevelTestMessage(level, "PREVIEW LEVEL " + level.number()));
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateGameOver() {
        level3D.stopAnimations();
        // delay state exit for 3 seconds
        gameState().timer().restartSeconds(3);
        if (!game().isDemoLevel() && randomInt(0, 100) < 25) {
            THE_UI.showFlashMessageSec(3, THE_UI.assets().localizedGameOverMessage());
        }
        THE_UI.sound().stopAll();
        THE_UI.sound().playGameOverSound();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        game().level().flatMap(GameLevel::bonus).ifPresent(
                bonus -> level3D.replaceBonus3D(bonus, THE_UI.configurations().current().spriteSheet()));
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_UI.sound().playBonusBouncingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_UI.sound().stopBonusBouncingSound();
        }
        THE_UI.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        //TODO check for moving bonus instead
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN)) {
            THE_UI.sound().stopBonusBouncingSound();
        }
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        THE_UI.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_UI.sound().playGhostEatenSound();
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = game().isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_UI.sound().playGameReadySound();
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
            THE_UI.sound().playMunchingSound();
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPowerMode(true);
        level3D.maze3D().playMaterialAnimation();
        THE_UI.sound().stopSiren();
        THE_UI.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPowerMode(false);
        level3D.maze3D().stopMaterialAnimation();
        THE_UI.sound().stopPacPowerSound();
    }

    protected void replaceGameLevel3D() {
        level3D = new GameLevel3D(game());
        int lastIndex = getChildren().size() - 1;
        getChildren().set(lastIndex, level3D);
        scores3D.translateXProperty().bind(level3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.translateZProperty().subtract(3.5 * TS));
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
        animation.setOnFinished(e -> gameState().timer().expire());
        animation.play();
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();

        items.add(contextMenuTitleItem(THE_UI.assets().text("scene_display")));

        var item = new MenuItem(THE_UI.assets().text("use_2D_scene"));
        item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute());
        items.add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(THE_UI.assets().text("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        items.add(miPiP);

        items.add(contextMenuTitleItem(THE_UI.assets().text("select_perspective")));

        // Camera perspective radio buttons
        var radioButtonGroup = new ToggleGroup();
        for (var perspective : Perspective.Name.values()) {
            var miPerspective = new RadioMenuItem(THE_UI.assets().text(perspective.name()));
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
        items.add(contextMenuTitleItem(THE_UI.assets().text("pacman")));

        var miAutopilot = new CheckMenuItem(THE_UI.assets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(THE_UI.assets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(THE_UI.assets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(THE_UI.sound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(THE_UI.assets().text("quit"));
        miQuit.setOnAction(ae -> GameActions2D.SHOW_START_PAGE.execute());
        items.add(miQuit);

        return items;
    }
}