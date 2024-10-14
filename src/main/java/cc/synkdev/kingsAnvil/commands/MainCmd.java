package cc.synkdev.kingsAnvil.commands;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.kingsAnvil.manager.*;
import cc.synkdev.synkLibs.bukkit.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainCmd implements CommandExecutor, TabCompleter {
    final KingsAnvil core = KingsAnvil.getInstance();
    CommandSender sender;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        this.sender = sender;
        if (args.length == 1) {
            switch (args[0]) {
                case "locations":
                    if (checkPerm("kingsanvil.manage.locations", true)) Util.sendLocsList(sender);
                    break;
                case "start":
                    if (checkPerm("kingsanvil.command.start", true)) MiniGameManager.start();
                    break;
                case "reload":
                    if (checkPerm("kingsanvil.command.reload", true)) {
                        core.loadConfig();
                        ScoreboardManager.read();
                        core.langMap.clear();
                        core.langMap.putAll(Lang.init(core, new File(core.getDataFolder(), "lang.json")));
                        LocationsFileManager.load();
                        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("success", core));
                    }
                    break;
                case "save":
                    if (checkPerm("kingsanvil.command.save", true)) {
                        core.saveLoop.run();
                        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("saved", core));
                    }
                    break;
                case "stop":
                    if (checkPerm("kingsanvil.command.stop", true)) {
                        if (!core.isRunning) {
                            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("notRunning", core));
                            return true;
                        }

                        if (core.scheduler.containsValue(MiniGameManager.endRunnable)) {
                            List<Long> remove = new ArrayList<>();
                            core.scheduler.forEach((aLong, runnable) -> {
                                if (runnable == MiniGameManager.endRunnable) remove.add(aLong);
                            });
                            for (Long l : remove) {
                                core.scheduler.remove(l);
                            }
                        }
                        MiniGameManager.endRunnable.run();
                    }
                    break;
                case "leaderboard":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        LeaderboardsManager.send(p, "wins");
                    } else sender.sendMessage(core.prefix()+ ChatColor.RED+ Lang.translate("playerOnly", core));
                    break;
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "locations":
                    if (args[1].equalsIgnoreCase("add")) {
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            if (checkPerm("kingsanvil.manage.locations", true)) {
                                if (!core.locations.containsValue(p.getLocation())) {
                                    core.locations.put(UUID.randomUUID(), p.getLocation());
                                    p.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successAdd", core));
                                } else p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("alreadyAdded", core));
                            }
                        } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly", core));
                    }
                    break;
                case "leaderboard":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        LeaderboardsManager.send(p, args[1]);
                    } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly", core));
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0]) {
                case "locations":
                    if (args[1].equalsIgnoreCase("remove")) {
                        if (checkPerm("kingsanvil.manage.locations", true)) {
                            UUID uuid;
                            try {
                                uuid = UUID.fromString(args[2]);
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("incorrUuid", core));
                                return true;
                            }
                            if (core.locations.containsKey(uuid)) {
                                core.locations.remove(uuid);
                                sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("success", core));
                            } else {
                                sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist", core));
                            }
                        }
                    }
                case "tp":
                    if (args[1].equalsIgnoreCase("tp")) {
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            if (checkPerm("kingsanvil.manage.locations", true)) {
                                UUID uuid;
                                try {
                                    uuid = UUID.fromString(args[2]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("incorrUuid", core));
                                    return true;
                                }
                                if (core.locations.containsKey(uuid)) {
                                    p.teleport(core.locations.get(uuid));
                                    sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("success", core));
                                } else {
                                    sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist", core));
                                }
                            }
                        } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly", core));
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        this.sender = sender;
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (checkPerm("kingsanvil.manage.locations", false)) list.add("locations");
            if (checkPerm("kingsanvil.command.start", false)) list.add("start");
            if (checkPerm("kingsanvil.command.reload", false)) list.add("reload");
            if (checkPerm("kingsanvil.command.save", false)) list.add("save");
            if (checkPerm("kingsanvil.command.stop", false)) list.add("stop");
            list.add("leaderboard");
        }
        else if (args.length == 2) {
            if (checkPerm("kingsanvil.manage.locations", false) && args[0].equalsIgnoreCase("locations")) list.add("tp");

            if (args[0].equalsIgnoreCase("leaderboard")) {
                list.add("wins");
                list.add("time");
            }
        }
        return list;
    }
    public Boolean checkPerm(String s, Boolean msg) {
        Boolean bool = sender.hasPermission(s);
        if (msg && !bool) sender.sendMessage(Lang.translate("noPerm", core));

        return bool;
    }
}
