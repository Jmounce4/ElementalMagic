package me.bhml.elementalmagictesting.skills;

public class SkillProgress {
    private int level;
    private double xp;

    public SkillProgress(int level, double xp) {
        this.level = level;
        this.xp = xp;
    }

    public static double getXpRequiredForLevel(int level) {
        return 100 * Math.pow(1.2, level - 1);  // tweak base or growth as needed
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public void addXp(double amount) {
        this.xp += amount;
        while (xp >= getXpRequiredForLevel(level)) {
            xp -= getXpRequiredForLevel(level);
            level++;
            // Optionally, trigger something here (e.g., spell unlocks)
        }
    }

}
