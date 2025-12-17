/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
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
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements GameUI_Config, GameScene_Config {

    private static final ResourceManager ARCADE_PAC_MAN_RESOURCES = () -> ArcadePacMan_UIConfig.class;
    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_PacMan_UIConfig.class;

    private final AssetMap assets = new AssetMap();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final SoundManager soundManager = new SoundManager();
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final GameUI ui;

    public PacManXXL_PacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadePacMan_SpriteSheet(ARCADE_PAC_MAN_RESOURCES.loadImage("graphics/pacman_spritesheet.png"));
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_pacman"));
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    public void loadAssets() {
        assets.set("app_icon", ARCADE_PAC_MAN_RESOURCES.loadImage("graphics/icons/pacman.png"));
        assets.set("color.game_over_message", ARCADE_RED);

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

        assets.set("audio.option.selection_changed",  LOCAL_RESOURCES.loadAudioClip("sound/ms-select1.wav"));
        assets.set("audio.option.value_changed",      LOCAL_RESOURCES.loadAudioClip("sound/ms-select2.wav"));

        soundManager.register(SoundID.VOICE_AUTOPILOT_OFF,          GameUI.VOICE_AUTOPILOT_OFF);
        soundManager.register(SoundID.VOICE_AUTOPILOT_ON,           GameUI.VOICE_AUTOPILOT_ON);
        soundManager.register(SoundID.VOICE_EXPLAIN_GAME_START,     GameUI.VOICE_EXPLAIN);
        soundManager.register(SoundID.VOICE_IMMUNITY_OFF,           GameUI.VOICE_IMMUNITY_OFF);
        soundManager.register(SoundID.VOICE_IMMUNITY_ON,            GameUI.VOICE_IMMUNITY_ON);

        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      ARCADE_PAC_MAN_RESOURCES.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,    ARCADE_PAC_MAN_RESOURCES.url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       ARCADE_PAC_MAN_RESOURCES.url("sound/extend.mp3"));
        soundManager.registerAudioClipURL(SoundID.GAME_OVER,        ARCADE_PAC_MAN_RESOURCES.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        ARCADE_PAC_MAN_RESOURCES.url("sound/game_start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      ARCADE_PAC_MAN_RESOURCES.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     ARCADE_PAC_MAN_RESOURCES.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    ARCADE_PAC_MAN_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    ARCADE_PAC_MAN_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    ARCADE_PAC_MAN_RESOURCES.url("sound/intermission.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,    ARCADE_PAC_MAN_RESOURCES.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,    ARCADE_PAC_MAN_RESOURCES.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,     ARCADE_PAC_MAN_RESOURCES.url("sound/pacman_death.wav"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING, ARCADE_PAC_MAN_RESOURCES.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,     ARCADE_PAC_MAN_RESOURCES.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMedia(SoundID.SIREN_1,                 ARCADE_PAC_MAN_RESOURCES.url("sound/siren_1.mp3"));
        soundManager.registerMedia(SoundID.SIREN_2,                 ARCADE_PAC_MAN_RESOURCES.url("sound/siren_2.mp3"));
        soundManager.registerMedia(SoundID.SIREN_3,                 ARCADE_PAC_MAN_RESOURCES.url("sound/siren_3.mp3"));
        soundManager.registerMedia(SoundID.SIREN_4,                 ARCADE_PAC_MAN_RESOURCES.url("sound/siren_4.mp3"));
    }

    @Override
    public void dispose() {
        Logger.info("Disposing {}", getClass().getSimpleName());
        assets.removeAll();
        soundManager.dispose();
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public PacManXXL_PacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameLevelRenderer(canvas, spriteSheet);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D) {
        GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet);
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(gameScene2D, canvas, spriteSheet);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet);
            case ArcadePacMan_CutScene1 ignored -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas, spriteSheet);
            case ArcadePacMan_CutScene2 ignored -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas, spriteSheet);
            case ArcadePacMan_CutScene3 ignored -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas, spriteSheet);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public HUD_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
        var hudRenderer = new ArcadePacMan_HUD_Renderer(canvas, spriteSheet);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.adaptRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        var actorRenderer = new ArcadePacMan_Actor_Renderer(canvas, spriteSheet);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        RectShort[] numberSprites = spriteSheet.spriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet.image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        RectShort[] sprites = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet.image(sprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        RectShort[] sprites = spriteSheet.spriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet.image(sprites[symbol]);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue(CONFIG_KEY_COLOR_MAP);
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
        Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> ArcadePacMan_ActorFactory.createBlinky();
            case PINK_GHOST_SPEEDY  -> ArcadePacMan_ActorFactory.createPinky();
            case CYAN_GHOST_BASHFUL -> ArcadePacMan_ActorFactory.createInky();
            case ORANGE_GHOST_POKEY -> ArcadePacMan_ActorFactory.createClyde();
            default -> throw new IllegalArgumentException("Illegal ghost personality: " + personality);
        };
        ghost.setAnimationManager(createGhostAnimations(personality));
        ghost.selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimationManager createGhostAnimations(byte personality) {
        return new ArcadePacMan_GhostAnimationManager(spriteSheet, personality);
    }

    @Override
    public ArcadePacMan_PacAnimationManager createPacAnimations() {
        return new ArcadePacMan_PacAnimationManager(spriteSheet);
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
    public PacBase3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        var pac3D = new PacMan3D(
            PacManModel3DRepository.theRepository(),
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
        pac3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pac3D;
    }

    @Override
    public byte munchingSoundDelay() {
        return 9;
    }

    // Game scenes

    @Override
    public GameScene_Config sceneConfig() {
        return this;
    }

    @Override
    public void createGameScenes(GameUI ui) {
        scenesByID.put(SCENE_ID_BOOT_SCENE,     new Arcade_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE,    new ArcadePacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE,    new ArcadePacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new Arcade_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new Arcade_PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadePacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadePacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadePacMan_CutScene3(ui));
    }

    @Override
    public boolean canvasDecorated(GameScene gameScene) {
        return true;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        String sceneID = switch (game.control().state()) {
            case GameState.BOOT -> SCENE_ID_BOOT_SCENE;
            case GameState.SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE;
            case GameState.INTRO -> SCENE_ID_INTRO_SCENE;
            case GameState.INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield sceneID_CutScene(cutSceneNumber);
            }
            case CutScenesTestState testState -> sceneID_CutScene(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? SCENE_ID_PLAY_SCENE_3D : SCENE_ID_PLAY_SCENE_2D;
        };
        return Optional.ofNullable(scenesByID.get(sceneID));
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }
}