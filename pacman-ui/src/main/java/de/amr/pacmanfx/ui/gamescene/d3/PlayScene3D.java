/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.ui.assets.DisposableGraphicsObject;
import de.amr.basics.ui.assets.RandomTextPicker;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.entities3D.comp.ScoreViewComp;
import de.amr.pacmanfx.ui.entities3D.comp.ScoresView;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.ActionBindingsComp;
import de.amr.pacmanfx.ui.gamescene.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsVM;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends AbstractGameScene implements DisposableGraphicsObject {

    private final DoubleProperty opacity = new SimpleDoubleProperty(0);

    private PerspectiveManager perspectiveManager;

    private ChangeListener<DrawMode> drawModeChangeListener;
    private PlayScene3D_GameEventHandler gameEventHandler;

    private final Group level3DHolder;

    private GameLevel3D level3D;
    private ScoresView scoresView;
    private PlaySceneContextMenu contextMenu;

    private RandomTextPicker textPicker;

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D() {
        setComponent(GameScene3DComp.class, new GameScene3DComp());
        setComponent(GameSceneAnimations3DComp.class, new GameSceneAnimations3DComp());

        level3DHolder = new Group();
        view3D().root().getChildren().addAll(level3DHolder);

        perspectiveManager = new PerspectiveManager(view3D().camera());
        gameEventHandler = new PlayScene3D_GameEventHandler(this);

        drawModeChangeListener = (_, _, drawMode) -> {
            if (level3D != null) {
                level3D.setDrawMode(drawMode);
            }
        };

        final var fadeInAnimation = new PlaySceneFadeInAnimation(Duration.seconds(3), this);
        animations3D().registry().register(fadeInAnimation.name(), fadeInAnimation);
    }

    public GameScene3DComp view3D() {
        return assertComponent(GameScene3DComp.class);
    }

    public GameSceneAnimations3DComp animations3D() {
        return assertComponent(GameSceneAnimations3DComp.class);
    }

    @Override
    protected void onAppConnected() {
        final GameViewModel viewModel = app().ui().viewModel();

        textPicker = new RandomTextPicker(app().ui().translationManager().textBundle(), "game.over");

        final var comp3D = assertComponent(GameScene3DComp.class);
        comp3D.coordinateSystem().visibleProperty().bind(viewModel.common3DSettings().axesVisibleProperty());
        comp3D.ambientLight().colorProperty().bind(viewModel.maze3DSettings().lightColorProperty());
    }

    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(gameEventHandler);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Stream.empty();
    }

    public RandomTextPicker textPicker() {
        return textPicker;
    }

    @Override
    public void dispose() {
        // Dispose the scene components
        super.dispose();

        if (perspectiveManager != null) {
            perspectiveManager.dispose();
            perspectiveManager = null;
        }

        disposeContextMenu();

        if (level3D != null) {
            level3D.dispose();
            level3D = null;
        }

        drawModeChangeListener = null;
        gameEventHandler = null;
    }

    @Override
    public void onBeforeEmbedded() {
        // TODO: reconsider whether scores need recreation here (variant/font change?)
        final String scoreTitle = app().ui().translationManager().translate("score.score");
        final String highScoreTitle = app().ui().translationManager().translate("score.high_score");
        replaceScoresView(scoreTitle, highScoreTitle);
    }

    @Override
    public void onActivate() {
        final Game3DSettingsVM settings3D = app().ui().viewModel().common3DSettings();
        perspectiveManager.activeIDProperty().bind(settings3D.cameraPerspectiveIDProperty());
        settings3D.drawModeProperty().addListener(drawModeChangeListener);
        registerActionBindings();
        assertComponent(GameScene3DComp.class).subScene().setFill(Color.BLACK);
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
        optComponent(ActionBindingsComp.class).ifPresent(comp -> {
            final Optional<GameAction> matchingAction = comp.registry().executeMatchingAction(app());
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

        level.entitySet().entities().anyOfType(Bonus.class).ifPresent(bonus -> level3D.ensureBonus3DViewAddedToSceneGraph(bonus));

        //TODO move out of this class?
        final World3DUpdateSystem updateSystem = GameSystems3D.reqSystem(World3DUpdateSystem.class);
        updateSystem.updateEntities(game, animations3D().registry());

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
        return Optional.of(assertComponent(GameScene3DComp.class).subScene());
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        contextMenu = new PlaySceneContextMenu(this);
        return Optional.of(contextMenu);
    }

    @Override
    public void onQuit() {
        onDeactivate();
        soundManager().setEnabled(false);
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    // Other stuff

    public SubScene subScene() {
        return assertComponent(GameScene3DComp.class).subScene();
    }

    public DoubleProperty opacityProperty() {
        return opacity;
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
                app().ui().translationManager().translate("score.game_over"),
                app().variantManager().currentRuntime().uiConfig().assets().color("color.game_over_message"));
        }

        // High score is always visible
        final Score highScore = session.hud().highScore();
        scoresView.showHighScore(highScore.data().points(), highScore.data().levelNumber());
    }

    public void initPac3DProperties(GameLevel level, Pac pac) {
        requireNonNull(pac);
        requireNonNull(level);

        final GameSystems3D.PacSystems3D systems3D = GameSystems3D.reqSystem(GameSystems3D.PacSystems3D.class);
        systems3D.transform().init(pac, level.worldMap());
        systems3D.animation().stopAnimations(pac);
        systems3D.animation().setPowerMode(pac, false);
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

        final GameVariantPlayConfig config = app().variantManager().currentRuntime().playConfig();
        final GameVariantUIConfig uiConfig = app().variantManager().currentRuntime().uiConfig();
        final GameViewModel viewModel      = app().ui().viewModel();
        final GameSession session          = game.session();

        if (level3D != null) {
            Logger.info("Old 3D game level is disposed...");
            level3D.dispose();
        }

        // Create a new 3D game level representation
        level3D = new GameLevel3D(game, animations3D().registry(), viewModel, uiConfig);
        addAdditional3DLevelElements(level3D);
        level3D.replaceLevelCounter3D(session.hud().levelCounter());
        level3D.setAnimationManager(new GameLevel3DAnimationManager(animations3D().registry(), level3D, config, uiConfig));

        level3DHolder.getChildren().setAll(level3D.root());

        //TODO check this
        final Pac pac = level.entitySet().pac();
        initPac3DProperties(level, pac);

        // Lives counter shapes follow Pac location
        final LivesCounter livesCounter = session.hud().livesCounter();
        final LivesCounter3DViewSystem livesCounter3DViewSystem = GameSystems3D.reqSystem(LivesCounter3DViewSystem.class);
        livesCounter3DViewSystem.startTrackingPac(livesCounter, pac);
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with additional stuff as done by the
     * Tengen Ms. Pac-Man game that displays the level number, game difficulty, map category, booster mode etc.
     */
    protected void addAdditional3DLevelElements(GameLevel3D level3D) {}

    protected void registerActionBindings() {
        actionBindings().registry().registerAllBindings(app().commonActions().camera3DActions().bindings());
    }

    private void replaceScoresView(String leftTitle, String rightTitle) {
        final GameSession session = game().session();

        final ScoresView oldScoresView = scoresView;
        if (oldScoresView != null) {
            assertComponent(GameScene3DComp.class).root().getChildren().remove(oldScoresView.root());
        }

        final Score leftScore = session.hud().gameScore();
        if (!leftScore.hasComponent(ScoreViewComp.class)) {
            leftScore.setComponent(ScoreViewComp.class, new ScoreViewComp());
        }
        leftScore.assertComponent(ScoreViewComp.class).titleDisplay().setText(leftTitle);

        final Score rightScore = session.hud().highScore();
        if (!rightScore.hasComponent(ScoreViewComp.class)) {
            rightScore.setComponent(ScoreViewComp.class, new ScoreViewComp());
        }
        rightScore.assertComponent(ScoreViewComp.class).titleDisplay().setText(rightTitle);

        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), 8);
        scoresView = new ScoresView(leftScore, rightScore);
        scoresView.setFont(arcade8);

        assertComponent(GameScene3DComp.class).root().getChildren().add(scoresView.root());

        //scoresView.textOpacity.bind(scoreOpacity);

        // Scores must always face towards viewer, independent of current perspective:
        final var comp3D = assertComponent(GameScene3DComp.class);
        final Node root = scoresView.root();
        root.rotationAxisProperty().bind(comp3D.camera().rotationAxisProperty());
        root.rotateProperty().bind(comp3D.camera().rotateProperty());

        // Scores are shown slightly "behind" and over game level from viewer's perspective
        root.translateXProperty().bind(level3DHolder.translateXProperty().add(TS));
        root.translateYProperty().bind(level3DHolder.translateYProperty().subtract(4.5 * TS));
        root.translateZProperty().bind(level3DHolder.translateZProperty().subtract(4.5 * TS));
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