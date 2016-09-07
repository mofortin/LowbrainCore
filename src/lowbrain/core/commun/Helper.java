package lowbrain.core.commun;

import lowbrain.core.main.LowbrainCore;
import lowbrain.core.rpg.LowbrainPlayer;
import org.bukkit.util.Vector;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Moofy on 19/07/2016.
 */


public class Helper {
    public static float eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            float parse() {
                nextChar();
                float x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            float parseExpression() {
                float x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            float parseTerm() {
                float x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            float parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                float x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Float.parseFloat(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = (float)Math.sqrt(x);
                    else if (func.equals("sin")) x = (float)Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = (float)Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = (float)Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = (float)Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static String FormatStringWithValues(String[] st, LowbrainPlayer p){
        if(st.length > 1 && p != null){
            for (int i = 1; i < st.length; i++) {
                st[i] = Integer.toString(p.getAttribute(st[i].trim().toLowerCase(),1));
            }
        }
        else {
            return st[0];
        }

        MessageFormat fmt = new MessageFormat(st[0]);
        return fmt.format(Arrays.copyOfRange(st,1, st.length));
    }

    /***
     * check if string is null or empty
     * @param s
     * @return
     */
    public static boolean StringIsNullOrEmpty(String s){
        return s == null || s.trim().length() == 0;
    }

    /**
     * generate a random float [min,max]
     * @param max max
     * @param min min
     * @return
     */
    public static float randomFloat(float min, float max){
        float range = (max - min);
        return ((float)Math.random() * range) + min;
    }

    /**
     * generate a int float [min,max]
     * @param max max
     * @param min min
     * @return
     */
    public static int randomInt(int min, int max){
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    /**
     * parse string to int
     * @param s string
     * @param d default value
     * @return
     */
    public static int intTryParse(String s, Integer d){
        try {
            return Integer.parseInt(s);
        }catch (Exception e){
            return d;
        }
    }

    public static double doubleTryParse(String s, Double d){
        try {
            return Double.parseDouble(s);
        }catch (Exception e){
            return d;
        }
    }

    public static float floatTryParse(String s, Float d){
        try {
            return Float.parseFloat(s);
        }catch (Exception e){
            return d;
        }
    }

    /***
     * parse string to date
     * @param s
     * @param c
     * @return
     */
    public static Calendar dateTryParse(String s, Calendar c){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date date = sdf.parse(s);// all done
            Calendar cal = sdf.getCalendar();
            cal.setTime(date);
            return cal;
        }catch (Exception e){
            return c;
        }
    }

    /***
     * rotate vector on the xy plan
     * @param dir
     * @param angleD
     * @return
     */
    public static Vector rotateYAxis(Vector dir, double angleD) {
        double angleR = Math.toRadians(angleD);
        double x = dir.getX();
        double z = dir.getZ();
        double cos = Math.cos(angleR);
        double sin = Math.sin(angleR);
        return (new Vector(x*cos+z*(-sin), dir.getY(), x*sin+z*cos)).normalize();
    }

    /***
     * gets the X value ( y = ax + b ) depending on attributes influence and player attributes
     * @param variables
     * @param p
     * @return
     */
    public static float getXValue(HashMap<String,Float> variables, LowbrainPlayer p){
        float x = 0;
        for(Map.Entry<String, Float> inf : variables.entrySet()) {
            String n = inf.getKey().toLowerCase();
            float v = inf.getValue();
            x += (v * p.getAttribute(n,0));
        }
        return x;
    }

    /***
     * return max stats. if max stats <= 0 return 100
     * @return
     */
    private static int getMaxStats(){
        return Settings.getInstance().max_stats <= 0 ? 100 : Settings.getInstance().max_stats;
    }

    public static float Slope(float max, float min){
        return Slope(max,min,getMaxStats());
    }

    public static float Slope(float max, float min, float y){
        return Slope(max,min,y,Settings.getInstance().maths.function_type);
    }

    public static float Slope(float max, float min, float y, int functionType){
        float slope = 0;
        switch (functionType){
            case 1:
                slope = (max - min)/(float)Math.pow(y,2);
                break;
            case 2:
                slope = (max - min)/(float)Math.pow(y,0.5);
                break;
            default:
                slope = (max - min)/y;
                break;
        }
        return slope;
    }

    public static int getAveragePlayerLevel(){
        Double averageLevel = 0.0;
        for (LowbrainPlayer rp :
                LowbrainCore.connectedPlayers.values()) {
            averageLevel += rp.getLvl();
        }

        return averageLevel.intValue();
    }

    /**
     * evaluate value
     * @param max max
     * @param min min
     * @param x value
     * @return
     */
    public static float ValueFromFunction(float max, float min, float x){
        float result = 0;
        switch (Settings.getInstance().maths.function_type){
            case 1:
                result = Slope(max,min) * (float)Math.pow(x,2) + min;
                break;
            case 2:
                result = Slope(max,min) * (float)Math.pow(x,0.5) + min;
                break;
            default:
                result = Slope(max,min) * x + min;
                break;
        }
        return result;
    }

    public static float ValueFromFunction(float max, float min,HashMap<String,Float> variables, LowbrainPlayer p){
        return ValueFromFunction(max,min,getXValue(variables,p));
    }

    /**
     * get all nearby players of a player
     * @param p1 player one
     * @param max maximum distance
     * @return
     */
    public static List<LowbrainPlayer> getNearbyPlayers(LowbrainPlayer p1, double max){
        List<LowbrainPlayer> lst = new ArrayList<LowbrainPlayer>();

        for (LowbrainPlayer p2: LowbrainCore.connectedPlayers.values()) {
            if(p1.equals(p2))continue;//if its the same player
            if(p1.getPlayer().getWorld().equals(p2.getPlayer().getWorld())){//check if they are in the same world
                double x = p1.getPlayer().getLocation().getX() - p2.getPlayer().getLocation().getX();
                double y = p1.getPlayer().getLocation().getY() - p2.getPlayer().getLocation().getY();
                double z = p1.getPlayer().getLocation().getZ() - p2.getPlayer().getLocation().getZ();

                double distance = Math.pow(x*x + y*y + z*z,0.5);

                if(distance <= max){
                    lst.add(p2);
                }
            }
        }

        return lst;
    }

    // ON PLAYER ATTACK

    public static float getBowArrowSpeed(LowbrainPlayer p){
        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerShootBow.speed_function)) {
            result = Helper.ValueFromFunction(Settings.getInstance().maths.onPlayerShootBow.speed_maximum,Settings.getInstance().maths.onPlayerShootBow.speed_minimum,
                    Settings.getInstance().maths.onPlayerShootBow.speed_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerShootBow.speed_function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(Settings.getInstance().maths.onPlayerShootBow.speed_range > 0){
            result = Helper.randomFloat((result - Settings.getInstance().maths.onPlayerShootBow.speed_range),(result + Settings.getInstance().maths.onPlayerShootBow.speed_range));
            if(result < Settings.getInstance().maths.onPlayerShootBow.speed_minimum)result = Settings.getInstance().maths.onPlayerShootBow.speed_minimum;
            else if(result > Settings.getInstance().maths.onPlayerShootBow.speed_maximum) result = Settings.getInstance().maths.onPlayerShootBow.speed_maximum;
        }

        return result;
    }
    public static float getBowPrecision(LowbrainPlayer p){
        float result = 1F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerShootBow.precision_function)) {
            result = Helper.ValueFromFunction(Settings.getInstance().maths.onPlayerShootBow.precision_maximum,Settings.getInstance().maths.onPlayerShootBow.precision_minimum,
                    Settings.getInstance().maths.onPlayerShootBow.precision_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerShootBow.precision_function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(Settings.getInstance().maths.onPlayerShootBow.precision_range > 0){
            result = Helper.randomFloat(result - Settings.getInstance().maths.onPlayerShootBow.precision_range,result + Settings.getInstance().maths.onPlayerShootBow.precision_range);
            if(result < 0)result = 0;
            else if(result > 1) result = 1;
        }

        return result;
    }
    public static float getAttackByWeapon(LowbrainPlayer damager, double damage){

        float max = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_maximum;
        float min = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_minimum;
        float range = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_range;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_function)) {
            result = (float)damage * Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_variables,damager);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.weapon_function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,damager));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }
        if(range > 0){
            result = Helper.randomFloat(result-range,result+range);
            if(result < 0)result = 0;
        }
        return result;
    }
    public static float getAttackByProjectile(LowbrainPlayer damager, double damage){
        float max = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_maximum;
        float min = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_minimum;
        float range = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_range;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_function)) {
            result = (float)damage * Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_variables,damager);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.projectile_function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,damager));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(range > 0){
            result = Helper.randomFloat(result - range,result + range);
            if(result < 0)result = 0;
        }

        return result;
    }
    public static float getAttackByMagic(LowbrainPlayer damager, double damage){
        float max = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_maximum;
        float min = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_minimum;
        float range = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_range;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_function)) {
            result = (float)damage * Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_variables,damager);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerAttackEntity.attackEntityBy.magic_function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,damager));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(range > 0){
            result = Helper.randomFloat(result - range,result + range);
            if(result < 0)result = 0;
        }
        return result;
    }
    public static float getCriticalHitChance(LowbrainPlayer damager){
        float max = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.maximumChance;
        float min = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.minimumChance;
        float range = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.chanceRange;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.chanceFunction)) {
            result = Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.chanceVariables,damager);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.chanceFunction.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,damager));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(range > 0){
            result = Helper.randomFloat(result - range,result + range);
        }
        return result;
    }
    public static float getCriticalHitMultiplier(LowbrainPlayer damager){
        float max = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.maximumDamageMultiplier;
        float min = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.minimumDamageMultiplier;
        float range = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.damageMultiplierRange;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.damageMultiplierFunction)) {
            result = Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.damageMultiplierVariables,damager);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerAttackEntity.criticalHit.damageMultiplierFunction.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,damager));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(range > 0){
            result = Helper.randomFloat(result - range,result + range);
            result = result < min ? min : result > max ? max : result;
        }
        return result;
    }

    // ON PLAYER CONSUME POTION

    public static float getConsumedPotionMultiplier(LowbrainPlayer p){

        float max = Settings.getInstance().maths.onPlayerConsumePotion.maximum;
        float min = Settings.getInstance().maths.onPlayerConsumePotion.minimum;
        float range = Settings.getInstance().maths.onPlayerConsumePotion.range;

        float result = 0F;
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerConsumePotion.function)) {
            result = Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerConsumePotion.variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerConsumePotion.function.split(",");
            if(st.length > 1){
                result = Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                result = Helper.eval(st[0]);
            }
        }

        if(range > 0){
            result = Helper.randomFloat(result - range,result + range);
            if(result < min)result = min;
            else if(result > max) result = max;
        }

        return result;
    }

    //PLAYER ATTRIBUTES

    public static float getPlayerMaxHealth(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.total_health_function)) {
            return Helper.ValueFromFunction(p.getLowbrainRace().getMax_health(), p.getLowbrainRace().getBase_health(),Settings.getInstance().maths.playerAttributes.total_health_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.total_health_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerMaxMana(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.total_mana_function)) {
            return Helper.ValueFromFunction(p.getLowbrainRace().getMax_mana(), p.getLowbrainRace().getBase_mana(),Settings.getInstance().maths.playerAttributes.total_mana_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.total_mana_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerManaRegen(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.mana_regen_function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.playerAttributes.mana_regen_maximum,
                    Settings.getInstance().maths.playerAttributes.mana_regen_minimum,
                    Settings.getInstance().maths.playerAttributes.mana_regen_variables,p
            );
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.mana_regen_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerAttackSpeed(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.attack_speed_function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.playerAttributes.attack_speed_maximum,
                    Settings.getInstance().maths.playerAttributes.attack_speed_minimum,
                    Settings.getInstance().maths.playerAttributes.attack_speed_variables,p
            );
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.attack_speed_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerMovementSpeed(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.movement_speed_function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.playerAttributes.movement_speed_maximum,Settings.getInstance().maths.playerAttributes.movement_speed_minimum,
                    Settings.getInstance().maths.playerAttributes.movement_speed_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.movement_speed_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerKnockbackResistance(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.knockback_resistance_function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.playerAttributes.knockback_resistance_maximum,Settings.getInstance().maths.playerAttributes.knockback_resistance_minimum,
                    Settings.getInstance().maths.playerAttributes.knockback_resistance_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.knockback_resistance_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerLuck(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.playerAttributes.luck_function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.playerAttributes.luck_maximum,Settings.getInstance().maths.playerAttributes.luck_minimum,
                    Settings.getInstance().maths.playerAttributes.luck_variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.playerAttributes.luck_function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getPlayerDropPercentage(LowbrainPlayer p){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerDies.function)) {
            return Helper.ValueFromFunction(Settings.getInstance().maths.onPlayerDies.items_drops_maximum,Settings.getInstance().maths.onPlayerDies.items_drops_minimum,
                    Settings.getInstance().maths.onPlayerDies.variables,p);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerDies.function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,p));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }

    //ON PLAYER GET DAMAGED

    public static float getChangeOfRemovingPotionEffect(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.chanceOfRemovingMagicEffect.function)){
            float minChance = Settings.getInstance().maths.onPlayerGetDamaged.chanceOfRemovingMagicEffect.minimum;
            float maxChance = Settings.getInstance().maths.onPlayerGetDamaged.chanceOfRemovingMagicEffect.maximum;

            return Helper.ValueFromFunction(maxChance,minChance,Settings.getInstance().maths.onPlayerGetDamaged.chanceOfRemovingMagicEffect.variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.chanceOfRemovingMagicEffect.function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getReducingPotionEffect(LowbrainPlayer damagee){
        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.function)){
            float min = Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.minimum;
            float max = Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.maximum;
            float range = Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.range;

            float reduction = Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.variables,damagee);

            float minReduction = reduction < (min - range) ? reduction + range : min;

            return Helper.randomFloat(reduction,minReduction);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.reducingBadPotionEffect.function.split(",");
            if(st.length > 1){
                return Helper.eval(Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByFire(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_fire_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_fire_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByFireTick(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_fire_tick_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_tick_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_tick_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_fire_tick_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_fire_tick_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByPoison(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_poison_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_poison_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_poison_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_poison_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_poison_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByWither(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_wither_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_wither_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_wither_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_wither_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_wither_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByContact(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_contact_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_contact_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_contact_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_contact_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_contact_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByFlyIntoWall(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_fly_into_wall_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_fly_into_wall_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_fly_into_wall_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_fly_into_wall_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_fly_into_wall_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByFall(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_fall_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_fall_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_fall_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_fall_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_fall_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByWeapon(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_weapon_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_weapon_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_weapon_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_weapon_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_weapon_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByArrow(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_arrow_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_arrow_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_arrow_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_arrow_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_arrow_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByProjectile(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_projectile_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_projectile_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_projectile_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_projectile_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_projectile_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByMagic(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_magic_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_magic_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_magic_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_magic_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_magic_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedBySuffocation(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_suffocation_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_suffocation_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_suffocation_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_suffocation_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_suffocation_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByDrowning(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_drowning_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_drowning_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_drowning_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_drowning_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_drowning_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByStarvation(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_starvation_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_starvation_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_starvation_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_starvation_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_starvation_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByLightning(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_lightning_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_lightning_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_lightning_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_lightning_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_lightning_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByVoid(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_void_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_void_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_void_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_void_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_void_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByHotFloor(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_hot_floor_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_hot_floor_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_hot_floor_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_hot_floor_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_hot_floor_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByExplosion(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_explosion_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_explosion_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_explosion_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_explosion_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_explosion_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByLava(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_lava_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_lava_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_lava_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_lava_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_lava_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
    public static float getDamagedByDefault(LowbrainPlayer damagee){

        if(Helper.StringIsNullOrEmpty(Settings.getInstance().maths.onPlayerGetDamaged.by_default_function)){
            float max = Settings.getInstance().maths.onPlayerGetDamaged.by_default_maximum;
            float min = Settings.getInstance().maths.onPlayerGetDamaged.by_default_minimum;

            return Helper.ValueFromFunction(max,min,Settings.getInstance().maths.onPlayerGetDamaged.by_default_variables,damagee);
        }
        else{
            String[] st = Settings.getInstance().maths.onPlayerGetDamaged.by_default_function.split(",");
            if(st.length > 1){
                return Helper.eval( Helper.FormatStringWithValues(st,damagee));
            }
            else{
                return Helper.eval(st[0]);
            }
        }
    }
}