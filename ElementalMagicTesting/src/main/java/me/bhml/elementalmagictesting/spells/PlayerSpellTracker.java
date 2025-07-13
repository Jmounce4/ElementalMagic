package me.bhml.elementalmagictesting.spells;

import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import org.bukkit.entity.Player;


import java.util.*;

public class PlayerSpellTracker {

    // --- Tracker Instances ---
    private static final Map<UUID, PlayerSpellTracker> trackers = new HashMap<>();

    // Maps player UUID to their current spell index
    private static final Map<UUID, Integer> currentSpellIndex = new HashMap<>();
    // Map to track cooldowns: Player UUID -> (Spell Name -> Cooldown End Timestamp)
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    private final Player player;
    private List<Spell> availableSpells = new ArrayList<>();
    private int selectedSpellIndex = 0;

    private PlayerSpellTracker(Player player) {
        this.player = player;
        refreshAvailableSpells();
    }

    /**
     * Retrieve or create the tracker for a player
     */
    public static PlayerSpellTracker get(Player player) {
        return trackers.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSpellTracker(player));
    }

    /**
     * Remove tracker on player quit
     */
    public static void remove(Player player) {
        trackers.remove(player.getUniqueId());
        currentSpellIndex.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Convenience static: cycle next spell for given player
     */
    public static void cycleNextSpell(Player player) {
        get(player).cycleNextSpell();
    }

    /**
     * Convenience static: get current spell for given player
     */
    public static Spell getCurrentSpell(Player player) {
        return get(player).getSelectedSpell();
    }

    /**
     * Rebuild availableSpells from PlayerData unlocked spell IDs
     */
    public void refreshAvailableSpells() {
        availableSpells.clear();
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Set<String> unlockedSpellIds = data.getUnlockedSpells();
        for (String spellId : unlockedSpellIds) {
            Spell spell = SpellRegistry.get(spellId);
            if (spell != null) {
                availableSpells.add(spell);
            }
        }

        // --- TEMPORARY: fill loadout if empty ---
        if (data.getLoadoutSpells().isEmpty()) {
            List<String> loadout = availableSpells.stream()
                    .limit(5)
                    .map(Spell::getId)
                    .toList();
            data.setLoadoutSpells(loadout);
        }

        selectedSpellIndex = 0;
        currentSpellIndex.put(player.getUniqueId(), selectedSpellIndex);
    }

    /** Cycle through availableSpells list */
    public void cycleNextSpell() {
        if (availableSpells.isEmpty()) return;
        selectedSpellIndex = (selectedSpellIndex + 1) % availableSpells.size();
        currentSpellIndex.put(player.getUniqueId(), selectedSpellIndex);
    }

    /** Get currently selected spell */
    public Spell getSelectedSpell() {
        if (availableSpells.isEmpty()) return null;
        return availableSpells.get(selectedSpellIndex);
    }


    // Other methods like castSelectedSpell(), etc.




// Check if a player is currently on cooldown for a spell
    public static boolean isOnCooldown(Player player, String spellName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;
        long now = System.currentTimeMillis();
        Long cooldownEnd = playerCooldowns.get(spellName);
        return cooldownEnd != null && cooldownEnd > now;
    }

    // Set cooldown for a player and spell (duration in milliseconds)
    public static void setCooldown(Player player, String spellName, long cooldownMillis) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(spellName, System.currentTimeMillis() + cooldownMillis);
    }

    public static long getRemainingCooldown(Player player, String spellName) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (!cooldowns.containsKey(uuid)) return 0L;
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(spellName)) return 0L;

        long endTime = playerCooldowns.get(spellName);
        long remaining = endTime - currentTime;
        return Math.max(0L, remaining);
    }



    //Casting checks
    // 1a) Storage for who’s actively casting right now
    private static final Set<UUID> castingPlayers = new HashSet<>();

    // 1b) Call this just before you invoke current.cast(player)
    public static void markCasting(Player p) {
        castingPlayers.add(p.getUniqueId());
    }

    // 1c) Call this one tick later to clear the flag
    public static void unmarkCasting(Player p) {
        castingPlayers.remove(p.getUniqueId());
    }

    // 1d) Query whether the player is in that brief “casting” window
    public static boolean isCasting(Player p) {
        return castingPlayers.contains(p.getUniqueId());
    }


}
