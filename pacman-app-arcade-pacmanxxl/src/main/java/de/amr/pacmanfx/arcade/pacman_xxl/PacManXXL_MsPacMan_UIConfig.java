/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GhostAnimationMap;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationMap;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_PlayScene2D;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_UIConfig implements PacManGames_UIConfig {

    private final Image appIcon;
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManXXL_MsPacMan_UIConfig(PacManGames_Assets assets) {
        ResourceManager rm = () -> ArcadeMsPacMan_UIConfig.class;

        appIcon = rm.loadImage("graphics/icons/mspacman.png");
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(rm.loadImage("graphics/mspacman_spritesheet.png"));

        assets.store("ms_pacman_xxl.flashing_mazes",                rm.loadImage("graphics/mazes_flashing.png"));

        assets.store("ms_pacman_xxl.startpage.image1",              rm.loadImage("graphics/f1.jpg"));
        assets.store("ms_pacman_xxl.startpage.image2",              rm.loadImage("graphics/f2.jpg"));

        assets.store("ms_pacman_xxl.logo.midway",                   rm.loadImage("graphics/midway_logo.png"));

        assets.store("ms_pacman_xxl.color.game_over_message",         ARCADE_RED);

        assets.store("ms_pacman_xxl.pac.color.head",                  ARCADE_YELLOW);
        assets.store("ms_pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman_xxl.pac.color.boobs",                 ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman_xxl.pac.color.hairbow",               ARCADE_RED);
        assets.store("ms_pacman_xxl.pac.color.hairbow.pearls",        ARCADE_BLUE);

        assets.store("ms_pacman_xxl.ghost.0.color.normal.dress",      ARCADE_RED);
        assets.store("ms_pacman_xxl.ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman_xxl.ghost.0.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman_xxl.ghost.1.color.normal.dress",      ARCADE_PINK);
        assets.store("ms_pacman_xxl.ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman_xxl.ghost.1.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman_xxl.ghost.2.color.normal.dress",      ARCADE_CYAN);
        assets.store("ms_pacman_xxl.ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman_xxl.ghost.2.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman_xxl.ghost.3.color.normal.dress",      ARCADE_ORANGE);
        assets.store("ms_pacman_xxl.ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman_xxl.ghost.3.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman_xxl.ghost.color.frightened.dress",    ARCADE_BLUE);
        assets.store("ms_pacman_xxl.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("ms_pacman_xxl.ghost.color.frightened.pupils",   ARCADE_ROSE);
        assets.store("ms_pacman_xxl.ghost.color.flashing.dress",      ARCADE_WHITE);
        assets.store("ms_pacman_xxl.ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        assets.store("ms_pacman_xxl.ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        assets.store("ms_pacman_xxl.audio.bonus_eaten",               rm.loadAudioClip("sound/Fruit.mp3"));
        assets.store("ms_pacman_xxl.audio.credit",                    rm.loadAudioClip("sound/credit.wav"));
        assets.store("ms_pacman_xxl.audio.extra_life",                rm.loadAudioClip("sound/ExtraLife.mp3"));
        assets.store("ms_pacman_xxl.audio.ghost_eaten",               rm.loadAudioClip("sound/Ghost.mp3"));
        assets.store("ms_pacman_xxl.audio.sweep",                     rm.loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman_xxl.audio.bonus_bouncing",          rm.url("sound/Fruit_Bounce.mp3"));
        assets.store("ms_pacman_xxl.audio.game_ready",              rm.url("sound/Start.mp3"));
        assets.store("ms_pacman_xxl.audio.game_over",               rm.url("sound/game-over.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.1",          rm.url("sound/Act_1_They_Meet.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.2",          rm.url("sound/Act_2_The_Chase.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.3",          rm.url("sound/Act_3_Junior.mp3"));
        assets.store("ms_pacman_xxl.audio.level_complete",          rm.url("sound/level-complete.mp3"));
        assets.store("ms_pacman_xxl.audio.pacman_death",            rm.url("sound/Died.mp3"));
        assets.store("ms_pacman_xxl.audio.pacman_munch",            rm.url("sound/munch.wav"));
        assets.store("ms_pacman_xxl.audio.pacman_power",            rm.url("sound/ScaredGhost.mp3"));
        assets.store("ms_pacman_xxl.audio.siren.1",                 rm.url("sound/GhostNoise1.wav"));
        assets.store("ms_pacman_xxl.audio.siren.2",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.siren.3",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.siren.4",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.ghost_returns",           rm.url("sound/GhostEyes.mp3"));

        rm = this::getClass;
        assets.store("ms_pacman_xxl.audio.option.selection_changed",  rm.loadAudioClip("sound/ms-select1.wav"));
        assets.store("ms_pacman_xxl.audio.option.value_changed",      rm.loadAudioClip("sound/ms-select2.wav"));

        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3());
    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public String assetNamespace() {
        return "ms_pacman_xxl";
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
    public GameScene2D createPiPScene(Canvas canvas) {
        var gameScene = new ArcadeCommon_PlayScene2D();
        gameScene.setGameRenderer(createRenderer(canvas));
        return gameScene;
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public PacManXXL_MsPacMan_GameRenderer createRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public Image createGhostNumberImage(int ghostIndex) {
        Sprite[] sprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        return spriteSheet.image(sprites[ghostIndex]);
    }

    @Override
    public Image createBonusSymbolImage(byte symbol) {
        return spriteSheet.image(spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[symbol]);
    }

    @Override
    public Image createBonusValueImage(byte symbol) {
        return spriteSheet.image(spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[symbol]);
    }

    @Override
    public Node createLivesCounter3D() {
        String namespace = assetNamespace();
        return new Group(
            Model3DRepository.get().createPacShape(
                    LIVES_COUNTER_3D_SIZE,
                    theAssets().color(namespace + ".pac.color.head"),
                    theAssets().color(namespace + ".pac.color.eyes"),
                    theAssets().color(namespace + ".pac.color.palate")
            ),
            Model3DRepository.get().createFemaleBodyParts(LIVES_COUNTER_3D_SIZE,
                    theAssets().color(namespace + ".pac.color.hairbow"),
                    theAssets().color(namespace + ".pac.color.hairbow.pearls"),
                    theAssets().color(namespace + ".pac.color.boobs")
            )
        );
    }

    @Override
    public PacBase3D createPac3D(Pac pac) {
        var pac3D = new MsPacMan3D(pac, PAC_3D_SIZE, theAssets(), assetNamespace());
        pac3D.light().setColor(theAssets().color(assetNamespace() + ".pac.color.head").desaturate());
        return pac3D;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {return spriteSheet;}

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

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationMap(spriteSheet);
    }
}
