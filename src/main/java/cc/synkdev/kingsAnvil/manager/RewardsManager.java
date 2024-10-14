package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.objects.Reward;
import cc.synkdev.kingsAnvil.objects.RewardType;
import cc.synkdev.synkLibs.bukkit.Lang;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardsManager {
    private static final KingsAnvil core = KingsAnvil.getInstance();
    private static final File file = new File(core.getDataFolder(), "rewards.yml");
    private static final List<String> header = new ArrayList<>(Arrays.asList("# The first value between [square brackets] is the reward type. See the full list in the docs (https://docs.synkdev.cc)",
            "# The second is the percentage of chance that the person holding the anvil when the event finished has to get this specific rewards",
            "# The third is the percentage of chance that the person who held the anvil the longest has to get this specific reward",
            "# Some of that can be configured in the config.yml file.",
            "# If you disabled reward-time in the config, you can leave the last value in square brackets to 0%",
            "# ",
            "# Example: [CONSOLE] give %player% apple 64 [60] [30]",
            "# Here, the last holder has 60% chance to get 64 apples, and the longest holger has 30% chance.",
            "# MONEY type rewards require Vault to be installed on your server."));
    public static void load() {
        try {
            if (!file.exists()) file.createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    Pattern pattern = Pattern.compile("\\[(.*?)\\]\\s+(.*?)\\s+\\[(.*?)\\]\\s+\\[(.*?)\\]");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.matches()) {
                        core.rewards.add(new Reward(RewardType.valueOf(matcher.group(1)), matcher.group(2), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4))));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void giveRewards() {
        if (core.rewards.isEmpty()) {
            SynkLibs.setSpl(core);
            Utils.log(ChatColor.RED + Lang.translate("noRewards", core));
            return;
        }

        if (core.holder == null) {
            Bukkit.broadcastMessage(core.prefix() + ChatColor.RED + Lang.translate("noHolder", core));
            return;
        }

        int maxRewards = core.maxRewards;
        if (core.rewards.size()<maxRewards) maxRewards = core.rewards.size();

        debug("Rolling rewards for "+core.holder.getName());
        List<Reward> selectedRewards = getUniqueRewards(maxRewards);

        for (Reward r : selectedRewards) {
            Boolean bool = r.roll();
            debug("Rolling a reward ("+r.getChance()+"%). Resulted in "+bool);

            if (bool) {
                r.give(core.holder);
            }
        }

        if (core.rewardTime && !Util.comparePlayers(core.holder, longestHolder())) {
            debug("Rolling rewards for "+longestHolder().getName());
            selectedRewards = getUniqueRewards(maxRewards);

            for (Reward r : selectedRewards) {
                Boolean bool = r.roll1();
                debug("Rolling a reward ("+r.getChance1()+"%). Resulted in "+bool);
                if (bool) {
                    r.give(longestHolder());
                }
            }
        } else if (core.rewardTime) {
            SynkLibs.setSpl(core);
            Utils.log(ChatColor.RED + "The winner is also the person who had the anvil for the longest time, so rewards were only given once.");
        }
    }

    private static List<Reward> getUniqueRewards(int maxRewards) {
        List<Reward> uniqueRewards = new ArrayList<>();
        Collections.shuffle(core.rewards);

        for (int i = 0; i < maxRewards; i++) {
            debug("Added a "+core.rewards.get(i).getRewardType().name()+ " reward to the possible drop list.");
            uniqueRewards.add(core.rewards.get(i));
        }
        return uniqueRewards;
    }
    public static Player longestHolder() {
        if (!core.rewardTime) return null;

        if (core.timeHeld.isEmpty()) return null;

        List<Integer> list = new ArrayList<>(core.timeHeld.values());
        Collections.sort(list);

        Player[] p = {null};
        core.timeHeld.forEach((uuid, integer) -> {
            if (integer.equals(list.get(list.size() - 1))) p[0] = Bukkit.getOfflinePlayer(uuid).getPlayer();
        });
        return p[0];
    }
    private static void debug(String s) {
        if (core.rollDebug) {
            Utils.log(ChatColor.GREEN+s);
        }
    }
}
