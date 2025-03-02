/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.ui2d.rendering.WorldMapColoring;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ArcadeMsPacMan_GameConfig implements GameConfiguration {

    private static final List<WorldMapColoring> WORLD_MAP_COLORINGS = List.of(
        new WorldMapColoring("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColoring("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColoring("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColoring("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColoring("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColoring("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public ArcadeMsPacMan_GameConfig(AssetStorage assets) {
        loadAssets(() -> ArcadeMsPacMan_GameConfig.class, assets);
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(assets.get("ms_pacman.spritesheet"));

        setGameScene("BootScene",   new ArcadeBootScene());
        setGameScene("IntroScene",  new IntroScene());
        setGameScene("StartScene",  new StartScene());
        setGameScene("PlayScene2D", new ArcadePlayScene2D());
        setGameScene("CutScene1",   new CutScene1());
        setGameScene("CutScene2",   new CutScene2());
        setGameScene("CutScene3",   new CutScene3());
    }

    @Override
    public GameVariant gameVariant() {
        return GameVariant.MS_PACMAN;
    }

    @Override
    public String assetKeyPrefix() {
        return "ms_pacman";
    }

    @Override
    public void setGameScene(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene getGameScene(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameScene2D createPiPScene(GameContext context, Canvas canvas) {
        var gameScene = new ArcadePlayScene2D();
        gameScene.setGameContext(context);
        gameScene.setGameRenderer(createRenderer(context.assets(), canvas));
        return gameScene;
    }

    @Override
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return WORLD_MAP_COLORINGS.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public ArcadeMsPacMan_GameRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new ArcadeMsPacMan_GameRenderer(assets, spriteSheet, canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return getGameScene(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    private void loadAssets(ResourceManager rm, AssetStorage assets) {
        assets.store("ms_pacman.spritesheet",                   rm.loadImage("graphics/mspacman_spritesheet.png"));
        assets.store("ms_pacman.flashing_mazes",                rm.loadImage("graphics/mazes_flashing.png"));

        assets.store("ms_pacman.startpage.image1",              rm.loadImage("graphics/f1.jpg"));
        assets.store("ms_pacman.startpage.image2",              rm.loadImage("graphics/f2.jpg"));

        assets.store("ms_pacman.icon",                          rm.loadImage("graphics/icons/mspacman.png"));
        assets.store("ms_pacman.logo.midway",                   rm.loadImage("graphics/midway_logo.png"));

        assets.store("ms_pacman.color.game_over_message",         Color.valueOf(Arcade.Palette.RED));

        assets.store("ms_pacman.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 Color.valueOf(Arcade.Palette.YELLOW).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow",               Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.pac.color.hairbow.pearls",        Color.valueOf(Arcade.Palette.BLUE));

        assets.store("ms_pacman.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

        // Clips
        assets.store("ms_pacman.audio.bonus_eaten",             rm.loadAudioClip("sound/Fruit.mp3"));
        assets.store("ms_pacman.audio.credit",                  rm.loadAudioClip("sound/credit.wav"));
        assets.store("ms_pacman.audio.extra_life",              rm.loadAudioClip("sound/ExtraLife.mp3"));
        assets.store("ms_pacman.audio.ghost_eaten",             rm.loadAudioClip("sound/Ghost.mp3"));
        assets.store("ms_pacman.audio.sweep",                   rm.loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman.audio.bonus_bouncing",          rm.url("sound/Fruit Bounce.mp3"));
        assets.store("ms_pacman.audio.game_ready",              rm.url("sound/Start.mp3"));
        assets.store("ms_pacman.audio.game_over",               rm.url("sound/game-over.mp3"));
        assets.store("ms_pacman.audio.intermission.1",          rm.url("sound/Act_1_They_Meet.mp3"));
        assets.store("ms_pacman.audio.intermission.2",          rm.url("sound/Act_2_The_Chase.mp3"));
        assets.store("ms_pacman.audio.intermission.3",          rm.url("sound/Act_3_Junior.mp3"));
        assets.store("ms_pacman.audio.level_complete",          rm.url("sound/level-complete.mp3"));
        assets.store("ms_pacman.audio.pacman_death",            rm.url("sound/Died.mp3"));
        assets.store("ms_pacman.audio.pacman_munch",            rm.url("sound/munch.wav"));
        assets.store("ms_pacman.audio.pacman_power",            rm.url("sound/ScaredGhost.mp3"));
        assets.store("ms_pacman.audio.siren.1",                 rm.url("sound/GhostNoise1.wav"));
        assets.store("ms_pacman.audio.siren.2",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.3",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.4",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.ghost_returns",           rm.url("sound/GhostEyes.mp3"));
    }
}