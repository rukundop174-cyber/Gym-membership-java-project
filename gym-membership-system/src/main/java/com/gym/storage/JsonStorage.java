package com.gym.storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/** Saves and loads {@link GymData} as pretty-printed JSON using Jackson. */
public class JsonStorage {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())                              // LocalDate / LocalDateTime
            .enable(SerializationFeature.INDENT_OUTPUT)                   // readable JSON
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)      // "2026-10-09" not [2026,10,9]
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)   // ignore derived fields like "role"
            .build();

    public void save(GymData data, Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        mapper.writeValue(file.toFile(), data);
    }

    public GymData load(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new NoSuchFileException(file.toString());
        }
        return mapper.readValue(file.toFile(), GymData.class);
    }
}
