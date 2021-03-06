package lowbrain.core.rpg;

import lowbrain.core.commun.Helper;
import lowbrain.core.events.CoreListener;
import lowbrain.core.main.LowbrainCore;
import lowbrain.library.FunctionType;
import lowbrain.library.fn;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * represents a single lowbrain skill
 */
public class LowbrainSkill {

    private String name;
    private int maxLevel;
    private int baseCooldown;
    private double baseManaCost;
    private int baseSkillpointsCost;
    private int skillpointsOperation;
    private int manaCostOperation;
    private int cooldownOperation;
    private int requirementsOperation;
    private double skillpointsOperationValue;
    private double manaCostOperationValue;
    private double cooldownOperationValue;
    private double requirementsOperationValue;
    private HashMap<String,Integer> baseRequirements;
    private boolean enable;
    private Calendar lastExecuted;
    private int currentLevel;
    private String eventType;
    private HashMap<String,String> effects;
    private int angle;
    private FunctionType functionType;
    private String description;

    public String info(){
        String s = "";
        s += "-------------------------" + "\n";
        s += "Name : " + getName() + "\n";
        s += "Skillpoints cost : " + getSkillpointsCost() + "\n";
        s += "Mana cost : " + getManaCost() + "\n";
        s += "Cooldown : " + getCoolDown() + "\n";
        s += "Max level : " + getMaxLevel() + "\n";
        s += "Current level : " + getCurrentLevel() + "\n";
        s += "Description : " + getDescription() + "\n";
        s += "Next upgrade requirements : ";

        for (Map.Entry<String, Integer> r :
                getBaseRequirements().entrySet()) {
            s += r.getKey() + " ";
            s += getRequirement(r.getKey());
            s += " ";
        }
        s += "-------------------------" + "\n";
        return s;
    }

    public String toString(){
        String s = "";
        s += "-------------------------" + "\n";
        s += "Name : " + getName() + "\n";
        s += "Skillpoints cost : " + getBaseSkillpointsCost() + "\n";
        s += "Mana cost : " + getBaseManaCost() + "\n";
        s += "Cooldown : " + getBaseCooldown() + "\n";
        s += "Max level : " + getMaxLevel() + "\n";
        s += "Description : " + getDescription() + "\n";
        s += "Requirements : ";

        for (Map.Entry<String, Integer> r :
                getBaseRequirements().entrySet()) {
            s += r.getKey() + " ";
            s += r.getValue();
            s += " ";
        }
        s += "-------------------------" + "\n";
        return s;
    }

    public LowbrainSkill(String n, int lvl){
        this.name = n;
        this.lastExecuted = Calendar.getInstance();
        initialize();
        this.currentLevel = lvl > this.maxLevel ? this.maxLevel : lvl;
    }

    public LowbrainSkill(String n){
        this.name = n;
        this.lastExecuted = Calendar.getInstance();
        initialize();
        this.currentLevel = 0;
    }

