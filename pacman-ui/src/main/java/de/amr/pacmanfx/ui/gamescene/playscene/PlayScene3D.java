/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Pool;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.ui.animation.ManagedAnimation;
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
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.entities3D.comp.ScoreViewComp;
import de.amr.pacmanfx.ui.entities3D.comp.ScoresView;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.pac.anim.MsPacManDyingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.anim.PacChewingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.anim.PacManDyingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.ui.entities3D.world.Energizer3D;
import de.amr.pacmanfx.ui.entities3D.world.EnergizerParticle3D;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.ActionBindingsComp;
import de.amr.pacmanfx.ui.gamescene.d3.GameSceneAnimations3DComp;
import de.amr.pacmanfx.ui.gamescene.d3.GameSceneView3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.*;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsVM;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.math.RandomNumbers.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static java.util.Objects.requireNonNull;

public class PlayScene3D extends AbstractGameScene implements DisposableGraphicsObject {

    private final DoubleProperty opacity = new SimpleDoubleProperty(0);

    private PerspectiveManager perspectiveManager;

    private ChangeListener<DrawMode> drawModeChangeListener;

    private PlayScene3D_GameEventHandler gameEventHandler;

    private GameLevelView3D level3D;

    private ScoresView scoresView;

    private PlaySceneContextMenu contextMenu;

    private RandomTextPicker textPicker;

    private final PlayScene3DAnimationSystem animationSystem = new  PlayScene3DAnimationSystem();

