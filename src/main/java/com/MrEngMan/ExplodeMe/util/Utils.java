package com.MrEngMan.ExplodeMe.util;

import com.MrEngMan.ExplodeMe.ExplodeMe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Utils {

    // Translate '&' as formatting codes
    public static String SendChatMessage(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // Print debug messages only if enabled
    public static void debugPrint(String msg) {
        if(ExplodeMe.getPlugin().isDebugEnabled()) {
            Bukkit.getLogger().info("[ExplodeMe] " + msg);
        }
    }

}

