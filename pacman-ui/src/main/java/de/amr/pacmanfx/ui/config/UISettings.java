package de.amr.pacmanfx.ui.config;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public record UISettings(
    double flashMessageDuration,
    Color canvasBackgroundColor,
    boolean fontSmoothingOn,
    boolean debugModeOn,
    boolean keyboardMonitorOn,
    boolean muted,
    int numSimulationSteps,
    MiniViewSettings miniView,
    UISettings3D d3)
{
    public static UISettings fromJSON(String jsonFilePath) {
        try (var in = UISettings.class.getResourceAsStream(jsonFilePath)) {
            if (in == null) {
                Logger.error("Could not access UI settings from path {}", jsonFilePath);
            }
            else {
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                var parser = new GsonBuilder()
                    .registerTypeAdapter(Color.class, new ColorAdapter())
                    .create();
                UISettings values = parser.fromJson(reader, UISettings.class);
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
