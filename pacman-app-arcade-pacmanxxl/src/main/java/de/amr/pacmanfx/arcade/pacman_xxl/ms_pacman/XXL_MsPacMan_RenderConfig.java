/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_RenderConfig;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import javafx.scene.canvas.Canvas;

public class XXL_MsPacMan_RenderConfig extends ArcadeMsPacMan_RenderConfig {

    public XXL_MsPacMan_RenderConfig(AssetMap assets) {
        super(assets);
    }

    @Override
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        return GlobalAssets.enhanceContrast(worldSettings, worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public XXL_MsPacMan_GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        return new XXL_MsPacMan_GameLevelRenderer(animSystem, canvas);
    }
}