    private void initialize(){
        ConfigurationSection sec = LowbrainCore.getInstance().getConfigHandler().skills().getConfigurationSection(this.name);
        if(sec == null) {
            LowbrainCore.getInstance().warn("Could not find configuration section for skill => " + this.name);
            enable = false;
            return;
        }
        enable = true;
        this.description = sec.getString("description");
        this.enable = sec.getBoolean("enable");
        this.angle = sec.getInt("angle");
        this.functionType = FunctionType.get(sec.getInt("effects_function_type",-1));
        this.maxLevel = sec.getInt("max_level");
        this.baseCooldown = sec.getInt("base_cooldown");
        this.baseManaCost = sec.getDouble("base_mana_cost",0);
        this.baseSkillpointsCost = sec.getInt("base_skillpoints_cost");
        this.eventType = sec.getString("event_type");
        this.skillpointsOperation = sec.getInt("base_skillpoints_operation");
        this.manaCostOperation = sec.getInt("mana_cost_operation");
        this.cooldownOperation = sec.getInt("cooldown_operation");
        this.requirementsOperation = sec.getInt("requirements_operation");
        this.skillpointsOperationValue = sec.getInt("base_skillpoints_operation_value");
        this.manaCostOperationValue = sec.getInt("mana_cost_operation_value");
        this.cooldownOperationValue = sec.getInt("cooldown_operation_value");
        this.requirementsOperationValue = sec.getInt("requirements_operation_value");

        this.baseRequirements = new HashMap<>();
        this.effects = new HashMap<>();

        ConfigurationSection requirementsSection = sec.getConfigurationSection("base_requirements");

        if(requirementsSection != null)
            for (String key : requirementsSection.getKeys(false))
                this.baseRequirements.put(key,requirementsSection.getInt(key));



        ConfigurationSection effectsSection = sec.getConfigurationSection("effects");

        if(effectsSection != null)
            for (String key : effectsSection.getKeys(false))
                this.effects.put(key,effectsSection.getString(key));

    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getBaseCooldown() {
        return baseCooldown;
    }

    public int getCoolDown(){
        return operation(this.baseCooldown,this.cooldownOperationValue,this.cooldownOperation);
    }

    public double getManaCost(){
        return operation(this.baseManaCost,this.manaCostOperationValue,this.manaCostOperation);
    }

    public int getSkillpointsCost(){
        return operation(this.baseSkillpointsCost,this.skillpointsOperationValue,this.skillpointsOperation);
    }

    public double getBaseManaCost() {
        return baseManaCost;
    }

    public int getBaseSkillpointsCost() {
        return baseSkillpointsCost;
    }

    public HashMap<String, Integer> getBaseRequirements() {
        return baseRequirements;
    }

    public int getRequirement(String n){
        return operation(this.baseRequirements.get(n),this.requirementsOperationValue,this.requirementsOperation);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean executeBowSkill(LowbrainPlayer p, Arrow ar, double speed) {
        try {
            boolean succeed = false;

            if(p == null || ar == null )
                return succeed;

            if(!canExecute(p))
                return succeed;

            switch (this.name) {
                case "spread":
                    int[] angles = new int[currentLevel * 2];
                    for (int i = 0; i < angles.length / 2; i++) {
                        angles[i] = (i + 1) * angle;
                        angles[i + (angles.length / 2)] = (i + 1) * -(angle);
                    }

                    for (int angle : angles) {
                        Vector vec;
                        vec = fn.rotateYAxis(p.getPlayer().getLocation().getDirection().clone().normalize(), angle);
                        Arrow marrow = p.getPlayer().launchProjectile(Arrow.class,vec.clone().multiply(ar.getVelocity().length()));
                        applyEffectToArrow(marrow);
                        marrow.setShooter(p.getPlayer());
                        marrow.setBounce(false);
                        marrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    }
                    succeed = true;
                    break;
                case "barrage":
                    new BukkitRunnable() {
                        int counts = 1;
                        @Override
                        public void run() {
                            if (counts > currentLevel)
                                cancel();

                            launchArrow(ar, p);
                            counts++;
                        }
                    }.runTaskTimer(CoreListener.plugin, 4L, 4L);
                    succeed = true;
                    break;
                case "burst":
                    new BukkitRunnable() {
                        int counts = 1;
                        @Override
                        public void run() {
                            if (counts > currentLevel)
                                cancel();

                            launchArrow(ar, p);
                            counts++;
                        }
                    }.runTaskTimer(CoreListener.plugin, 2L, 2L);
                    succeed = true;
                    break;
                default:
                    applyEffectToArrow(ar);
                    ar.setCustomName(this.name);
                    succeed = true;
                    break;
            }

            if(succeed){
                this.setLastExecuted(Calendar.getInstance());
                p.setCurrentMana(p.getCurrentMana() - getBaseManaCost());
                CoreListener.plugin.debugInfo(this.name);
                p.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("skilled_attack_succesfull"));
            }

            return succeed;
        }catch (Exception e){
            return false;
        }
    }

    private void launchArrow(Arrow ar, LowbrainPlayer p) {
        Arrow arrow = p.getPlayer().launchProjectile(Arrow.class,p.getPlayer().getLocation().getDirection().clone().normalize().multiply(ar.getVelocity().length()));
        applyEffectToArrow(arrow);
        arrow.setShooter(p.getPlayer());
        arrow.setBounce(false);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        p.getPlayer().getWorld().playEffect(p.getPlayer().getLocation(), Effect.BOW_FIRE, 1, 0);
    }

    private void applyEffectToArrow(Arrow arrow){
        String speed = this.getEffects().getOrDefault("speed",null);
        if(!fn.StringIsNullOrEmpty(speed)){
            arrow.setVelocity(arrow.getVelocity().normalize().multiply(getEffectValue(speed)));
        }
        int gravity = fn.toInteger(this.getEffects().getOrDefault("gravity","1"),1);
        arrow.setGravity(gravity <= 0 ? false : true);
    }

    public double getEffectValue(String effect){
        if(fn.StringIsNullOrEmpty(effect))return 0F;

        String[] tmp = effect.split(",");
        double min = 0F;
        double max = 0F;

        min = tmp.length > 0 ? fn.toDouble(tmp[0], 0F) : 0F;
        max = tmp.length > 1 ? fn.toDouble(tmp[1],0F) : min * this.getMaxLevel();

        return Helper.Slope(max,min,this.getMaxLevel(),this.getFunctionType()) * this.getCurrentLevel() + min;
    }

    public boolean executeWeaponAttackSkill(LowbrainPlayer p, LivingEntity to, double damage){

        try {
            if(p == null || to == null)
                return false;

            if(!canExecute(p))
                return false;

            for (Map.Entry<String, String> effect :
                    this.getEffects().entrySet()) {

                PotionEffect po = null;

                switch (effect.getKey()){
                    case "poison":
                        po = new PotionEffect(PotionEffectType.WITHER,(int)(getEffectValue(effect.getValue()) *20),this.getCurrentLevel(),true,true);
                        break;
                    case "fire_tick":
                        to.setFireTicks((int)(getEffectValue(effect.getValue()) * 20));
                        break;
                    case "freeze":
                        po = new PotionEffect(PotionEffectType.SLOW,(int)(getEffectValue(effect.getValue()) *20),this.getCurrentLevel(),true,true);
                        break;
                    case "absorb":
                        double pc = getEffectValue(effect.getValue());
                        double absorb = damage * pc;
                        double newHealth = p.getPlayer().getHealth() + absorb;
                        newHealth = newHealth > p.getMaxHealth() ? p.getMaxHealth() : newHealth;
                        p.getPlayer().setHealth(newHealth);
                        p.sendMessage(absorb + "HP absorbed !");
                        break;
                    case "knockback":
                        to.setVelocity(to.getEyeLocation().getDirection().multiply(-1 * getEffectValue(effect.getValue())));
                        break;
                    case "lightning":
                        to.getWorld().strikeLightningEffect(to.getLocation());
                        break;
                    case "damage":
                        to.damage(getEffectValue(effect.getValue()));
                        break;
                }

                if(po != null)
                    po.apply(to);

            }

            this.setLastExecuted(Calendar.getInstance());
            p.setCurrentMana(p.getCurrentMana() - getBaseManaCost());
            CoreListener.plugin.debugInfo(this.name);
            p.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("skilled_attack_successful", this.name));

            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean canExecute(LowbrainPlayer p){
        if(p.getPlayer().isSneaking()){

            if(currentLevel == 0)
                return false;

            Calendar cooldownTime = Calendar.getInstance();
            cooldownTime.add(Calendar.SECOND,-getCoolDown());

            if(this.getLastExecuted().before(cooldownTime)){
                if(p.getCurrentMana() < this.getManaCost()){
                    p.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("insufficient_mana"));
                    return false;
                } else {
                    return true;
                }
            }
            else{
                int rest = (int)((getLastExecuted().getTimeInMillis() - cooldownTime.getTimeInMillis()) / 1000);
                p.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("skill_in_cooldown", rest));
                return false;
            }
        }
        else{
            return false;
        }
    }