    private final ParticlesAnimationConfig particlesAnimationConfig = Game3DSettingsVM.DEFAULT_PARTICLE_ANIMATION_CONFIG;

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D() {
        setComponent(GameSceneView3D.class, new GameSceneView3D());
        setComponent(GameSceneAnimations3DComp.class, new GameSceneAnimations3DComp());


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

    public GameSceneView3D view3D() {
        return assertComponent(GameSceneView3D.class);
    }

    public GameSceneAnimations3DComp animations3D() {
        return assertComponent(GameSceneAnimations3DComp.class);
    }

    @Override
    protected void onAppConnected() {
        final GameViewModel viewModel = app().ui().viewModel();

        textPicker = new RandomTextPicker(app().ui().translationManager().textBundle(), "game.over");

        final var comp3D = assertComponent(GameSceneView3D.class);
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

        if (particlePool != null) {
            particlePool.dispose();
        }
        disposeEnergizerAnimations();

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
        assertComponent(GameSceneView3D.class).subScene().setFill(Color.BLACK);
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

        level.entitySet().entities().anyOfType(Bonus.class).ifPresent(bonus ->
            level3D.ensureBonus3DViewAddedToSceneGraph(bonus, animations3D().registry()));

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
        return Optional.of(assertComponent(GameSceneView3D.class).subScene());
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

    public PlayScene3DAnimationSystem animationSystem() {
        return animationSystem;
    }

    public SubScene subScene() {
        return assertComponent(GameSceneView3D.class).subScene();
    }

    public DoubleProperty opacityProperty() {
        return opacity;
    }

    public PerspectiveManager perspectiveManager() {
        return perspectiveManager;
    }

    public Optional<GameLevelView3D> optGameLevel3D() {
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
            animationSystem.startEnergizerPumping(this);
        }
        level3D.energizers3D()
            .forEach(energizer3D -> energizer3D.root().setVisible(!level.food().hasEatenFoodAtTile(energizer3D.tile())));
    }

    public void replaceGameLevel3D(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameVariantUIConfig uiConfig = app().variantManager().currentRuntime().uiConfig();
        final GameViewModel viewModel      = app().ui().viewModel();
        final GameSession session          = game.session();

        if (level3D != null) {
            Logger.info("Old 3D game level is disposed...");
            level3D.dispose();
        }

        // Create a new 3D game level representation
        level3D = new GameLevelView3D(game, animations3D().registry(), viewModel, uiConfig);
        addAdditional3DLevelElements(level3D);
        level3D.replaceLevelCounter3D(session.hud().levelCounter());

        view3D().level3DHolder().getChildren().setAll(level3D.root());

        createAnimations(level3D);

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
    protected void addAdditional3DLevelElements(GameLevelView3D level3D) {}

    protected void registerActionBindings() {
        actionBindings().registry().registerAllBindings(app().commonActions().camera3DActions().bindings());
    }

    private void replaceScoresView(String leftTitle, String rightTitle) {
        final GameSession session = game().session();

        final ScoresView oldScoresView = scoresView;
        if (oldScoresView != null) {
            assertComponent(GameSceneView3D.class).root().getChildren().remove(oldScoresView.root());
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

        assertComponent(GameSceneView3D.class).root().getChildren().add(scoresView.root());

        //scoresView.textOpacity.bind(scoreOpacity);

        // Scores must always face towards viewer, independent of current perspective:
        final var comp3D = assertComponent(GameSceneView3D.class);
        final Node root = scoresView.root();
        root.rotationAxisProperty().bind(comp3D.camera().rotationAxisProperty());
        root.rotateProperty().bind(comp3D.camera().rotateProperty());

        // Scores are shown slightly "behind" and over game level from viewer's perspective
        root.translateXProperty().bind(view3D().level3DHolder().translateXProperty().add(TS));
        root.translateYProperty().bind(view3D().level3DHolder().translateYProperty().subtract(4.5 * TS));
        root.translateZProperty().bind(view3D().level3DHolder().translateZProperty().subtract(4.5 * TS));
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }

    private void ensureAnimationsRunning() {
        if (game().state().hasSameNameAs(CommonGameStateID.DEMO_LEVEL_PLAYING) ||
            game().state().hasSameNameAs(CommonGameStateID.GAME_LEVEL_PLAYING)) {
            animationSystem.startEnergizerPumping(this);
        }
    }

    private void createAnimations(GameLevelView3D level3D) {
        final GameVariantPlayConfig config = app().variantManager().currentRuntime().playConfig();
        final GameVariantUIConfig uiConfig = app().variantManager().currentRuntime().uiConfig();

        final GameLevel level = level3D.level();
        final int numFlashes = config.rules().numLevelFlashes(level.number());
        final GameVariantRenderConfig renderConfig = uiConfig.renderConfig();
        final GenericWorldMapColorScheme mapColorScheme = renderConfig.colorScheme(level.worldMap(), uiConfig.worldSettings());

        animations3D().registry().register(PlayScene3DAnimationID.WALL_COLOR_FLASHING,
            new WallColorFlashingAnimation(mapColorScheme, level3D.maze3D().materials().wallTopMaterial()));

        animations3D().registry().register(PlayScene3DAnimationID.LEVEL_COMPLETED_FULL,
            new LevelCompletedAnimation(level3D, config.rules().numLevelFlashes(level.number())));

        animations3D().registry().register(PlayScene3DAnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(level3D, numFlashes));

        final House house = level.entitySet().entities().theOne(House.class);
        createHouseAnimations(house);

        createEnergizerAnimations(uiConfig.worldSettings().energizer());
        createEnergizerParticlesAnimation(level3D.maze3D(), level);

        createGhostAnimations(level, uiConfig.worldSettings().ghosts(), numFlashes);
        createGhostLightAnimation(uiConfig, level, level3D.ghostHunterLight());

        final Pac pac = level.entitySet().pac();
        if (pac.state().isMale()) {
            createPacManAnimations(pac);
        }
        else {
            createMsPacManAnimations(pac);
        }
    }

    private void createPacManAnimations(Pac pac) {
        final Pac3DViewComp view3D = pac.assertComponent(Pac3DViewComp.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HeadBangingAnimation3D(pac));
        anim3D.setDying(new PacManDyingAnimation3D(view3D));
    }

    private void createMsPacManAnimations(Pac pac) {
        final Pac3DViewComp view3D = pac.assertComponent(Pac3DViewComp.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HipSwayingAnimation3D(pac));
        anim3D.setDying(new MsPacManDyingAnimation3D(view3D));
    }

    private Pac3DAnimationComp ensurePacAnim3DExists(Pac pac) {
        if (!pac.hasComponent(Pac3DAnimationComp.class)) {
            final var anim3D = new Pac3DAnimationComp(animations3D().registry());
            pac.setComponent(Pac3DAnimationComp.class, anim3D);
        }
        return pac.assertComponent(Pac3DAnimationComp.class);
    }

    private void createGhostAnimations(GameLevel level, List<GhostSettings> settingsByPersonality, int numFlashes) {
        level.entitySet().ghosts().forEach(ghost -> {
            final GhostSettings settings = settingsByPersonality.get(ghost.personality().ordinal());
            createGhostAnimations(ghost, settings, numFlashes);
        });
    }

    private void createGhostAnimations(Ghost ghost, GhostSettings settings, int numFlashes) {
        final Ghost3DAnimationComp anim3D = ensureGhostAnim3DExists(ghost);
        anim3D.build(animations3D().registry(), ghost, settings, numFlashes);
    }

    private Ghost3DAnimationComp ensureGhostAnim3DExists(Ghost ghost) {
        if (!ghost.hasComponent(Ghost3DAnimationComp.class)) {
            ghost.setComponent(Ghost3DAnimationComp.class, new Ghost3DAnimationComp());
        }
        return ghost.assertComponent(Ghost3DAnimationComp.class);
    }


    private void createEnergizerAnimations(Energizer3DSettings settings) {
        final int pumpingFrequency = settings.pumpingFrequency();
        final double inflatedSize = settings.scalingInflated();
        final double expandedSize = settings.scalingExpanded();
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            final String animationID = Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile);
            animations3D().registry().optAnimation(animationID).ifPresent(ManagedAnimation::dispose);
            final var pumping = createEnergizerPumpingAnimation(
                "Energizer Pumping, Tile %s".formatted(tile),
                energizer3D.root(),
                pumpingFrequency,
                inflatedSize,
                expandedSize
            );
            animations3D().registry().register(animationID, pumping);
        });
    }

