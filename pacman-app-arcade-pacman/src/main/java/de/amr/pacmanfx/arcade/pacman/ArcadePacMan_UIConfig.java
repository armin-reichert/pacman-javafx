/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostFactory;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.gamescene.GameSceneConfig;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState.GAME_INTRO;
import static de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState.GAME_PREPARATION;
import static de.amr.pacmanfx.core.Globals.*;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

/**
 * UI configuration for the Arcade Pac‑Man game variant.
 *
 * <p>This class defines the complete visual, audio, and scene‑selection
 * configuration for the authentic arcade‑style Pac‑Man experience. It acts
 * as the central theme provider for this variant, supplying all assets,
 * renderers, animations, color schemes, and game scenes required by the
 * {@link AppContext} framework.</p>
 *
 * <p>The configuration covers several major responsibilities:</p>
 *
 * <ul>
 *   <li><strong>Asset management</strong> – loads and disposes images,
 *       colors, localized texts, and sprite sheets used throughout the
 *       arcade UI. All assets are stored in an {@link AssetMap} for easy
 *       lookup by renderers and scenes.</li>
 *
 *   <li><strong>Sound initialization</strong> – registers all sound effects,
 *       voice clips, and siren loops with the {@link SoundManager}, ensuring
 *       that audio playback matches the original arcade behavior.</li>
 *
 *   <li><strong>Renderer factories</strong> – creates specialized renderers
 *       for 2D scenes, the game level, the HUD, and individual actors. These
 *       renderers use arcade‑accurate palettes, sprite sheets, and smoothing
 *       rules.</li>
 *
 *   <li><strong>3D model factories</strong> – provides 3D shapes and models
 *       for Pac‑Man and the lives counter when the 3D play scene is enabled.
 *       Colors and lighting are derived from the arcade palette.</li>
 *
 *   <li><strong>Animation factories</strong> – constructs animation sets for
 *       Pac‑Man and each ghost personality, ensuring that movement and state
 *       transitions match the arcade rules.</li>
 *
 *   <li><strong>Scene creation and caching</strong> – lazily creates and
 *       caches all game scenes (boot, intro, start, play, cutscenes, 3D
 *       play scene). Scenes are reused across the game lifecycle and
 *       disposed when the configuration is released.</li>
 *
 *   <li><strong>Scene selection logic</strong> – maps the current game state
 *       to the appropriate {@link GameScene}, including support for
 *       intermissions, cutscenes, and developer test states.</li>
 *
 *   <li><strong>Color scheme selection</strong> – provides the arcade
 *       wall/door color scheme for maze rendering.</li>
 * </ul>
 *
 * <p>Although this class is large, it serves as the authoritative definition
 * of the Arcade Pac‑Man presentation layer. All visual and audio behavior
 * specific to this variant is centralized here, keeping the rest of the UI
 * framework clean and variant‑agnostic.</p>
 *
 * <p>Instances of this configuration are created by the {@link AppContext}
 * during initialization and remain active for the lifetime of the UI.</p>
 */
