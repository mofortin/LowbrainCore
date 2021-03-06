package lowbrain.core.abstraction;

import lowbrain.core.commun.Settings;
import lowbrain.core.main.LowbrainCore;
import lowbrain.library.fn;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Mooffy on 2017-07-20.
 */
public abstract class Attributable extends Playable {

    protected Attributable(Player player) {
        super(player);
    }

    protected int strength = 0;
    protected int intelligence = 0;
    protected int dexterity = 0;
    protected int vitality = 0;
    protected int defence = 0;
    protected int agility = 0;
    protected int magicResistance = 0;
    protected int points = 0;
    protected int skillPoints = 0;
    protected double experience = 0;
    protected int lvl = 0;
    protected int kills = 0;
    protected int deaths = 0;
    protected int reputation = 0;
    protected int courage = 0;

    public int getCourage() {return courage;}
    public int getReputation(){return reputation;}
    public int getStrength(){return strength;}
    public int getIntelligence(){return intelligence;}
    public int getDexterity(){return dexterity;}
    public int getVitality(){return vitality;}
    public int getDefence(){return defence;}
    public int getAgility(){return agility;}
    public int getPoints() {return points;}
    public int getLvl() {return lvl;}
    public int getKills() {return kills;}
    public int getDeaths() {return deaths;}
    public double getExperience() {return experience;}
    public int getSkillPoints() {return skillPoints;}
    public int getMagicResistance() {return magicResistance;}

    public void setPoints(int n) {this.points = n < 0 ? 0 : n; onAttributeChange();}
    public void setSkillPoints(int n) {this.skillPoints = n < 0 ? 0 : n; onAttributeChange();}
    public void setExperience(double n) {this.experience = n < 0 ? 0 : n; onAttributeChange();}
    public void setLvl(int n) {this.lvl = n < 0 ? 0 : n; onAttributeChange();}
    public void setKills(int n) {this.kills = n < 0 ? 0 : n; onAttributeChange();}
    public void setDeaths(int n) {this.deaths = n < 0 ? 0 : n; onAttributeChange();}

    public void setReputation(int n){this.reputation = n; onAttributeChange();}
    public void setCourage(int n){this.courage = n; onAttributeChange();}

    public void addCourage(int n) {this.courage += n; onAttributeChange();}
    public void addReputation(int n) {this.reputation += n; onAttributeChange();}

    public void addPoints(int n) {points += n; points = points < 0 ? 0 : points; onAttributeChange();}
    public void addLvl(int n) {lvl += n; lvl = lvl < 0 ? 0 : lvl; onAttributeChange();}
    public void addKills(int n) {kills += n; kills = kills < 0 ? 0 : kills; onAttributeChange();}
    public void addDeaths(int n) {deaths += n; deaths = deaths < 0 ? 0 : deaths; onAttributeChange();}
    public void addExperience(double n) {experience += n; experience = experience < 0 ? 0 : experience; onAttributeChange();}
    public void addSkillPoints(int n) {skillPoints += n; skillPoints = skillPoints < 0 ? 0 : skillPoints; onAttributeChange();}
    public void addMagicResistance(int n) {magicResistance += n; magicResistance = magicResistance < 0 ? 0 : magicResistance; onAttributeChange();}

    /**
     * set dexterity of current player
     * @param val
     */
    public void setDexterity(int val) {
        this.dexterity = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.dexterity > Settings.getInstance().getMaxStats())
                this.dexterity = Settings.getInstance().getMaxStats();

        else if(this.dexterity < 0)
            this.dexterity = 0;

