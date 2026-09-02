/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.gamescene.common.ActionBindingsSupport;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.vm.Game3DSettingsVM;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DTransformSystem;
import de.amr.pacmanfx.uilib.entities3D.score.comp.ScoreViewComp;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import de.amr.pacmanfx.uilib.widgets.ScoresView;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PlayScene3D extends GameScene
    implements PlayScene3D_GameEventHandler, DisposableGraphicsObject {

    public final DoubleProperty scoreOpacity = new SimpleDoubleProperty(0);

    private final PerspectiveManager perspectiveManager;
    private final Set<ActionKeyBinding> actionBindings;
    private final AnimationRegistry registry = new AnimationRegistry();
    private final SubScene subScene;
    private final Group subSceneRoot;
    private final PerspectiveCamera camera;
    private final Group level3DParent = new Group();
    private final RandomTextPicker textPicker;
    private final ChangeListener<DrawMode> drawModeChangeListener;
    private final ManagedAnimation fadeInAnimation = new PlaySceneFadeInAnimation(Duration.seconds(3), this);

    private GameLevel3D level3D;
    private ScoresView scoresView;
    private PlaySceneContextMenu contextMenu;
    private AmbientLight ambientLight;

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D(GameAppContext app) {
        super(app);

        final GameViewModel viewModel = app.ui().viewModel();

        textPicker = new RandomTextPicker(app.ui().translations().textBundle(), "game.over");

        camera = new PerspectiveCamera(true);
        perspectiveManager = new PerspectiveManager(camera);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(viewModel.common3DSettings().axesVisibleProperty());

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(viewModel.maze3DSettings().lightColorProperty());

        subSceneRoot = new Group(level3DParent, coordinateSystem, ambientLight);

        subScene = new SubScene(subSceneRoot, 888, 666, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        actionBindings = app.commonActions().camera3DActions().bindings();

        drawModeChangeListener = (_, _, drawMode) -> {
            if (level3D != null) {
                level3D.setDrawMode(drawMode);
            }
        };
    }

    @Override
    public RandomTextPicker textPicker() {
        return textPicker;
    }

    @Override
    public PlayScene3D gameScene() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose();

        perspectiveManager.dispose();
        disposeContextMenu();
        if (level3D != null) {
            level3DParent.getChildren().clear();
            level3D.dispose();
            level3D = null;
        }
        cleanupLight(ambientLight);
        ambientLight = null;
    }

    @Override
    public void onBeforeEmbedded() {
        // TODO: reconsider whether scores need recreation here (variant/font change?)
        final String scoreTitle = app().ui().translations().translate("score.score");
        final String highScoreTitle = app().ui().translations().translate("score.high_score");
        replaceScoresView(scoreTitle, highScoreTitle);
    }

    @Override
    public void onActivate() {
        final Game3DSettingsVM settings3D = app().ui().viewModel().common3DSettings();
        perspectiveManager.activeIDProperty().bind(settings3D.cameraPerspectiveIDProperty());
        settings3D.drawModeProperty().addListener(drawModeChangeListener);
        subScene.setFill(Color.BLACK);
        bindActions();
    }

    @Override
    public void onDeactivate() {
        perspectiveManager.activeIDProperty().unbind();
        app().ui().viewModel().common3DSettings().drawModeProperty().removeListener(drawModeChangeListener);
        disposeContextMenu();
    }

    @Override
    public void onInput() {
        final Keyboard keyboard = app().input().keyboard();
        components().optComp(ActionBindingsSupport.class).ifPresent(comp -> {
            final Optional<GameAction> matchingAction = comp.bindingsMap().executeMatchingAction(app());
            if (matchingAction.isEmpty()) {
                // Handle CTRL-PLUS, CTRL_MINUS and CTRL-0
                perspectiveManager.optPerspective(PerspectiveID.DRONE).ifPresent(perspective -> {
                    if (perspective instanceof DronePerspective dronePerspective) {
                        dronePerspective.handleKeyPressed(keyboard);
                    }
                });
            }

        });
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final long tick = session.thisFrame().tick();

        if (level == null) {
            Logger.info("Tick {}: Game level not yet created, update ignored", tick);
            return;
        }

        if (level3D == null) {
            Logger.info("Tick {}: Game level 3D not yet created, update ignored", tick);
            return;
        }

        GameLevel3DUpdateController.update3DSceneEntities(game, level3D);
        updateHUD3D(game);

        ensureAnimationsRunning();

        perspectiveManager.updatePerspective(level);

        optSoundEffects().ifPresent(soundEffects -> {
            soundEffects.setEnabled(!session.isAttractMode());
            soundEffects.playAmbientGameLevelSound(game, level);
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
    public Optional<ContextMenu> optContextMenu() {
        contextMenu = new PlaySceneContextMenu(app());
        return Optional.of(contextMenu);
    }

    @Override
    public void handleQuit(GameAppContext appContext) {
        onDeactivate();
        soundManager().setEnabled(false);
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    // Other stuff

    public SubScene subScene() {
        return subScene;
    }

    public PerspectiveManager perspectiveManager() {
        return perspectiveManager;
    }

    public Optional<GameLevel3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

    public Optional<ScoresView> optScoresView() {
        return Optional.ofNullable(scoresView);
    }

    public void fadeIn() {
        fadeInAnimation.playFromStart();
    }

    public void replaceActionBindings(GameSession session, GameLevel level) {
        // No-op — override in subclasses if variant needs different bindings
    }

    public void updateHUD3D(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();

        // If score is disabled, show "GAME OVER" text instead
        final Score score = session.hud().gameScore();
        if (score.data().isEnabled()) {
            scoresView.showScore(score.data().points(), score.data().levelNumber());
        } else {
            scoresView.showTextForScore(
                app().ui().translations().translate("score.game_over"),
                app().gameVariants().currentGameVariant().uiConfig().assets().color("color.game_over_message"));
        }

        // High score is always visible
        final Score highScore = session.hud().highScore();
        scoresView.showHighScore(highScore.data().points(), highScore.data().levelNumber());
    }

    public void initPac3DProperties(GameLevel level, Pac pac) {
        requireNonNull(pac);
        requireNonNull(level);

        Pac3DTransformSystem.init(pac, level);
        Pac3DAnimationSystem.stopAll(pac);
        Pac3DAnimationSystem.setPowerMode(pac, false);
    }

    public void initFood3D(GameLevel level, boolean startEnergizerPumping) {
        level3D.pellets3D().forEach(pellet3D -> pellet3D.root().setVisible(!level.food().hasEatenFoodAtTile(pellet3D.tile())));

        if (startEnergizerPumping) {
            level3D.animationManager().startEnergizerPumping();
        }
        level3D.energizers3D()
            .forEach(energizer3D -> energizer3D.root().setVisible(!level.food().hasEatenFoodAtTile(energizer3D.tile())));
    }

    public void replaceGameLevel3D(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameVariantConfig config     = app().currentGameVariantConfig();
        final GameVariantUIConfig uiConfig = app().currentGameVariantUIConfig();
        final GameViewModel viewModel      = app().ui().viewModel();
        final GameSession session          = game.session();

        if (level3D != null) {
            Logger.info("Old 3D game level is disposed...");
            level3D.dispose();
        }

        // Create a new 3D game level representation
        level3D = new GameLevel3D(game, registry, viewModel, uiConfig);
        addAdditional3DLevelElements(level3D);
        level3D.replaceLevelCounter3D(session.hud().levelCounter());
        level3D.setAnimationManager(new GameLevel3DAnimationManager(registry, level3D, config, uiConfig));

        level3DParent.getChildren().setAll(level3D.root());

        //TODO check this
        final Pac pac = level.entities().pac();
        initPac3DProperties(level, pac);

        LivesCounter3DViewSystem.startTracking(session.hud().livesCounter(), pac);
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with additional stuff as done by the
     * Tengen Ms. Pac-Man game that displays the level number, game difficulty, map category, booster mode etc.
     */
    protected void addAdditional3DLevelElements(GameLevel3D level3D) {}

    protected void bindActions() {
        components().optComp(ActionBindingsSupport.class)
            .ifPresent(comp -> comp.bindingsMap().registerAllBindings(actionBindings));
    }

    private void replaceScoresView(String leftTitle, String rightTitle) {
        final GameSession session = game().session();

        final ScoresView oldScoresView = scoresView;
        if (oldScoresView != null) {
            subSceneRoot.getChildren().remove(oldScoresView.root());
        }

        final Score leftScore = session.hud().gameScore();
        if (!leftScore.hasComp(ScoreViewComp.class)) {
            leftScore.setComp(ScoreViewComp.class, new ScoreViewComp());
        }
        leftScore.reqComp(ScoreViewComp.class).titleDisplay().setText(leftTitle);

        final Score rightScore = session.hud().highScore();
        if (!rightScore.hasComp(ScoreViewComp.class)) {
            rightScore.setComp(ScoreViewComp.class, new ScoreViewComp());
        }
        rightScore.reqComp(ScoreViewComp.class).titleDisplay().setText(rightTitle);

        scoresView = new ScoresView(leftScore, rightScore);
        scoresView.setFont(GlobalAssets.Fonts.ARCADE8.font());

        subSceneRoot.getChildren().add(scoresView.root());

        //scoresView.textOpacity.bind(scoreOpacity);

        // Scores must always face towards viewer, independent of current perspective:
        final Node root = scoresView.root();
        root.rotationAxisProperty().bind(camera.rotationAxisProperty());
        root.rotateProperty().bind(camera.rotateProperty());

        // Scores are shown slightly "behind" and over game level from viewer's perspective
        root.translateXProperty().bind(level3DParent.translateXProperty().add(WorldMap.TS));
        root.translateYProperty().bind(level3DParent.translateYProperty().subtract(4.5 * WorldMap.TS));
        root.translateZProperty().bind(level3DParent.translateZProperty().subtract(4.5 * WorldMap.TS));
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }

    private void ensureAnimationsRunning() {
        if (game().state().hasSameNameAs(CommonGameStateID.DEMO_LEVEL_PLAYING) ||
            game().state().hasSameNameAs(CommonGameStateID.GAME_LEVEL_PLAYING)) {
            level3D.animationManager().startEnergizerPumping();
        }
    }
}