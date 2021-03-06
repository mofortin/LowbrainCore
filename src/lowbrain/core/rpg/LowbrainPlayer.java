package lowbrain.core.rpg;
import lowbrain.core.abstraction.Attributable;
import lowbrain.core.commun.Settings;
import lowbrain.core.commun.SubParameters.ReputationStatus;
import lowbrain.core.main.LowbrainCore;
import lowbrain.library.fn;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * represents the extended version of the bukkit player
 */
public class LowbrainPlayer extends Attributable {

	private double nextLvl = 0;
	private String className = "";
	private boolean classIsSet = false;

	private double maxMana = 0;
	private double currentMana = 0;
	private BukkitTask manaRegenTask = null;
	private LowbrainClass lowbrainClass = null;
	private LowbrainRace lowbrainRace = null;
	private String raceName = "";
	private boolean raceIsSet = false;
	private boolean showStats = true;
	private HashMap<String,Integer> mobKills;
	private HashMap<String,LowbrainSkill> skills;
	private HashMap<String,LowbrainPower> powers;
	private String currentSkill;
	private Multipliers multipliers;
	private ReputationStatus repStatus;

	private StatsBoard statsBoard;

	//========================================================= CONSTRUCTOR====================================
	/**
	 * construct player with bukkit.Player
	 * will retrieve the player's information using his uuid in
     * the fodder ../data/uuid.yml
	 * @param p Bukkit.Player
	 */
	public LowbrainPlayer(Player p){
		super(p);
		initializePlayer();
	}

