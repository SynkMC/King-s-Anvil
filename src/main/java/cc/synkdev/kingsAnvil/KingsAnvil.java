package cc.synkdev.kingsAnvil;

import cc.synkdev.kingsAnvil.commands.MainCmd;
import cc.synkdev.kingsAnvil.compatibility.PAPIExpa;
import cc.synkdev.kingsAnvil.manager.EventListener;
import cc.synkdev.kingsAnvil.manager.*;
import cc.synkdev.kingsAnvil.objects.Reward;
import cc.synkdev.kingsAnvil.objects.LeaderboardLine;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import cc.synkdev.synkLibs.components.SynkPlugin;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class KingsAnvil extends JavaPlugin implements SynkPlugin {
    @Getter private static KingsAnvil instance;
    private File configFile = new File(this.getDataFolder(), "config.yml");
    @Getter private FileConfiguration config;
    public Map<String, String> langMap = new HashMap<>();
    public Map<UUID, Location> locations = new HashMap<>();
    public Map<UUID, Integer> timeHeld = new HashMap<>();
    public List<Reward> rewards = new ArrayList<>();

    public Location currLocation = null;
    public long timeTake;
    public Boolean isRunning = false;
    public Player holder = null;
    public int endTime = 0;
    public ArmorStand display;
    public ArmorStand timer;

    public List<String> scoreboardLines = new ArrayList<>();
    public Map<UUID, FastBoard> boardMap = new HashMap<>();

    public int loopDelay;
    public Boolean coordsWorld;
    public int duration;
    public int minOnline;
    public Boolean sbd;
    public Boolean displayAn;
    public Boolean rewardTime;
    public int maxRewards;
    public Boolean eventMsg;
    public Boolean rollDebug;

    public List<LeaderboardLine> timeLeaderboard = new ArrayList<>();
    public List<LeaderboardLine> winLeaderboard = new ArrayList<>();

    public Map<Long, Runnable> scheduler = new HashMap<>();

    private Boolean isCrashing = true;

    @Getter private Economy econ;

    @Override
    public void onEnable() {
        instance = this;

        Lang.init();

        new Metrics(this, 23133);

        SynkLibs.setSpl(this);
        Utils.checkUpdate(this, this);

        dlConfig();
        loadConfig();

        LocationsFileManager.load();

        ScoreboardManager.read();

        RewardsManager.load();

        LeaderboardsManager.load();
        LeaderboardsManager.sort();

        if (!setupEconomy()) {
            Utils.log("Vault dependency wasn't found! You can't use MONEY rewards!");
            return;
        }

        getCommand("ka").setTabCompleter(new MainCmd());
        getCommand("ka").setExecutor(new MainCmd());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        gameLoop.runTaskTimer(this, 20L *loopDelay, 20L *loopDelay);
        saveLoop.runTaskTimer(this, 1200, 1200);
        schedlr.runTaskTimer(this, 10L, 10L);

        if (sbd) {
            new PAPIExpa().register();
        }

        isCrashing = false;
    }

    private void dlConfig() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdirs();
        try {
            if (!configFile.exists()) configFile.createNewFile();
            config = Utils.loadWebConfig("https://synkdev.cc/storage/config-ka.php", configFile);
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BukkitRunnable gameLoop = new BukkitRunnable() {
        @Override
        public void run() {
            MiniGameManager.start();
        }
    };
    public BukkitRunnable saveLoop = new BukkitRunnable() {
        @Override
        public void run() {
            RewardsManager.write();
            LocationsFileManager.save();
            LeaderboardsManager.save();
        }
    };

    public void loadConfig() {
        reloadConfig();
        loopDelay = config.getInt("loop-delay");
        duration = config.getInt("event-duration");
        coordsWorld = config.getBoolean("coords-world");
        minOnline = config.getInt("min-online");
        sbd = config.getBoolean("enable-scoreboard");
        displayAn = config.getBoolean("anvil-display");
        rewardTime = config.getBoolean("reward-time");
        maxRewards = config.getInt("max-rewards");
        eventMsg = config.getBoolean("event-type-messages");
        rollDebug = config.getBoolean("roll-debug");

        if (sbd) {
            if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                Utils.log(ChatColor.RED+"You need to have PlaceholderAPI installed on your server, or disable the scoreboard in config!");
                sbd = false;
            }
        }
    }

    @Override
    public void onDisable() {
        if (!isCrashing) {
            LocationsFileManager.save();
        }

        if (display != null) display.setHealth(0);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    private BukkitRunnable schedlr = new BukkitRunnable() {
        @Override
        public void run() {
            List<Runnable> run = new ArrayList<>();
            Iterator<Map.Entry<Long, Runnable>> iterator = scheduler.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Runnable> entry = iterator.next();
                if (entry.getKey() <= System.currentTimeMillis()) {
                    run.add(entry.getValue());
                    iterator.remove();
                }
            }

            for (Runnable r : run) {
                r.run();
            }
        }
    };

    @Override
    public String name() {
        return "King's Anvil";
    }

    @Override
    public String ver() {
        return "1.0";
    }

    @Override
    public String dlLink() {
        return "https://modrinth.com/plugin/kings-anvil";
    }

    @Override
    public String prefix() {
        return ChatColor.translateAlternateColorCodes('&', "&r&8[&6"+Lang.translate("name")+"&8] » &r");
    }
}
