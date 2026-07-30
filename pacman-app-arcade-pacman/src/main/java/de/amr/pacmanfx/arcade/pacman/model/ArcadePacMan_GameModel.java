/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.world.WorldMapSelector;
import de.amr.pacmanfx.core.rules.GameRules;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.WorldMap.tile;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GameModel extends GameModel {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    public static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected ArcadePacMan_GameRules rules;

    public ArcadePacMan_GameModel() {
        this(new ArcadePacMan_MapSelector());
    }

    /**
     * @param mapSelector e.g. selector that selects custom maps before standard maps
     */
    public ArcadePacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        rules = new ArcadePacMan_GameRules();
        levelCounter = new ArcadePacMan_LevelCounter();
        configureGateKeeper();
    }

    @Override
    public void init() {
        mapSelector().loadMapPrototypes();
        setInitialLifeCount(3);
    }

    @Override
    public ArcadePacMan_LevelCounter levelCounter() {
        return (ArcadePacMan_LevelCounter) levelCounter;
    }

    @Override
    public GameRules rules() {
        return rules;
    }

    // helpers

    protected void configureGateKeeper() {
        gateKeeper.setGhostReleasedCallback((level, prisoner) -> {
            if (prisoner.personality() == GhostPersonality.ORANGE_GHOST_POKEY) {
                final Ghost redGhost = level.ghost(GhostPersonality.RED_GHOST_SHADOW);
                final Elroy elroy = redGhost.requireComponent(Elroy.class);
                if (elroy.boost() != Elroy.Boost.NONE && !elroy.enabled()) {
                    elroy.setEnabled(true);
                    Logger.debug("Re-enabled {}'s Cruise Elroy mode because {} is released:", redGhost.name(), prisoner.name());
                }
            }
        });
    }

}