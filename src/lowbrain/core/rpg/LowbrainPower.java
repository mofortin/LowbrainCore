package lowbrain.core.rpg;

import lowbrain.core.commun.Helper;
import lowbrain.core.commun.Multiplier;
import lowbrain.core.config.Internationalization;
import lowbrain.core.config.Powers;
import lowbrain.core.events.CoreListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;

import java.util.Calendar;
import java.util.HashMap;

/**
 * represents a single lowbrain power / spell
 */
public class LowbrainPower {
    private float mana;
    private String name;

    private Multiplier duration;
    private Multiplier amplifier;
    private Multiplier cooldown;
    private float castRange;
    private PotionEffectType potionEffectType;
    private Calendar lastCast;
    private HashMap<String,Integer> requirements;

    /**
     * construct the power object using the name
     * will retrieve information from power.yml
     * @param name power's name
     */
    public LowbrainPower(String name){
        FileConfiguration config = Powers.getInstance();

        this.name = name;
        this.requirements = new HashMap<>();
        this.lastCast = Calendar.getInstance();
        this.mana = (float)config.getDouble(this.name + ".mana",0);
        this.castRange = (float)config.getDouble(this.name + ".cast_range",0);

        ConfigurationSection castSection = config.getConfigurationSection(this.name + ".cast");
        ConfigurationSection amplifierSection;
        ConfigurationSection durationSection;
        ConfigurationSection cooldownSection;

        if (castSection == null)
            throw new Error("Could not find cast configuration section for power => " + this.name);

        amplifierSection = castSection.getConfigurationSection("amplifier");
        durationSection = castSection.getConfigurationSection("duration");
        cooldownSection = castSection.getConfigurationSection("cooldown");

        if (amplifierSection == null || durationSection == null || cooldownSection == null)
            throw new Error("Could not find one of duration, amplifier or cooldown configuration section for power => " + this.name);

        amplifier = new Multiplier(amplifierSection);
        cooldown = new Multiplier(cooldownSection);
        duration = new Multiplier(durationSection);

        this.potionEffectType = LowbrainPower.getPotionEffectTypeFromName(this.name);

        if (this.potionEffectType == null)
            throw new Error("Could not find (or is invalid) potion effect type for power => " + this.name);
    }

    /**
     * return the mana necessary to use the spell
     * @return
     */
    public float getMana() {
        return mana;
    }

    /**
     * return the name of the spell
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * return the cast range of the spell
     * @return cast range
     */
    public float getCastRange() {
        return castRange;
    }

    /**
     * compute cast duration depending on player attributes
     * @param from LowbrainPlayer
     * @return cast duration value
     */
    private int getCastDuration(LowbrainPlayer from){
        return (int)duration.randomize(duration.compute(from));
    }

    /**
     * compute cast amplifier depending on player attribute
     * @param from LowbrainPlayer
     * @return cast amplifier value
     */
    private int getCastAmplifier(LowbrainPlayer from){
        return (int)amplifier.randomize(amplifier.compute(from));
    }

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    @Contract(pure = true)
    public static PotionEffectType getPotionEffectTypeFromName(String name) {
        PotionEffectType pet = null;

        switch (name){
            case"healing":
                pet = PotionEffectType.HEAL;
                break;
            case"fire_resistance":
                pet = PotionEffectType.FIRE_RESISTANCE;
                break;
            case"resistance":
                pet = PotionEffectType.DAMAGE_RESISTANCE;
                break;
            case"water_breathing":
                pet = PotionEffectType.WATER_BREATHING;
                break;
            case"invisibility":
                pet = PotionEffectType.INVISIBILITY;
                break;
            case"regeneration":
                pet = PotionEffectType.REGENERATION;
                break;
            case"jump_boost":
                pet = PotionEffectType.JUMP;
                break;
            case"strength":
                pet = PotionEffectType.INCREASE_DAMAGE;
                break;
            case"haste":
                pet = PotionEffectType.FAST_DIGGING;
                break;
            case"speed":
                pet = PotionEffectType.SPEED;
                break;
            case"night_vision":
                pet = PotionEffectType.NIGHT_VISION;
                break;
            case"health_boost":
                pet = PotionEffectType.HEALTH_BOOST;
                break;
            case"absorption":
                pet = PotionEffectType.ABSORPTION;
                break;
            case"saturation":
                pet = PotionEffectType.SATURATION;
                break;
        }

        return pet;
    }

    /**
     * compute the cooldown depending on player attributes
     * @param from LowbrainPlayer
     * @return cooldown value
     */
    private int getCooldown(LowbrainPlayer from){
        return (int)cooldown.randomize(cooldown.compute(from));
    }

    /**
     * execute the spell
     * @param from from this LowbrainPlayer
     * @param to to this LowbrainPlayer (if set to null, will cast to from/self)
     * @return true if cast succeeded
     */
    public boolean Cast(LowbrainPlayer from, LowbrainPlayer to){
        try{
            if(from == null || to == null)
                return false;

            Calendar cooldowntime = Calendar.getInstance();
            cooldowntime.add(Calendar.SECOND,-getCooldown(from));

            if(this.lastCast.after(cooldowntime)){
                int rest = (int)((lastCast.getTimeInMillis() - cooldowntime.getTimeInMillis()) / 1000);
                from.sendMessage(Internationalization.format("spell_in_cooldown") + " "
                        + Internationalization.format("seconds_left", rest),ChatColor.RED);
                return false;
            }

            if(to != null && this.getCastRange() == 0){
                from.sendMessage(Internationalization.format("cant_cast_this_spell_on_others"),ChatColor.RED);
                return false;
            }
            if(to != null && this.getCastRange() > 0){
                double x = from.getPlayer().getLocation().getX() - to.getPlayer().getLocation().getX();
                double y = from.getPlayer().getLocation().getY() - to.getPlayer().getLocation().getY();
                double z = from.getPlayer().getLocation().getZ() - to.getPlayer().getLocation().getZ();

                double distance = Math.sqrt(x*x + y*y + z*z);

                if(this.getCastRange() < distance){
                    from.sendMessage(Internationalization.format("player_out_of_range", this.getCastRange() + "/" + distance),ChatColor.RED);
                    return false;
                }
            }

            String msg = from.meetRequirementsString(this.requirements);

            if(!Helper.StringIsNullOrEmpty(msg)){
                from.sendMessage(Internationalization.format("spell_requirements_to_high", msg),ChatColor.RED);
                return false;
            }

            if(from.getCurrentMana() < this.getMana()){
                from.sendMessage(Internationalization.format("insufficient_mana") + " " + this.getMana() + "/" + from.getCurrentMana(),ChatColor.RED);
                return false;
            }

            PotionEffect p =  new PotionEffect(this.potionEffectType,this.getCastDuration(from),this.getCastAmplifier(from),true,true);
            p.apply(to.getPlayer());

            from.setCurrentMana(from.getCurrentMana() - this.getMana());
            this.lastCast = Calendar.getInstance();
            CoreListener.plugin.debugInfo(from.getPlayer().getName() + " cast " + this.getName());
            from.sendMessage(Internationalization.format("cast_succesfull"));
            return true;
        }catch (Exception e){
            from.sendMessage(Internationalization.format("cast_failed", this.name), ChatColor.RED);
        }
        return false;
    }

}
