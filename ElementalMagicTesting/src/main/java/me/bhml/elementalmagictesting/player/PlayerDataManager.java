package me.bhml.elementalmagictesting.player;

import org.bukkit.entity.Player;
import java.util.*;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static void createData(Player player) {
        UUID uuid = player.getUniqueId();
        playerDataMap.put(uuid, new PlayerData(uuid));
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public static boolean hasData(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public static void remove(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static Collection<PlayerData> getAllData() {
        return playerDataMap.values();
    }



}
