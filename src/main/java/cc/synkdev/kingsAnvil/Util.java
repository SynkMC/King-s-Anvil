package cc.synkdev.kingsAnvil;

import cc.synkdev.kingsAnvil.manager.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Util {
    private static final KingsAnvil core = KingsAnvil.getInstance();
    public static Location locFromString(String s) {
        String[] strings = s.split(";");
        return new Location(Bukkit.getWorld(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
    }
    public static String locToString(Location l) {
        return l.getWorld().getName()+";"+l.getBlockX()+";"+l.getBlockY()+";"+l.getBlockZ();
    }
    public static String locToStringReadable(Location l) {
        StringBuilder sb = new StringBuilder();
        if (core.coordsWorld) sb.append(l.getWorld().getName()).append(", ");

        sb.append(l.getBlockX()+", "+l.getBlockY()+", "+l.getBlockZ());
        return sb.toString();
    }
    public static void sendLocsList(CommandSender p) {
        p.sendMessage(core.prefix()+ ChatColor.GOLD+ Lang.translate("locsList")+":");

        TextComponent hover = new TextComponent(Lang.translate("clickTp"));
        hover.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        TextComponent[] hoverS = { hover };

        core.locations.forEach((uuid, l) -> {
            TextComponent comp = new TextComponent(l.getWorld().getName()+", "+l.getBlockX()+", "+l.getBlockY()+", "+l.getBlockZ()+" ");
            comp.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            comp.setBold(true);
            comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverS));
            comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ka locations tp "+uuid.toString()));

            TextComponent del = new TextComponent("[-]");
            del.setColor(net.md_5.bungee.api.ChatColor.RED);
            del.setBold(true);
            del.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ka locations remove "+ uuid));

            comp.addExtra(del);

            p.spigot().sendMessage(comp);
        });

        TextComponent hover1 = new TextComponent(Lang.translate("addNewLore"));
        hover1.setColor(net.md_5.bungee.api.ChatColor.GREEN);

        TextComponent[] hovers1 = { hover1 };

        TextComponent add = new TextComponent("["+Lang.translate("addNew")+"]");
        add.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        add.setBold(true);
        add.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hovers1));
        add.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ka locations add"));
        p.spigot().sendMessage(add);
    }
    public static String toDigiTime(int secs) {
        int mins = secs;
        secs = secs%60;
        mins = (mins-secs)/60;
        int hours = mins;
        mins = mins%60;
        hours=(hours-mins)/60;
        String s = "";
        if (hours != 0) s = hours+":";

        String sMins = mins+"";
        String sSecs = secs+"";
        if (mins<10) sMins = "0"+mins;
        if (secs<10) sSecs = "0"+secs;
        return s+sMins+":"+sSecs;
    }

    public static Boolean comparePlayers(OfflinePlayer p1, OfflinePlayer p2) {
        return p1.getUniqueId().toString().equals(p2.getUniqueId().toString());
    }
    public static Boolean compareBlockLocs(Location l1, Location l2) {
        if (l1 == null || l2 == null) return false;

        return l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
    }
    public static int random(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
