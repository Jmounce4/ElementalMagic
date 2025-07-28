package me.bhml.elementalmagictesting.player;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.player.PlayerData;

public class TargetingUtils {

    public static boolean canHit(LivingEntity caster, LivingEntity target) {
        if (caster.equals(target)) return false;

        if (caster instanceof Player casterPlayer) {
            PlayerData data = PlayerDataManager.get(casterPlayer);
            if (data == null) {
                Bukkit.getLogger().warning("NO PLAYER DATA FOUND for " + casterPlayer.getName() + " in canHit()");
                return true;
            }

            //Bukkit.getLogger().info("Checking canHit for caster: " + casterPlayer.getName() + ", target: " + target.getType() + ", canHitAnimals: " + data.canHitAnimals());

            if (target instanceof Player targetPlayer) {
                if (!data.allowsFriendlyFire() && data.isInPartyWith(targetPlayer.getUniqueId())) {
                    Bukkit.getLogger().info("Blocked due to friendly fire on player " + targetPlayer.getName());
                    return false;
                }
            }

            if (target instanceof Tameable tameable && tameable.isTamed()) {
                if (!data.canHitPets()) {
                    Bukkit.getLogger().info("Blocked due to canHitPets = false on " + target.getType());
                    return false;
                }
            }

            if (isPassiveAnimal(target)) {
                Bukkit.getLogger().info("Target is passive animal: " + target.getType());
                if (!data.canHitAnimals()) {
                    Bukkit.getLogger().info("Blocked due to canHitAnimals = false");
                    return false;
                }
                Bukkit.getLogger().info("Allowed to hit passive animal: " + target.getType());
            }

            if (target instanceof Villager) {
                if (!data.canHitVillagers()) {
                    Bukkit.getLogger().info("Blocked due to canHitVillagers = false");
                    return false;
                }
            }
        }

        return true;
    }


    public static boolean canHeal(Player caster, LivingEntity target) {
        if (target.equals(caster)) return true;
        if (isPetOf(caster, target)) return true;
        if (target instanceof Player targetPlayer) {
            if (PlayerDataManager.get(caster).isInPartyWith(targetPlayer.getUniqueId())) return true;
        }
        return false;
    }

    public static boolean isPetOf(Player caster, Entity target) {
        if (!(target instanceof Tameable)) return false;
        Tameable tameable = (Tameable) target;
        return tameable.isTamed() && caster.equals(tameable.getOwner());
    }

    private static boolean isPassiveAnimal(Entity entity) {
        return entity instanceof Animals || entity instanceof Ambient;
    }

    private static boolean isHostile(Entity entity) {
        return entity instanceof Monster;
    }



}
