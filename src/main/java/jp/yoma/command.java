package jp.yoma;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class command{

    public boolean reloadcommand(CommandSender sender) {
        if (!sender.hasPermission("uponlykb.reload")) {
            sender.sendMessage(ChatColor.RED + "You have no permission!");
            return false;
        }
        configvalues.getInstance().loadConfigValues();
        sender.sendMessage(ChatColor.GREEN + "UponlyKB's config reloaded.");
        return true;
    }
}
