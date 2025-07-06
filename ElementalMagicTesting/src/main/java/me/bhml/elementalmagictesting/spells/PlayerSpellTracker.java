package me.bhml.elementalmagictesting.spells;

import org.bukkit.entity.Player;


import java.util.*;

public class PlayerSpellTracker {

    // Maps player UUID to their current spell index
    private static final Map<UUID, Integer> currentSpellIndex = new HashMap<>();

    // Map to track cooldowns: Player UUID -> (Spell Name -> Cooldown End Timestamp)
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Temporary: all available spells (replace with player-specific later)
    private static List<Spell> availableSpells;

    public static void setAvailableSpells(List<Spell> spells) {
        availableSpells = spells;
    }

    public static Spell getCurrentSpell(Player player) {
        int index = currentSpellIndex.getOrDefault(player.getUniqueId(), 0);
        if (availableSpells == null || availableSpells.isEmpty()) return null;
        return availableSpells.get(index % availableSpells.size());
    }

    public static void cycleNextSpell(Player player) {
        int index = currentSpellIndex.getOrDefault(player.getUniqueId(), 0);
        index = (index + 1) % availableSpells.size();
        currentSpellIndex.put(player.getUniqueId(), index);

        /*
        Spell current = PlayerSpellTracker.getCurrentSpell(player);
        if (current == null) {
            SpellUtils.clearCooldownBar(player);
        } else {
            String spellName = current.getName();
            long remaining = PlayerSpellTracker.getRemainingCooldown(player, spellName);
            if (remaining > 0) {
                SpellUtils.startCooldownBar(player, remaining, spellName);
            } else {
                SpellUtils.clearCooldownBar(player);
            }
        }*/
    }


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
