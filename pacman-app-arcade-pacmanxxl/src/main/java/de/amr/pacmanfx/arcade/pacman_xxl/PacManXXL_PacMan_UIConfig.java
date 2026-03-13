/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_Factory3D;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadeGameState;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements UIConfig, GameSceneConfig, ResourceManager {

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new ArcadePacMan_Factory3D();
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    @Override
    public void init(GameUI ui) {
        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
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

    private void loadAssets() {
        assets.clear();
        assets.set("app_icon", loadImage("graphics/icons/pacman.png"));
        assets.set("color.game_over_message", ARCADE_RED);
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_pacman"));
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
    public PacManXXL_PacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameLevelRenderer(canvas);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameUI ui, Canvas canvas, GameScene2D gameScene2D) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D      ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, ui.prefs(), spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas, ui.prefs());
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas, ui.prefs());
            case Arcade_PlayScene2D      ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, ui.prefs(), spriteSheet());
            case ArcadePacMan_CutScene1  ignored -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas, ui.prefs());
            case ArcadePacMan_CutScene2  ignored -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas, ui.prefs());
            case ArcadePacMan_CutScene3  ignored -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas, ui.prefs());
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
        return worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
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
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(factory3D);
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
            case ArcadeGameState.BOOT -> CommonSceneID.BOOT_SCENE;
            case ArcadeGameState.SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case ArcadeGameState.INTRO -> CommonSceneID.INTRO_SCENE;
            case ArcadeGameState.INTERMISSION -> {
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