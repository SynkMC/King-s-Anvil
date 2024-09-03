package cc.synkdev.kingsAnvil.objects;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.Iterator;
import java.util.List;

@Getter @Setter
public class LeaderboardLine {
    private final KingsAnvil core = KingsAnvil.getInstance();
    private OfflinePlayer player;
    private int value;
    public LeaderboardLine(OfflinePlayer player, int value) {
        this.player = player;
        this.value = value;
    }
    public List<LeaderboardLine> update(int value, List<LeaderboardLine> list) {
        this.setValue(getValue()+value);
        Iterator<LeaderboardLine> iter = list.iterator();
        while (iter.hasNext()) {
            LeaderboardLine lL = iter.next();
            if (Util.comparePlayers(lL.getPlayer(), this.getPlayer())) {
                iter.remove();
                break;
            }
        }
        list.add(this);
        return list;
    }
    public String toString() {
        if (player == null) return null;
        return player.getUniqueId().toString()+";"+value;
    }
}
