/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.*;
import static de.amr.pacmanfx.ui.ArcadePalette.*;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static java.util.Objects.requireNonNull;

/**
 * UI configuration for the Arcade Pac‑Man game variant.
 *
 * <p>This class defines the complete visual, audio, and scene‑selection
 * configuration for the authentic arcade‑style Pac‑Man experience. It acts
 * as the central theme provider for this variant, supplying all assets,
 * renderers, animations, color schemes, and game scenes required by the
 * {@link GameUI} framework.</p>
 *
 * <p>The configuration covers several major responsibilities:</p>
 *
 * <ul>
 *   <li><strong>Asset management</strong> – loads and disposes images,
 *       colors, localized texts, and sprite sheets used throughout the
 *       arcade UI. All assets are stored in an {@link AssetMap} for easy
 *       lookup by renderers and scenes.</li>
 *
 *   <li><strong>Sound initialization</strong> – registers all sound effects,
 *       voice clips, and siren loops with the {@link SoundManager}, ensuring
 *       that audio playback matches the original arcade behavior.</li>
 *
 *   <li><strong>Renderer factories</strong> – creates specialized renderers
 *       for 2D scenes, the game level, the HUD, and individual actors. These
 *       renderers use arcade‑accurate palettes, sprite sheets, and smoothing
 *       rules.</li>
 *
 *   <li><strong>3D model factories</strong> – provides 3D shapes and models
 *       for Pac‑Man and the lives counter when the 3D play scene is enabled.
 *       Colors and lighting are derived from the arcade palette.</li>
 *
 *   <li><strong>Animation factories</strong> – constructs animation sets for
 *       Pac‑Man and each ghost personality, ensuring that movement and state
 *       transitions match the arcade rules.</li>
 *
 *   <li><strong>Scene creation and caching</strong> – lazily creates and
 *       caches all game scenes (boot, intro, start, play, cutscenes, 3D
 *       play scene). Scenes are reused across the game lifecycle and
 *       disposed when the configuration is released.</li>
 *
 *   <li><strong>Scene selection logic</strong> – maps the current game state
 *       to the appropriate {@link GameScene}, including support for
 *       intermissions, cutscenes, and developer test states.</li>
 *
 *   <li><strong>Color scheme selection</strong> – provides the arcade
 *       wall/door color scheme for maze rendering.</li>
 * </ul>
 *
 * <p>Although this class is large, it serves as the authoritative definition
 * of the Arcade Pac‑Man presentation layer. All visual and audio behavior
 * specific to this variant is centralized here, keeping the rest of the UI
 * framework clean and variant‑agnostic.</p>
 *
 * <p>Instances of this configuration are created by the {@link GameUI}
 * during initialization and remain active for the lifetime of the UI.</p>
 */
public class ArcadePacMan_UIConfig implements UIConfig, GameSceneConfig, ResourceManager {

    public static final Set<ActionBinding> GAME_START_BINDINGS = Set.of(
        new ActionBinding(ArcadeMachineActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5), bare(KeyCode.NUMPAD5)),
        new ActionBinding(ArcadeMachineActions.ACTION_START_GAME,  bare(KeyCode.DIGIT1), bare(KeyCode.NUMPAD1))
    );

    public static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private final AssetMap assets = new AssetMap();
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    public ArcadePacMan_UIConfig() {}

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    @Override
    public void init(GameUI ui) {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(ui.soundManager());
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        disposeAssets();
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(400, 0, 256, 160);
    }

    private void loadAssets() {
        assets.clear();

        assets.set("app_icon", loadImage("graphics/icons/pacman.png"));

        assets.set("color.game_over_message", ARCADE_RED);

        assets.set("maze.bright", UfxImages.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));

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

    private void initSound(SoundManager soundManager) {
        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       url("sound/extend.mp3"));
        soundManager.registerAudioClipURL(SoundID.GAME_OVER,        url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        url("sound/game_start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    url("sound/intermission.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,    url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,    url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,     url("sound/pacman_death.wav"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,     url("sound/ghost-turn-to-blue.mp3"));

        soundManager.registerSirens(
            url("sound/siren_1.mp3"),
            url("sound/siren_2.mp3"),
            url("sound/siren_3.mp3"),
            url("sound/siren_4.mp3")
        );
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        return WORLD_MAP_COLOR_SCHEME;
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameUI ui, Canvas canvas, GameScene2D gameScene2D) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored      -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene2 ignored  -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene3 ignored  -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas);
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
        final var actorRenderer = new ArcadePacMan_ActorRenderer(canvas);
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
        ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimations createGhostAnimations(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return new ArcadePacMan_GhostAnimations(personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations() {
        return new ArcadePacMan_PacAnimations(spriteSheet());
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public byte munchingSoundDelay() {
        return 9;
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbol]);
    }

    @Override
    public PacBody createLivesCounterShape3D(double size) {
        return Models3D.PAC_MAN_MODEL.createPacBody(
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
    }

    @Override
    public PacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        requireNonNull(animationRegistry);
        requireNonNull(pac);
        final var pacMan3D = new PacMan3D(
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
            case CommonSceneID.BOOT_SCENE    -> new Arcade_BootScene2D();
            case CommonSceneID.INTRO_SCENE   -> new ArcadePacMan_IntroScene();
            case CommonSceneID.START_SCENE   -> new ArcadePacMan_StartScene();
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D();
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D();
            case CommonSceneID.CUTSCENE_1    -> new ArcadePacMan_CutScene1();
            case CommonSceneID.CUTSCENE_2    -> new ArcadePacMan_CutScene2();
            case CommonSceneID.CUTSCENE_3    -> new ArcadePacMan_CutScene3();
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
            case BOOT -> CommonSceneID.BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case INTRO -> CommonSceneID.INTRO_SCENE;
            case INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                final int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield GameSceneConfig.cutSceneID(cutSceneNumber);
            }
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
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