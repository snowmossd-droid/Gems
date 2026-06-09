package me.vennlmao.gems.gem;

import me.vennlmao.gems.GemsPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;

import java.util.List;

public class AbilityExecutor {

    private final GemsPlugin plugin;

    public AbilityExecutor(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, GemDefinition def) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        GemDefinition.GemStats s = def.stats();
        List<LivingEntity> nearby = getNearby(world, loc, def.aoeRadius(), player);

        plugin.scheduler().runAtLocation(loc, () -> {
            for (String ability : def.abilities()) {
                runAbility(ability, player, world, loc, def, s, nearby);
            }
            spawnParticles(world, loc, def);
            world.playSound(loc, def.sound(), 1f, def.soundPitch());
        });
    }

    private void runAbility(String ability, Player player, World world, Location loc,
                             GemDefinition def, GemDefinition.GemStats s,
                             List<LivingEntity> nearby) {
        switch (ability.toUpperCase()) {

            case "FIREBALL" -> {
                Vector dir = loc.getDirection().normalize();
                Fireball fb = world.spawn(loc.clone().add(dir.clone().multiply(1.5)), Fireball.class);
                fb.setDirection(dir.multiply(2));
                fb.setShooter(player);
                fb.setIsIncendiary(true);
            }

            case "AOE_BURN" -> nearby.forEach(e -> {
                e.setFireTicks(s.burnTicks());
                e.damage(def.damage(), player);
            });

            case "FIRE_TRAIL" -> plugin.scheduler().runGlobalTimer(() -> {
                Location cur = player.getLocation();
                world.spawnParticle(Particle.FLAME, cur, 5, 0.3, 0, 0.3, 0.02);
            }, 1, 2);

            case "AOE_SLOW" -> nearby.forEach(e -> {
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, s.slowDuration(), s.slowAmplifier()));
                e.damage(def.damage(), player);
            });

            case "SELF_HEAL" -> {
                double newHp = Math.min(player.getMaxHealth(), player.getHealth() + s.healAmount());
                plugin.scheduler().runForEntity(player, () -> player.setHealth(newHp));
            }

            case "WATER_SHIELD" -> plugin.scheduler().runForEntity(player, () ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, s.resistanceDuration(), 0)));

            case "LIGHTNING_STRIKE" -> {
                Entity closest = findClosest(nearby, loc);
                if (closest != null) world.strikeLightning(closest.getLocation());
            }

            case "AOE_STUN" -> nearby.forEach(e -> {
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, s.stunDuration(), 10));
                e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, s.stunDuration(), 3));
                e.damage(def.damage(), player);
            });

            case "CHAIN_LIGHTNING" -> {
                int count = 0;
                for (LivingEntity e : nearby) {
                    if (count++ >= 3) break;
                    world.strikeLightningEffect(e.getLocation());
                    e.damage(def.damage() * 0.5, player);
                }
            }

            case "STONE_ARMOR" -> plugin.scheduler().runForEntity(player, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, s.resistanceDuration(), s.resistanceAmplifier()));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            });

            case "SPIKE_WALL", "AOE_KNOCKBACK" -> nearby.forEach(e -> {
                Vector kb = e.getLocation().subtract(loc).toVector().normalize()
                    .multiply(s.knockbackStrength()).setY(0.5);
                plugin.scheduler().runForEntity(e, () -> {
                    e.setVelocity(kb);
                    e.damage(def.damage(), player);
                });
            });

            case "TORNADO", "AOE_LAUNCH" -> nearby.forEach(e -> {
                Vector kb = e.getLocation().subtract(loc).toVector().normalize()
                    .multiply(3.0).setY(1.2);
                plugin.scheduler().runForEntity(e, () -> {
                    e.setVelocity(kb);
                    e.damage(def.damage(), player);
                });
            });

            case "SPEED_BOOST" -> plugin.scheduler().runForEntity(player, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, s.speedDuration(), s.speedAmplifier()));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, s.jumpDuration(), s.jumpAmplifier()));
                Vector dir = loc.getDirection().multiply(s.launchMultiplier()).setY(0.9);
                player.setVelocity(dir);
            });

            case "WITHER_AURA" -> nearby.forEach(e -> {
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, s.witherDuration(), s.witherAmplifier()));
                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, s.blindDuration(), 1));
                e.damage(def.damage(), player);
            });

            case "DARK_HOLE" -> {
                nearby.forEach(e -> {
                    Vector pull = loc.clone().subtract(e.getLocation()).toVector().normalize().multiply(2);
                    plugin.scheduler().runForEntity(e, () -> e.setVelocity(pull));
                });
                world.spawnParticle(Particle.PORTAL, loc, 200, 2, 2, 2, 0.5);
            }

            case "SHADOW_CLONE" -> plugin.scheduler().runForEntity(player, () ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, s.invisDuration(), 0)));

            case "BLIZZARD" -> nearby.forEach(e -> {
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, s.freezeDuration(), 5));
                e.damage(def.damage(), player);
                world.spawnParticle(Particle.SNOWFLAKE, e.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            });

            case "ICE_ARMOR", "FREEZE_AOE" -> plugin.scheduler().runForEntity(player, () ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, s.iceArmorDuration(), s.resistanceAmplifier())));

            case "POISON_CLOUD" -> {
                world.spawnParticle(Particle.ITEM, loc, 100, 3, 1, 3, 0.05, new org.bukkit.inventory.ItemStack(Material.SLIME_BALL));
                nearby.forEach(e -> {
                    e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, s.poisonDuration(), s.poisonAmplifier()));
                    e.damage(def.damage(), player);
                });
            }

            case "VENOM_SPIT" -> {
                Vector dir = loc.getDirection().normalize();
                Arrow arrow = world.spawnArrow(loc.clone().add(0, 1, 0), dir.multiply(2), 1.5f, 5f);
                arrow.setShooter(player);
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 2), true);
            }

            case "WEAKNESS_AURA" -> nearby.forEach(e ->
                e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, s.weaknessDuration(), s.weaknessAmplifier())));

            case "LIFESTEAL" -> nearby.forEach(e -> {
                double dmg = def.damage();
                e.damage(dmg, player);
                double heal = Math.min(player.getMaxHealth() - player.getHealth(), dmg * s.lifeStealPercent());
                plugin.scheduler().runForEntity(player, () -> player.setHealth(player.getHealth() + heal));
            });

            case "BLOOD_RAGE" -> plugin.scheduler().runForEntity(player, () ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, s.strengthDuration(), s.strengthAmplifier())));

            case "HEMORRHAGE" -> nearby.forEach(e -> {
                e.damage(def.damage(), player);
                world.spawnParticle(Particle.FALLING_DUST, e.getLocation().add(0, 1, 0),
                    30, 0.3, 0.5, 0.3, 0.1, Material.REDSTONE_BLOCK.createBlockData());
            });

            case "SOUL_DRAIN" -> nearby.forEach(e -> {
                e.damage(def.damage(), player);
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, s.witherDuration(), s.witherAmplifier()));
                world.spawnParticle(Particle.SOUL, e.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            });

            case "PHASE_SHIFT" -> plugin.scheduler().runForEntity(player, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, s.invisDuration(), 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, s.speedDuration(), 2));
            });

            case "SPIRIT_BOMB" -> {
                nearby.forEach(e -> {
                    e.damage(def.damage(), player);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                });
                world.spawnParticle(Particle.SOUL, loc, 150, 3, 3, 3, 0.2);
                world.createExplosion(loc, 0, false, false, player);
            }

            case "MAGMA_BURST" -> {
                world.createExplosion(loc, (float) s.explosionPower(), true, false, player);
                nearby.forEach(e -> e.setFireTicks(s.burnTicks()));
            }

            case "LAVA_POOL" -> {
                world.spawnParticle(Particle.LAVA, loc, 80, 2, 0.1, 2);
                nearby.forEach(e -> {
                    e.setFireTicks(s.burnTicks());
                    e.damage(def.damage() * 0.7, player);
                });
            }

            case "ERUPTION" -> nearby.forEach(e -> {
                Vector up = new Vector(0, 2.5, 0);
                plugin.scheduler().runForEntity(e, () -> {
                    e.setVelocity(up);
                    e.setFireTicks(s.burnTicks());
                    e.damage(def.damage(), player);
                });
            });

            case "BACKSTAB" -> {
                Entity closest = findClosest(nearby, loc);
                if (closest instanceof LivingEntity le) {
                    le.damage(def.damage() * 1.5, player);
                    world.spawnParticle(Particle.SMOKE, closest.getLocation(), 30, 0.3, 0.5, 0.3);
                }
            }

            case "SHADOW_STEP" -> plugin.scheduler().runForEntity(player, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, s.speedDuration(), s.speedAmplifier()));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, s.invisDuration(), 0));
            });

            case "BLIND_STRIKE" -> nearby.forEach(e -> {
                e.damage(def.damage(), player);
                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, s.blindDuration(), 0));
            });
        }
    }

    private void spawnParticles(World world, Location loc, GemDefinition def) {
        try {
            world.spawnParticle(def.particle(), loc, def.particleCount(),
                1.5, 1, 1.5, 0.05, Material.STONE.createBlockData());
        } catch (Exception ignored) {
            try {
                world.spawnParticle(def.particle(), loc, def.particleCount(), 1.5, 1, 1.5, 0.05);
            } catch (Exception ignored2) {
                world.spawnParticle(Particle.CLOUD, loc, def.particleCount(), 1.5, 1, 1.5, 0.05);
            }
        }
    }

    private List<LivingEntity> getNearby(World world, Location loc, double radius, Player exclude) {
        return world.getNearbyEntities(loc, radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && e != exclude)
            .map(e -> (LivingEntity) e)
            .toList();
    }

    private Entity findClosest(List<LivingEntity> list, Location from) {
        return list.stream()
            .min((a, b) -> Double.compare(
                a.getLocation().distanceSquared(from),
                b.getLocation().distanceSquared(from)))
            .orElse(null);
    }
                    }
