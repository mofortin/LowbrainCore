package lowbrain.mcrpg.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Created by Moofy on 10/08/2016.
 */
public class ItemsRequirements extends absConfig {
    public static FileConfiguration getInstance(){
        if(instance == null){
            file = new File(getDataFolder(),"itemsrequirements.yml");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                saveResource("itemsrequirements.yml", false);
            }

            instance = new YamlConfiguration();

            try {
                instance.load(file);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return instance;
    }


}