    private ManagedAnimation createEnergizerPumpingAnimation(
        String label,
        Shape3D shape3D,
        int pumpingFrequency,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(label);
        animation.setAnimationFactory(() -> {
            final Duration duration = Duration.seconds(1).divide(2 * pumpingFrequency);
            final var pumping = new ScaleTransition(duration, shape3D);
            pumping.setAutoReverse(true);
            pumping.setCycleCount(Animation.INDEFINITE);
            pumping.setInterpolator(Interpolator.EASE_BOTH);
            pumping.setFromX(expandedSize);
            pumping.setFromY(expandedSize);
            pumping.setFromZ(expandedSize);
            pumping.setToX(inflatedSize);
            pumping.setToY(inflatedSize);
            pumping.setToZ(inflatedSize);
            return pumping;
        });
        return animation;
    }

    private void createEnergizerParticlesAnimation(WorldMapView3D maze3D, GameLevel level) {
        final ExplosionConfig explosionConfig = particlesAnimationConfig.explosion();

        final List<PhongMaterial> ghostDressMaterials = level.entitySet().ghosts()
            .map(ghost -> ghost.assertComponent(Ghost3DViewComp.class))
            .map(ghostView3D -> ghostView3D.appearanceMaterialSet().normal().dress())
            .toList();

        particlePool = new Pool<>(300, 300,
            () -> {
                final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
                final double scale = Math.clamp(RANDOM_GENERATOR.nextGaussian(2, 0.1), 0.5, 4);
                final double radius = scale * explosionConfig.particleMeanRadius();
                return new EnergizerParticle3D(radius, material, Vector3f.ZERO);
            },
            particle -> {
                particle.reset();
                particle.shape().setVisible(false);
            }
        );

        final House house = level.entitySet().entities().theOne(House.class);

        animations3D().registry().register(PlayScene3DAnimationID.PARTICLES, new ParticlesAnimation3D(
            house,
            ghostDressMaterials,
            particlePool,
            particlesAnimationConfig,
            maze3D.particlesGroup(),
            particle -> particle.collidesWith(maze3D.floor3D()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void disposeEnergizerAnimations() {
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            animations3D().registry().optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
                .ifPresent(ManagedAnimation::dispose);
        });
    }

    private void createHouseAnimations(House house) {
        final House3DViewComp house3D = house.assertComponent(House3DViewComp.class);
        final var animation =  new House3DAnimationComp(animations3D().registry());
        animation.createDoorsMeltingAnimationFactory(house3D.barThicknessProperty);
        if (!house.hasComponent(House3DAnimationComp.class)) {
            house.setComponent(House3DAnimationComp.class, animation);
        }
    }

    private void createGhostLightAnimation(GameVariantUIConfig gameVariantConfig, GameLevel level, PointLight ghostHunterLight) {
        final var animation = new GhostLightRelayAnimation(ghostHunterLight, level.entitySet().ghosts().toList(),
            gameVariantConfig.worldSettings().ghosts());
        animations3D().registry().register(PlayScene3DAnimationID.GHOST_LIGHT, animation);
    }

}