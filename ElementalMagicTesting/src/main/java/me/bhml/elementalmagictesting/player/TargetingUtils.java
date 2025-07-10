package me.bhml.elementalmagictesting.player;
import org.bukkit.entity.*;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.player.PlayerData;

public class TargetingUtils {

    public static boolean canHit(LivingEntity caster, LivingEntity target) {
        if (caster.equals(target)) return false;

        // --- If caster is a player, use their preferences ---
        if (caster instanceof Player casterPlayer) {
            PlayerData data = PlayerDataManager.get(casterPlayer);
            if (data == null){
                casterPlayer.sendMessage("NO PLAYER DATA FOUND FOR YOU!");
                return true; // fallback if no data
            }

            // Party/friends check
            if (target instanceof Player targetPlayer) {
                if (!data.allowsFriendlyFire() && data.isInPartyWith(targetPlayer.getUniqueId())) {
                    return false;
                }
            }

            // Pets (tamed wolves, cats, etc)
            if (target instanceof Tameable tameable && tameable.isTamed()) {
                if (!data.canHitPets()) return false;
            }

            // Animals
            if (isPassiveAnimal(target)) {
                if (!data.canHitAnimals()) return false;
            }

            // Villagers
            if (target instanceof Villager) {
                //casterPlayer.sendMessage("Trying to hit a villager! Allowed: " + data.canHitVillagers());
                if (!data.canHitVillagers()) return false;
            }
        }

        // --- Always allow hostile mobs ---
        return isHostile(target);
    }

    private static boolean isPassiveAnimal(Entity entity) {
        return entity instanceof Animals || entity instanceof Ambient;
    }

    private static boolean isHostile(Entity entity) {
        return entity instanceof Monster;
    }



}
