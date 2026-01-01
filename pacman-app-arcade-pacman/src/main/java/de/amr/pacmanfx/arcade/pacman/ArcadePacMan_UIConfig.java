/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements GameUI_Config, GameScene_Config {

    public enum AnimationID {
        ANIM_BIG_PAC_MAN,
        ANIM_BLINKY_DAMAGED,
        ANIM_BLINKY_PATCHED,
        ANIM_BLINKY_NAIL_DRESS_RAPTURE,
        ANIM_BLINKY_NAKED
    }

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadePacMan_UIConfig.class;

    public static final Set<ActionBinding> DEFAULT_BINDINGS = Set.of(
        new ActionBinding(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5), bare(KeyCode.NUMPAD5)),
        new ActionBinding(ArcadeActions.ACTION_START_GAME,  bare(KeyCode.DIGIT1), bare(KeyCode.NUMPAD1))
    );

    public static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
        "#000000", "#2121ff", "#ffb7ff", "#febdb4"
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private final GameUI ui;
    private final AssetMap assets = new AssetMap();
    private final SoundManager soundManager = new SoundManager();
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    public ArcadePacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    @Override
    public void init() {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        registerSounds();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        assets.dispose();
        soundManager.dispose();
        Logger.info("Dispose game scenes: {} entries", scenesByID.size());
        scenesByID.clear();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
    }

    private void loadAssets() {
        assets.clear();

        assets.set("app_icon", LOCAL_RESOURCES.loadImage("graphics/icons/pacman.png"));

        assets.set("color.game_over_message", ARCADE_RED);

        assets.set("maze.bright", Ufx.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));

        assets.set("pac.color.head",   ARCADE_YELLOW);
        assets.set("pac.color.eyes",   Color.grayRgb(33));
        assets.set("pac.color.palate", ARCADE_BROWN);

        assets.set("ghost.0.color.normal.dress",      ARCADE_RED);
        assets.set("ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.0.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.1.color.normal.dress",      ARCADE_PINK);
        assets.set("ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.1.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.2.color.normal.dress",      ARCADE_CYAN);
        assets.set("ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.2.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.3.color.normal.dress",      ARCADE_ORANGE);
        assets.set("ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.3.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.color.frightened.dress",    ARCADE_BLUE);
        assets.set("ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.set("ghost.color.frightened.pupils",   ARCADE_ROSE);
        assets.set("ghost.color.flashing.dress",      ARCADE_WHITE);
        assets.set("ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        assets.set("ghost.color.flashing.pupils",     ARCADE_RED);

        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts"));
    }

    private void registerSounds() {
        soundManager.register(SoundID.VOICE_AUTOPILOT_OFF,          GameUI.VOICE_AUTOPILOT_OFF);
        soundManager.register(SoundID.VOICE_AUTOPILOT_ON,           GameUI.VOICE_AUTOPILOT_ON);
        soundManager.register(SoundID.VOICE_EXPLAIN_GAME_START,     GameUI.VOICE_EXPLAIN_GAME_START);
        soundManager.register(SoundID.VOICE_IMMUNITY_OFF,           GameUI.VOICE_IMMUNITY_OFF);
        soundManager.register(SoundID.VOICE_IMMUNITY_ON,            GameUI.VOICE_IMMUNITY_ON);

        soundManager.registerMedia(SoundID.VOICE_FLYER_TEXT,        LOCAL_RESOURCES.url("sound/flyer-text.mp3"));

        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      LOCAL_RESOURCES.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,    LOCAL_RESOURCES.url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       LOCAL_RESOURCES.url("sound/extend.mp3"));
        soundManager.registerAudioClipURL(SoundID.GAME_OVER,        LOCAL_RESOURCES.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        LOCAL_RESOURCES.url("sound/game_start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      LOCAL_RESOURCES.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     LOCAL_RESOURCES.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    LOCAL_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    LOCAL_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    LOCAL_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,    LOCAL_RESOURCES.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,    LOCAL_RESOURCES.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,     LOCAL_RESOURCES.url("sound/pacman_death.wav"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING, LOCAL_RESOURCES.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,     LOCAL_RESOURCES.url("sound/ghost-turn-to-blue.mp3"));

        soundManager.registerSirens(
            LOCAL_RESOURCES.url("sound/siren_1.mp3"),
            LOCAL_RESOURCES.url("sound/siren_2.mp3"),
            LOCAL_RESOURCES.url("sound/siren_3.mp3"),
            LOCAL_RESOURCES.url("sound/siren_4.mp3")
        );
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        return WORLD_MAP_COLOR_SCHEME;
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final UIPreferences prefs = ui.preferences();
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored      -> new Arcade_BootScene2D_Renderer(prefs, gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, prefs, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(prefs, gameScene2D, canvas);
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(prefs, gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> new ArcadePacMan_CutScene1_Renderer(prefs, gameScene2D, canvas);
            case ArcadePacMan_CutScene2 ignored  -> new ArcadePacMan_CutScene2_Renderer(prefs, gameScene2D, canvas);
            case ArcadePacMan_CutScene3 ignored  -> new ArcadePacMan_CutScene3_Renderer(prefs, gameScene2D, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public ArcadePacMan_GameLevel_Renderer createGameLevelRenderer(Canvas canvas) {
        requireNonNull(canvas);
        return new ArcadePacMan_GameLevel_Renderer(canvas, assets.image("maze.bright"));
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.adaptRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        requireNonNull(canvas);
        final var actorRenderer = new ArcadePacMan_Actor_Renderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> ArcadePacMan_GameModel.createBlinky();
            case PINK_GHOST_SPEEDY  -> ArcadePacMan_GameModel.createPinky();
            case CYAN_GHOST_BASHFUL -> ArcadePacMan_GameModel.createInky();
            case ORANGE_GHOST_POKEY -> ArcadePacMan_GameModel.createClyde();
            default -> throw new IllegalArgumentException("Illegal ghost personality: " + personality);
        };
        ghost.setAnimationManager(createGhostAnimations(personality));
        ghost.selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimationManager createGhostAnimations(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return new ArcadePacMan_GhostAnimationManager(spriteSheet(), personality);
    }

    @Override
    public ArcadePacMan_PacAnimationManager createPacAnimations() {
        return new ArcadePacMan_PacAnimationManager(spriteSheet());
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().spriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public byte munchingSoundDelay() {
        return 9;
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().spriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbol]);
    }

    @Override
    public PacBody createLivesCounterShape3D() {
        return PacManModel3DRepository.theRepository().createPacBody(
            ui.preferences().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
    }

    @Override
    public PacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        requireNonNull(animationRegistry);
        requireNonNull(pac);
        final var pacMan3D = new PacMan3D(PacManModel3DRepository.theRepository(),
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"));
        pacMan3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pacMan3D;
    }

    // Game scene config

    private static GameScene createGameScene(SceneID sceneID) {
        return switch (sceneID) {
            case BOOT_SCENE    -> new Arcade_BootScene2D();
            case INTRO_SCENE   -> new ArcadePacMan_IntroScene();
            case START_SCENE   -> new ArcadePacMan_StartScene();
            case PLAY_SCENE_2D -> new Arcade_PlayScene2D();
            case PLAY_SCENE_3D -> new Arcade_PlayScene3D();
            case CUTSCENE_1    -> new ArcadePacMan_CutScene1();
            case CUTSCENE_2    -> new ArcadePacMan_CutScene2();
            case CUTSCENE_3    -> new ArcadePacMan_CutScene3();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        return true;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        requireNonNull(game);
        final SceneID sceneID = switch (game.control().state()) {
            case BOOT -> SceneID.BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> SceneID.START_SCENE;
            case INTRO -> SceneID.INTRO_SCENE;
            case INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                final int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield GameScene_Config.cutSceneID(cutSceneNumber);
            }
            case CutScenesTestState testState -> GameScene_Config.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? SceneID.PLAY_SCENE_3D : SceneID.PLAY_SCENE_2D;
        };
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, ArcadePacMan_UIConfig::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}