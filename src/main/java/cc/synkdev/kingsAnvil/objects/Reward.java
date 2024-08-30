package cc.synkdev.kingsAnvil.objects;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter @Setter
public class Reward {
    private final KingsAnvil core = KingsAnvil.getInstance();

    private RewardType rewardType;
    private String arg;
    private int chance;
    private int chance1 = 0;
    public Reward(RewardType rewardType, String arg, int chance, int chance1) {
        this.rewardType = rewardType;
        this.arg = arg;
        this.chance = chance;
        this.chance1 = chance1;
    }
    public Boolean roll() {
        int rdm = Util.random(1, 100);

        return rdm <= chance;
    }
    public Boolean roll1() {
        if (chance1 == 0) return false;

        int rdm = Util.random(1, 100);

        return rdm <= chance1;
    }
    public String toString() {
        return "["+rewardType.arg+"] "+arg+" ["+chance+"] ["+chance1+"]";
    }
    public void give(Player p) {
        switch (rewardType) {
            case XP:
                p.giveExp(Integer.parseInt(arg));
                break;
            case MONEY:
                if (core.getEcon() != null) {
                    core.getEcon().depositPlayer(p, Double.parseDouble(arg));
                } else {
                    SynkLibs.setSpl(core);
                    Utils.log(ChatColor.RED+"You need to have Vault installed to use the MONEY reward type!");
                }
                break;
            case PLAYER:
                p.chat("/"+arg.replace("%player%", p.getName()));
                break;
            case CONSOLE:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), arg.replace("%player%", p.getName()));
                break;
        }
    }
}
