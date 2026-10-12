package com.group_finity.mascot.cloud;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Optional UTF-8 data belonging to a character pack. No global message fallback. */
public final class CloudMessages {
    public static final String FILE_NAME = "cloud_messages_ar.properties";
    private static final Logger LOG = Logger.getLogger(CloudMessages.class.getName());

    private CloudMessages() { }

    public static List<String> load(String imageSet) {
        Path conf=com.group_finity.mascot.AppPaths.pack(imageSet).resolve("conf");
        Path localized=conf.resolve(com.group_finity.mascot.settings.RuntimeSettings.arabic()?FILE_NAME:"cloud_messages_en.properties");
        if(!Files.isRegularFile(localized))localized=conf.resolve(FILE_NAME);
        var messages=new java.util.LinkedHashSet<>(load(localized));
        if(com.group_finity.mascot.settings.RuntimeSettings.flag("CustomCloudEnabled",true))
            for(var entry:CustomMessages.load())if(entry.enabled())messages.add(entry.text());
        return messages.size()>=2?List.copyOf(messages):List.of();
    }

    public static List<String> load(Path file) {
        if (!Files.isRegularFile(file)) return List.of();
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
            List<String> keys = properties.stringPropertyNames().stream()
                    .filter(key -> key.matches("message\\.[1-9][0-9]*"))
                    .sorted(Comparator.comparingInt(key -> Integer.parseInt(key.substring(8))))
                    .toList();
            List<String> messages = new ArrayList<>();
            for (String key : keys) {
                String value = properties.getProperty(key);
                // Keep punctuation, diacritics and whitespace inside each message untouched.
                if (!value.isBlank() && !messages.contains(value)) messages.add(value);
            }
            // A singleton cannot satisfy the no-immediate-repetition requirement.
            return messages.size() >= 2 ? List.copyOf(messages) : List.of();
        } catch (IOException | IllegalArgumentException ex) {
            LOG.log(Level.WARNING, "Unable to load optional cloud messages: " + file, ex);
            return List.of();
        }
    }
}
