package me.bhml.elementalmagictesting.spells;

import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import org.bukkit.entity.Player;


import java.util.*;
import java.util.stream.Collectors;

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
        // 1) Clear out whatever spells were in the old list
        availableSpells.clear();

        // 2) Grab the PlayerData so we can read their saved loadout
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;  // bail if somehow missing

        // 3) Get the *loadout* IDs (List<String>) in the exact slot order
        List<String> loadoutIds = data.getLoadoutSpells();
        //    - Each entry is a spell ID like "fireball" or "healingaura"
        //    - If the player hasn’t equipped 5 yet, some entries may be missing

        // 4) For each equipped spell ID, look up the Spell object
        for (String id : loadoutIds) {
            // skip any nulls or invalid entries
            if (id == null) continue;

            // fetch from your central registry
            Spell spell = SpellRegistry.get(id);
            if (spell != null) {
                // only add valid spells into the cycle list
                availableSpells.add(spell);
            }
        }
        /*
        // 5) If the player has no loadout yet (first join), you could
        //    auto‐fill with their first N unlocked spells:
        if (availableSpells.isEmpty() && !data.getUnlockedSpells().isEmpty()) {
            data.setLoadoutSpells(
                    data.getUnlockedSpells().stream()
                            .limit(5)                    // up to 5 spells
                            .collect(Collectors.toList())
            );
            // recurse once to pick them up
            refreshAvailableSpells();
            return;
        }*/

        // 6) Reset selection index so they start on slot #1
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