public class ArcadePacMan_UIConfig implements UIConfig, ResourceManager {

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public static final GameAction ACTION_INSERT_COIN = new GameAction("insert_coin") {
        @Override
        public void doAction(AppContext context) {
            final CoinMechanism slot = context.coinMechanism();
            final GameModel game = context.currentGame();
            context.ui().sounds().stopAndDisposeVoice();
            context.ui().sounds().setEnabled(true);
            slot.insertCoin();
            game.flow().enterState(GAME_PREPARATION.state());
            game.flow().publishGameEvent(new CreditAddedEvent(context, 1));
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final CoinMechanism slot = context.coinMechanism();
            if (slot.isFull()) {
                return false;
            }
            final GameModel game = context.currentGame();
            // In demo level, coin can always be inserted
            if (game.isDemoLevelRunning()) {
                return true;
            }
            final State<GameContext> gameState = game.flow().state();
            return gameState == GAME_INTRO.state() || gameState == GAME_PREPARATION.state();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("start_game") {
        @Override
        public void doAction(AppContext context) {
            context.ui().sounds().stopAndDisposeVoice();
            context.gameFlow().enterState(Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final CoinMechanism slot = context.coinMechanism();
            if (slot.isEmpty()) {
                return false;
            }
            final GameModel game = context.currentGame();
            final State<GameContext> gameState = context.currentGameState();
            return (gameState == GAME_INTRO.state() || gameState == GAME_PREPARATION.state()) && game.canStartNewGame();
        }
    };

    public static final Set<ActionBinding> GAME_START_ACTION_BINDINGS = Set.of(
        new ActionBinding(ACTION_INSERT_COIN, bare(KeyCode.DIGIT5), bare(KeyCode.NUMPAD5)),
        new ActionBinding(ACTION_START_GAME,  bare(KeyCode.DIGIT1), bare(KeyCode.NUMPAD1))
    );

    public static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private final AssetMap assets = new AssetMap();
    private final ArcadePacMan_Factory3D factory3D = new ArcadePacMan_Factory3D();
    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects soundEffects;

    public ArcadePacMan_UIConfig() {}

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    @Override
    public void init(AppContext context) {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(context.ui().sounds());
        gameSceneConfig = new ArcadePacMan_GameSceneConfig(context);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        disposeAssets();
        gameSceneConfig.dispose();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(400, 0, 256, 160);
    }

    private void loadAssets() {
        assets.clear();
        assets.set("app_icon", loadImage("graphics/icons/pacman.png"));
        assets.set("color.game_over_message", ARCADE_RED);
        assets.set("maze.bright", UfxImages.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts"));
    }

    private void initSound(SoundManager soundManager) {
        soundManager.setAudioClip(PacManGameSoundID.BONUS_EATEN,      url("sound/eat_fruit.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.setAudioClip(PacManGameSoundID.EXTRA_LIFE,       url("sound/extend.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.GAME_OVER,        url("sound/common/game-over.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.GAME_READY,        url("sound/game_start.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.GHOST_EATEN,      url("sound/eat_ghost.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.GHOST_RETURNS,     url("sound/retreating.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_1,    url("sound/intermission.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_2,    url("sound/intermission.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_3,    url("sound/intermission.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.LEVEL_CHANGED,    url("sound/common/sweep.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.LEVEL_COMPLETE,    url("sound/common/level-complete.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.PAC_MAN_DEATH,     url("sound/pacman_death.wav"));
        soundManager.setAudioClip(PacManGameSoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.setMediaPlayer(PacManGameSoundID.PAC_MAN_POWER,     url("sound/ghost-turn-to-blue.mp3"));

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            url("sound/siren_1.mp3"),
            url("sound/siren_2.mp3"),
            url("sound/siren_3.mp3"),
            url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        return enhanceContrast(WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored      -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene2 ignored  -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene3 ignored  -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public ArcadePacMan_GameLevel_Renderer createGameLevelRenderer(Canvas canvas) {
        requireNonNull(canvas);
        return new ArcadePacMan_GameLevel_Renderer(canvas, assets.image("maze.bright"));
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        requireNonNull(canvas);
        final var actorRenderer = new ArcadePacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createGhostWithAnimations(SpriteAnimationSet animationSet, byte personality) {
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW -> GhostFactory.createRedGhostShadow("Blinky");
            case PINK_GHOST_SPEEDY -> GhostFactory.createPinkGhostAmbusher("Pinky");
            case CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Clyde");
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
        ghost.setAnimations(createGhostAnimations(animationSet, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimations createGhostAnimations(SpriteAnimationSet animationSet, byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return new ArcadePacMan_GhostAnimations(animationSet, personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations(SpriteAnimationSet animationSet) {
        return new ArcadePacMan_PacAnimations(animationSet, spriteSheet());
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbolCode]);
    }
}