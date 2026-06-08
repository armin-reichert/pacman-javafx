/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.ui.app.AppConstants;
import de.amr.pacmanfx.ui.app.AppContext;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
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

import static de.amr.pacmanfx.core.Globals.TS;
import static de.amr.pacmanfx.ui.app.AppConstants.PROPERTY_3D_DRAW_MODE;
import static de.amr.pacmanfx.ui.app.AppConstants.PROPERTY_3D_LIGHT_COLOR;
import static de.amr.pacmanfx.ui.action.CommonActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends GameScene implements DisposableGraphicsObject {

    public final DoubleProperty scoreOpacity = new SimpleDoubleProperty(0);

    protected PerspectiveManager perspectives;
    protected Set<ActionBinding> bindings;

    protected Group subSceneRoot;
    protected SubScene subScene;
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

    private final RandomTextPicker gameOverMessagePicker;

    private final ManagedAnimation fadeInAnimation = new PlaySceneFadeInAnimation(Duration.seconds(3), this);

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D(AppContext context) {
        super(context);
        gameOverMessagePicker = new RandomTextPicker(context.ui().translations().textBundle(), "game.over");
        createSubScene();
        createBindings();
        bindActions();
        setGameEventHandler(new PlayScene3DGameEventHandler(this));
    }

    public SubScene subScene() {
        return subScene;
    }

    public PerspectiveManager perspectiveManager() {
        return perspectives;
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
        // If score is disabled, show "GAME OVER" text
        final Score score = level.game().score();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            scores3D.showTextForScore(
                appContext().ui().translations().translate("score.game_over"),
                appContext().currentUIConfig().assets().color("color.game_over_message"));
        }

        // High score is always visible
        final Score highScore = level.game().highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    public void initPac3D(GameContext gameContext, Pac3D pac3D, GameLevel level) {
        requireNonNull(gameContext);
        requireNonNull(pac3D);
        requireNonNull(level);
        pac3D.init(gameContext, level);
        pac3D.update(gameContext, level);
    }

    public void initFood3D(FoodLayer foodLayer, boolean startEnergizerPumping) {
        requireNonNull(foodLayer);
        level3D.pellets3D().forEach(pellet3D -> pellet3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(pellet3D.tile())));
        level3D.energizers3D().forEach(energizer3D -> {
            energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile()));
            if (startEnergizerPumping && energizer3D.shape().isVisible()) {
                energizer3D.startPumping();
            }
        });
    }

    public void replaceGameLevel3D(GameContext gameContext, GameLevel level) {
        requireNonNull(level);
        if (level3D != null) {
            Logger.info("Old 3D game level gets disposed...");
            level3D.dispose();
        }
        level3D = new GameLevel3D(gameContext, level, appContext().currentUIConfig());
        decorate(level3D);
        level3DParent.getChildren().setAll(level3D);

        level3D.entities().selectAll().forEach(entity -> entity.init(gameContext, level));
        level3D.startLivesCounterTrackingPac();

        level3D.createAnimations(AppConstants.DEFAULT_PARTICLE_ANIMATION_CONFIG);

        Logger.info("Created and added new 3D game level to play scene");
    }

    public void showRandomGameOverMessage() {
        appContext().shortMessage(Duration.seconds(2.5), gameOverMessagePicker.selectNextText());
    }

    @Override
    public void dispose() {
        actionBindings().dispose();
        perspectives.dispose();
        disposeContextMenu();
        removeAndDisposeGameLevel3D();
        cleanupLight(ambientLight);
        ambientLight = null;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // GameScene interface implementation
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void onEmbedded() {
        // TODO: reconsider whether scores need recreation here (variant/font change?)
        replaceScores3D();
    }

    @Override
    public void onActivate() {
        perspectives.activeIDProperty().bind(AppConstants.PROPERTY_3D_PERSPECTIVE_ID);
        PROPERTY_3D_DRAW_MODE.addListener(drawModeChangeListener);
        subScene.setFill(Color.BLACK);
    }

    @Override
    public void onDeactivate() {
        perspectives.activeIDProperty().unbind();
        PROPERTY_3D_DRAW_MODE.removeListener(drawModeChangeListener);
        disposeContextMenu();
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
        updateHUD3D(level);
        perspectives.updatePerspective(level);
        optSoundEffects().ifPresent(soundEffects -> {
            soundEffects.setEnabled(!level.isDemoLevel());
            soundEffects.playAmbientGameLevelSound(gameContext(), level);
        });
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            perspectives.actionDroneClimb().executeIfEnabled(appContext());
        } else if (scrollEvent.getDeltaY() > 0) {
            perspectives.actionDroneDescent().executeIfEnabled(appContext());
        }
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.of(subScene);
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        contextMenu = new PlaySceneContextMenu(appContext());
        return Optional.of(contextMenu);
    }

    // Other stuff

    // Initial subscene size is irrelevant (will be bound to parent scene size)
    private void createSubScene() {
        subSceneRoot = new Group();
        subScene = new SubScene(subSceneRoot, 888, 666, true, SceneAntialiasing.BALANCED);

        camera = new PerspectiveCamera(true);
        perspectives = new PerspectiveManager(camera);
        subScene.setCamera(camera);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(AppConstants.PROPERTY_3D_AXES_VISIBLE);

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);

        subSceneRoot.getChildren().addAll(level3DParent, coordinateSystem, ambientLight);
    }

    private void createBindings() {
        bindings = Set.of(
            new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,       alt(KeyCode.LEFT)),
            new ActionBinding(ACTION_PERSPECTIVE_NEXT,           alt(KeyCode.RIGHT)),
            new ActionBinding(perspectives.actionDroneClimb(),   control(KeyCode.MINUS)),
            new ActionBinding(perspectives.actionDroneDescent(), control(KeyCode.PLUS)),
            new ActionBinding(perspectives.actionDroneReset(),   control(KeyCode.DIGIT0)),
            new ActionBinding(ACTION_TOGGLE_DRAW_MODE,           alt(KeyCode.W))
        );
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with additional stuff as done by the
     * Tengen Ms. Pac-Man game that displays the level number, game difficulty, map category, booster mode etc.
     */
    protected void decorate(GameLevel3D level3D) {}

    protected void bindActions() {
        actionBindings().registerAllBindings(bindings);
    }

    private void replaceScores3D() {
        final Scores3D oldScores3D = scores3D;

        scores3D = new Scores3D(
            appContext().ui().translations().translate("score.score"),
            appContext().ui().translations().translate("score.high_score"),
            AppConstants.FONT_ARCADE_8);

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