    public Calendar getLastExecuted() {
        return lastExecuted;
    }

    public void setLastExecuted(Calendar cal) {
        this.lastExecuted = cal;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int lvl){
        this.currentLevel = lvl > this.maxLevel ? this.maxLevel : lvl;
    }

    public void addLevel(int lvl) {
        this.currentLevel += lvl;
    }

    public int operation(int baseValue, double opValue,int operation){
        int returnValue = baseValue;
        switch (operation){
            case 1:
                returnValue = (int)(baseValue * Math.pow(baseValue,getCurrentLevel() - 1));
                break;
            case 2:
                returnValue = (int)(baseValue + ( (baseValue * opValue) * ( getCurrentLevel()-1 )));
                break;
            default:
                returnValue = (int)(baseValue + opValue * (getCurrentLevel() - 1));
                break;
        }
        return returnValue;
    }

    public double operation(double baseValue, double opValue,int operation){
        double returnValue = baseValue;
        switch (operation){
            case 1:
                returnValue = baseValue * Math.pow(baseValue,getCurrentLevel() - 1);
                break;
            case 2:
                returnValue = baseValue + ( (baseValue * baseValue) * ( getCurrentLevel()-1 ));
                break;
            default:
                returnValue = baseValue + opValue * ( getCurrentLevel() - 1 );
                break;
        }
        return returnValue;
    }

    public int getSkillpointsOperation() {
        return skillpointsOperation;
    }

    public int getManaCostOperation() {
        return manaCostOperation;
    }

    public int getCooldownOperation() {
        return cooldownOperation;
    }

    public int getRequirementsOperation() {
        return requirementsOperation;
    }

    public double getSkillpointsOperationValue() {
        return skillpointsOperationValue;
    }

    public double getManaCostOperationValue() {
        return manaCostOperationValue;
    }

    public double getCooldownOperationValue() {
        return cooldownOperationValue;
    }

    public double getRequirementsOperationValue() {
        return requirementsOperationValue;
    }

    public String getEventType() {
        return eventType;
    }

    public HashMap<String, String> getEffects() {
        return effects;
    }

    public int getAngle() {
        return angle;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public String getDescription() {
        return description;
    }
}
