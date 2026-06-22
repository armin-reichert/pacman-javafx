/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SettingsLoader {
    protected final Gson parser;

    public SettingsLoader() {
        parser = createParser();
    }

    protected Gson createParser() {
        return new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            //.setStrictness(Strictness.LENIENT)
            .create();
    }

    public <T> T loadJSON(String jsonFilePath, Class<T> settingsClass) {
        try (var in = getClass().getResourceAsStream(jsonFilePath)) {
            if (in == null) {
                Logger.error("Could not access UI settings from path {}", jsonFilePath);
            }
            else {
                final var reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                final var values = parser.fromJson(reader, settingsClass);
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
