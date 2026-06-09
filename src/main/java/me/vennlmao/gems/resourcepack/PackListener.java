package me.vennlmao.gems.resourcepack;

import me.vennlmao.gems.GemsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PackListener implements Listener {

    private final GemsPlugin plugin;
    private final PackServer packServer;
    private final Map<UUID, Integer> retryCount = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 2;

    public PackListener(GemsPlugin plugin, PackServer packServer) {
        this.plugin = plugin;
        this.packServer = packServer;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!packServer.isReady()) return;
        Player player = event.getPlayer();
        int delayTicks = plugin.getConfig().getInt("resourcepack.send-delay-ticks", 20);
        plugin.scheduler().runLaterForEntity(player, () -> sendPack(player), delayTicks);
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        switch (status) {
            case SUCCESSFULLY_LOADED -> {
                retryCount.remove(player.getUniqueId());
                if (plugin.getConfig().getBoolean("resourcepack.notify-success", true)) {
                    String msg = plugin.getConfig().getString("resourcepack.messages.success", "§aResource pack loaded!");
                    plugin.scheduler().runForEntity(player, () -> player.sendMessage(msg));
                }
            }
            case DECLINED -> {
                retryCount.remove(player.getUniqueId());
                if (plugin.getConfig().getBoolean("resourcepack.kick-on-decline", false)) {
                    String msg = plugin.getConfig().getString("resourcepack.messages.kick-message", "§cBạn phải chấp nhận Resource Pack!");
                    plugin.scheduler().runForEntity(player, () ->
                        player.kick(net.kyori.adventure.text.Component.text(msg)));
                } else {
                    String msg = plugin.getConfig().getString("resourcepack.messages.declined", "§cBạn từ chối resource pack!");
                    plugin.scheduler().runForEntity(player, () -> player.sendMessage(msg));
                }
            }
            case FAILED_DOWNLOAD -> {
                int retries = retryCount.getOrDefault(player.getUniqueId(), 0);
                if (retries < MAX_RETRIES) {
                    retryCount.put(player.getUniqueId(), retries + 1);
                    plugin.getLogger().warning("Pack download failed for " + player.getName()
                        + " — retrying (" + (retries + 1) + "/" + MAX_RETRIES + ")");
                    plugin.scheduler().runLaterForEntity(player, () -> sendPack(player), 60L);
                } else {
                    retryCount.remove(player.getUniqueId());
                    String msg = plugin.getConfig().getString("resourcepack.messages.failed", "§cTải resource pack thất bại!");
                    plugin.scheduler().runForEntity(player, () -> player.sendMessage(msg));
                }
            }
            case ACCEPTED -> plugin.getLogger().info(player.getName() + " đang tải resource pack...");
            default -> {}
        }
    }

    private void sendPack(Player player) {
        if (!player.isOnline() || !packServer.isReady()) return;
        boolean required = plugin.getConfig().getBoolean("resourcepack.required", false);
        String prompt = plugin.getConfig().getString("resourcepack.prompt", "Gems Plugin cần resource pack!");
        player.setResourcePack(
            packServer.getPackUrl(),
            packServer.getPackSha1(),
            net.kyori.adventure.text.Component.text(prompt),
            required
        );
    }

    public void resendPack(Player player) {
        retryCount.remove(player.getUniqueId());
        sendPack(player);
    }
}