        onAttributeChange();
    }

    /**
     * set strength of current player
     * @param val
     */
    public void setStrength(int val) {
        this.strength = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.strength > Settings.getInstance().getMaxStats())
            this.strength = Settings.getInstance().getMaxStats();

        else if(this.strength < 0)
            this.strength = 0;

        onAttributeChange();
    }

    /**
     * set intelligence of current player
     * @param val
     */
    public void setIntelligence(int val) {
        this.intelligence = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.intelligence > Settings.getInstance().getMaxStats())
            this.intelligence = Settings.getInstance().getMaxStats();

        else if(this.intelligence < 0)
            this.intelligence = 0;

        onAttributeChange();
    }

    /**
     * set vitality of current player
     * @param val
     */
    public void setVitality(int val) {
        this.vitality = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.vitality > Settings.getInstance().getMaxStats())
            this.vitality = Settings.getInstance().getMaxStats();

        else if(this.vitality < 0)
            this.vitality = 0;

        onAttributeChange();
    }

    /**
     * set defence of current player
     * @param val
     */
    public void setDefence(int val) {
        this.defence = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.defence > Settings.getInstance().getMaxStats())
            this.defence = Settings.getInstance().getMaxStats();

        else if(this.defence < 0)
            this.defence = 0;

        onAttributeChange();
    }

    /**
     * set agility of current player
     * @param val
     */
    public void setAgility(int val) {
        this.agility = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.agility > Settings.getInstance().getMaxStats())
            this.agility = Settings.getInstance().getMaxStats();

        else if(this.agility < 0)
            this.agility = 0;

        onAttributeChange();
    }

    /**
     * set magic resistance of current player
     * @param val
     */
    public void setMagicResistance(int val) {
        this.dexterity = val;
        if(Settings.getInstance().getMaxStats() >=0
                && this.dexterity > Settings.getInstance().getMaxStats())
            this.dexterity = Settings.getInstance().getMaxStats();

        else if(this.dexterity < 0)
            this.dexterity = 0;

        onAttributeChange();
    }

    /**
     * add agility to the player
     * @param nb
     * @param usePoints
     */
    public void addAgility(int nb, boolean usePoints,boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();

        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.agility == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldAgility = this.agility;
            this.agility += nb;
            if(maxStats >= 0 && this.agility > maxStats){
                this.agility = maxStats;
            }
            else if (this.agility < 0) this.agility = 0;

            double dif = this.agility - oldAgility;
            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"agility", dif}));
        }
        else if(!usePoints){
            this.agility += nb;
            if(maxStats >= 0 && this.agility > maxStats){
                this.agility = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"agility", Settings.getInstance().getMaxStats()}));
            }else{
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"agility", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add strength to current player
     * @param nb
     * @param usePoints
     */
    public void addStrength(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();
        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.strength == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldStrength = this.strength;
            this.strength += nb;
            if(maxStats >= 0 && this.strength > maxStats){
                this.strength = maxStats;
            }
            else if(this.strength < 0){
                this.strength = 0;
            }

            int dif = this.strength - oldStrength;

            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"strength", dif}));
        }
        else if(!usePoints){
            this.strength += nb;
            if(maxStats >= 0 && this.strength > maxStats){
                this.strength = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"strength", Settings.getInstance().getMaxStats()}));
            }else {
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"strength", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add intelligence to current player
     * @param nb
     * @param usePoints
     */
    public void addIntelligence(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();
        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.intelligence == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldIntelligence = this.intelligence;
            this.intelligence += nb;
            if(maxStats >= 0 && this.intelligence > maxStats){
                this.intelligence = maxStats;
            }
            else if(this.intelligence < 0){
                this.intelligence = 0;
            }
            int dif = this.intelligence - oldIntelligence;

            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"intelligence", dif}));

        }
        else if(!usePoints){
            this.intelligence += nb;
            if(maxStats >= 0 && this.intelligence > maxStats){
                this.intelligence = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"intelligence", Settings.getInstance().getMaxStats()}));
            }
            else{
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"intelligence", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add dexterity to current player
     * @param nb
     * @param usePoints
     */
    public void addDexterity(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();
        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.dexterity == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldDexterity = this.dexterity;
            this.dexterity += nb;
            if(maxStats >= 0 && this.dexterity > maxStats){
                this.dexterity = maxStats;
            }
            else if(this.dexterity < 0 ){
                this.dexterity = 0;
            }
            int dif = this.dexterity - oldDexterity;

            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"dexterity", nb}));
        }
        else if(!usePoints){
            this.dexterity += nb;
            if(maxStats >= 0 && this.dexterity > maxStats){
                this.dexterity = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"dexterity", Settings.getInstance().getMaxStats()}));
            }else {
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"dexterity", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add vitality to current player
     * @param nb
     * @param usePoints
     */
    public void addVitality(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();
        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point", null));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.vitality == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldHealth = this.vitality;
            this.vitality += nb;
            if(maxStats >= 0 && this.vitality > maxStats){
                this.vitality = maxStats;
            }
            else if (this.vitality < 0 )this.vitality = 0;

            int dif = this.vitality - oldHealth;

            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"vitality", dif}));
        }
        else if(!usePoints){
            this.vitality += nb;
            if(maxStats >= 0 && this.vitality > maxStats){
                this.vitality = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"vitality", Settings.getInstance().getMaxStats()}));
            }else{
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"vitality", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add defence to current player
     * @param nb
     * @param usePoints
     */
    public void addDefence(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();

        if(nb == 0){
            return;
        } else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        } else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.defence == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        } else if(usePoints && this.points >= nb){
            int oldDefence = this.defence;
            this.defence += nb;
            if(maxStats >= 0 && this.defence > maxStats){
                this.defence = maxStats;
            }
            else if (this.defence < 0 ) this.defence = 0;

            int dif = this.defence - oldDefence;

            this.points -= dif;
            if(callChange) onAttributeChange();
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"defence", dif}));
        } else if(!usePoints){
            this.defence += nb;
            if(maxStats >= 0 && this.defence > maxStats){
                this.defence = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"defence", Settings.getInstance().getMaxStats()}));
            }else{
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"defence", nb}));
            }
            if(callChange) onAttributeChange();
        } else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    /**
     * add magic resistance to current player
     * @param nb
     * @param usePoints
     */
    public void addMagicResistance(int nb, boolean usePoints, boolean callChange){
        int maxStats = Settings.getInstance().getMaxStats();

        if(nb == 0){
            return;
        }
        else if(!Settings.getInstance().isAllowPointDeduction() && nb < 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if (Settings.getInstance().isAllowPointDeduction() && nb < 0 && this.magicResistance == 0){
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_deduct_anymore_point"));
            return;
        }
        else if(usePoints && this.points >= nb){
            int oldMagicResistance = this.defence;
            this.magicResistance += nb;
            if(maxStats >= 0 && this.defence > maxStats){
                this.magicResistance = maxStats;
            }
            else if (this.magicResistance < 0)this.magicResistance = 0;

            int dif = this.magicResistance - oldMagicResistance;
            if(callChange) onAttributeChange();
            this.points -= dif;

            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"magic resistance", dif}));
        }
        else if(!usePoints){
            this.magicResistance += nb;
            if(maxStats >= 0 && this.magicResistance > maxStats){
                this.magicResistance = maxStats;
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_set_to", new Object[]{"magic resistance", Settings.getInstance().getMaxStats()}));
            } else{
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("attribute_incremented_by", new Object[]{"magic resistance", nb}));
            }
            if(callChange) onAttributeChange();
        }
        else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_enough_points"));
            return;
        }
    }

    protected abstract void onAttributeChange();

    /**
     * get the value of an attribute by name
     * @param n the name of the attribute
     * @return getAttribute with 0 as default value
     */
    public int getAttribute(String n){
        return getAttribute(n, 0);
    }

    /**
     * get the value of an attribute by name
     * @param n the name of the attribute
     * @param d default value in cause of not found
     * @return value
     */
    public int getAttribute(String n, int d){
        if(fn.StringIsNullOrEmpty(n))return d;

        switch (n.toLowerCase()){
            case "level":
            case "lvl":
                return this.getLvl();

            case "intelligence":
            case "intel":
                return this.getIntelligence();

            case "agility":
            case "agi":
                return this.getAgility();

            case "vitality":
            case "vit":
                return this.getVitality();

            case "strength":
            case "str":
                return this.getStrength();

            case "dexterity":
            case "dext":
                return this.getDexterity();

            case "defence":
            case "def":
                return this.getDefence();

            case "magic_resistance":
            case "magicresistance":
            case "mr":
                return this.getMagicResistance();
            case "kills":
            case "kill":
                return this.getKills();

            case "deaths":
            case "death":
                return this.getDeaths();

            case "reputation" :
            case "rep" :
                return this.getReputation();

            case "courage":
                return this.getCourage();
        }
        return d;
    }

}
