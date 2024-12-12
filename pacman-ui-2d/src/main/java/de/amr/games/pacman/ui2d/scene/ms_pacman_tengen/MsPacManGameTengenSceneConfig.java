/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.PacManGames2dApp;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.scene.common.WorldMapColoring;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class MsPacManGameTengenSceneConfig implements GameSceneConfig {

    public static final Vector2i NES_TILES = new Vector2i(32, 30);
    public static final Vector2i NES_SIZE = NES_TILES.scaled(TS);

    private final AssetStorage assets;
    private final MsPacManGameTengenSpriteSheet spriteSheet;
    private final SpriteSheet_ArcadeMaps arcadeMapSprites;
    private final SpriteSheet_NonArcadeMaps nonArcadeMapSprites;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameTengenSceneConfig(AssetStorage assets) {
        this.assets = checkNotNull(assets);

        spriteSheet = new MsPacManGameTengenSpriteSheet(assets.image("tengen.spritesheet"));
        arcadeMapSprites = new SpriteSheet_ArcadeMaps(assets.image(GameAssets2D.PFX_MS_PACMAN_TENGEN + ".mazes.arcade"));
        nonArcadeMapSprites = new SpriteSheet_NonArcadeMaps(assets.image(GameAssets2D.PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade"));

        set("BootScene",      new BootScene());
        set("IntroScene",     new IntroScene());
        set("StartScene",     new OptionsScene());
        set("ShowingCredits", new CreditsScene());
        set("PlayScene2D",    new PlayScene2D());
        set("CutScene1",      new CutScene1());
        set("CutScene2",      new CutScene2());
        set("CutScene3",      new CutScene3());
        set("CutScene4",      new CutScene4());

        //TODO where is the best place to do that?
        PlayScene2D playScene2D = (PlayScene2D) get("PlayScene2D");
        playScene2D.displayModeProperty().bind(PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
    }

    public static Color nesPaletteColor(int index) {
        return Color.web(NES.Palette.color(index));
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return get(sceneID);
    }

    @Override
    public void set(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene get(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public MsPacManGameTengenRenderer createRenderer(Canvas canvas) {
        return new MsPacManGameTengenRenderer(assets, spriteSheet, arcadeMapSprites, nonArcadeMapSprites, canvas);
    }

    @Override
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return new WorldMapColoring((NES_ColorScheme) worldMap.getConfigValue("nesColorScheme"));
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}