package me.bhml.elementalmagictesting.listeners;

import org.bukkit.event.Listener;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class ProjectileHitListener implements Listener {
    private final JavaPlugin plugin;

    public ProjectileHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();

        if (!proj.hasMetadata("spell_type")) {
            return; // No spell metadata; ignore
        }

        MetadataValue meta = proj.getMetadata("spell_type").stream()
                .filter(m -> m.getOwningPlugin() == plugin)
                .findFirst()
                .orElse(null);

        if (meta == null) return;

        String spellType = meta.asString();

        switch (spellType) {
            case "flame_bolt" -> handleFlameBoltHit(event);
            // Add other cases here as you add new spells
        }
    }

    private void handleFlameBoltHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();

        if (event.getHitEntity() instanceof LivingEntity target) {
            Entity shooter = null;
            ProjectileSource source = proj.getShooter();

            if (source instanceof Entity entityShooter) {
                shooter = entityShooter;
            }

            target.setFireTicks(100);  // Longer burn
            if (shooter != null) {
                target.damage(6.0, shooter);  // Increased damage
            } else {
                target.damage(6.0);
            }

            Location loc = target.getLocation().add(0, 1, 0);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 30, 0.3, 0.3, 0.3, 0.02);
            loc.getWorld().spawnParticle(Particle.LAVA, loc, 10, 0.3, 0.3, 0.3, 0.02);
            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 1.5f, 1.2f);

            // Optional nausea effect for a quick "camera shake"
            //target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20, 1));
        }

        if (event.getHitBlock() != null) {
            Location loc = proj.getLocation();
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 20, 0.3, 0.3, 0.3, 0.05);
            loc.getWorld().spawnParticle(Particle.LAVA, loc, 10, 0.3, 0.3, 0.3, 0.05);
            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 1.5f, 1.2f);
        }
    }



}
