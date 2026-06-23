/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class SettingsLoader {

    public static <T> T load(URL url, Class<T> settingsClass) {
        requireNonNull(url);
        requireNonNull(settingsClass);
        try {
            return new SettingsLoader().loadJSON(url, settingsClass);
        } catch (IOException e) {
            throw new RuntimeException("Error loading settings file from URL '%s'".formatted(url), e);
        }
    }

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

    public <T> T loadJSON(URL settingsFileURL, Class<T> settingsClass) throws IOException {
        requireNonNull(settingsFileURL);
        requireNonNull(settingsClass);
        try (var in = settingsFileURL.openStream()) {
            final var reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            final var settingsObject = parser.fromJson(reader, settingsClass);
            Logger.info(settingsObject);
            return settingsObject;
        }
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
