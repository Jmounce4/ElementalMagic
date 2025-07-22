package me.bhml.elementalmagictesting.listeners;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class MobSpawningListener implements Listener{

    private static final NamespacedKey SPAWNER_TAG = new NamespacedKey(JavaPlugin.getProvidingPlugin(MobSpawningListener.class), "spawned_from_spawner");

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            LivingEntity entity = event.getEntity();
            entity.getPersistentDataContainer().set(SPAWNER_TAG, PersistentDataType.BYTE, (byte) 1);

        }
    }

    public static boolean isSpawnerMob(LivingEntity entity) {
        Byte tagged = entity.getPersistentDataContainer().get(SPAWNER_TAG, PersistentDataType.BYTE);
        return tagged != null && tagged == 1;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasMetadata("lastSpellDamager")) return;

        List<MetadataValue> metadata = entity.getMetadata("lastSpellDamager");
        if (metadata.isEmpty()) return;

        String uuidString = metadata.get(0).asString();
        UUID damagerUUID = UUID.fromString(uuidString);
        Player player = Bukkit.getPlayer(damagerUUID);
        if (player == null) return;

        // Drop XP manually
        int xp = event.getDroppedExp();
        event.setDroppedExp(0);
        ExperienceOrb orb = entity.getWorld().spawn(entity.getLocation(), ExperienceOrb.class);
        orb.setExperience(xp);
    }

}



