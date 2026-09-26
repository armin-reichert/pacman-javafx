package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameEntityViewBuilder {

    public enum ActorZOrder {
        BONUS(0),
        PAC(10),
        ORANGE_GHOST(20),
        CYAN_GHOST(30),
        PINK_GHOST(40),
        RED_GHOST(50);

        ActorZOrder(int z) {
            this.z = z;
        }

        final int z;
    }

    // Common methods

    public static GameEntityView pacView(Pac pac) {
        return builder().entity(pac).layer(RenderingLayer.ACTORS).z(ActorZOrder.PAC.z).build();
    }

    public static GameEntityView ghostView(Ghost ghost) {
        requireNonNull(ghost);
        final ActorZOrder actorZOrder = switch (ghost.personality()) {
            case RED_GHOST_SHADOW ->  ActorZOrder.RED_GHOST;
            case PINK_GHOST_SPEEDY -> ActorZOrder.PINK_GHOST;
            case CYAN_GHOST_BASHFUL -> ActorZOrder.CYAN_GHOST;
            case ORANGE_GHOST_POKEY -> ActorZOrder.ORANGE_GHOST;
        };
        return builder().entity(ghost).layer(RenderingLayer.ACTORS).z(actorZOrder.z).build();
    }

    public static GameEntityView bonusView(Bonus bonus) {
        return builder().entity(bonus).layer(RenderingLayer.ACTORS).z(ActorZOrder.BONUS.z).build();
    }

    public static GameEntityView levelEntityView(GameEntity gameEntity) {
        return builder().entity(gameEntity).layer(RenderingLayer.LEVEL).build();
    }

    public static GameEntityView messageEntityView(GameEntity gameEntity) {
        return builder().entity(gameEntity).layer(RenderingLayer.PROPS).z(-10).build();
    }

    public static GameEntityView propView(GameEntity gameEntity) {
        return builder().entity(gameEntity).layer(RenderingLayer.PROPS).build();
    }

    public static GameEntityView propView(GameEntity gameEntity, int z) {
        return builder().entity(gameEntity).layer(RenderingLayer.PROPS).z(z).build();
    }

    public static Stream<Renderable> streamOfPropViews(GameEntity... entities) {
        return Stream.of(entities)
            .filter(Objects::nonNull)
            .filter(GameEntity::isVisible)
            .map(GameEntityViewBuilder::propView);
    }

    public static Stream<Renderable> streamOfPropViews(Collection<GameEntity> entities) {
        return entities.stream()
            .filter(Objects::nonNull)
            .filter(GameEntity::isVisible)
            .map(GameEntityViewBuilder::propView);
    }

    public static Stream<Renderable> streamOfViews(Stream<GameEntity> entities, RenderingLayer layer) {
        return entities
            .filter(Objects::nonNull)
            .filter(GameEntity::isVisible)
            .map(gameEntity -> builder().entity(gameEntity).layer(layer).build());
    }

    // General builder API

    public static GameEntityViewBuilder builder() {
        return new GameEntityViewBuilder();
    }

    private GameEntity entity;
    private RenderingLayer layer;
    private int z = 0;
    private Vector2f offset = Vector2f.ZERO;
    private InfoMap renderInfo = InfoMap.EMPTY;

    private GameEntityViewBuilder() {}

    public GameEntityViewBuilder entity(final GameEntity entity) {
        this.entity = requireNonNull(entity);
        return this;
    }

    public GameEntityViewBuilder layer(final RenderingLayer layer) {
        this.layer = requireNonNull(layer);
        return this;
    }

    public GameEntityViewBuilder z(final int z) {
        this.z = z;
        return this;
    }

    public GameEntityViewBuilder offset(final Vector2f offset) {
        this.offset = requireNonNull(offset);
        return this;
    }

    public GameEntityViewBuilder renderInfo(final InfoMap renderInfo) {
        this.renderInfo = requireNonNull(renderInfo);
        return this;
    }

    public GameEntityView build() {
        requireNonNull(entity);
        requireNonNull(layer);
        requireNonNull(offset);
        requireNonNull(renderInfo);
        return new GameEntityView(entity, layer, z, offset, renderInfo);
    }
}
