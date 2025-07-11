/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GhostAnimationMap;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimationMap;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements PacManGames_UIConfig {

    private static final String NAMESPACE = "pacman_xxl";

    private static final ResourceManager RES_PACMAN_UI = () -> PacManGames_UI.class;
    private static final ResourceManager RES_ARCADE_PAC_MAN = () -> ArcadePacMan_UIConfig.class;
    private static final ResourceManager RES_PAC_MAN_XXL = () -> PacManXXL_PacMan_UIConfig.class;

    private boolean assetsLoaded;
    private ArcadePacMan_SpriteSheet spriteSheet;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();

    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public void loadAssets(AssetStorage assets) {
        if (assetsLoaded) {
            Logger.warn("Assets are already loaded");
            return;
        }
        assetsLoaded = true;

        spriteSheet = new ArcadePacMan_SpriteSheet(RES_ARCADE_PAC_MAN.loadImage("graphics/pacman_spritesheet.png"));

        storeInMyNamespace(assets, "app_icon",                RES_ARCADE_PAC_MAN.loadImage("graphics/icons/pacman.png"));
        storeInMyNamespace(assets, "color.game_over_message", ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 7; ++symbol) {
            storeInMyNamespace(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeInMyNamespace(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeInMyNamespace(assets, "pac.color.head",                  ARCADE_YELLOW);
        storeInMyNamespace(assets, "pac.color.eyes",                  Color.grayRgb(33));
        storeInMyNamespace(assets, "pac.color.palate",                ARCADE_BROWN);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeInMyNamespace(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeInMyNamespace(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeInMyNamespace(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeInMyNamespace(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeInMyNamespace(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeInMyNamespace(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);

        storeInMyNamespace(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeInMyNamespace(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);

        storeInMyNamespace(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeInMyNamespace(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);

        storeInMyNamespace(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeInMyNamespace(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);

        storeInMyNamespace(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeInMyNamespace(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);

        storeInMyNamespace(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeInMyNamespace(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       RES_PACMAN_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        RES_PACMAN_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        RES_PACMAN_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         RES_PACMAN_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             RES_PACMAN_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         RES_ARCADE_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            RES_ARCADE_PAC_MAN.url("sound/extend.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           RES_ARCADE_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          RES_ARCADE_PAC_MAN.url("sound/game_start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       RES_ARCADE_PAC_MAN.url("sound/retreating.mp3"));
        soundManager.registerAudioClip("audio.intermission",          RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         RES_ARCADE_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      RES_ARCADE_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       RES_ARCADE_PAC_MAN.url("sound/pacman_death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,    RES_ARCADE_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       RES_ARCADE_PAC_MAN.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             RES_ARCADE_PAC_MAN.url("sound/siren_1.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             RES_ARCADE_PAC_MAN.url("sound/siren_2.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             RES_ARCADE_PAC_MAN.url("sound/siren_3.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             RES_ARCADE_PAC_MAN.url("sound/siren_4.mp3"));

        storeInMyNamespace(assets, "audio.option.selection_changed",  RES_PAC_MAN_XXL.loadAudioClip("sound/ms-select1.wav"));
        storeInMyNamespace(assets, "audio.option.value_changed",      RES_PAC_MAN_XXL.loadAudioClip("sound/ms-select2.wav"));
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public void destroy() {
        theAssets().removeAll(NAMESPACE + ".");
        soundManager.destroy();
    }

    @Override
    public String assetNamespace() {
        return NAMESPACE;
    }

    @Override
    public PacManXXL_PacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public Image killedGhostPointsImage(Ghost ghost, int killedIndex) {
        return theAssets().image(NAMESPACE + ".ghost_points_" + killedIndex);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return theAssets().image(NAMESPACE + ".bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return theAssets().image(NAMESPACE + ".bonus_value_" + symbol);
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {return spriteSheet;}

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadePacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadePacMan_PacAnimationMap(spriteSheet);
    }

    @Override
    public PacBody createLivesCounterShape3D(Model3DRepository model3DRepository) {
        String namespace = assetNamespace();
        return model3DRepository.createPacBody(
                PacManGames_UI.LIVES_COUNTER_3D_SHAPE_SIZE,
                theAssets().color(namespace + ".pac.color.head"),
                theAssets().color(namespace + ".pac.color.eyes"),
                theAssets().color(namespace + ".pac.color.palate")
        );
    }

    @Override
    public PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new PacMan3D(model3DRepository, animationManager, pac, PacManGames_UI.PAC_3D_SIZE,
            theAssets().color(NAMESPACE + ".pac.color.head"),
            theAssets().color(NAMESPACE + ".pac.color.eyes"),
            theAssets().color(NAMESPACE + ".pac.color.palate"));
        pac3D.light().setColor(theAssets().color(NAMESPACE + ".pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadePacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadePacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadePacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadePacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadePacMan_CutScene3());
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

    @Override
    public GameScene selectGameScene(GameModel game, GameState gameState) {
        String sceneID = switch (gameState) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> {
                if (optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = theGameLevel().number();
                if (game.cutSceneNumber(levelNumber).isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + game.cutSceneNumber(levelNumber).getAsInt();
            }
            case GameState.TESTING_CUT_SCENES -> "CutScene" + game.<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }
}