	/**
	 * read player config yml to initialize current player
	 */
	private void initializePlayer(){
		if(LowbrainCore.getInstance() == null)
			return;

		File userdata = new File(LowbrainCore.getInstance().getDataFolder(), File.separator + "PlayerDB");
        File f = new File(userdata, File.separator + player.getUniqueId() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);

		this.skills = new HashMap<String,LowbrainSkill>();
		this.mobKills = new HashMap<String,Integer>();
		this.powers = new HashMap<String,LowbrainPower>();

		//When the player file is created for the first time...
		if (!f.exists()) {
			playerData.createSection("class");
			playerData.set("class.is_set", false);
			playerData.set("class.name", "");

			playerData.createSection("race");
			playerData.set("race.is_set", false);
			playerData.set("race.name", "");

			playerData.createSection("stats");
			playerData.set("stats.vitality", 0);
			playerData.set("stats.lvl", 1);
			playerData.set("stats.strength", 0);
			playerData.set("stats.intelligence", 0);
			playerData.set("stats.dexterity", 0);
			playerData.set("stats.defence", 0);
			playerData.set("stats.agility",0);
			playerData.set("stats.magic_resistance", 0);
			playerData.set("stats.points", getSettings().getStartingPoints());
			playerData.set("stats.experience", 0);
			playerData.set("stats.next_lvl", getSettings().getFirstLvlExp());
			playerData.set("stats.kills",0);
			playerData.set("stats.deaths",0);
			playerData.set("stats.current_mana",0);
			playerData.set("stats.skill_points", getSettings().getStartingSkillPoints());
			playerData.set("stats.current_skill","");
			playerData.set("stats.reputation", getSettings().getParameters().getReputation().getInitial());
            playerData.set("stats.courage", getSettings().getParameters().getCourage().getInitial());

			playerData.createSection("mob_kills");

			playerData.createSection("skills");

			for (LowbrainSkill skill : LowbrainCore.getInstance().getSkills().values())
				playerData.set("skills." + skill.getName(),0);

			playerData.createSection("settings");
			playerData.set("settings.show_stats",true);

			try {
				playerData.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		classIsSet = playerData.getBoolean("class.is_set",false);
		className = playerData.getString("class.name","");

		raceIsSet = playerData.getBoolean("race.is_set",false);
		raceName = playerData.getString("race.name","");

        strength = playerData.getInt("stats.strength",0);
        intelligence = playerData.getInt("stats.intelligence",0);
        vitality = playerData.getInt("stats.vitality",0);
        defence = playerData.getInt("stats.defence",0);
        dexterity = playerData.getInt("stats.dexterity",0);
		magicResistance = playerData.getInt("stats.magic_resistance",0);
        experience = playerData.getDouble("stats.experience",0);
        points = playerData.getInt("stats.points",0);
		skillPoints = playerData.getInt("stats.skill_points",0);
        lvl = playerData.getInt("stats.lvl",0);
        nextLvl = playerData.getDouble("stats.next_lvl",0);
		kills = playerData.getInt("stats.kills",0);
		deaths = playerData.getInt("stats.deaths",0);
		currentMana = playerData.getDouble("stats.current_mana",0);
		agility = playerData.getInt("stats.agility",0);
		reputation = playerData.getInt("stats.reputation", 0);
        courage = playerData.getInt("stats.courage", 0);

		ConfigurationSection skillsSection = playerData.getConfigurationSection("skills");

		//in case of new skills added
		this.skills = (HashMap<String, LowbrainSkill>) LowbrainCore.getInstance().getSkills().clone();

		if(skillsSection != null)
			for (String skill : skillsSection.getKeys(false))
				this.skills.put(skill,new LowbrainSkill(skill,skillsSection.getInt(skill)));

		currentSkill = this.skills.containsKey(playerData.getString("stats.current_skill"))
                ? playerData.getString("stats.current_skill")
                : "";

		ConfigurationSection mob = playerData.getConfigurationSection("mob_kills");
		if(mob != null)
			for (String key :mob.getKeys(false))
				this.mobKills.put(key,mob.getInt(key));

		showStats = playerData.getBoolean("settings.show_stats");

		this.lowbrainClass = new LowbrainClass(className);
		this.lowbrainRace = new LowbrainRace(raceName);

		initializePowers();

		start();
	}

    /**
     * initialize player's powers
     */
	private void initializePowers(){
		this.powers = new HashMap<>();
		if(classIsSet && this.lowbrainClass != null )
			for (String powa : this.lowbrainClass.getPowers())
				this.powers.put(powa,new LowbrainPower(powa));

		if(raceIsSet && this.lowbrainRace != null )
			for (String powa : this.lowbrainRace.getPowers())
				this.powers.put(powa,new LowbrainPower(powa));
	}

	private void start(){
	    if (!isPlayable())
	        return;

	    if (statsBoard == null)
            statsBoard = new StatsBoard(this);

        validateEquippedArmor();
        onAttributeChange();
        setDisplayName();
        startManaRegenTask();
	}

	//==========================================================END OF CONSTRUCTOR=============================

	//====================================================== USEFULL ==========================================

	/**
	 * return 0 if equals, -1 if lower or not equals, +1 if higher
	 * @param n name of the attribute
	 * @param v value to compare
     * @return 0 if equals, -1 if lower or not equals, +1 if higher
     */
	public int compareAttributesByName(String n, Object v){
		try {
			int v1 = -1;
			int v2 = -1;

			switch (n.toLowerCase()){
				case "strength":
				case "str":
					v1 = this.getStrength();
					v2 = (int)v;
					break;

				case "intelligence":
				case "intel":
					v1 = this.getIntelligence();
					v2 = (int)v;
					break;

				case "dexterity":
				case "dext":
					v1 = this.getDexterity();
					v2 = (int)v;
					break;

				case "defence":
				case "def":
					v1 = this.getDefence();
					v2 = (int)v;
					break;

				case "agility":
				case "agi":
					v1 = this.getAgility();
					v2 = (int)v;
					break;

				case "magic_resistance":
				case "magicresistance":
				case "mr":
					v1 = this.getMagicResistance();
					v2 = (int)v;
					break;

				case "vitality":
				case "vit":
					v1 = this.getVitality();
					v2 = (int)v;
					break;

				case "level":
				case "lvl":
					v1 = this.getLvl();
					v2 = (int)v;
					break;

				case "class":
					if(this.getLowbrainClass() != null)
						return this.getLowbrainClass().getName().equals(v) ? 0 : -1;
					else
					    return -1;

				case "race":
					if(this.getLowbrainRace() != null)
						return this.getLowbrainRace().getName().equals(v) ? 0 : -1;
					else
					    return -1;

				case "kills":
				case "kill":
					v1 = this.getKills();
					v2 = (int)v;
					break;

				case "deaths":
				case "death":
					v1 = this.getDeaths();
					v2 = (int)v;
					break;

                case "rep":
                case "reputation":
                    if (v instanceof String) {
                        ReputationStatus status = getSettings().getParameters().getReputation().getRepFrom(this.reputation);
                        return status == null || status.getName() != v ? -1 : 0;
                    }
                    v1 = this.getCourage();
                    v2 = (int)v;
                    break;

                case "courage":
                    v1 = this.getCourage();
                    v2 = (int)v;
                    break;

				default:
					return -1;
			}

			return v1 - v2;

		} catch (Exception e){
			e.printStackTrace();
			return -1;
		}

	}

	/***
	 * validate if player can wear equipped armor
	 */
	public void validateEquippedArmor(){
		if(!canEquipItem(this.getPlayer().getInventory().getHelmet())){
			this.getPlayer().getInventory().addItem(this.getPlayer().getInventory().getHelmet());
			this.getPlayer().getInventory().setHelmet(null);
		}
		if(!canEquipItem(this.getPlayer().getInventory().getChestplate())){
			this.getPlayer().getInventory().addItem(this.getPlayer().getInventory().getChestplate());
			this.getPlayer().getInventory().setChestplate(null);
		}
		if(!canEquipItem(this.getPlayer().getInventory().getLeggings())){
			this.getPlayer().getInventory().addItem(this.getPlayer().getInventory().getLeggings());
			this.getPlayer().getInventory().setLeggings(null);
		}
		if(!canEquipItem(this.getPlayer().getInventory().getBoots())){
			this.getPlayer().getInventory().addItem(this.getPlayer().getInventory().getBoots());
			this.getPlayer().getInventory().setBoots(null);
		}
		if(!canEquipItem(this.getPlayer().getInventory().getItemInOffHand())){
			this.getPlayer().getInventory().addItem(this.getPlayer().getInventory().getItemInOffHand());
			this.getPlayer().getInventory().setItemInOffHand(null);
		}
	}

	/**
	 * check if a player can wear a specific set of armor
	 * @param item item being equipped
	 * @return true if can equip
     */
	public boolean canEquipItem(ItemStack item){
		if(item == null)
		    return true;

		String name = "";

        //custom items
		if(item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null)
			name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        //vanilla items
		else
			name = item.getType().name();

		LowbrainCore.ItemRequirements i = LowbrainCore.getInstance().getItemsRequirements().get(name);
		if(i == null)
		    return true;

		return meetRequirements(i.getRequirements());
	}

	/**
	 * check if a player can wear a specific set of armor
	 * @param item item being equipped
	 * @return string with the requirements that failed... empty if they all passed
	 */
	public String canEquipItemString(ItemStack item){
		String msg = "";
		if(item == null) return msg;
		String name = "";

        //custom items
		if(item.getItemMeta().getDisplayName() != null)
			name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
		//vanilla items
		else
			name = item.getType().name();

		LowbrainCore.ItemRequirements i = LowbrainCore.getInstance().getItemsRequirements().get(name);
		if(i == null)
		    return msg;

		return meetRequirementsString(i.getRequirements());
	}

	/**
	 * check if a player passes a list of requirements
	 * @param requirements list of requirements
	 * @return true if passes, false if not
	 */
	public boolean meetRequirements(Map<String,Integer> requirements){
		if(requirements == null)
		    return true;

		for(Map.Entry<String, Integer> r : requirements.entrySet()) {
			String n = r.getKey().toLowerCase();
			int v = r.getValue();
			if(this.compareAttributesByName(n,v) < 0)
				return false;

		}
		return true;
	}

	/**
	 * check if a player passes a list of requirements
	 * @param requirements list of requirements
	 * @return a string of failed requirements, empty string if passes
	 */
	public String meetRequirementsString(Map<String,Integer> requirements){
		String msg = "";
		if(requirements == null)
		    return msg;

		for(Map.Entry<String, Integer> r : requirements.entrySet()) {
			String n = r.getKey().toLowerCase();
			int v = r.getValue();

			if(this.compareAttributesByName(n,v) < 0)
				msg += " " + n + ":" + v;
		}
		return msg;
	}

	/**
	 * cast a spell
	 * @param name name of the spell
	 * @param to LowbrainPlayer to cast the spell to.. if null will cast to himself
     * @return true if the cast has succeeded
     */
	public boolean castSpell(String name, LowbrainPlayer to){
		LowbrainPower powa = this.powers.get(name);

		if (powa == null) {
			sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("you_cannot_cast_this_spell", powa.getName()));
			return false;
		}

		if (powa.cast(this, to == null ? this : to)) {
			statsBoard.refresh();
			return true;
		}
		return false;
	}

	/**
	 * save player current data in yml
	 */
	public void saveData(){
		try {
	        File userdata = new File(LowbrainCore.getInstance().getDataFolder(), File.separator + "PlayerDB");
	        File f = new File(userdata, File.separator + this.player.getUniqueId() + ".yml");
	        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);

            playerData.set("class.is_set", this.classIsSet);
            playerData.set("class.name", this.className);

			playerData.set("race.is_set", this.raceIsSet);
			playerData.set("race.name", this.raceName);

            playerData.set("stats.vitality",this.vitality);
            playerData.set("stats.lvl", this.lvl);
            playerData.set("stats.strength", this.strength);
            playerData.set("stats.intelligence", this.intelligence);
            playerData.set("stats.dexterity", this.dexterity);
			playerData.set("stats.magic_resistance",this.magicResistance);
            playerData.set("stats.defence", this.defence);
			playerData.set("stats.agility",this.agility);
            playerData.set("stats.points", this.points);
            playerData.set("stats.experience", this.experience);
			playerData.set("stats.next_lvl", this.nextLvl);
			playerData.set("stats.kills",this.kills);
			playerData.set("stats.deaths",this.deaths);
			playerData.set("stats.current_mana", this.currentMana);
			playerData.set("stats.skill_points", this.skillPoints);
			playerData.set("stats.reputation", this.reputation);
            playerData.set("stats.courage", this.courage);

			for(Map.Entry<String, Integer> r : this.mobKills.entrySet()) {
				String n = r.getKey();
				int v = r.getValue();
				playerData.set("mob_kills." + n,v);
			}

			playerData.set("stats.current_skill",this.currentSkill);

			for(Map.Entry<String, LowbrainSkill> s : this.skills.entrySet()) {
				String n = s.getKey();
				int v = s.getValue().getCurrentLevel();
				playerData.set("skills." + n,v);
			}

			playerData.set("settings.show_stats", showStats);


            playerData.save(f);
		} catch (Exception e) {
			LowbrainCore.getInstance().warn(e.getMessage());// TODO: handle exception
		}
	}

