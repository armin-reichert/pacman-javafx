package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoCustomMaps extends InfoBox {

    @Override
    public void init(GameContext context) {
        this.context = context;

        List<WorldMap> maps = AbstractPacManGame.loadCustomMaps(GameModel.CUSTOM_MAP_DIR);
        var pattern = Pattern.compile(".*/(.*\\.world)$");
        for (var map : maps) {
            if (map.url() != null) {
                Matcher m = pattern.matcher(map.url().toExternalForm());
                if (m.matches()) {
                    String mapName = m.group(1);
                    String mapSize = "(%dx%d)".formatted(map.numRows(), map.numCols());
                    infoText(mapName, mapSize);
                }
            }
        }
    }
}
