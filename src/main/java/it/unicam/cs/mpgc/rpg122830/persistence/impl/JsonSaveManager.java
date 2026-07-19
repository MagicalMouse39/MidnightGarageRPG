package it.unicam.cs.mpgc.rpg122830.persistence.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unicam.cs.mpgc.rpg122830.core.model.Component;
import it.unicam.cs.mpgc.rpg122830.core.model.Player;
import it.unicam.cs.mpgc.rpg122830.persistence.api.SaveManager;
import java.io.*;

public class JsonSaveManager implements SaveManager<Player> {
    private final Gson gson;

    public JsonSaveManager() {
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Component.class, new ComponentAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void save(Player state, File file) throws IOException {
        // Ensure parent directories exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(state, writer);
        }
    }

    @Override
    public Player load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Save file not found: " + file.getAbsolutePath());
        }
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, Player.class);
        }
    }
}
