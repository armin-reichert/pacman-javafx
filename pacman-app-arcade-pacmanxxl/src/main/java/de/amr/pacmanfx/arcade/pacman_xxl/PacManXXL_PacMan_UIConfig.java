/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
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
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
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
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements GameUI_Config, GameScene_Config {

    private static final ResourceManager RES_GAME_UI        = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_PAC_MAN = () -> ArcadePacMan_UIConfig.class;
    private static final ResourceManager RES_PAC_MAN_XXL    = () -> PacManXXL_PacMan_UIConfig.class;

    private final GameUI ui;
    private final AssetMap assets = new AssetMap();
    private final SoundManager soundManager = new SoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadePacMan_SpriteSheet spriteSheet;

    public PacManXXL_PacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadePacMan_SpriteSheet(RES_ARCADE_PAC_MAN.loadImage("graphics/pacman_spritesheet.png"));
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_pacman"));
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    public void loadAssets() {
        assets.set("app_icon", RES_ARCADE_PAC_MAN.loadImage("graphics/icons/pacman.png"));
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

        assets.set("audio.option.selection_changed",  RES_PAC_MAN_XXL.loadAudioClip("sound/ms-select1.wav"));
        assets.set("audio.option.value_changed",      RES_PAC_MAN_XXL.loadAudioClip("sound/ms-select2.wav"));

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         RES_ARCADE_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            RES_ARCADE_PAC_MAN.url("sound/extend.mp3"));
        soundManager.registerAudioClip(SoundID.GAME_OVER,             RES_ARCADE_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          RES_ARCADE_PAC_MAN.url("sound/game_start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       RES_ARCADE_PAC_MAN.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,      RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,      RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,      RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         RES_ARCADE_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      RES_ARCADE_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       RES_ARCADE_PAC_MAN.url("sound/pacman_death.wav"));
        soundManager.registerAudioClip(SoundID.PAC_MAN_MUNCHING,      RES_ARCADE_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       RES_ARCADE_PAC_MAN.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             RES_ARCADE_PAC_MAN.url("sound/siren_1.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             RES_ARCADE_PAC_MAN.url("sound/siren_2.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             RES_ARCADE_PAC_MAN.url("sound/siren_3.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             RES_ARCADE_PAC_MAN.url("sound/siren_4.mp3"));
    }

    @Override
    public void dispose() {
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
    public HUD_Renderer createHUDRenderer(Canvas canvas) {
        var hudRenderer = new ArcadePacMan_HUD_Renderer(canvas, spriteSheet);
        hudRenderer.setImageSmoothing(true);
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
    public Ghost createAnimatedGhost(byte personality) {
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

    // Game scenes

    @Override
    public GameScene_Config sceneConfig() {
        return this;
    }

    @Override
    public void createGameScenes(GameUI ui) {
        scenesByID.put(SCENE_ID_BOOT_SCENE,  new Arcade_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE, new ArcadePacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE, new ArcadePacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new Arcade_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadePacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadePacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadePacMan_CutScene3(ui));
    }

    @Override
    public boolean canvasDecorated(GameScene gameScene) {
        return true;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        final Game game = context.currentGame();
        String sceneID = switch (game.state()) {
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
        return scenesByID.get(sceneID);
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