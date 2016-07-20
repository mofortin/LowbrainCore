package lowbrain.mcrpg.main;

import lowbrain.mcrpg.commun.Helper;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import lowbrain.mcrpg.commun.RPGPlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


public class PlayerListener implements Listener {

	public static Main plugin;
    private int max_stats;

    public PlayerListener(Main instance) {
        plugin = instance;
        max_stats = plugin.config.max_stats <= 0 ? 100 : plugin.config.max_stats;
    }

    /**
     * Called when a player get damaged
     * @param e
     */
    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            RPGPlayer damagee = plugin.connectedPlayers.get(e.getEntity().getUniqueId());

            // multiplier = a * x + b : where a = max-min/max_stats, and b = min
            float multiplier = 1;

            plugin.debugMessage("Damage caused by : " + e.getCause().name());
            plugin.debugMessage("Initial damage : " +e.getDamage());

            //FALL DAMAGE
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL && plugin.config.math.damaged_by_fall_enable){
               multiplier = Helper.getDamagedByFall(damagee,plugin.config);
            }

            //FIRE DAMAGE
            else if((e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                    || e.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || e.getCause() == EntityDamageEvent.DamageCause.DRAGON_BREATH
                    || e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) && plugin.config.math.damaged_by_fire_enable){

                multiplier = Helper.getDamagedByFire(damagee,plugin.config);
            }

            //EXPLOSION
            else if((e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                    || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)&& plugin.config.math.damaged_by_explosion_enable){

                multiplier = Helper.getDamagedByExplosion(damagee,plugin.config);
            }

            //MAGIC POTION DAMAGE
            else if((e.getCause() == EntityDamageEvent.DamageCause.MAGIC
                    || e.getCause() == EntityDamageEvent.DamageCause.WITHER
                    || e.getCause() == EntityDamageEvent.DamageCause.POISON) && plugin.config.math.damaged_by_magic_enable){

                multiplier = Helper.getDamagedByMagic(damagee,plugin.config);

                float changeOfRemovingEffect = -1F;

                if(plugin.config.math.chance_of_removing_magic_effect_enable){
                    changeOfRemovingEffect = Helper.getChangeOfRemovingPotionEffect(damagee,plugin.config);
                }

                double rdm = Math.random();
                if(rdm < changeOfRemovingEffect){
                    RemoveBadPotionEffect(damagee.getPlayer());
                    plugin.debugMessage("all effect removed");
                }
                else if(plugin.config.math.reducing_bad_potion_effect_enable){
                    ReducingBadPotionEffect(damagee);
                }
            }

            else if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && plugin.config.math.damaged_by_weapon_enable){
                multiplier = Helper.getDamagedByWeapon(damagee,plugin.config);
            }

            //ARROW
            else if(e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && plugin.config.math.damaged_by_projectile_enable){
                multiplier = Helper.getDamagedByProjectile(damagee,plugin.config);
            }

            //CONTACT
            else if(e.getCause() == EntityDamageEvent.DamageCause.CONTACT && plugin.config.math.damaged_by_contact_enable){
                multiplier = Helper.getDamagedByContact(damagee,plugin.config);
            }

            plugin.debugMessage("Deffencive damage multiplier : " + multiplier);
            e.setDamage(e.getDamage() * multiplier);
            plugin.debugMessage("Damage after deffencive multiplier : " + e.getDamage());
        }
    }

    /**
     * called when a player experience changes naturally
     * @param e
     */
    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent e){
        Player p = e.getPlayer();
        RPGPlayer rp = plugin.connectedPlayers.get(p.getUniqueId());
        plugin.debugMessage("Player gains " + e.getAmount() * plugin.config.math.natural_xp_gain_multiplier + " xp");
        rp.addExp(e.getAmount() * plugin.config.math.natural_xp_gain_multiplier);
    }

    /**
     * called when a player dies
     * @param e
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(e.getEntity() instanceof Player){

            plugin.debugMessage("Player dies !");

            RPGPlayer rpKiller = null;
            Player killed = e.getEntity();
            RPGPlayer rpKilled = plugin.connectedPlayers.get(killed.getUniqueId());
            if(killed.getKiller() != null) {
                Player killer = killed.getKiller();
                rpKiller = plugin.connectedPlayers.get(killer.getUniqueId());
            }
            else if(killed.getLastDamageCause() != null){
                if(killed.getLastDamageCause().getEntity() instanceof Arrow){
                    if(((Arrow) killed.getLastDamageCause().getEntity()).getShooter() != null && ((Arrow) killed.getLastDamageCause().getEntity()).getShooter() instanceof Player){
                        rpKiller = plugin.connectedPlayers.get((((Player) ((Arrow) killed.getLastDamageCause().getEntity()).getShooter()).getPlayer().getUniqueId()));
                        plugin.debugMessage("Killed by arrow!");
                    }
                }/*
                else if(killed.getLastDamageCause().getEntity() instanceof TippedArrow){
                    if(((TippedArrow) killed.getLastDamageCause().getEntity()).getShooter() != null && ((TippedArrow) killed.getLastDamageCause().getEntity()).getShooter() instanceof Player){
                        rpKiller = plugin.connectedPlayers.get((((Player) ((TippedArrow) killed.getLastDamageCause().getEntity()).getShooter()).getPlayer().getUniqueId()));
                        plugin.debugMessage("Killed by tipped arrow!");
                    }
                }
                else if(killed.getLastDamageCause().getEntity() instanceof SpectralArrow){
                    if(((SpectralArrow) killed.getLastDamageCause().getEntity()).getShooter() != null && ((SpectralArrow) killed.getLastDamageCause().getEntity()).getShooter() instanceof Player){
                        rpKiller = plugin.connectedPlayers.get((((Player) ((SpectralArrow) killed.getLastDamageCause().getEntity()).getShooter()).getPlayer().getUniqueId()));
                        plugin.debugMessage("Killed by spectral arrow!");
                    }
                }*/
                else if(killed.getLastDamageCause().getEntity() instanceof ThrownPotion){
                    if(((ThrownPotion) killed.getLastDamageCause().getEntity()).getShooter() != null && ((ThrownPotion) killed.getLastDamageCause().getEntity()).getShooter() instanceof Player){
                        rpKiller = plugin.connectedPlayers.get((((Player) ((ThrownPotion) killed.getLastDamageCause().getEntity()).getShooter()).getPlayer().getUniqueId()));
                        plugin.debugMessage("Killed by potion!");
                    }
                }
            }

            if(rpKiller != null){
                double diffLvl = Math.abs(rpKilled.getLvl() - rpKiller.getLvl());
                rpKiller.addKills(1);
                double xpGained = 0.0;
                if(diffLvl == 0){
                    xpGained = plugin.config.exp_on_player_kill * rpKiller.getLvl() * plugin.config.math.killer_level_gain_multiplier;
                }else if(rpKilled.getLvl() < rpKiller.getLvl()){
                    xpGained = plugin.config.exp_on_player_kill / (diffLvl * plugin.config.math.level_difference_multiplier) * rpKiller.getLvl() * plugin.config.math.killer_level_gain_multiplier;
                }else{
                    xpGained = plugin.config.exp_on_player_kill * (diffLvl * plugin.config.math.level_difference_multiplier) * rpKiller.getLvl() * plugin.config.math.killer_level_gain_multiplier;
                }
                rpKiller.addExp(xpGained);
                plugin.debugMessage("Killer gains "+ xpGained+" xp!");
            }

            rpKilled.addExp(-(plugin.config.exp_loss_on_death / 100 * rpKilled.getExperience()));
            rpKilled.addDeaths(1);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        RPGPlayer rp = plugin.connectedPlayers.get(p.getUniqueId());
        //TODO
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e){
        //TODO
    }

    /**
     * call when a player attacks
     * @param e
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {

        RPGPlayer damager = null;

        boolean magicAttack = false;
        boolean arrowAttact = false;
        boolean normalAttack = false;

        double oldDamage = e.getDamage();

        plugin.debugMessage("inital damage : " + oldDamage);

        //DEFINING CAUSE OF DAMAGE
        if (e.getDamager() instanceof Player) {
            damager = plugin.connectedPlayers.get(e.getDamager().getUniqueId());
            plugin.debugMessage("Attacked by another player");
            normalAttack  = true;
        } else if (e.getDamager() instanceof Arrow) {
            Arrow ar = (Arrow) e.getDamager();
            if (ar.getShooter() instanceof Player) {
                damager = plugin.connectedPlayers.get(e.getDamager().getUniqueId());
            }
            plugin.debugMessage("Attacked by arrow");
            arrowAttact = true;
        } else if (e.getDamager() instanceof ThrownPotion) {
            ThrownPotion pot = (ThrownPotion) e.getDamager();
            if (pot.getShooter() instanceof Player) {
                damager = plugin.connectedPlayers.get(e.getDamager().getUniqueId());
            }
            plugin.debugMessage("Attacked by potion");
            magicAttack = true;
        }
        else{
            normalAttack  = true;
        }


        //APLLYING MAGIC EFFECT BY ATTACKER
        if(damager != null && !magicAttack){
            plugin.debugMessage("From " + damager.getPlayer().getName());
            double chanceOfMagicEffect = Gradient(plugin.config.math.chance_of_creating_magic_attack_maximum,plugin.config.math.chance_of_removing_magic_effect_minimum)
                    * (damager.getIntelligence() * plugin.config.math.chance_of_creating_magic_attack_intelligence_effect
                    + damager.getDexterity() * plugin.config.math.chance_of_creating_magic_attack_dexterity_effect)
                    + plugin.config.math.chance_of_creating_magic_attack_minimum;

            double rdm = Math.random();
            if(rdm < chanceOfMagicEffect){
                PotionEffect effect = CreateMagicAttack(damager);
                if(e.getEntity() instanceof LivingEntity){
                    ((LivingEntity) e.getEntity()).addPotionEffect(effect);
                    plugin.debugMessage("magic effect added : " + effect.getType().getName() + ", " + effect.getDuration()/20 + ", " + effect.getAmplifier());
                }
            }
        }

        double max;
        double min;
        double multiplier_range;

        //APPLYING DAMAGE CHANGE DEPENDING ON OFFENCIVE ATTRIBUTES
        if(arrowAttact && damager != null){
            max = plugin.config.math.attack_by_projectile_maximum;
            min = plugin.config.math.attack_by_projectile_minimum;
            multiplier_range = plugin.config.math.attack_by_projectile_range;

            double baseDamage = e.getDamage() * Gradient(max,min)
                    * (damager.getDexterity() * plugin.config.math.attack_by_projectile_dexterity
                    + damager.getStrength() * plugin.config.math.attack_by_projectile_strength) + min;

            double rdm = (baseDamage - multiplier_range) + (Math.random() * (baseDamage + multiplier_range));

            e.setDamage(rdm);
        }
        else if(normalAttack && damager != null){
            max = plugin.config.math.attack_by_weapon_maximum;
            min = plugin.config.math.attack_by_weapon_minimum;
            multiplier_range = plugin.config.math.attack_by_weapon_range;

            double baseDamage = e.getDamage() * Gradient(max,min) *
                    (damager.getDexterity() * plugin.config.math.attack_by_weapon_dexterity
                            +  damager.getStrength() * plugin.config.math.attack_by_weapon_strength)
                    + min;

            double rdm = (baseDamage - multiplier_range) + (Math.random() * (baseDamage + multiplier_range));
            e.setDamage(rdm);
        }
        else if(magicAttack && damager != null){
            max = plugin.config.math.attack_by_potion_maximum;
            min = plugin.config.math.attack_by_potion_minimum;
            multiplier_range = plugin.config.math.attack_by_potion_range;

            double baseDamage = e.getDamage() * Gradient(max,min)
                    * (damager.getDexterity() * plugin.config.math.attack_by_potion_dexterity
                    + damager.getIntelligence() * plugin.config.math.attack_by_potion_intelligence) + min;

            double rdm = (baseDamage - multiplier_range) + (Math.random() * (baseDamage + multiplier_range));
            e.setDamage(rdm);
        }

        double damageMultiplier = e.getDamage() / oldDamage;
        plugin.debugMessage("Offencive damage multiplier : " + damageMultiplier);
        plugin.debugMessage("Damage after offencive multiplier : " + e.getDamage());

        /*
        //APPYING DAMAGE CHANGE DEPENDING ON DEFENCIVE ATTRIBUTES
        if(e.getEntity() instanceof Player){
            damagee = plugin.connectedPlayers.get(e.getEntity().getUniqueId());
            if(arrowAttact || normalAttack){
                max = plugin.config.math.normal_and_arrow_attack_defence_maximum_damage_multiplier;
                min = plugin.config.math.normal_and_arrow_attack_defence_minimum_damage_multiplier;

                e.setDamage(e.getDamage() * Gradient(max,min) * damagee.getDefence() * plugin.config.math.normal_and_arrow_attack_defence_defence_effect + min);
            }
            else if(magicAttack){
                max = plugin.config.math.potion_attack_defence_maximum_damage_multiplier;
                min = plugin.config.math.potion_attack_defence_minimum_damage_multiplier;

                e.setDamage(e.getDamage() * Gradient(max,min)
                        * (damagee.getMagicResistance() * plugin.config.math.potion_attack_defence_magic_resistance_effect
                        + damagee.getIntelligence() * plugin.config.math.potion_attack_defence_intelligence_effect)
                        + min);
            }

            max = plugin.config.math.chance_of_removing_magic_effect_maximum;
            min = plugin.config.math.chance_of_removing_magic_effect_minimum;

            double changeOfRemovingEffect = Gradient(max,min)
                    * (damagee.getMagicResistance()* plugin.config.math.chance_of_removing_magic_effect_magic_resistance_effect_on_multiplier
                    + damagee.getIntelligence() * plugin.config.math.chance_of_removing_magic_effect_intelligence_effect_on_multiplier
                    + damagee.getDexterity() * plugin.config.math.chance_of_removing_magic_effect_dexterity_effect_on_multiplier)
                    +min;

            double rdm = Math.random();
            if(rdm < changeOfRemovingEffect){
                RemoveBadPotionEffect(damagee.getPlayer());
                plugin.debugMessage("all effect removed");
            }
            else{
                ReducingBadPotionEffect(damagee);
            }
        }
        */
        //plugin.debugMessage("final damage : " + e.getDamage());
    }

    /**
     * When a player consume a potion
     * @param e
     */
    @EventHandler
    public void onPlayerConsumePotion(PlayerItemConsumeEvent e){
        if(e.getItem() != null && e.getItem().getType().equals(Material.POTION)){
            RPGPlayer rp = plugin.connectedPlayers.get(e.getPlayer().getUniqueId());
            if(rp != null) {

                double max = plugin.config.math.on_player_consume_potion_maximum;
                double min = plugin.config.math.on_player_consume_potion_minimum;
                double range = plugin.config.math.on_player_consume_potion_range;

                double multiplier = Gradient(max,min)
                        * (rp.getIntelligence() * plugin.config.math.on_player_consume_potion_intelligence)
                        + min;
                double maxMultiplier = multiplier < (max-range) ? multiplier + range : max;
                double minMultiplier = multiplier >= (min+range) ? multiplier - range : min;
                double rdm = minMultiplier + (Math.random() * maxMultiplier);//top lvl will have multiplier of 2%

                PotionEffect po = (PotionEffect) rp.getPlayer().getActivePotionEffects().toArray()[rp.getPlayer().getActivePotionEffects().size() -1];

                int newDuration = (int) (po.getDuration() * rdm);
                PotionEffect tmp = new PotionEffect(po.getType(), newDuration, po.getAmplifier());
                rp.getPlayer().removePotionEffect(po.getType());
                rp.getPlayer().addPotionEffect(tmp);

                /*
                for (PotionEffect pe : rp.getPlayer().getActivePotionEffects()) {
                    int newDuration = (int) (pe.getDuration() * rdm);
                    PotionEffect tmp = new PotionEffect(pe.getType(), newDuration, pe.getAmplifier());
                    rp.getPlayer().removePotionEffect(pe.getType());
                    rp.getPlayer().addPotionEffect(tmp);
                }
                */
                plugin.debugMessage("effect duration multiply by " + multiplier);
            }
        }
    }

    /**
     * called when a player shoots with a  bow
     * @param e
     */
    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent e){
        if(e.getEntity() instanceof Player){
            //set new force
            Arrow ar = (Arrow) e.getProjectile();
            RPGPlayer rpPlayer = plugin.connectedPlayers.get(e.getEntity().getUniqueId());
            //plugin.debugMessage("inital fall distance : " + ar.getFallDistance());
            //ar.setFallDistance( (float)(ar.getFallDistance() * rpPlayer.getStrength() / 100));
            //plugin.debugMessage("inital fall distance : " + ar.getFallDistance());

            if(rpPlayer.getDexterity() < plugin.config.math.on_player_shoot_bow_min_dexterity_for_max_precision) {
                double precision = rpPlayer.getDexterity() / plugin.config.math.on_player_shoot_bow_min_dexterity_for_max_precision;
                //ex: with 10dext : rdm between 0.2 and 1.8
                //ex: with 50dext : rdm between 1 and 1 = 100%
                double rdm = precision + (Math.random() * (plugin.config.math.on_player_shoot_bow_range - precision));
                ar.setVelocity(new Vector(ar.getVelocity().getX() * rdm, ar.getVelocity().getY() * rdm, ar.getVelocity().getZ() * rdm));
            }
        }
    }

    /**
     * called when a player join the server
     * @param e
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
    	Player p = e.getPlayer();
        plugin.connectedPlayers.put(p.getUniqueId(),new RPGPlayer(p));
        SetServerDifficulty();
    }

    /**
     * called when a player quit the server
     * @param e
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        plugin.connectedPlayers.get(e.getPlayer().getUniqueId()).Disconnect();
        plugin.connectedPlayers.remove(e.getPlayer().getUniqueId());
        SetServerDifficulty();
    }

    /**
     * create damaging effect depending on player attributes
     * @param p
     * @return
     */
    private PotionEffect CreateMagicAttack(RPGPlayer p){
        int rdm = 1 + (int)(Math.random() * 7);
        int duration = 0;
        int amplifier = 0;

        double max = plugin.config.math.creating_magic_attack_maximum_duration;
        double min = plugin.config.math.creating_magic_attack_minimum_duration;

        duration = (int)(Gradient(max,min)
                * (p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_duration
                + p.getDexterity() * plugin.config.math.creating_magic_attack_dexterity_on_duration)
                + min);

        PotionEffect effect;
        PotionEffectType type = PotionEffectType.POISON;
        switch (rdm){
            case 1:
                type = PotionEffectType.BLINDNESS;
                amplifier = 0;
                break;
            case 2:
                type = PotionEffectType.CONFUSION;
                amplifier = 0;
                break;
            case 3:
                type = PotionEffectType.HARM;
                duration = 1;
                amplifier = (int)(Gradient(plugin.config.math.creating_magic_attack_maximum_harm_amplifier,plugin.config.math.creating_magic_attack_minimum_harm_amplifier)
                        * p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_harm_amplifier);
                break;
            case 4:
                type = PotionEffectType.POISON;
                amplifier = (int)(Gradient(plugin.config.math.creating_magic_attack_maximum_poison_amplifier,plugin.config.math.creating_magic_attack_minimum_poison_amplifier)
                        * p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_poison_amplifier);
                break;
            case 5:
                type = PotionEffectType.SLOW;
                amplifier = (int)(Gradient(plugin.config.math.creating_magic_attack_maximum_slow_amplifier,plugin.config.math.creating_magic_attack_minimum_slow_amplifier)
                        * p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_slow_amplifier);
                break;
            case 6:
                type = PotionEffectType.WEAKNESS;
                amplifier = (int)(Gradient(plugin.config.math.creating_magic_attack_maximum_weakness_amplifier,plugin.config.math.creating_magic_attack_minimum_weakness_amplifier)
                        * p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_weakness_amplifier);
                break;
            case 7:
                type = PotionEffectType.WITHER;
                amplifier = (int)(Gradient(plugin.config.math.creating_magic_attack_maximum_wither_amplifier,plugin.config.math.creating_magic_attack_minimum_wither_amplifier)
                        * p.getIntelligence() * plugin.config.math.creating_magic_attack_intelligence_on_wither_amplifier);
                break;
        }

        effect = new PotionEffect(type, duration * 20, amplifier, true,true);
        return effect;
    }

    private PotionEffect CreateMagicDefence(RPGPlayer p){
        return null;
    }

    /**
     * remove bad potion effect from player
     * @param p
     */
    private void RemoveBadPotionEffect(Player p){
        if(p.hasPotionEffect(PotionEffectType.BLINDNESS)){
            p.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if(p.hasPotionEffect(PotionEffectType.CONFUSION)){
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
        if(p.hasPotionEffect(PotionEffectType.HARM)){
            p.removePotionEffect(PotionEffectType.HARM);
        }
        if(p.hasPotionEffect(PotionEffectType.POISON)){
            p.removePotionEffect(PotionEffectType.POISON);
        }
        if(p.hasPotionEffect(PotionEffectType.SLOW)){
            p.removePotionEffect(PotionEffectType.SLOW);
        }
        if(p.hasPotionEffect(PotionEffectType.WEAKNESS)){
            p.removePotionEffect(PotionEffectType.WEAKNESS);
        }
        if(p.hasPotionEffect(PotionEffectType.WITHER)){
            p.removePotionEffect(PotionEffectType.WITHER);
        }
    }

    /**
     * reducing player potion effect depending on attributes
     * @param rp
     */
    private void ReducingBadPotionEffect(RPGPlayer rp){

        float rdm = Helper.getReducingPotionEffect(rp,plugin.config);

        for (PotionEffect pe : rp.getPlayer().getActivePotionEffects()) {
            int newDuration = (int)(pe.getDuration() * rdm);
            int newAmplifier = (int)(pe.getAmplifier() * rdm);
            PotionEffect tmp = new PotionEffect(pe.getType(),newDuration,newAmplifier);
            rp.getPlayer().removePotionEffect(pe.getType());
            rp.getPlayer().addPotionEffect(tmp);
        }
        plugin.debugMessage("all effect reduced by " + rdm);
    }

    /**
     * Set the server difficulty depending on average connectedplayer level
     */
    private void SetServerDifficulty(){

        Difficulty diff;
        Double averageLevel = 0.0;
        for (RPGPlayer rp :
                this.plugin.connectedPlayers.values()) {
            averageLevel += rp.getLvl();
        }
        averageLevel = averageLevel / this.plugin.connectedPlayers.size();

        if(averageLevel <= 25){
            diff = Difficulty.EASY;
        }
        else if(averageLevel <= 60){
            diff = Difficulty.NORMAL;
        }
        else {
            diff = Difficulty.HARD;
        }

        for (World world :
                this.plugin.getServer().getWorlds()) {
            world.setDifficulty(diff);
        }

    }

    private double Gradient(double max, double min){
        return (max - min)/max_stats;
    }
}
