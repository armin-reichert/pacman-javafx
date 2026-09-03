/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class XXL_PacMan_RenderConfig extends ArcadePacMan_RenderConfig {

    public XXL_PacMan_RenderConfig(AssetMap assets) {
        super(assets);
    }

    @Override
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        requireNonNull(worldSettings);

        return GlobalAssets.enhanceContrast(worldSettings, worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public XXL_PacMan_GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(animController);
        requireNonNull(canvas);

        return new XXL_PacMan_GameLevelRenderer(canvas);
    }
}

