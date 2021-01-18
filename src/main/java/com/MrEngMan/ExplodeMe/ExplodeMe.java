package com.MrEngMan.ExplodeMe;

import com.MrEngMan.ExplodeMe.listeners.Listeners;
import com.MrEngMan.ExplodeMe.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;

public class ExplodeMe extends JavaPlugin implements Listener {

    private static ExplodeMe plugin;

    private boolean debug;
    private float blockExplodeCheckRadius;

    // When plugin is first enabled
    @SuppressWarnings("static-access")
    @Override
    public void onEnable() {
        this.plugin = this;
        reloadTheConfig();

        // Register stuff
        getCommand("emreload").setExecutor(new ReloadCommandHandler());
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);

    }

    public void reloadTheConfig() {

        // Generate the config file if it was deleted
        if (!(new File(this.getDataFolder(), "config.yml").exists())) {
            this.saveDefaultConfig();
        }

        // Load new config values
        reloadConfig();
        debug = getConfig().getBoolean("debug", false);
        blockExplodeCheckRadius = getConfig().getInt("block-explode-check-radius", 10);

    }

    public class ReloadCommandHandler implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            // Player issued command
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // Make sure they have permission
                if (player.hasPermission("emreload.reload")) {
                    plugin.reloadTheConfig();
                    player.sendMessage(Utils.SendChatMessage(plugin.getConfig().getString("reloaded-message")));
                } else {
                    player.sendMessage(Utils.SendChatMessage(plugin.getConfig().getString("no-permission-message")));
                }

            }

            // Console issued command
            else if (sender instanceof ConsoleCommandSender) {
                plugin.reloadTheConfig();
                ConsoleCommandSender console = getServer().getConsoleSender();
                console.sendMessage(Utils.SendChatMessage(plugin.getConfig().getString("reloaded-message")));
            }

            return true;
        }

    }

    // Getters
    public static ExplodeMe getPlugin() {
        return plugin;
    }

    public float getBlockExplodeCheckRadius() {
        return blockExplodeCheckRadius;
    }

    public boolean isDebugEnabled() {
        return debug;
    }


}