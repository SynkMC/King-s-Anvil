package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    private final KingsAnvil core = KingsAnvil.getInstance();

    @EventHandler
    public void interact (PlayerInteractEvent event) {
        if (!core.isRunning) return;

        Player p = event.getPlayer();

        if (event.getClickedBlock() == null) return;
        if (!Util.compareBlockLocs(event.getClickedBlock().getLocation(), core.currLocation)) {
            return;
        }

        core.holder = p;

        Bukkit.broadcastMessage(core.prefix()+ChatColor.YELLOW+Lang.translate("pickUp", Lang.translate("name"), p.getName()));

        core.currLocation = null;

        event.setCancelled(true);
        event.getClickedBlock().setType(Material.AIR);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), "block.anvil.place", 1, 1);
        }

        if (core.rewardTime) core.timeTake = System.currentTimeMillis();

        if (core.displayAn) {
            Location l = p.getLocation();
            l.add(0, 1, 0);
            l.setYaw(l.getYaw()+90);
            core.display = (ArmorStand) p.getLocation().getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
            core.display.setGravity(false);
            core.display.setInvulnerable(true);
            core.display.setBasePlate(false);
            core.display.setVisible(false);
            core.display.setHelmet(new ItemStack(Material.ANVIL));
        }

        core.timer.setHealth(0);
        core.timer = null;
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (core.isRunning && core.sbd) {
            ScoreboardManager.createBoard(p);
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        if (core.isRunning && Util.comparePlayers(p, core.holder)) {
            core.holder = null;

            Bukkit.broadcastMessage(core.prefix() + ChatColor.YELLOW + Lang.translate("holderDisconnected", Lang.translate("name")));

            MiniGameManager.reRollSpawn();
        }

        if (core.sbd) {
            if (core.boardMap.containsKey(p.getUniqueId())) {
                FastBoard fB = core.boardMap.remove(p.getUniqueId());
                fB.delete();
            }
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        if (!core.isRunning) return;

        if (core.holder == null) return;

        Player p = event.getEntity();
        if (Util.comparePlayers(p, core.holder)) {
            if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent && isPlayerDamage((EntityDamageByEntityEvent) p.getLastDamageCause()) != null) {
                Player dmgr = isPlayerDamage((EntityDamageByEntityEvent) p.getLastDamageCause());

                Bukkit.broadcastMessage(core.prefix()+ ChatColor.YELLOW+Lang.translate("holderKilled", p.getName(), dmgr.getName(), Lang.translate("name")));
                core.holder = dmgr;

                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.playSound(pl.getLocation(), "block.anvil.place", 1, 1);
                }
                    if (core.timeHeld.containsKey(p.getUniqueId())) {
                        long time = System.currentTimeMillis() - core.timeTake;
                        LeaderboardLine lL = LeaderboardsManager.get(core.timeLeaderboard, p);
                        if (lL != null) {
                            lL.update(Math.toIntExact(time), core.timeLeaderboard);
                        }
                        time = time + core.timeHeld.get(p.getUniqueId());
                        core.timeHeld.replace(p.getUniqueId(), Math.toIntExact(time));
                    } else {
                        long time = System.currentTimeMillis() - core.timeTake;
                        core.timeHeld.put(p.getUniqueId(), Math.toIntExact(time));
                        LeaderboardLine lL = LeaderboardsManager.get(core.timeLeaderboard, p);
                        if (lL != null) {
                            lL.update(Math.toIntExact(time), core.timeLeaderboard);
                        }
                    }
                    core.timeTake = System.currentTimeMillis();
            } else MiniGameManager.reRollSpawn();
        }
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        if (!core.displayAn) return;
        if (!core.isRunning) {
            if (core.display != null) {
                core.display.setHealth(0);
                core.display = null;
            }
            return;
        }

        if (core.holder == null) {
            if (core.display != null) {
                core.display.setHealth(0);
                core.display = null;
            }
            return;
        }

        if (!Util.comparePlayers(event.getPlayer(), core.holder)) return;

        Location l = event.getPlayer().getLocation();
        l.add(0, 1, 0);
        l.setYaw(l.getYaw()+90);
        core.display.teleport(l);
    }

    private Player isPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.PLAYER) {
            return (Player) event.getDamager();
        } else return null;
    }
}
