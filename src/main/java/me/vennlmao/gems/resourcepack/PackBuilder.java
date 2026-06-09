package me.vennlmao.gems.resourcepack;

import me.vennlmao.gems.GemsPlugin;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class PackBuilder {

    private final GemsPlugin plugin;

    public PackBuilder(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildIfNeeded() throws Exception {
        Path outZip = plugin.getDataFolder().toPath().resolve("resourcepack.zip");
        Path srcDir = plugin.getDataFolder().toPath().resolve("resource_pack");

        if (!Files.exists(srcDir)) {
            extractBuiltinPack(srcDir);
        }

        plugin.getLogger().info("Building resourcepack.zip...");
        buildZip(srcDir, outZip);
        plugin.getLogger().info("resourcepack.zip built → " + outZip.toAbsolutePath());
    }

    private void buildZip(Path srcDir, Path outZip) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outZip))) {
            Files.walk(srcDir).filter(p -> !Files.isDirectory(p)).forEach(file -> {
                String entryName = srcDir.relativize(file).toString().replace("\\", "/");
                try {
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to add " + entryName + " to zip: " + e.getMessage());
                }
            });
        }
    }

    private void extractBuiltinPack(Path destDir) throws Exception {
        Files.createDirectories(destDir);
        // plugin.yml, models, textures sudah di-bundle ke dalam jar
        // Gunakan getResourceAsStream untuk setiap file yang di-bundle
        String[] bundledFiles = {
            "pack.mcmeta",
            "assets/minecraft/models/item/diamond.json",
            "assets/minecraft/models/item/gem_fire.json",
            "assets/minecraft/models/item/gem_water.json",
            "assets/minecraft/models/item/gem_thunder.json",
            "assets/minecraft/models/item/gem_earth.json",
            "assets/minecraft/models/item/gem_wind.json",
            "assets/minecraft/models/item/gem_dark.json",
            "assets/minecraft/models/item/gem_ice.json",
            "assets/minecraft/models/item/gem_poison.json",
            "assets/minecraft/models/item/gem_blood.json",
            "assets/minecraft/models/item/gem_soul.json",
            "assets/minecraft/models/item/gem_magma.json",
            "assets/minecraft/models/item/gem_shadow.json",
            "assets/minecraft/textures/item/gems/gem_fire.png",
            "assets/minecraft/textures/item/gems/gem_water.png",
            "assets/minecraft/textures/item/gems/gem_thunder.png",
            "assets/minecraft/textures/item/gems/gem_earth.png",
            "assets/minecraft/textures/item/gems/gem_wind.png",
            "assets/minecraft/textures/item/gems/gem_dark.png",
            "assets/minecraft/textures/item/gems/gem_ice.png",
            "assets/minecraft/textures/item/gems/gem_poison.png",
            "assets/minecraft/textures/item/gems/gem_blood.png",
            "assets/minecraft/textures/item/gems/gem_soul.png",
            "assets/minecraft/textures/item/gems/gem_magma.png",
            "assets/minecraft/textures/item/gems/gem_shadow.png"
        };

        for (String rel : bundledFiles) {
            String resource = "resource_pack/" + rel;
            try (InputStream is = plugin.getResource(resource)) {
                if (is == null) {
                    plugin.getLogger().warning("Bundled resource not found: " + resource);
                    continue;
                }
                Path dest = destDir.resolve(rel.replace("/", File.separator));
                Files.createDirectories(dest.getParent());
                Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        plugin.getLogger().info("Extracted built-in resource pack to " + destDir);
    }
}
