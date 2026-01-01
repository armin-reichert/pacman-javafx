/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
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
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_UIConfig implements GameUI_Config, GameScene_Config {

    private static final ResourceManager ARCADE_MS_PACMAN_RESOURCES = () -> ArcadeMsPacMan_UIConfig.class;
    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_MsPacMan_UIConfig.class;

    private final AssetMap assets = new AssetMap();
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();
    private final SoundManager soundManager = new SoundManager();
    private final GameUI ui;

    public PacManXXL_MsPacMan_UIConfig(GameUI ui) {
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

    private void loadAssets() {
        assets.clear();

        assets.set("app_icon", ARCADE_MS_PACMAN_RESOURCES.loadImage("graphics/icons/mspacman.png"));
        assets.set("logo.midway", ARCADE_MS_PACMAN_RESOURCES.loadImage("graphics/midway_logo.png"));
        assets.set("color.game_over_message", ARCADE_RED);

        assets.set("pac.color.head",           ARCADE_YELLOW);
        assets.set("pac.color.eyes",           Color.grayRgb(33));
        assets.set("pac.color.palate",         ARCADE_BROWN);
        assets.set("pac.color.boobs",          ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.set("pac.color.hairbow",        ARCADE_RED);
        assets.set("pac.color.hairbow.pearls", ARCADE_BLUE);

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

        assets.set("audio.option.selection_changed", LOCAL_RESOURCES.loadAudioClip("sound/ms-select1.wav"));
        assets.set("audio.option.value_changed",     LOCAL_RESOURCES.loadAudioClip("sound/ms-select2.wav"));

        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_ms_pacman"));
    }

    private void registerSounds() {
        soundManager.register(SoundID.VOICE_AUTOPILOT_OFF,          GameUI.VOICE_AUTOPILOT_OFF);
        soundManager.register(SoundID.VOICE_AUTOPILOT_ON,           GameUI.VOICE_AUTOPILOT_ON);
        soundManager.register(SoundID.VOICE_EXPLAIN_GAME_START,     GameUI.VOICE_EXPLAIN_GAME_START);
        soundManager.register(SoundID.VOICE_IMMUNITY_OFF,           GameUI.VOICE_IMMUNITY_OFF);
        soundManager.register(SoundID.VOICE_IMMUNITY_ON,            GameUI.VOICE_IMMUNITY_ON);

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,      ARCADE_MS_PACMAN_RESOURCES.url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      ARCADE_MS_PACMAN_RESOURCES.url("sound/Fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,    ARCADE_MS_PACMAN_RESOURCES.url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       ARCADE_MS_PACMAN_RESOURCES.url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,         ARCADE_MS_PACMAN_RESOURCES.url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        ARCADE_MS_PACMAN_RESOURCES.url("sound/Start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      ARCADE_MS_PACMAN_RESOURCES.url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     ARCADE_MS_PACMAN_RESOURCES.url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    ARCADE_MS_PACMAN_RESOURCES.url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    ARCADE_MS_PACMAN_RESOURCES.url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    ARCADE_MS_PACMAN_RESOURCES.url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,    ARCADE_MS_PACMAN_RESOURCES.url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,    ARCADE_MS_PACMAN_RESOURCES.url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,     ARCADE_MS_PACMAN_RESOURCES.url("sound/Died.mp3"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING, ARCADE_MS_PACMAN_RESOURCES.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,     ARCADE_MS_PACMAN_RESOURCES.url("sound/ScaredGhost.mp3"));

        soundManager.registerSirens(
            ARCADE_MS_PACMAN_RESOURCES.url("sound/GhostNoise1.wav"),
            ARCADE_MS_PACMAN_RESOURCES.url("sound/GhostNoise2.wav"),
            ARCADE_MS_PACMAN_RESOURCES.url("sound/GhostNoise3.wav"),
            ARCADE_MS_PACMAN_RESOURCES.url("sound/GhostNoise4.wav")
        );
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return worldMap.getConfigValue(ConfigKey.COLOR_SCHEME);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D) {
        final UIPreferences prefs = ui.preferences();
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D        ignored -> new Arcade_BootScene2D_Renderer(prefs, gameScene2D, canvas, spriteSheet());
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, prefs, gameScene2D, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(prefs, gameScene2D, canvas);
            case Arcade_PlayScene2D        ignored -> new Arcade_PlayScene2D_Renderer(prefs, gameScene2D, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1  ignored -> new ArcadeMsPacMan_CutScene1_Renderer(this, prefs, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene2  ignored -> new ArcadeMsPacMan_CutScene2_Renderer(this, prefs, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene3  ignored -> new ArcadeMsPacMan_CutScene3_Renderer(this, prefs, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public PacManXXL_MsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameLevelRenderer(canvas);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
        final var hudRenderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.adaptRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        final var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
        requireValidGhostPersonality(personality);
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> ArcadeMsPacMan_GameModel.createBlinky();
            case PINK_GHOST_SPEEDY  -> ArcadeMsPacMan_GameModel.createPinky();
            case CYAN_GHOST_BASHFUL -> ArcadeMsPacMan_GameModel.createInky();
            case ORANGE_GHOST_POKEY -> ArcadeMsPacMan_GameModel.createSue();
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
        ghost.setAnimationManager(createGhostAnimations(personality));
        ghost.selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
        return ghost;
    }

    @Override
    public SpriteAnimationManager<SpriteID> createGhostAnimations(byte personality) {
        return new ArcadeMsPacMan_GhostAnimationManager(personality);
    }

    @Override
    public SpriteAnimationManager<SpriteID> createPacAnimations() {
        return new ArcadeMsPacMan_PacAnimationManager();
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().spriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
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
    public MsPacManBody createLivesCounterShape3D() {
        return PacManModel3DRepository.theRepository().createMsPacManBody(
            ui.preferences().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        var msPacMan3D = new MsPacMan3D(
            PacManModel3DRepository.theRepository(),
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
        msPacMan3D.light().setColor(assets.color("pac.color.head").desaturate());
        return msPacMan3D;
    }

    // Game scenes

    private GameScene createGameScene(SceneID sceneID) {
        final GameScene gameScene = switch (sceneID) {
            case BOOT_SCENE    -> new Arcade_BootScene2D();
            case INTRO_SCENE   -> new ArcadeMsPacMan_IntroScene();
            case START_SCENE   -> new ArcadeMsPacMan_StartScene();
            case PLAY_SCENE_2D -> new Arcade_PlayScene2D();
            case PLAY_SCENE_3D -> new Arcade_PlayScene3D();
            case CUTSCENE_1    -> new ArcadeMsPacMan_CutScene1();
            case CUTSCENE_2    -> new ArcadeMsPacMan_CutScene2();
            case CUTSCENE_3    -> new ArcadeMsPacMan_CutScene3();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
        return gameScene;
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        return true;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        final SceneID sceneID = switch (game.control().state()) {
            case GameState.BOOT -> SceneID.BOOT_SCENE;
            case GameState.SETTING_OPTIONS_FOR_START -> SceneID.START_SCENE;
            case GameState.INTRO -> SceneID.INTRO_SCENE;
            case GameState.INTERMISSION -> {
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
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}