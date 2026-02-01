/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.Translator;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.MessageView;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D extends Group implements Disposable {

    private final GameUI ui;
    private final UIConfig uiConfig;
    private final GameLevel level;

    private final RandomTextPicker<String> pickerLevelCompleteMessages;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final GameLevel3DAnimations animations;

    private List<MeshView> ghostDressMeshViews;
    private List<MeshView> ghostPupilsMeshViews;
    private List<MeshView> ghostEyesMeshViews;

    private Node[] livesCounterShapes;

    private AmbientLight ambientLight;

    private Maze3D maze3D;
    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutableGhost3D> ghosts3D;
    private Bonus3D bonus3D;
    private MessageView messageView;

    public GameLevel3D(GameUI ui, GameLevel level) {
        this.ui = requireNonNull(ui);
        this.uiConfig = ui.currentConfig();
        this.level = requireNonNull(level);

        PROPERTY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);

        setMouseTransparent(true); // this increases performance, they say...

        ghostDressMeshViews  = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostDressMesh());
        ghostPupilsMeshViews = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostPupilsMesh());
        ghostEyesMeshViews   = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostEyeballsMesh());

        createLevelCounter3D();
        createLivesCounter3D();
        createPac3D(ui.prefs().getFloat("3d.pac.size"));
        ghosts3D = level.ghosts().map(this::createMutatingGhost3D).toList();
        final List<PhongMaterial> ghostNormalDressMaterials = ghosts3D.stream()
            .map(MutableGhost3D::ghost3D)
            .map(Ghost3D::normalMaterialSet)
            .map(Ghost3D.MaterialSet::dress)
            .toList();
        createMaze3D(ghostNormalDressMaterials);
        createAmbientLight();

        animations = new GameLevel3DAnimations(ui, this);

        getChildren().add(maze3D.floor());
        getChildren().add(levelCounter3D);
        getChildren().add(livesCounter3D);
        getChildren().addAll(pac3D, pac3D.light());
        getChildren().addAll(ghosts3D);
        getChildren().addAll(maze3D.house().arcadeHouse3D().swirls());
        getChildren().add(maze3D.food().particleGroupsContainer());
        getChildren().addAll(maze3D.food().energizers3D().stream().map(Energizer3D::shape).toList());
        getChildren().addAll(maze3D.food().pellets3D());
        getChildren().add(maze3D.house().arcadeHouse3D().doors()); // Note order of addition!
        // Note: The order in which children are added to the root matters!
        // Walls and house must be added *after* the actors and swirls, otherwise the transparency is not working correctly.
        getChildren().add(maze3D);
        getChildren().add(ambientLight);

        getChildren().add(animations.ghostLight());

        ghosts3D.forEach(ghost3D -> ghost3D.init(level));
        maze3D.house().arcadeHouse3D().startSwirlAnimations();

        pickerLevelCompleteMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "level.complete");
    }

    public GameLevel3DAnimations animations() {
        return animations;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public GameLevel level() {
        return level;
    }

    public Maze3D maze3D() {
        return maze3D;
    }

    private List<MeshView> createGhostComponentMeshViews(Mesh componentMesh) {
        return IntStream.range(0, 4).mapToObj(_ -> new MeshView(componentMesh)).toList();
    }

    private GhostColorSet createGhostColorSet(byte personality) {
        AssetMap assets = uiConfig.assets();
        return new GhostColorSet(
            new GhostComponentColors(
                assets.color("ghost.%d.color.normal.dress".formatted(personality)),
                assets.color("ghost.%d.color.normal.pupils".formatted(personality)),
                assets.color("ghost.%d.color.normal.eyeballs".formatted(personality))
            ),
            new GhostComponentColors(
                assets.color("ghost.color.frightened.dress"),
                assets.color("ghost.color.frightened.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            ),
            new GhostComponentColors(
                assets.color("ghost.color.flashing.dress"),
                assets.color("ghost.color.flashing.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            )
        );
    }

    private MutableGhost3D createMutatingGhost3D(Ghost ghost) {
        var mutatingGhost3D = new MutableGhost3D(
            animationRegistry,
            ghost,
            createGhostColorSet(ghost.personality()),
            ghostDressMeshViews.get(ghost.personality()),
            ghostPupilsMeshViews.get(ghost.personality()),
            ghostEyesMeshViews.get(ghost.personality()),
            ui.prefs().getFloat("3d.ghost.size"),
            level.numFlashes()
        );
        mutatingGhost3D.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        ));
        return mutatingGhost3D;
    }

    private boolean outsideWorld(Ghost ghost) {
        Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
    }

    private void createPac3D(double size) {
        pac3D = uiConfig.createPac3D(animationRegistry, level.pac(), size);
        pac3D.init(level);
    }

    private void createLivesCounter3D() {
        final double shapeSize = ui.prefs().getFloat("3d.lives_counter.shape_size");
        final int capacity = ui.prefs().getInt("3d.lives_counter.capacity");
        final Color pillarColor = ui.prefs().getColor("3d.lives_counter.pillar_color");
        final Color plateColor = ui.prefs().getColor("3d.lives_counter.plate_color");
        livesCounterShapes = new Node[capacity];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = uiConfig.createLivesCounterShape3D(shapeSize);
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
    }

    private void createLevelCounter3D() {
        WorldMap worldMap = level.worldMap();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (worldMap.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-ui.prefs().getFloat("3d.level_counter.elevation"));
    }

    private void createAmbientLight() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);
    }

    private void createMaze3D(List<PhongMaterial> ghostMaterials) {
        maze3D = new Maze3D(ui, level, animationRegistry, ghostMaterials);
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public PacBase3D pac3D() { return pac3D; }

    public List<MutableGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }


    public AnimationRegistry animationManager() { return animationRegistry; }

    /**
     * Called on each clock tick (frame).
     */
    public void update() {
        pac3D.update(level);
        ghosts3D.forEach(ghost3D -> ghost3D.update(level));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(level));
        if (maze3D != null) {
            maze3D.house().update(level);
        }
        updateLivesCounter3D();
    }

    private void updateLivesCounter3D() {
        if (livesCounter3D != null) {
            final GameControl gameControl = level.game().control();
            final boolean oneMore = gameControl.state().matches(GameControl.StateName.STARTING_GAME_OR_LEVEL) && !level.pac().isVisible();
            final boolean visible = level.game().canStartNewGame();
            int lifeCount = level.game().lifeCount() - 1;
            // when the game starts and Pac-Man is not yet visible, show one more
            if (oneMore) lifeCount += 1;
            livesCounter3D.livesCountProperty().set(lifeCount);
            livesCounter3D.setVisible(visible);
        }
    }

    public void onStartingGame() {
        maze3D.food().energizers3D().forEach(Energizer3D::stopPumping);
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, level.game());
        }
    }

    public void onHuntingStart() {
        pac3D.init(level);
        ghosts3D.forEach(ghost3D -> ghost3D.init(level));
        maze3D.food().energizers3D().forEach(Energizer3D::startPumping);
        maze3D.house().arcadeHouse3D().startSwirlAnimations();
        animations.playGhostLightAnimation();
    }

    public void onPacManDying(StateMachine.State<Game> state) {
        state.timer().resetIndefiniteTime(); // expires when level animation ends
        ui.soundManager().stopAll();
        animations.stopGhostLightAnimation();
        // do one last update before dying animation starts
        pac3D.update(level);
        ghosts3D.forEach(MutableGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        var animation = new SequentialTransition(
            pauseSec(1.5),
            doNow(() -> ui.soundManager().play(SoundID.PAC_MAN_DEATH)),
            pac3D.dyingAnimation().getOrCreateAnimationFX(),
            pauseSec(0.5)
        );
        // Note: adding this inside the animation as last action does not work!
        animation.setOnFinished(_ -> level.game().control().terminateCurrentGameState());
        animation.play();
    }

    public void onEatingGhost() {
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            byte personality = killedGhost.personality();
            int killedIndex = level.energizerVictims().indexOf(killedGhost);
            Image pointsImage = uiConfig.killedGhostPointsImage(killedIndex);
            ghosts3D.get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(StateMachine.State<Game> state, ObjectProperty<PerspectiveID> perspectiveIDProperty) {
        state.timer().resetIndefiniteTime(); // expires when animation ends
        ui.soundManager().stopAll();
        animationRegistry.stopAllAnimations();
        maze3D.food().energizers3D().forEach(Energizer3D::stopPumping); //TODO needed?
        // hide 3D food explicitly because level might have been completed using cheat!
        maze3D.food().pellets3D().forEach(pellet3D -> pellet3D.setVisible(false));
        maze3D.food().energizers3D().forEach(Energizer3D::hide);
        maze3D.food().particleGroupsContainer().getChildren().clear();
        maze3D.house().cleanUp();
        maze3D.house().hideDoors();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        if (messageView != null) {
            messageView.setVisible(false);
        }

        final boolean cutSceneFollows = level.cutSceneNumber() != 0;
        final Animation levelCompletedAnimation = animations.getLevelCompleteAnimation(cutSceneFollows).getOrCreateAnimationFX();

        var animation = new SequentialTransition(
            pauseSecThen(2, () -> {
                perspectiveIDProperty.unbind();
                perspectiveIDProperty.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            levelCompletedAnimation,
            pauseSec(0.25)
        );
        animation.setOnFinished(_ -> {
            maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
            perspectiveIDProperty.bind(PROPERTY_3D_PERSPECTIVE_ID);
            level.game().control().terminateCurrentGameState();
        });
        animation.play();
    }

    public void onGameOver(StateMachine.State<Game> state) {
        state.timer().restartSeconds(3);
        animations.stopGhostLightAnimation();
        maze3D.food().energizers3D().forEach(Energizer3D::hide);
        maze3D.house().arcadeHouse3D().stopSwirlAnimations();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        ui.soundManager().stopAll();
        ui.soundManager().play(SoundID.GAME_OVER);
    }

    public void showAnimatedMessage(String messageText, float displaySeconds, double centerX, double centerY) {
        if (messageView != null) {
            messageView.dispose();
            getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(GameUI_Resources.FONT_ARCADE_6)
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(animationRegistry);
        getChildren().add(messageView);
        messageView.showCenteredAt(centerX, centerY);
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), ui.prefs().getFloat("3d.bonus.symbol.width"),
            uiConfig.bonusValueImage(bonus.symbol()), ui.prefs().getFloat("3d.bonus.points.width"));
        getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    public void updateLevelCounter3D() {
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, level.game());
        }
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> obs, DrawMode oldDrawMode, DrawMode newDrawMode) {
        try {
            Predicate<Node> includeAll = _ -> false;
            setDrawModeUnder(maze3D, node -> node instanceof Shape3D && maze3D.food().pellets3D().contains(node), newDrawMode);
            setDrawModeUnder(pac3D, includeAll, newDrawMode);
            setDrawModeUnder(livesCounter3D, includeAll, newDrawMode);
            ghosts3D.forEach(ghost3D -> setDrawModeUnder(ghost3D, includeAll, newDrawMode));
        }
        catch (Exception x) {
            Logger.error(x, "Could not change 3D draw mode");
        }
    }

    private static void setDrawModeUnder(Node node, Predicate<Node> exclusionFilter, DrawMode drawMode) {
        if (node == null) return; //TODO why does this happen?
        node.lookupAll("*").stream()
            .filter(exclusionFilter.negate())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    private String translatedLevelCompleteMessage(Translator translator, int levelNumber) {
        return pickerLevelCompleteMessages.hasEntries()
            ? pickerLevelCompleteMessages.nextText() + "\n\n" + translator.translate("level_complete", levelNumber)
            : "";
    }

    private boolean disposed = false;

    public void dispose() {
        if (disposed) {
            Logger.warn("Game level 3D already has been disposed!");
            return;
        }
        Logger.info("Disposing game level 3D...");
        disposed = true;

        animationRegistry.stopAllAnimations();
        Logger.info("Stopped all managed animations");

        animations.dispose();

        // Dispose all remaining animations
        animationRegistry.dispose();

        PROPERTY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        getChildren().clear();
        Logger.info("Removed all nodes under game level");

        if (ambientLight != null) {
            ambientLight.colorProperty().unbind();
            ambientLight = null;
            Logger.info("Unbound and cleared ambient light");
        }
        if (maze3D != null) {
            maze3D.dispose();
            maze3D = null;
        }
        if (livesCounterShapes != null) {
            disposeAll(List.of(livesCounterShapes));
            livesCounterShapes = null;
        }
        if (livesCounter3D != null) {
            livesCounter3D.dispose();
            livesCounter3D = null;
            Logger.info("Disposed lives counter 3D");
        }
        if (levelCounter3D != null) {
            levelCounter3D.dispose();
            levelCounter3D = null;
            Logger.info("Disposed level counter 3D");
        }
        if (pac3D != null) {
            pac3D.dispose();
            pac3D = null;
            Logger.info("Disposed Pac 3D");
        }
        if (ghosts3D != null) {
            disposeAll(ghosts3D);
            ghosts3D = null;
            Logger.info("Disposed 3D ghosts");
        }
        if (bonus3D != null) {
            bonus3D.dispose();
            bonus3D = null;
            Logger.info("Disposed 3D bonus");
        }
        if (messageView != null) {
            messageView.dispose();
            messageView = null;
            Logger.info("Disposed message view");
        }
        disposeGhostMeshViews();
    }

    private void disposeGhostMeshViews() {
        if (ghostDressMeshViews != null) {
            for (MeshView meshView : ghostDressMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostDressMeshViews = null;
            Logger.info("Cleared dress mesh views");
        }
        if (ghostPupilsMeshViews != null) {
            for (MeshView meshView : ghostPupilsMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostPupilsMeshViews = null;
            Logger.info("Cleared pupils mesh views");
        }
        if (ghostEyesMeshViews != null) {
            for (MeshView meshView : ghostEyesMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostEyesMeshViews = null;
            Logger.info("Cleared eyes mesh views");
        }
    }
}