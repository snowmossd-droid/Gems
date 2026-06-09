package me.vennlmao.gems.manager;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.gem.GemDefinition;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class GemRegistry {

    public static final String PDC_KEY = "gem_id";

    private final GemsPlugin plugin;
    private final NamespacedKey key;
    private Map<String, GemDefinition> gems;

    public GemRegistry(GemsPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, PDC_KEY);
    }

    public void loadAll() {
        gems = plugin.cfg().loadGems();
    }

    public Optional<GemDefinition> get(String id) {
        return Optional.ofNullable(gems.get(id));
    }

    public Collection<GemDefinition> all() {
        return gems.values();
    }

    public int size() {
        return gems.size();
    }

    public ItemStack buildItem(GemDefinition def) {
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(def.displayName());
        meta.setLore(def.lore());
        meta.setCustomModelData(def.customModelData());
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, def.id());
        item.setItemMeta(meta);
        return item;
    }

    public Optional<GemDefinition> fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return Optional.empty();
        return get(id);
    }
}
