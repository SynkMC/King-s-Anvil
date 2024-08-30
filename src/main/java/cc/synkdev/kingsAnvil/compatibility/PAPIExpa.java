package cc.synkdev.kingsAnvil.compatibility;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.manager.Lang;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPIExpa extends PlaceholderExpansion {
    private final KingsAnvil core = KingsAnvil.getInstance();
    @Override
    public @NotNull String getIdentifier() {
        return "kingsanvil";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Synk";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer oP, @NotNull String args) {
        if (args.equalsIgnoreCase("holder")) {
            if (!core.isRunning || core.holder == null) return Lang.translate("noOne");
            
            if (Util.comparePlayers(oP.getPlayer(), core.holder)) return Lang.translate("you");
            
            return core.holder.getName();
        } else if (args.equalsIgnoreCase("time")) {
            if (core.isRunning) return Util.toDigiTime(core.endTime-Math.toIntExact(System.currentTimeMillis()/1000));
            return "00:00";
        } else if (args.equalsIgnoreCase("location")) {
            if (core.isRunning) {
                if (core.currLocation != null) return Util.locToStringReadable(core.currLocation);

                return Util.locToStringReadable(core.holder.getLocation());
            }

            return "0, 0, 0";
        } else if (args.contains("leaderboard_")) {
            int spot = Integer.parseInt(args.split("_")[1]);
            if (core.winLeaderboard.size() >= spot) {
                LeaderboardLine lL = core.winLeaderboard.get(spot-1);
                return Lang.translate("leaderboardWins", spot+"", lL.getPlayer().getName(), lL.getValue()+"");
            } else return Lang.translate("leaderboardWins", spot+"", Lang.translate("noOne"), 0+"");
        } else if (args.contains("leaderboardtime_")) {
            int spot = Integer.parseInt(args.split("_")[1]);
            if (core.timeLeaderboard.size() >= spot) {
                LeaderboardLine lL = core.timeLeaderboard.get(spot-1);
                return Lang.translate("leaderboardTime", spot+"", lL.getPlayer().getName(), Util.toDigiTime(lL.getValue()/1000));
            } else return Lang.translate("leaderboardTime", spot+"", Lang.translate("noOne"), "00:00");
        }

        return null; //
    }
}
