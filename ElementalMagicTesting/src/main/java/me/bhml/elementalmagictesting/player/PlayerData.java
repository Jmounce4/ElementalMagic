package me.bhml.elementalmagictesting.player;
import java.util.*;
import java.util.UUID;
public class PlayerData {
    private final UUID playerId;

    // --- Targeting Preferences ---
    private boolean allowFriendlyFire = false;
    private boolean canHitPets = false;
    private boolean canHitAnimals = false;
    private boolean canHitVillagers = false;

    // --- Relationships ---
    private final Set<UUID> partyMembers = new HashSet<>();

    // --- Spellcasting ---
    private double mana = 100.0;
    private double maxMana = 100.0;

    private int level = 1;
    private double xp = 0.0;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    // Getters/Setters for preferences
    public boolean canHitPets() { return canHitPets; }
    public void setCanHitPets(boolean value) { this.canHitPets = value; }

    public boolean canHitAnimals() { return canHitAnimals; }
    public void setCanHitAnimals(boolean value) { this.canHitAnimals = value; }

    public boolean canHitVillagers() { return canHitVillagers; }
    public void setCanHitVillagers(boolean value) { this.canHitVillagers = value; }

    public boolean allowsFriendlyFire() { return allowFriendlyFire; }
    public void setAllowFriendlyFire(boolean value) { this.allowFriendlyFire = value; }

    // Party logic
    public void addPartyMember(UUID uuid) { partyMembers.add(uuid); }
    public void removePartyMember(UUID uuid) { partyMembers.remove(uuid); }
    public boolean isInPartyWith(UUID uuid) { return partyMembers.contains(uuid); }

    // Mana
    public double getMana() { return mana; }
    public void setMana(double mana) { this.mana = Math.min(mana, maxMana); }
    public double getMaxMana() { return maxMana; }

    // XP & Level
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }


}
