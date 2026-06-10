/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.ui.Globals_GameUI;
import de.amr.pacmanfx.ui.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.d3.camera.DronePerspective;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals_Core.TS;
import static de.amr.pacmanfx.ui.action.CommonActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends GameScene implements DisposableGraphicsObject {

    public final DoubleProperty scoreOpacity = new SimpleDoubleProperty(0);

    protected PerspectiveManager perspectiveManager;
    protected Set<ActionKeyBinding> actionBindings;

    protected SubScene subScene;
    protected Group subSceneRoot;
    protected PerspectiveCamera camera;
    protected Group level3DParent = new Group();
    protected GameLevel3D level3D;
    protected Scores3D scores3D;
    protected PlaySceneContextMenu contextMenu;
    protected AmbientLight ambientLight;

    private final ChangeListener<DrawMode> drawModeChangeListener = (_, _, drawMode) -> {
        if (level3D != null) {
            level3D.setDrawMode(drawMode);
        }
    };

    private final ManagedAnimation fadeInAnimation = new PlaySceneFadeInAnimation(Duration.seconds(3), this);

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D(Game game) {
        super(game);

        createSubScene();
        createBindings();

        bindActions();
        setGameEventHandler(new PlayScene3DGameEventHandler(this));
    }

    public SubScene subScene() {
        return subScene;
    }

    public PerspectiveManager perspectiveManager() {
        return perspectiveManager;
    }

    public Optional<GameLevel3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

    public ManagedAnimation fadeInAnimation() {
        return fadeInAnimation;
    }

    public void replaceActionBindings(GameLevel level) {
        // No-op — override in subclasses if variant needs different bindings
    }

    public void updateHUD3D(GameLevel level) {
        requireNonNull(level);

        // If score is disabled, show "GAME OVER" text instead
        final Score score = level.game().score();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            scores3D.showTextForScore(
                game().ui().translations().translate("score.game_over"),
                game().currentUIConfig().assets().color("color.game_over_message"));
        }

        // High score is always visible
        final Score highScore = level.game().highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    public void initPac3D(Pac3D pac3D, GameLevel level) {
        requireNonNull(pac3D);
        requireNonNull(level);

        pac3D.init(gameContext(), level);
        pac3D.update(gameContext(), level);
    }

    public void initFood3D(GameLevel level, boolean startEnergizerPumping) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        level3D.pellets3D().forEach(pellet3D -> pellet3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(pellet3D.tile())));

        level3D.energizers3D().forEach(energizer3D -> {
            energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile()));
            if (startEnergizerPumping && energizer3D.shape().isVisible()) {
                energizer3D.startPumping();
            }
        });
    }

    public void replaceGameLevel3D(GameLevel level) {
        requireNonNull(level);

        if (level3D != null) {
            Logger.info("Old 3D game level is disposed...");
            level3D.dispose();
        }
        level3D = new GameLevel3D(gameContext(), level, game().currentUIConfig());
        decorate(level3D);
        level3DParent.getChildren().setAll(level3D);

        level3D.createAnimations(Globals_3D.DEFAULT_PARTICLE_ANIMATION_CONFIG);
        level3D.entities().selectAll().forEach(entity -> entity.init(gameContext(), level));
        level3D.startLivesCounterTrackingPac();

        Logger.info("New 3D game level created");
    }

    @Override
    public void dispose() {
        actionBindings().dispose();
        perspectiveManager.dispose();
        disposeContextMenu();
        removeAndDisposeGameLevel3D();
        cleanupLight(ambientLight);
        ambientLight = null;
    }

    @Override
    public void onEmbedded() {
        // TODO: reconsider whether scores need recreation here (variant/font change?)
        replaceScores3D();
    }

    @Override
    public void onActivate() {
        perspectiveManager.activeIDProperty().bind(Globals_3D.PROPERTY_3D_PERSPECTIVE_ID);
        Globals_3D.PROPERTY_3D_DRAW_MODE.addListener(drawModeChangeListener);
        subScene.setFill(Color.BLACK);
    }

    @Override
    public void onDeactivate() {
        perspectiveManager.activeIDProperty().unbind();
        Globals_3D.PROPERTY_3D_DRAW_MODE.removeListener(drawModeChangeListener);
        disposeContextMenu();
    }

    @Override
    public void onInput() {
        final Keyboard keyboard = game().input().keyboard();
        final GameAction gameAction = actionBindings().triggeredAction(keyboard).orElse(null);
        if (gameAction != null) {
            gameAction.execute(game());
        } else {
            // Handle CTRL-PLUS, CTRL_MINUS and CTRL-0
            perspectiveManager.optPerspective(PerspectiveID.DRONE).ifPresent(perspective -> {
                if (perspective instanceof DronePerspective dronePerspective) {
                    dronePerspective.handleKeyPressed(keyboard);
                }
            });
        }
    }

    @Override
    public void onTick(long tick) {
        final GameLevel level = gameContext().optCurrentLevel().orElse(null);

        if (level == null) {
            Logger.info("Tick {}: Game level not yet created, update ignored", tick);
            return;
        }

        if (level3D == null) {
            Logger.info("Tick {}: Game level 3D not yet created, update ignored", tick);
            return;
        }

        level3D.entities().selectAll().forEach(entity -> entity.update(gameContext(), level));

        perspectiveManager.updatePerspective(level);
        updateHUD3D(level);

        optSoundEffects().ifPresent(soundEffects -> {
            soundEffects.setEnabled(!level.isDemoLevel());
            soundEffects.playAmbientGameLevelSound(gameContext(), level);
        });
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        perspectiveManager.currentPerspective().ifPresent(perspective -> {
            if (perspective instanceof DronePerspective dronePerspective) {
                dronePerspective.handleScrollEvent(scrollEvent);
            }
        });
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        contextMenu = new PlaySceneContextMenu(game());
        return Optional.of(contextMenu);
    }

    // Other stuff

    // Initial subscene size is irrelevant (will be bound to parent scene size)
    private void createSubScene() {
        subSceneRoot = new Group();
        subScene = new SubScene(subSceneRoot, 888, 666, true, SceneAntialiasing.BALANCED);

        camera = new PerspectiveCamera(true);
        perspectiveManager = new PerspectiveManager(camera);
        subScene.setCamera(camera);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(Globals_3D.PROPERTY_3D_AXES_VISIBLE);

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(Globals_3D.PROPERTY_3D_LIGHT_COLOR);

        subSceneRoot.getChildren().addAll(level3DParent, coordinateSystem, ambientLight);
    }

    private void createBindings() {
        actionBindings = Set.of(
            new ActionKeyBinding(ACTION_PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT)),
            new ActionKeyBinding(ACTION_PERSPECTIVE_NEXT, alt(KeyCode.RIGHT)),
            new ActionKeyBinding(ACTION_TOGGLE_DRAW_MODE, alt(KeyCode.W)));
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with additional stuff as done by the
     * Tengen Ms. Pac-Man game that displays the level number, game difficulty, map category, booster mode etc.
     */
    protected void decorate(GameLevel3D level3D) {}

    protected void bindActions() {
        actionBindings().registerAllBindings(actionBindings);
    }

    private void replaceScores3D() {
        final Scores3D oldScores3D = scores3D;

        scores3D = new Scores3D(
            game().ui().translations().translate("score.score"),
            game().ui().translations().translate("score.high_score"),
            Globals_GameUI.FONT_ARCADE_8);

        scores3D.textOpacity.bind(scoreOpacity);

        // Scores are always displayed towards viewer, independent of camera perspective
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(level3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(level3DParent.translateZProperty().subtract(4.5 * TS));

        if (oldScores3D != null) {
            subSceneRoot.getChildren().remove(oldScores3D);
        }
        subSceneRoot.getChildren().add(scores3D);
    }

    private void removeAndDisposeGameLevel3D() {
        if (level3D != null) {
            level3DParent.getChildren().clear();
            level3D.dispose();
            level3D = null;
        }
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }
}