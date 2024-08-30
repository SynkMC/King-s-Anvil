package cc.synkdev.kingsAnvil.manager;

import cc.synkdev.kingsAnvil.KingsAnvil;
import cc.synkdev.kingsAnvil.Util;

import java.io.*;
import java.util.UUID;

public class LocationsFileManager {
    static final KingsAnvil core = KingsAnvil.getInstance();
    static final File file = new File(core.getDataFolder(), "locations.yml");
    public static void load() {
        try {
            if (!file.exists()) file.createNewFile();
            core.locations.clear();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) core.locations.put(UUID.fromString(line.split(";")[4]), Util.locFromString(line));
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void save() {
        try {
            File temp = new File(core.getDataFolder(), "temp-locs-"+System.currentTimeMillis()+".yml");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

            core.locations.forEach((uuid, l) -> {
                try {
                    writer.write(Util.locToString(l)+";"+uuid.toString());
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.close();
            temp.renameTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
