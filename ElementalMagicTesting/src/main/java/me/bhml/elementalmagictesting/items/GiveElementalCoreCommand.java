package me.bhml.elementalmagictesting.items;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.bhml.elementalmagictesting.items.ItemManager;
import org.bukkit.ChatColor;

public class GiveElementalCoreCommand implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        player.getInventory().addItem(ItemManager.getElementalCore());
        player.sendMessage(ChatColor.GREEN + "You have received the Elemental Core!");
        return true;
    }


}
