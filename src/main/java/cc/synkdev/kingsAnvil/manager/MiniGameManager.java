package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import cc.synkdev.synkLibs.bukkit.Lang;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MiniGameManager {
    private static final KingsAnvil core = KingsAnvil.getInstance();

    public static void start() {
        SynkLibs.setSpl(core);

        if (core.isRunning) {
            Utils.log(Lang.translate("alreadyOn", core, Lang.translate("name", core)));

            return;
        }

        if (Bukkit.getOnlinePlayers().size() < core.minOnline) {
            Utils.log(ChatColor.RED + Lang.translate("notEnoughStart", core));
            return;
        }

        if (reRollSpawn()) {
            if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.YELLOW + Lang.translate("starting", core, Lang.translate("name", core)));
            core.isRunning = true;

            core.scheduler.put(System.currentTimeMillis()+(core.duration*1000L), endRunnable);
            core.scheduler.put(System.currentTimeMillis()+500L, runningLoop);
            if (core.sbd) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ScoreboardManager.createBoard(p);
                }

            }

            core.endTime = Math.toIntExact(System.currentTimeMillis() / 1000) + core.duration;
        } else start();
    }

    private static Location pickLoc() {
        List<Location> locationList = new ArrayList<>(core.locations.values());

        int randomIndex;
        if (!locationList.isEmpty()) randomIndex = ThreadLocalRandom.current().nextInt(locationList.size());
        else return null;

        return locationList.get(randomIndex);
    }

    public static Boolean reRollSpawn() {
        Location loc = pickLoc();
        SynkLibs.setSpl(core);

        if (loc == null) {
            Utils.log(ChatColor.RED + Lang.translate("noLocs", core));

            return false;
        }

        if (loc.getBlock().getType() != Material.AIR) {
            if (loc.getBlock().getType() != Material.ANVIL) {
                Utils.log(ChatColor.RED + Lang.translate("obstructed", core, Util.locToStringReadable(loc)));
                start();

                return false;
            }
        }

        loc.getBlock().setType(Material.ANVIL);
        core.currLocation = loc;
        Bukkit.broadcastMessage(core.prefix() + ChatColor.YELLOW + Lang.translate("spawnMsg", core, Lang.translate("name", core), Util.locToStringReadable(loc)));

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), "block.anvil.place", 1, 1);
        }
        return true;
    }

    public static Runnable endRunnable = new Runnable() {
        @Override
        public void run() {
            if (core.isRunning) {
                if (core.currLocation != null) {
                    core.currLocation.getBlock().setType(Material.AIR);
                    core.timer.setHealth(0);
                    core.timer = null;
                }

                if (core.sbd) {
                    core.boardMap.forEach((uuid, fastBoard) -> {
                        fastBoard.delete();
                    });
                    core.boardMap.clear();
                }

                String name;
                if (core.holder == null) name = ChatColor.RED + Lang.translate("noOne", core);
                else {
                    name = core.holder.getName();
                }

                if (core.holder != null) {
                    if (core.timeHeld.containsKey(core.holder.getUniqueId())) {
                        long time = System.currentTimeMillis() - core.timeTake;
                        time = time + core.timeHeld.get(core.holder.getUniqueId());
                        core.timeHeld.replace(core.holder.getUniqueId(), Math.toIntExact(time));
                    } else {
                        long time = System.currentTimeMillis() - core.timeTake;
                        core.timeHeld.put(core.holder.getUniqueId(), Math.toIntExact(time));
                    }
                }
                core.timeHeld.forEach((uuid, integer) -> {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(uuid);
                    LeaderboardLine lL = LeaderboardsManager.get(core.timeLeaderboard, oP);
                    if (lL == null) {
                        lL = new LeaderboardLine(oP, integer);
                        core.timeLeaderboard.add(lL);
                    } else {
                        lL.update(integer, core.timeLeaderboard);
                    }
                });
                LeaderboardsManager.sort();

                RewardsManager.giveRewards();
                if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("ended", core));
                if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("winner", core, name));

                if (core.rewardTime && core.eventMsg && RewardsManager.longestHolder() != null)
                    Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("winnerTime", core, RewardsManager.longestHolder().getName(), Lang.translate("name", core), Util.toDigiTime(core.timeHeld.get(RewardsManager.longestHolder().getUniqueId())/1000)));

                LeaderboardLine lL = LeaderboardsManager.get(core.winLeaderboard, core.holder);
                if (lL != null) {
                    lL.update(1, core.winLeaderboard);
                } else {
                    lL = new LeaderboardLine(core.holder, 1);
                    core.winLeaderboard.add(lL);
                }
                LeaderboardsManager.sort();

                core.holder = null;
                core.timeTake = 0;
                core.timeHeld.clear();
                core.isRunning = false;
            }
        }
    };
    public static Runnable runningLoop = new Runnable() {
        @Override
        public void run() {
            if (core.isRunning) {
                if (core.sbd) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        List<String> list = new ArrayList<>();
                        for (String s : core.scoreboardLines) {
                            s = PlaceholderAPI.setPlaceholders(p, s);
                            list.add(s);
                        }

                        FastBoard fB = core.boardMap.get(p.getUniqueId());
                        fB.updateLines(list);
                        core.boardMap.replace(p.getUniqueId(), fB);
                    }
                }

                if (core.currLocation != null) {
                    if (core.timer == null) core.timer = (ArmorStand) core.currLocation.getWorld().spawnEntity(core.currLocation, EntityType.ARMOR_STAND);
                    core.timer.setVisible(false);
                    core.timer.setInvulnerable(true);
                    core.timer.setBasePlate(false);
                    core.timer.setCustomNameVisible(true);
                    core.timer.setCustomName(ChatColor.RED+Util.toDigiTime(core.endTime-Math.toIntExact(System.currentTimeMillis()/1000)));
                }

                core.scheduler.put(System.currentTimeMillis()+500L, this);
            }
        }
    };
}
