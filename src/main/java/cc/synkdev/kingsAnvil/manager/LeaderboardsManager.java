package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class LeaderboardsManager {
    private static final KingsAnvil core = KingsAnvil.getInstance();
    private static final File timeFile = new File(core.getDataFolder(), "leaderboard-time.yml");
    private static final File winsFile = new File(core.getDataFolder(), "leaderboard-wins.yml");
    public static Boolean contains(List<LeaderboardLine> list, OfflinePlayer p) {
        for (LeaderboardLine l : list) {
            if (Util.comparePlayers(l.getPlayer(), p)) return true;
        }
        return false;
    }
    public static void sort() {
        core.timeLeaderboard.sort(Comparator.comparingInt(LeaderboardLine::getValue));
        reverseList(core.timeLeaderboard);

        core.winLeaderboard.sort(Comparator.comparingInt(LeaderboardLine::getValue));
        reverseList(core.winLeaderboard);
    }
    public static void load() {
        try {
            if (!timeFile.exists()) timeFile.createNewFile();
            if (!winsFile.exists()) winsFile.createNewFile();

            readFile(timeFile, core.timeLeaderboard);
            readFile(winsFile, core.winLeaderboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void save() {
        try {
            File temp = new File(core.getDataFolder(), "temp-leaderboard-time-"+System.currentTimeMillis()+".yml");
            temp.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            for (LeaderboardLine lL : core.timeLeaderboard) {
                writer.write(lL.toString());
                writer.newLine();
            }

            temp.renameTo(timeFile);


            temp = new File(core.getDataFolder(), "temp-leaderboard-wins-"+System.currentTimeMillis()+".yml");
            temp.createNewFile();

            writer = new BufferedWriter(new FileWriter(temp));
            for (LeaderboardLine lL : core.winLeaderboard) {
                writer.write(lL.toString());
                writer.newLine();
            }
            for (LeaderboardLine lL : core.timeLeaderboard) {
                writer.write(lL.toString());
                writer.newLine();
            }
            temp.renameTo(winsFile);

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static LeaderboardLine get(List<LeaderboardLine> list, OfflinePlayer p) {
        LeaderboardLine lL = null;
        for (LeaderboardLine l : list) {
            if (Util.comparePlayers(l.getPlayer(), p)) lL = l;
        }
        return lL;
    }
    private static void readFile(File file, List<LeaderboardLine> list) {
        list.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(";");
                list.add(new LeaderboardLine(Bukkit.getOfflinePlayer(UUID.fromString(split[0])), Integer.parseInt(split[1])));
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void send(Player p, String type) {
        if (type.equalsIgnoreCase("time")) {
            p.sendMessage(core.prefix()+ ChatColor.GOLD+Lang.translate("leaderboard"));
            for (int i = 1; i < 11; i++) {
                p.sendMessage(PlaceholderAPI.setPlaceholders(p, "%kingsanvil_leaderboardtime_"+i+"%"));
            }
            p.spigot().sendMessage(switcher("wins"));
        } else if (type.equalsIgnoreCase("wins")) {
            p.sendMessage(core.prefix()+ ChatColor.GOLD+Lang.translate("leaderboard"));
            for (int i = 1; i < 11; i++) {
                p.sendMessage(PlaceholderAPI.setPlaceholders(p, "%kingsanvil_leaderboard_"+i+"%"));
            }
            p.spigot().sendMessage(switcher("time"));
        } else {
            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("incorrectLeaderboard", type));
        }
    }
    private static TextComponent switcher(String type) {
        TextComponent prefix = new TextComponent(core.prefix());

        TextComponent tC = new TextComponent(Lang.translate("leaderboardType"));
        tC.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        tC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ka leaderboard "+type));

        prefix.addExtra(tC);
        return prefix;
    }
    private static void reverseList (List<LeaderboardLine> list) {
        List<LeaderboardLine> temp = new ArrayList<>();
        for (int i = list.size()-1; i > -1; i--) {
            temp.add(list.get(i));
        }
        list.clear();
        list.addAll(temp);
    }
}
