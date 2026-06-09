package me.vennlmao.gems.resourcepack;

import me.vennlmao.gems.GemsPlugin;

public class PackBuilder {

    private final GemsPlugin plugin;

    public PackBuilder(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildIfNeeded() {
        plugin.getLogger().info("Using remote resource pack, no local build needed.");
    }
}
