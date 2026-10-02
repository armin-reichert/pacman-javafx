/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.Door;
import de.amr.pacmanfx.core.entities.world.DoorDataComp;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_LevelRenderInfoKey;
import de.amr.pacmanfx.tengenmspacman.sprites.ColorSchemedMapSprite;
import de.amr.pacmanfx.tengenmspacman.sprites.MapImageSet;
import de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.ActionBindingsComp;
import de.amr.pacmanfx.ui.gamescene.d2.FlashingState;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.uilib.view2d.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay.SCROLLING;
import static de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends AbstractGameScene {

    static final Vector2f RENDER_OFFSET = new Vector2f(2 * TS, 0);

    private final StackPane rootPane = new StackPane();

    private LevelCompletedAnimation levelCompletedAnimation;

    private final GameEventHandler eventHandler = new GameEventHandler(this);

    public TengenMsPacMan_PlayScene2D() {
        setComp(GameSceneRendering2DComp.class, createCanvasRendering());
    }

    @Override
    protected void onAppConnected() {
        final Game2DSettingsVM viewModel = app().ui().viewModel().common2DSettings();
        rootPane.backgroundProperty().bind(viewModel.canvasBackgroundColorProperty().map(Background::fill));
    }

    @Override
    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(eventHandler);
    }

    @Override
    public Stream<Renderable> renderables() {
        final GameLevel level = game().session().optLevel().orElse(null);
        if (level == null) return Stream.empty();

        final long tick = game().session().thisFrame().tick();
        final GameVariantRenderConfig renderConfig = app().variantManager().currentRuntime().uiConfig().renderConfig();

        final Door door = level.entitySet().entities().theOne(House.class).door();
        return Ufx.streamOf(
            createRenderableLevel(level, tick),

            //TODO simplify!
            level.entitySet().all()
                .filter(gameEntity -> gameEntity != door)
                .map(renderConfig::createEntityView)
                .map(entityView -> entityView.newOffset(RENDER_OFFSET)),

            // Ghosts entering/leaving the house are drawn under the house door!
            new GameEntityView(door, RenderingLayer.ACTORS, 100, RENDER_OFFSET)
        );
    }

    public FlashingState flashingState() {
        if (levelCompletedAnimation == null || levelCompletedAnimation.optFlashingState().isEmpty()) {
            return null;
        }
        return levelCompletedAnimation.optFlashingState().get();
    }

    @Override
    public void onEnteredFrom3DScene() {
        final GameSession session = game().session();

        final HUD hud = session.hud();
        hud.levelCounter().show();
        hud.livesCounter().show();
        session.setHudVisible(true);

        session.optLevel().ifPresent(level -> onAcceptGameLevel(session, level));
    }

    @Override
    public void onActivate() {
        final GameSession session = game().session();
        final HUD hud = session.hud();

        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().show();
        session.setHudVisible(true);

        resetRendering2D();
        updateScaling();
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            ensureActorAnimationsCreated(level, gameOptions(session).boosterEnabled());
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!session.isAttractMode());
                soundEffects.playAmbientGameLevelSound(game(), level);
            });
        });
    }

    @Override
    public void onQuit() {
        onDeactivate();
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final var uiSettings = uiSettings();

        final TranslationManager translations = app().ui().translationManager();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();
        final var contextMenu = new ContextMenu();

        final RadioMenuItem miScaledToFit = addLocalizedRadioButton(contextMenu, translations, "context_menu.scaled_to_fit");
        miScaledToFit.setSelected(displayMode == SceneDisplay.SCALED_TO_FIT);
        miScaledToFit.setOnAction(_ -> uiSettings.playSceneDisplay.set(SceneDisplay.SCALED_TO_FIT));

        final RadioMenuItem miScrolling = addLocalizedRadioButton(contextMenu, translations, "context_menu.scrolling");
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(_ -> uiSettings.playSceneDisplay.set(SCROLLING));

        final ToggleGroup toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacImmuneProperty(), "context_menu.immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, app().ui().viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(app(), contextMenu, translations, app().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onAcceptGameLevel(GameSession session, GameLevel level) {
        final WorldMap worldMap = level.worldMap();
        final TerrainLayer terrain = worldMap.terrainLayer();
        final Vector2i size = terrain.sizeInPixel();

        reqRendering2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqRendering2D().unscaledHeightProperty().set(size.y() + 2*TS);

        // Store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET)) {
            final int numFlashes = 3;
            final MapImageSet mapImageSet = TengenMsPacMan_MapRepository.instance().createMapImageSet(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET, mapImageSet);
            Logger.info("Maze sprite set created: {}", mapImageSet);

            final House house = level.entitySet().entities().theOne(House.class);
            final var doorData = house.door().reqComp(DoorDataComp.class);
            doorData.setColor(mapImageSet.mapImage().colorScheme().door());
            Logger.info("Door color set to {}", doorData.color());
        }

        if (session.isAttractMode()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel();
        }

        Logger.info(actionBindings().registry());
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());

        game().eventManager().publishEvent(new GenericChangeEvent("Re-embed"));
    }

    // private area, do NOT enter!

    private void resetRendering2D() {
        final GameSceneRendering2DComp oldComp = reqRendering2D();
        final GameSceneRendering2DComp newComp = createCanvasRendering();
        newComp.setRenderingSurface(oldComp.renderingSurface());
        removeComp(GameSceneRendering2DComp.class);
        setComp(GameSceneRendering2DComp.class, newComp);
    }

    private GameSceneRendering2DComp createCanvasRendering() {
        final var r2d = new GameSceneRendering2DComp();
        r2d.setRenderingSurface(new RenderingSurface());
        r2d.unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        r2d.unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
        // Clip 16 pixels on each side of the canvas such that actors moving through horizontal portal are not visible.
        // All maps are 28 tiles wide but NES screen is 32 tiles wide, so we have to clip 16 pixels on each side.
        // The one extra pixel clipped on the right side helps to hide a spritesheet issue (hides ugly map image border).
        r2d.setClipRect(RectShort.sprite(2 * TS, 0, NES_SCREEN_WIDTH - 4 * TS - 1, Short.MAX_VALUE));
        return r2d;
    }

    private TengenMsPacMan_Actions actions() {
        return app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);
    }

    private TengenMsPacMan_UISettings uiSettings() {
        return app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    private void acceptNormalLevel() {
        soundManager().setEnabled(true); //TODO needed?

        // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
        actionBindings().registry().registerAllBindings(actions().steeringBindings());

        actionBindings().registry().registerAllBindings(app().commonActions().cheatActions().bindings());

        actionBindings().registry().selectAnyMatchingBinding(actions().actionTogglePlaySceneDisplayMode(), actions().localBindings());
        actionBindings().registry().selectAnyMatchingBinding(actions().actionTogglePacBooster(), actions().localBindings());
    }

    private void acceptDemoLevel() {
        soundManager().setEnabled(false); //TODO needed?

        final var actions = actions();

        final var bindingsMap = actionBindings().registry();
        bindingsMap.selectAnyMatchingBinding(actions.actionTogglePlaySceneDisplayMode(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionQuitDemoLevel(), actions.localBindings());
    }

    private void updateScaling() {
        final var uiSettings = uiSettings();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();
        final RenderingSurface renderingSurface = reqRendering2D().renderingSurface();
/*
        renderingSurface.setScaling(switch (displayMode) {
            case SCALED_TO_FIT -> subScene.getHeight() / canvasHeightUnscaled.get();
            case SCROLLING -> subScene.getHeight() / NES_SCREEN_HEIGHT;
        });
        Logger.debug("Tengen 2D play scene sub-scene: w={0.00} h={0.00} scaling={0.00}",
            subScene.getWidth(), subScene.getHeight(), reqRendering2D().renderingSurface().scaling());

 */
    }

    void playLevelCompleteAnimation(GameLevel level, int numFlashes) {
        levelCompletedAnimation = new LevelCompletedAnimation();
        levelCompletedAnimation.setOnFinished(() -> game().state().triggerTimeout());
        levelCompletedAnimation.play(level, numFlashes);
    }

    private void ensureActorAnimationsCreated(GameLevel level, boolean boosterEnabled) {
        final GameVariantRuntime variantConfig = app().variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variantConfig.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer = variantConfig.spriteAnimContainer();
        final ActorSpriteAnimController animController = variantConfig.playConfig().systems().actorSpriteAnimController();

        final Pac pac = level.entitySet().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, renderConfig.createPacAnimations(animContainer));
            eventHandler.resetPacAnimation(animController, boosterEnabled, pac);
        }

        level.entitySet().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost, renderConfig.createGhostAnimations(animContainer, ghost.personality()));
                eventHandler.resetGhostAnimation(animController, ghost);
            }
        });
    }

    private GameLevelView createRenderableLevel(GameLevel level, long tick) {
        final InfoMap renderInfo = InfoMap.create();
        final WorldMap worldMap = level.worldMap();

        final int mapNumber = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_CATEGORY);
        final MapImageSet mapImageSet = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET);
        final MapImageSet imageSet = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET);

        final var flashing = flashingState();
        final boolean highlighted = flashing != null && flashing.isHighlighted();
        final int flashingIndex = flashing != null ? flashing.flashingIndex() : -1;

        renderInfo.put(TengenMsPacMan_LevelRenderInfoKey.MAP_CATEGORY, mapCategory);
        renderInfo.put(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET, mapImageSet);

        renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, highlighted);
        renderInfo.put(LevelRenderInfoKey.FLASHING_INDEX, flashingIndex);

        if (highlighted) {
            final int imageIndex = Math.clamp(flashingIndex, 0, imageSet.flashingMapImages().size() - 1);
            final ColorSchemedMapSprite flashingMapImage = imageSet.flashingMapImages().get(imageIndex);
            renderInfo.put(LevelRenderInfoKey.MAZE_IMAGE, flashingMapImage.spriteSheetImage());
            renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE, flashingMapImage.sprite());
        }
        else {
            renderInfo.put(LevelRenderInfoKey.MAZE_IMAGE, imageSet.mapImage().spriteSheetImage());
            if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
                final int spriteIndex = strangeMap15AnimationFrame(tick);
                renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE,
                    NonArcadeMapsSpriteSheet.instance().findSpriteSequence(MAP32_ANIMATED)[spriteIndex]);
            } else {
                renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE, imageSet.mapImage().sprite());
            }
        }

        return new GameLevelView(level, renderInfo, RenderingLayer.LEVEL, 0, RENDER_OFFSET);
    }

    /**
     * Strange map #15 (maze #32) has a "psychedelic" animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private static int strangeMap15AnimationFrame(long tick) {
        final long phase = (tick % 32) / 8;
        return (int) (phase < 3 ? phase : 1);
    }
}