	/**
	 * disconnect the player and stop all tasks
     * will save his data
     */
	public void disconnect(){
		stopManaRegenTask();
		this.saveData();
	}

	/***
	 * reload everything when config are reloaded
	 */
	public void reload(){
		saveData();
		stopManaRegenTask();
		onAttributeChange();
		validateEquippedArmor();
		startManaRegenTask();
	}

	/**
	 * level up add one level... increment player points
	 */
	public void levelUP(){
        if((getSettings().getMaxLvl() >= 0 && this.lvl >= getSettings().getMaxLvl()))
            return;

        this.lvl += 1;

        addBonusAttributes(1);

        this.addPoints(getSettings().getPointsPerLvl());

        if(this.lvl % getSettings().getSkillPointsLevelInterval() == 0)
            this.addSkillPoints(getSettings().getSkillPointsPerInterval());

        double lvlExponential = getSettings().getParameters().getNextLvlMultiplier();
        this.nextLvl += this.nextLvl * lvlExponential;
        setDisplayName();

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); //restore health on level up
        this.currentMana = this.maxMana;//restore maxMana on level up
        sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("level_up", this.lvl));
        this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,0);
	}

	/**
	 * reset player class and race
	 * @param c class name
	 * @param r race name
     */
	public void reset(String c, String r){
		if(getSettings().isAllowStatsReset()){
			setClass(c,true);
			setRace(r, true);
		}
	}

	/**
	 * reset all stats of the player
	 * @param override by pass settings restriction
	 */
	public void resetAll(boolean override){
	    if (!getSettings().isAllowCompleteReset() && !override) {
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("not_allowed_to_reset_stats"));
            return;
        }

        // reset stats
        strength = 0;
        intelligence = 0;
        vitality = 0;
        defence = 0;
        dexterity = 0;
        magicResistance = 0;
        classIsSet = false;
        className = "";
        raceIsSet = false;
        lowbrainClass = null;
        lowbrainRace = null;
        raceName = "";
        experience = 0;
        points = getSettings().getStartingPoints();
        lvl = 1;
        nextLvl = getSettings().getFirstLvlExp();
        kills = 0;
        deaths = 0;
        currentMana = 0;
        agility = 0;

        // reset generic attributes with original minecraft attributes
        this.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(2);
        this.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
        this.getPlayer().setWalkSpeed(0.2F);
        this.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10);
        this.getPlayer().getAttribute(Attribute.GENERIC_LUCK).setBaseValue(0);

        // stop mana regen task
        this.stopManaRegenTask();

        // set all skill level to zero
        for (String k : this.getSkills().keySet())
            this.getSkills().get(k).setCurrentLevel(0);

        // reset mob kills count
        for (String k : this.getMobKills().keySet())
            this.getMobKills().put(k,0);

        setDisplayName();
        sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("stats_reset"));
	}

	//=================================================== END OF USEFUL =====================================

	//=====================================================  ADD AND SETTER METHODS =================================

	/**
	 * add experience to current player
	 * @param exp experience
	 */
	public void addExperience(double exp){
		this.experience += exp;
		statsBoard.refresh();

		if(this.experience >= nextLvl)
			this.levelUP();
		else if (this.experience < 0 )
			this.experience = 0;

	}

	public void setLvl(int lvl) {
	    this.setLvl(lvl, false);
    }

	/**
	 * set player level. will not add points
	 * @param lvl
	 */
	public void setLvl(int lvl, boolean ajust) {
		addLvl(lvl-this.lvl, ajust);
	}

    /**
     * add level without adjusting points and bonus attributes
     * @param n lvl to add
     */
	public void addLvl(int n) {
	    this.addLvl(n, false);
    }

	/**
	 * add lvl to current player. will add points as well
	 * @param nbLvl lvl to add
     * @param ajust  adjust points or not
	 */
	public void addLvl(int nbLvl, boolean ajust){
		int oldLvl = this.lvl;
		this.lvl += nbLvl;
		int maxLvl = getSettings().getMaxLvl();
		int nbPointsPerLevel = getSettings().getPointsPerLvl();

		if(maxLvl > 0 && this.lvl > maxLvl)
		    this.lvl= maxLvl;
		else if (this.lvl <= 0)
		    this.lvl = 1;

		int dif = this.lvl - oldLvl;
		this.setDisplayName();
		if(ajust){
			this.points += dif * nbPointsPerLevel;
			if(this.points < 0) this.points = 0;

			addBonusAttributes(dif);
		}
	}

	/**
	 * add deaths
	 * @param deaths number of deaths to add
     */
	public void addDeaths(int deaths) {
		this.deaths += deaths;
		if(Settings.getInstance().isHardCoreEnable() && this.deaths >= Settings.getInstance().getHardCoreMaxDeaths()){
			this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("player_dies_on_hardcore_mode"));
			this.resetAll(true);
		}
	}

	/**
	 * set player current mana
	 * @param currentMana set mana
     */
	public void setCurrentMana(double currentMana) {
		this.currentMana = currentMana;
	}

	/**
	 * set current player race
	 * @param n name of the race
	 * @param override override current configuration (can reset race, can switch race)
     */
	public void setRace(String n, boolean override){
		if (!raceIsSet || override) {
			this.lowbrainRace = new LowbrainRace(n);

			this.defence += lowbrainRace.getDefence();
			this.dexterity += lowbrainRace.getDexterity();
			this.intelligence += lowbrainRace.getIntelligence();
			this.strength += lowbrainRace.getStrength();
			this.vitality += lowbrainRace.getVitality();
			this.magicResistance += lowbrainRace.getMagicResistance();
			this.agility += lowbrainRace.getAgility();

			this.raceName = n;
			this.experience = 0;
			this.nextLvl = getSettings().getFirstLvlExp();
			this.lvl = 1;
			sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class", lowbrainRace.getName()));
			this.raceIsSet = true;
			initializePowers();
			start();
		} else if(getSettings().canSwitchRace()){
			if(this.raceName == n){
                sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class_same", lowbrainRace.getName()));
				return;
			}

			LowbrainRace newRace = new LowbrainRace(n);
			this.raceIsSet = true;

			this.defence -= lowbrainRace.getDefence();
			this.dexterity -= lowbrainRace.getDexterity();
			this.intelligence -= lowbrainRace.getIntelligence();
			this.strength -= lowbrainRace.getStrength();
			this.vitality -= lowbrainRace.getVitality();
			this.agility -= lowbrainRace.getAgility();

			addBonusAttributes(this.lvl * -1 - 1);

			this.defence += newRace.getDefence();
			this.dexterity += newRace.getDexterity();
			this.intelligence += newRace.getIntelligence();
			this.strength += newRace.getStrength();
			this.vitality += newRace.getVitality();
			this.agility += newRace.getAgility();
			this.raceName = n;
			this.lowbrainRace = newRace;

			addBonusAttributes(this.lvl -1);
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class", newRace.getName()));
			initializePowers();
			start();
		} else{
            sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cant_switch_race"));
		}
		this.raceIsSet = true;
	}

	/**
	 * set current player class
	 * @param n class name
	 * @param override override current configuration (can reset class, can switch class)
     */
	public void setClass(String n, boolean override){
		if(!classIsSet || override){
			this.lowbrainClass = new LowbrainClass(n);

			this.defence = lowbrainClass.getDefence();
			this.dexterity = lowbrainClass.getDexterity();
			this.intelligence = lowbrainClass.getIntelligence();
			this.strength = lowbrainClass.getStrength();
			this.vitality = lowbrainClass.getVitality();
			this.magicResistance = lowbrainClass.getMagicResistance();
			this.agility = lowbrainClass.getAgility();

			this.className = n;
			this.experience = 0;
			this.nextLvl = getSettings().getFirstLvlExp();
			this.lvl = 1;
			sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class", lowbrainClass.getName()));
			this.classIsSet = true;

			initializePowers();
			start();
		} else if (getSettings().canSwitchClass()) {
			if(this.className == n){
				sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class_same", lowbrainClass.getName()));
				return;
			}

			this.classIsSet = true;
			LowbrainClass newClass = new LowbrainClass(n);

			this.defence -= lowbrainClass.getDefence();
			this.dexterity -= lowbrainClass.getDexterity();
			this.intelligence -= lowbrainClass.getIntelligence();
			this.strength -= lowbrainClass.getStrength();
			this.vitality -= lowbrainClass.getVitality();
			this.agility -= lowbrainClass.getAgility();

			addBonusAttributes(this.lvl * -1 - 1);

			this.defence += newClass.getDefence();
			this.dexterity += newClass.getDexterity();
			this.intelligence += newClass.getIntelligence();
			this.strength += newClass.getStrength();
			this.vitality += newClass.getVitality();
			this.agility += newClass.getAgility();
			this.className = n;
			this.lowbrainClass = newClass;

			addBonusAttributes(this.lvl -1);

			initializePowers();
			start();
			sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("set_race_and_class", newClass.getName()));
		} else{
			sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cant_switch_class"));
		}
		this.classIsSet = true;
	}

	/**
	 * set current skill
	 * @param n name of the skill
	 */
	public void setCurrentSkill(String n) {
		if (this.skills.containsKey(n)) {
			if(this.skills.get(n).getCurrentLevel() == 0){
				this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("cannot_use_skill", n));
				return;
			}
			this.currentSkill = n;
		} else {
			this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("no_such_skill", n));
		}
	}

	/**
	 * upgrade skill
	 * @param n name of the skill
	 */
	public void upgradeSkill(String n){
		if (!LowbrainCore.getInstance().getSkills().containsKey(n)){
			this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("no_such_skill", n));
			return;
		}

		LowbrainSkill s = this.skills.get(n);
		if (!s.isEnable()) {
			this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("skill_is_disabled", s.getName()));
			return;
		}

		if (s.getMaxLevel() <= s.getCurrentLevel()) {
			this.sendMessage(LowbrainCore.getInstance().getConfigHandler().localization().format("skill_fully_upgraded", s.getName()));
			return;
		}

		String msg = "";
		for(Map.Entry<String, Integer> r : s.getBaseRequirements().entrySet()) {
			String requirement = r.getKey().toLowerCase();
			int value = s.operation(r.getValue(),s.getRequirementsOperationValue(),s.getRequirementsOperation());
			if(this.compareAttributesByName(requirement,value) < 0)
				msg += " " + n + ":" + value;

		}

		if (!fn.StringIsNullOrEmpty(msg)) {
			this.sendMessage(
			        LowbrainCore.getInstance().getConfigHandler().localization()
                            .format("skill_requirement_to_high",
                                    new Object[] {msg, s.getName()}));
			return;
		}

		if (s.getSkillpointsCost() > this.skillPoints) {
			this.sendMessage(
			        LowbrainCore.getInstance().getConfigHandler().localization()
                            .format("skills_required_points",
                                    new Object[] {s.getBaseSkillpointsCost(), s.getName()}));
			return;
		}

		s.addLevel(1);
		this.addSkillPoints(- s.getSkillpointsCost());

	}

	//=============================================== END OF ADD AND SETTER ===============================

	//================================================ GETTER ==============================================

	/**
	 * return formatted player's information as string
	 * @return player's information
     */
	public String toString(){
	    if (!classIsSet || !raceIsSet)
            return "Class or (both) Race is not set. No stats available !";

	    String s = "\n\n" + "*******" + this.getPlayer().getName() + "'s stats *******";
        s += "Level : " + lvl + "\n";
        s += "Class : " + getClassName() + "\n";
        s += "Race : " + getRaceName() + "\n";
        s += "Defence : " + defence + "\n";
        s += "Strength : " + strength + "\n";
        s += "Health : " + vitality + "\n";
        s += "Dexterity : " + dexterity + "\n";
        s += "Intelligence : " + intelligence + "\n";
        s += "Magic Resistance : " + magicResistance + "\n";
        s += "Agility : " + agility + "\n";
        s += "Kills : " + kills + "\n";
        s += "Deaths : " + deaths + "\n";
        s += "Reputation : " + reputation + "\n";
        s += "Courage : " + courage + "\n";
        s += "Points : " + points + "\n";
        s += "Skill points: " + skillPoints + "\n";
        s += "Experience : " + experience + "\n";
        s += "Next lvl in : " + (nextLvl - experience) + " xp" + "\n";

        s += "Attack speed : " + this.getAttackSpeed()+ "\n";
        s += "Movement speed : " + this.getMovementSpeed()+ "\n";
        s += "Mana regen : " + this.getMultipliers().getPlayerManaRegen()+ "\n";
        s += "Max Mana : " + this.getMaxMana()+ "\n";
        s += "Max vitality : " + this.getMaxHealth()+ "\n";
        s += "Luck : " + this.getLuck()+ "\n";
        s += "Knockback resistance : " + this.getKnockBackResistance()+ "\n";

        s += "Powers : ";
        for (LowbrainPower powa : this.powers.values())
            s += powa.getName() + ", ";

        s += "******************************************\n";

        return s;
	}

    /**
     * get the player LowbrainRace reference
     * @return this.lowbrainRace
     */
	public LowbrainRace getLowbrainRace() {
		return lowbrainRace;
	}

    /**
     * get the player LowbrainClass refenrece
     * @return this.lowbrainClass
     */
	public LowbrainClass getLowbrainClass(){ return lowbrainClass; }

    /**
     * get the player race name
     * @return
     */
	public String getRaceName() {
		return raceName;
	}

    /**
     * check if the player has set his race
     * @return true if set
     */
	public boolean isRaceSet() {
		return raceIsSet;
	}

	/**
	 * return experience needed to achieve next level
	 * @return
	 */
	public double getNextLvl(){
		return this.nextLvl;
	}

    /**
     * get the player class name
     * @return class name
     */
	public String getClassName() {
		return className;
	}

    /**
     * check if the player has sets his class
     * @return class is set
     */
	public boolean isClassSet() {
		return classIsSet;
	}

    /**
     * get the max mana
     * @return maximum mana
     */
	public double getMaxMana() {
		return maxMana;
	}

    /**
     * get the current amount of mana
     * @return current mana
     */
	public double getCurrentMana() {
		return currentMana;
	}

    /**
     * get player luck from generic attribute
     * @return luck
     */
	public double getLuck(){
		return this.getPlayer().getAttribute(Attribute.GENERIC_LUCK).getBaseValue();
	}

    /**
     * get player attack speed from generic attribute
     * @return attack speed
     */
	public double getAttackSpeed(){
		return this.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
	}

	public double getMaxHealth() {
		return this.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
	}

    /**
     * get player knockback resistance from generic attribute
     * @return
     */
	public double getKnockBackResistance(){
		return this.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();
	}

    /**
     * get player movement speed from generic attribute
     * @return
     */
	public double getMovementSpeed(){
		return this.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
	}

    /**
     * return the list of mob the player killed (type)
     * @return HashMap<String, Integer> list of mob kills
     */
	public HashMap<String, Integer> getMobKills() {
		return mobKills;
	}

    /**
     * return the list of skill
     * @return HashMap<String, LowbrainSkill> list of skills
     */
	public HashMap<String, LowbrainSkill> getSkills() {
		return skills;
	}

    /**
     * get the current player LowbrainSkill object
     * @return current skill
     */
	public LowbrainSkill getCurrentSkill() {
		return this.skills.get(this.currentSkill);
	}

    /**
     * return the list of powers
     * @return list of powers
     */
    public HashMap<String, LowbrainPower> getPowers() {
        return powers;
    }

	//================================================= END OF GETTER =======================================

	//============================================PRIVATE METHODS FOR PLAYER ATTRIBUTES======================

    /**
     * set the player generic attribute of attack speed using his attributes
     */
	private void setAttackSpeed(){
		if(getSettings().getParameters().getPlayerAttributes().getAttackSpeed().isEnabled())
			this.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(this.getMultipliers().getPlayerAttackSpeed());
	}

    /**
     * set the player generic attribute of knockback resistance using his attributes
     */
	private void setKnockBackResistance(){
		if(getSettings().getParameters().getPlayerAttributes().getKnockbackResistance().isEnabled())
			this.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(this.getMultipliers().getPlayerKnockbackResistance());
	}

    /**
     * set the player generic attribute of movement speed using his attributes
     */
	private void setMovementSpeed(){
		if(getSettings().getParameters().getPlayerAttributes().getMovementSpeed().isEnabled())
			//this.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
			this.getPlayer().setWalkSpeed((float)this.getMultipliers().getPlayerMovementSpeed());
	}

    /**
     * set the player generic attribute of luck using his attributes
     */
    private void setLuck(){
        if(getSettings().getParameters().getPlayerAttributes().getLuck().isEnabled())
            this.getPlayer().getAttribute(Attribute.GENERIC_LUCK).setBaseValue(this.getMultipliers().getPlayerLuck());
    }

	/**
	 * set player maximum vitality based on his attributes
	 */
	private void setPlayerMaxHealth(){
		if(getSettings().getParameters().getPlayerAttributes().getTotalHealth().isEnabled())
			this.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.getMultipliers().getPlayerMaxHealth());
	}

	/**
	 * reset player maxMana
	 */
	private void setMana() {
		if(getSettings().getParameters().getPlayerAttributes().getTotalMana().isEnabled())
			this.maxMana = this.getMultipliers().getPlayerMaxMana();

	}

	/**
	 * set player display name with current level
	 */
	private void setDisplayName(){
		if(classIsSet && raceIsSet) {
			String prefix = ChatColor.GREEN + "[" + this.lowbrainClass.getTag() + "] ";
			String suffix = ChatColor.GOLD + "[" + this.lvl + "]";
			String name =  ChatColor.WHITE + getPlayer().getName();
			getPlayer().setCustomName(prefix + name + suffix);
			getPlayer().setDisplayName(prefix + name + suffix);
		} else {
			getPlayer().setCustomName(getPlayer().getName());
			getPlayer().setDisplayName(getPlayer().getName());
		}
	}

	//==================================================END OF PRIVATE METHODS==============================

	//=============================================PRIVATE METHODS HELPERS==================================

	/**
	 * regenerate player maxMana based on player intelligence
     */
	private void regenMana(){
		if(currentMana == maxMana)
			return;

		if(getSettings().getParameters().getPlayerAttributes().getManaRegen().isEnabled()){
			double regen = this.getMultipliers().getPlayerManaRegen();
			this.currentMana += regen;

			if(this.currentMana > maxMana)
			    this.currentMana = maxMana;

			statsBoard.refresh();
		}
	}

	/**
	 * start a new maxMana regeneration task on the server
     */
	private void startManaRegenTask(){
	    if (this.manaRegenTask != null)
	        return;

        this.manaRegenTask = LowbrainCore.getInstance().getServer().getScheduler().runTaskTimer(LowbrainCore.getInstance(), new Runnable() {
            @Override
            public void run() {
                regenMana();
            }
        }, 0, getSettings().getManaRegenInterval() * 20);
        LowbrainCore.getInstance().debugInfo("Start regen maxMana task !");
	}

	/**
	 * stop maxMana regeneration task on server
     */
	private void stopManaRegenTask(){
		if (LowbrainCore.getInstance() != null && this.manaRegenTask != null){
			LowbrainCore.getInstance().getServer().getScheduler().cancelTask(this.manaRegenTask.getTaskId());
			this.manaRegenTask = null;
		}
	}

    /**
     * return the instance of setting
     * @return setting
     */
	private Settings getSettings(){
		return Settings.getInstance();
	}

    /**
     * increment all attributes from bonus attributes
     * @param nb increment by nb
     */
	private void addBonusAttributes(int nb){
		for (String attribute : this.lowbrainRace.getBonusAttributes()) {
			switch (attribute){
				case "vitality":
					addVitality(nb,false,false);
					break;
				case "strength":
					addStrength(nb,false,false);
					break;
				case "intelligence":
					addIntelligence(nb,false,false);
					break;
				case "dexterity":
					addDexterity(nb,false,false);
					break;
				case "magic_resistance":
					addMagicResistance(nb,false,false);
					break;
				case "defence":
					addDefence(nb,false,false);
					break;
				case "agility":
					addAgility(nb,false,false);
					break;
				case "all":
					addVitality(nb,false,false);
					addDefence(nb,false,false);
					addMagicResistance(nb,false,false);
					addDexterity(nb,false,false);
					addIntelligence(nb,false,false);
					addVitality(nb,false,false);
					addStrength(nb,false,false);
					addAgility(nb, false,false);
					break;
			}
		}
		
		onAttributeChange();
	}

    /**
     * get multiplier class object
     * @return multiplier object
     */
	public Multipliers getMultipliers() {
		return multipliers != null ? multipliers : (this.multipliers = new Multipliers(this));
	}

	//===============================================END OF PRIVATE METHODES HELPER===============================

    protected void onAttributeChange(){
	    if (!classIsSet || !raceIsSet)
	        return;

        if(multipliers == null)
            multipliers = new Multipliers(this);
        else
            multipliers.update();

        this.repStatus = getSettings().getParameters().getReputation().getRepFrom(this.reputation);

        setPlayerMaxHealth();
        setLuck();
        setMana();
        setKnockBackResistance();
        setAttackSpeed();
        setMovementSpeed();
        statsBoard.refresh();
    }

    public boolean isShowStats() {
        return showStats;
    }

    public void setShowStats(boolean showStats) {
        this.showStats = showStats;
    }

    public StatsBoard getStatsBoard() {
        return this.statsBoard;
    }

    public boolean isPlayable() {
        return this.classIsSet && this.raceIsSet && this.lowbrainClass != null && this.lowbrainRace != null;
    }
}

