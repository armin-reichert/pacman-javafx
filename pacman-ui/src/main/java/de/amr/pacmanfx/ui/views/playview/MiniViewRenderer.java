/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevelEntities;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiniViewRenderer extends BaseRenderer {

    public static final List<GhostPersonality> GHOST_Z_ORDER = List.of(
        GhostPersonality.ORANGE_GHOST_POKEY,
        GhostPersonality.CYAN_GHOST_BASHFUL,
        GhostPersonality.PINK_GHOST_SPEEDY,
        GhostPersonality.RED_GHOST_SHADOW);

    // Note: The level and actor renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no variant configuration is available yet!
    private final BaseRenderer levelRenderer;
    private final BaseRenderer actorRenderer;

    private final List<Renderable> actorsInZOrder = new ArrayList<>();

    public MiniViewRenderer(
        Canvas canvas,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameViewModel vm) {

        super(canvas);

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());

        actorRenderer = renderConfig.createEntityRenderer(animController, canvas);
        actorRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());
    }

    public BaseRenderer actorRenderer() {
        return actorRenderer;
    }

    public BaseRenderer levelRenderer() {
        return levelRenderer;
    }

    public void drawDebugInfo() {
        fillTextCentered("scaling: %.2f".formatted(scaling()),
            Color.WHITE,
            Font.font(12 * scaling()),
            0.5 * ctx().getCanvas().getWidth(),
            scaling() * 16
        );
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof MiniPlaySceneView miniView)) {
            return;
        }
        clearCanvas();

        final GameContext game = miniView.app().game();

        game.session().optLevel().ifPresent(level -> {
            infoMap.putAll(Map.of(
                CommonRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON,
                CommonRenderInfoKey.MAP_BRIGHT, false,
                CommonRenderInfoKey.MAP_EMPTY, level.food().remainingFoodCount() == 0,
                CommonRenderInfoKey.MAP_FLASHING, false,
                CommonRenderInfoKey.TICK, tick
            ));
            levelRenderer.setInfoMap(infoMap);
            levelRenderer.render(level, tick);

            updateActorZOrder(level.entities());
            actorsInZOrder.forEach(actor -> actorRenderer.render(actor, game.session().thisFrame().tick()));
        });
    }

    // Actor z-order: Bonus under Pac-Man under ghosts in z-order.
    private void updateActorZOrder(GameLevelEntities entities) {
        actorsInZOrder.clear();
        entities.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.addAll(entities.theGhostPoints());
        actorsInZOrder.addAll(entities.theBonusPoints());
        actorsInZOrder.add(entities.pac());
        GHOST_Z_ORDER.stream().map(entities::ghost).forEach(actorsInZOrder::add);
    }
}
