package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MiniGameManager {
    private static final KingsAnvil core = KingsAnvil.getInstance();

    public static void start() {
        SynkLibs.setSpl(core);

        if (core.isRunning) {
            Utils.log(Lang.translate("alreadyOn", Lang.translate("name")));

            return;
        }

        if (Bukkit.getOnlinePlayers().size() < core.minOnline) {
            Utils.log(ChatColor.RED + Lang.translate("notEnoughStart"));
            return;
        }

        if (reRollSpawn()) {
            if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.YELLOW + Lang.translate("starting", Lang.translate("name")));
            core.isRunning = true;

            endRunnable.runTaskLater(core, 20L * core.duration);
            if (core.sbd) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ScoreboardManager.createBoard(p);
                }

                ScoreboardManager.updateBoards.runTaskTimer(core, 10, 10);
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
            Utils.log(ChatColor.RED + Lang.translate("noLocs"));

            return false;
        }

        if (loc.getBlock().getType() != Material.AIR) {
            if (loc.getBlock().getType() != Material.ANVIL) {
                Utils.log(ChatColor.RED + Lang.translate("obstructed", Util.locToStringReadable(loc)));
                start();

                return false;
            }
        }

        loc.getBlock().setType(Material.ANVIL);
        core.currLocation = loc;
        Bukkit.broadcastMessage(core.prefix() + ChatColor.YELLOW + Lang.translate("spawnMsg", Lang.translate("name"), Util.locToStringReadable(loc)));

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), "block.anvil.place", 1, 1);
        }
        return true;
    }

    public static BukkitRunnable endRunnable = new BukkitRunnable() {
        @Override
        public void run() {
            if (core.isRunning) {
                if (core.currLocation != null) {
                    core.currLocation.getBlock().setType(Material.AIR);
                }

                if (core.sbd) {
                    ScoreboardManager.updateBoards.cancel();
                    core.boardMap.forEach((uuid, fastBoard) -> {
                        fastBoard.delete();
                    });
                    core.boardMap.clear();
                }

                String name;
                if (core.holder == null) name = ChatColor.RED + Lang.translate("noOne");
                else {
                    name = core.holder.getName();
                }

                if (core.timeHeld.containsKey(core.holder.getUniqueId())) {
                    long time = System.currentTimeMillis() - core.timeTake;
                    time = time + core.timeHeld.get(core.holder.getUniqueId());
                    core.timeHeld.replace(core.holder.getUniqueId(), Math.toIntExact(time));
                } else {
                    long time = System.currentTimeMillis() - core.timeTake;
                    core.timeHeld.put(core.holder.getUniqueId(), Math.toIntExact(time));
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
                if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("ended"));
                if (core.eventMsg) Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("winner", name));

                if (core.rewardTime && core.eventMsg)
                    Bukkit.broadcastMessage(core.prefix() + ChatColor.GOLD + Lang.translate("winnerTime", RewardsManager.longestHolder().getName(), Lang.translate("name"), Util.toDigiTime(core.timeHeld.get(RewardsManager.longestHolder().getUniqueId())/1000)));

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
}
