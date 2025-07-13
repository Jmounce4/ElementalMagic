package me.bhml.elementalmagictesting.listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
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
}



