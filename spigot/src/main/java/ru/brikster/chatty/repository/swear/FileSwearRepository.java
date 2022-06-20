package ru.brikster.chatty.repository.swear;

import ru.brikster.chatty.Chatty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSwearRepository implements SwearRepository {

    private final List<String> words = new ArrayList<>();
    private final List<String> whitelist = new ArrayList<>();

    public FileSwearRepository() throws IOException {
        Path swearsDirectory = Chatty.get().getDataFolder().toPath().resolve("swears");
        Path swearsFile = swearsDirectory.resolve("swears.txt");
        Path whitelistFile = swearsDirectory.resolve("whitelist.txt");

        if (!Files.exists(swearsDirectory)) {
            Files.createDirectory(swearsFile);
        }

        if (!Files.exists(swearsFile)) {
            Files.createFile(swearsFile);
        }

        if (!Files.exists(whitelistFile)) {
            Files.createFile(whitelistFile);
        }

        for (String swear : Files.readAllLines(swearsFile, StandardCharsets.UTF_8)) {
            if (swear.isEmpty()) {
                continue;
            }

            words.add(swear);
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
