package me.bhml.elementalmagictesting.spells;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpellTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        /*
        if (!(sender instanceof Player player)) return false;

        if (args.length == 0) {
            player.sendMessage("Specify a spell to cast.");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "fireball" -> FireballSpell.cast(player);
            //case "waterwave" -> WaterWaveSpell.cast(player);
            // "quake" -> EarthQuakeSpell.cast(player);
            case "gust" -> AirGustSpell.cast(player);
            case "lightning" -> {
                if (args.length < 2) {
                    player.sendMessage("Specify variant: 1, 2, or 3");
                    return true;
                }
                switch (args[1]) {
                    case "1" -> LightningSpell.fakeRay(player);
                    case "2" -> LightningSpell.projectileLightning(player);
                    case "3" -> LightningSpell.castKiBlast(player);
                    default -> player.sendMessage("Unknown lightning type.");
                }
            }




            default -> player.sendMessage("Unknown spell: " + args[0]);
        }*/

        return true;
    }
}