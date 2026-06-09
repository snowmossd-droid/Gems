package me.vennlmao.gems.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FoliaScheduler {

    private final JavaPlugin plugin;
    private final boolean folia;
    private final List<Object> tasks = new ArrayList<>();

    public FoliaScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folia = detectFolia();
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean isFolia() { return folia; }

    public void runAtLocation(Location loc, Runnable task) {
        if (folia) {
            plugin.getServer().getRegionScheduler().execute(plugin, loc, task);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public void runForEntity(Entity entity, Runnable task) {
        if (folia) {
            entity.getScheduler().execute(plugin, task, null, 1L);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public void runAsync(Runnable task) {
        if (folia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, $ -> task.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runGlobal(Runnable task) {
        if (folia) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (folia) {
            var t = plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, $ -> task.run(), delayTicks, periodTicks);
            tasks.add(t);
        } else {
            var t = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks);
            tasks.add(t);
        }
    }

    public void runLaterForEntity(Entity entity, Runnable task, long delayTicks) {
        if (folia) {
            entity.getScheduler().runDelayed(plugin, $ -> task.run(), null, delayTicks);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void cancelAll() {
        for (Object t : tasks) {
            if (t instanceof org.bukkit.scheduler.BukkitTask bt) bt.cancel();
            else if (t instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask st) st.cancel();
        }
        tasks.clear();
    }
}
