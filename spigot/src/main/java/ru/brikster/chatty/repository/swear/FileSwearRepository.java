package ru.brikster.chatty.repository.swear;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSwearRepository implements SwearRepository {

    private final List<String> words = new ArrayList<>();
    private final List<String> whitelist = new ArrayList<>();

    @SneakyThrows(IOException.class)
    public FileSwearRepository(Path dataFolderPath) {
        Path swearsDirectory = dataFolderPath.resolve("swears");
        Path swearsFile = swearsDirectory.resolve("swears.txt");
        Path whitelistFile = swearsDirectory.resolve("whitelist.txt");

        if (!Files.exists(swearsDirectory)) {
            Files.createDirectory(swearsDirectory);
        }

        if (!Files.exists(swearsFile)) {
            Files.createFile(swearsFile);
        }

        if (!Files.exists(whitelistFile)) {
            Files.createFile(whitelistFile);
        }

        for (String swearPattern : Files.readAllLines(swearsFile, StandardCharsets.UTF_8)) {
            if (swearPattern.isEmpty()) {
                continue;
            }

            words.add(swearPattern);
        }

        whitelist.addAll(Files.readAllLines(whitelistFile, StandardCharsets.UTF_8));
    }

    @Override
    public List<String> getSwears() {
        return words;
    }

    @Override
    public List<String> getWhitelist() {
        return whitelist;
    }

}
