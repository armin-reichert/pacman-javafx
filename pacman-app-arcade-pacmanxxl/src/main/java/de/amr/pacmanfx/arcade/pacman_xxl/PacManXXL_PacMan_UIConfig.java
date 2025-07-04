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
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PacMan3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui._3d.Settings3D;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_PacMan_UIConfig implements PacManGames_UIConfig {

    private static final String ANS = "pacman_xxl";

    private boolean assetsLoaded;
    private ArcadePacMan_SpriteSheet spriteSheet;

    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public void loadAssets(AssetStorage assets) {
        if (assetsLoaded) {
            Logger.warn("Assets are already loaded");
            return;
        }
        assetsLoaded = true;

        ResourceManager rm = () -> ArcadePacMan_UIConfig.class;

        spriteSheet = new ArcadePacMan_SpriteSheet(rm.loadImage("graphics/pacman_spritesheet.png"));

        storeLocalAsset(assets, "app_icon",                        rm.loadImage("graphics/icons/pacman.png"));
        storeLocalAsset(assets, "color.game_over_message",         ARCADE_RED);

        Sprite[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        Sprite[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 7; ++symbol) {
            storeLocalAsset(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeLocalAsset(assets, "bonus_value_"  + symbol,  spriteSheet.image(valueSprites[symbol]));
        }

        storeLocalAsset(assets, "pac.color.head",                  ARCADE_YELLOW);
        storeLocalAsset(assets, "pac.color.eyes",                  Color.grayRgb(33));
        storeLocalAsset(assets, "pac.color.palate",                Color.rgb(240, 180, 160));

        Sprite[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeLocalAsset(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeLocalAsset(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeLocalAsset(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeLocalAsset(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeLocalAsset(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeLocalAsset(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAsset(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeLocalAsset(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAsset(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeLocalAsset(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAsset(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeLocalAsset(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAsset(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);

        storeLocalAsset(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        storeLocalAsset(assets, "audio.bonus_eaten",    rm.loadAudioClip("sound/eat_fruit.mp3"));
        storeLocalAsset(assets, "audio.credit",         rm.loadAudioClip("sound/credit.wav"));
        storeLocalAsset(assets, "audio.extra_life",     rm.loadAudioClip("sound/extend.mp3"));
        storeLocalAsset(assets, "audio.ghost_eaten",    rm.loadAudioClip("sound/eat_ghost.mp3"));
        storeLocalAsset(assets, "audio.sweep",          rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        storeLocalAsset(assets, "audio.game_ready",     rm.url("sound/game_start.mp3"));
        storeLocalAsset(assets, "audio.game_over",      rm.url("sound/common/game-over.mp3"));
        storeLocalAsset(assets, "audio.intermission",   rm.url("sound/intermission.mp3"));
        storeLocalAsset(assets, "audio.pacman_death",   rm.url("sound/pacman_death.wav"));
        storeLocalAsset(assets, "audio.pacman_munch",   rm.url("sound/munch.wav"));
        storeLocalAsset(assets, "audio.pacman_power",   rm.url("sound/ghost-turn-to-blue.mp3"));
        storeLocalAsset(assets, "audio.level_complete", rm.url("sound/common/level-complete.mp3"));
        storeLocalAsset(assets, "audio.siren.1",        rm.url("sound/siren_1.mp3"));
        storeLocalAsset(assets, "audio.siren.2",        rm.url("sound/siren_2.mp3"));
        storeLocalAsset(assets, "audio.siren.3",        rm.url("sound/siren_3.mp3"));
        storeLocalAsset(assets, "audio.siren.4",        rm.url("sound/siren_4.mp3"));
        storeLocalAsset(assets, "audio.ghost_returns",  rm.url("sound/retreating.mp3"));

        rm = this::getClass;
        storeLocalAsset(assets, "audio.option.selection_changed",  rm.loadAudioClip("sound/ms-select1.wav"));
        storeLocalAsset(assets, "audio.option.value_changed",      rm.loadAudioClip("sound/ms-select2.wav"));
    }

    @Override
    public void unloadAssets(AssetStorage assetStorage) {
        assetStorage.removeAll(ANS + ".");
    }

    @Override
    public String assetNamespace() {
        return ANS;
    }

    @Override
    public PacManXXL_PacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return theAssets().image(ANS + ".bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return theAssets().image(ANS + ".bonus_value_" + symbol);
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

    @SuppressWarnings("unchecked")
    @Override
    public PacBody createLivesCounterShape3D(Model3DRepository model3DRepository) {
        String namespace = assetNamespace();
        return model3DRepository.createPacBody(
                Settings3D.LIVES_COUNTER_3D_SHAPE_SIZE,
                theAssets().color(namespace + ".pac.color.head"),
                theAssets().color(namespace + ".pac.color.eyes"),
                theAssets().color(namespace + ".pac.color.palate")
        );
    }

    @Override
    public PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new PacMan3D(model3DRepository, animationManager, pac, Settings3D.PAC_3D_SIZE, theAssets(), assetNamespace());
        pac3D.light().setColor(theAssets().color(assetNamespace() + ".pac.color.head").desaturate());
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