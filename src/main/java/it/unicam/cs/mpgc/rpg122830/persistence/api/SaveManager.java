package it.unicam.cs.mpgc.rpg122830.persistence.api;

import java.io.File;
import java.io.IOException;

public interface SaveManager<T> {
    void save(T state, File file) throws IOException;
    T load(File file) throws IOException;
}
