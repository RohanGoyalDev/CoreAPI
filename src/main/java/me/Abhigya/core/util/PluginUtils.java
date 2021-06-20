package me.Abhigya.core.util;

import me.Abhigya.core.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class PluginUtils {

    public static void onPluginLoaded(String pluginName, Consumer<Plugin> callback) {
        if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
            callback.accept(Bukkit.getPluginManager().getPlugin(pluginName));
        } else {
            Bukkit.getPluginManager().registerEvents(new PluginLoadedListener(pluginName, callback), Main.getInstance());
        }
    }

    public static NamespacedKey parseNamespacedKey(String raw) {
        int sepIndex = raw.indexOf(':');
        if (sepIndex == -1) {
            return NamespacedKey.minecraft(raw);
        }
        @SuppressWarnings("deprecation")// Come on bukkit...
        NamespacedKey x = new NamespacedKey(raw.substring(0, sepIndex), raw.substring(sepIndex + 1));
        return x;
    }

    private static class PluginLoadedListener implements Listener {
        private final String pluginName;
        private final Consumer<Plugin> callback;

        private PluginLoadedListener(String pluginName, Consumer<Plugin> callback) {
            this.pluginName = pluginName;
            this.callback = callback;
        }


        @EventHandler
        public void onPluginEnable(PluginEnableEvent e) {
            // Check if the plugin name is what we're searching for
            if (!e.getPlugin().getName().equals(pluginName)) return;

            // Prepare for destruction (unregister listener)
            HandlerList.unregisterAll(this);

            // Call callback then wait for GC.
            callback.accept(e.getPlugin());
        }
    }

    private PluginUtils() {}

}