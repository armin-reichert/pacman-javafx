/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor;


import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AsmMapImport {

    public static void main(String[] args) {
        var importer = new AsmMapImport();
        File dir = new File("asm");
        File[] asmFiles = dir.listFiles();
        if (asmFiles == null) {
            Logger.error("No asm files in directory {}", dir);
            return;
        }
        try {
            for (File f : asmFiles) {
                byte[][] mapContent = importer.importTerrainFromAsm(f);
            }
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    private int currentLine;
    private List<Byte> currentRow;

    public byte[][] importTerrainFromAsm(File asmFile) throws IOException {
        Logger.info("Processing file {}", asmFile);
        currentLine = 0;
        currentRow = new ArrayList<>();
        List<List<Byte>> rows = new ArrayList<>();
        rows.add(currentRow);

        try (Stream<String> lines = Files.lines(asmFile.toPath())) {
            lines.forEach(line -> {
                if (line.contains(";")) {
                    int index = line.indexOf(";");
                    line = line.substring(0, index);
                }
                if (line.isBlank()) {
                    currentRow = new ArrayList<>();
                    rows.add(currentRow);
                }
                else if (line.startsWith(".byte Maze_EndMazeBuilding")) {
                    // end of file reached
                    Logger.debug("Reached end of file");
                }
                else if (line.startsWith(".byte ")) {
                    String tileString = line.substring(6).trim();
                    if (tileString.startsWith("$")) {
                        tileString = tileString.substring(1);
                    }
                    byte tile;
                    try {
                        tile = (byte) Integer.parseInt(tileString, 16);
                        currentRow.add(tile);
                    }
                    catch (NumberFormatException x) {
                        Logger.error(x);
                    }
                }
                else if (line.startsWith("RepeatMazeTile ")) {
                    String rest = line.substring(15).trim();
                    String[] tile_times = rest.split(",");

                    String tileString = tile_times[0].trim();
                    if (tileString.startsWith("$")) {
                        tileString = tileString.substring(1);
                    }
                    byte tile = -1;
                    try {
                        tile = (byte) Integer.parseInt(tileString, 16);
                    } catch (NumberFormatException x) {
                        Logger.error(x);
                    }

                    String timesString = tile_times[1].trim();
                    int times = -1;
                    try {
                        times = Integer.parseInt(timesString);
                    } catch (NumberFormatException x) {
                        Logger.error(x);
                    }
                    if (tile != -1 && times != -1) {
                        for (int i = 0; i < times; ++i) {
                            currentRow.add(tile);
                        }
                    }
                } else {
                    Logger.debug("Skipped line: {}", line);
                }
            });
        }
        rows.removeLast();
        int rowLength = 0;
        for (List<Byte> row : rows) {
            if (row.size() > rowLength) {
                rowLength = row.size();
            }
        }
        rowLength *= 2;
        byte[][] result = new byte[rows.size()][rowLength];
        for (int i = 0; i < rows.size(); ++i) {
            List<Byte> row = rows.get(i);
            int len = row.size();
            result[i] = new byte[rowLength];
            if (len != rowLength / 2) {
                Logger.error("File {}: Row {} has length {}, should be {}", asmFile, i, len, rowLength);
            }
            for (int j = 0; j < rowLength / 2; ++j) {
                if (j < row.size()) {
                    result[i][j] = row.get(j);
                    result[i][rowLength - 1 - j] = row.get(j); // TODO mirrored value
                }
            }
        }
        Logger.info("{} rows of (half) length {}", rows.size(), 2*rowLength);

        return result;
    }
}
