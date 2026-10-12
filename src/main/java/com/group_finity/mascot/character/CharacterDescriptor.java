package com.group_finity.mascot.character;
import java.nio.file.Path;
/** Display metadata is independent of instance identity and engine configuration. */
public record CharacterDescriptor(String id,String displayName,String author,String version,String description,
 Path root,String preview,boolean combat,boolean cloud,boolean mood,boolean legacy) { }
