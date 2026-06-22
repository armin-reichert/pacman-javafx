/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public record WorldConfig(
    PacConfig pac,
    List<GhostConfig> ghosts,
    BonusConfig bonus,
    LevelCounterConfig3D levelCounter,
    LivesCounterConfig3D livesCounter,
    MazeConfig3D maze,
    HouseConfig3D house,
    FloorConfig3D floor,
    PelletConfig3D pellet,
    EnergizerConfig3D energizer)
{
    public static WorldConfig fromJSON(String jsonFilePath) {
        try (var in = UISettings.class.getResourceAsStream(jsonFilePath)) {
            if (in == null) {
                Logger.error("Could not access UI settings from path {}", jsonFilePath);
            }
            else {
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                var parser = new GsonBuilder()
                    .registerTypeAdapter(Color.class, new UISettings.ColorAdapter())
                    //.setStrictness(Strictness.LENIENT)
                    .create();
                WorldConfig values = parser.fromJson(reader, WorldConfig.class);
                Logger.info(values);
                return values;
            }
        }
        catch (IOException x) {
            Logger.error(x, "Could not read UI settings from {}", jsonFilePath);
        }
        throw new IllegalArgumentException("Could not read UI settings from " + jsonFilePath);
    }

    public static class ColorAdapter extends TypeAdapter<Color> {

        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            return Color.web(in.nextString());
        }
    }

}
