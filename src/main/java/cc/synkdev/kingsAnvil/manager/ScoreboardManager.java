package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.synkLibs.bukkit.Lang;
import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {
    private static final KingsAnvil core = KingsAnvil.getInstance();
    private static final File file = new File(core.getDataFolder(), "scoreboard.yml");
    public static void read() {

        try {
            if (!file.exists()) {
                file.createNewFile();
                URL url = new URL("https://synkdev.cc/storage/scoreboard-ka.php");
                BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()));
                BufferedWriter write = new BufferedWriter(new FileWriter(file));

                String line;
                while ((line = read.readLine()) != null) {
                    String[] split = line.split("<br>");
                    for (String s : split) {
                        write.write(s);
                        write.newLine();

                        if (s.length() == 0 || s.charAt(0) != '#') core.scoreboardLines.add(ChatColor.translateAlternateColorCodes('&', s));
                    }
                }
                read.close();
                write.close();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("") || line.charAt(0) != '#') core.scoreboardLines.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void createBoard(Player p) {
        if (core.isRunning && core.sbd) {
            List<String> list = new ArrayList<>();
            for (String s : core.scoreboardLines) {
                s = PlaceholderAPI.setPlaceholders(p, s);
                list.add(s);
            }

            FastBoard fB = new FastBoard(p);
            fB.updateTitle(ChatColor.YELLOW+""+ChatColor.BOLD+ Lang.translate("name", core).toUpperCase());
            fB.updateLines(list);

            core.boardMap.put(p.getUniqueId(), fB);
        }
    }

}
