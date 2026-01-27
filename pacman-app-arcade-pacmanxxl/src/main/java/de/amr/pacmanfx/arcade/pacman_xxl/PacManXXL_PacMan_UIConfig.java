/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
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
import de.amr.pacmanfx.ui.GameScene_Config;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
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
import static de.amr.pacmanfx.ui.ArcadePalette.*;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements GameUI_Config, GameScene_Config {

    private static final ResourceManager ARCADE_PAC_MAN_RESOURCES = () -> ArcadePacMan_UIConfig.class;

    private final AssetMap assets = new AssetMap();
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();
    private final SoundManager soundManager = new SoundManager();

    @Override
    public void init() {
        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();
    }

    @Override
    public void dispose() {
        GameUI_Config.super.dispose();
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    private void loadAssets() {
        assets.clear();

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

        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_pacman"));

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

        soundManager.registerSirens(
            ARCADE_PAC_MAN_RESOURCES.url("sound/siren_1.mp3"),
            ARCADE_PAC_MAN_RESOURCES.url("sound/siren_2.mp3"),
            ARCADE_PAC_MAN_RESOURCES.url("sound/siren_3.mp3"),
            ARCADE_PAC_MAN_RESOURCES.url("sound/siren_4.mp3")
        );
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public PacManXXL_PacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameLevelRenderer(canvas);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D      ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D      ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1  ignored -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene2  ignored -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene3  ignored -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.adaptRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        final var actorRenderer = new ArcadePacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
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
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return worldMap.getConfigValue(ConfigKey.COLOR_SCHEME);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(400, 0, 256, 160);
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
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
        return new ArcadePacMan_GhostAnimations(personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations() {
        return new ArcadePacMan_PacAnimations(spriteSheet());
    }

    @Override
    public PacBody createLivesCounterShape3D() {
        return PacManModel3DRepository.INSTANCE.createPacBody(
            userPrefs().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
    }

    @Override
    public PacBase3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        var pacMan3D = new PacMan3D(
            PacManModel3DRepository.INSTANCE,
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
        pacMan3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pacMan3D;
    }

    @Override
    public byte munchingSoundDelay() {
        return 9;
    }

    // Game scenes

    private GameScene createGameScene(SceneID sceneID) {
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
    public boolean sceneDecorationRequested(GameScene gameScene) {
        return true;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        final SceneID sceneID = switch (game.control().state()) {
            case GameState.BOOT -> CommonSceneID.BOOT_SCENE;
            case GameState.SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case GameState.INTRO -> CommonSceneID.INTRO_SCENE;
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
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }
}