package lowbrain.mcrpg.Powa;

import lowbrain.mcrpg.commun.RPGPlayer;

/**
 * Created by moo on 2016-07-21.
 */
public class invisibility extends Powa {

    public invisibility(String name) {
        super(name);
    }

    @Override
    public boolean Cast(RPGPlayer from, RPGPlayer to) {
        return false;
    }
}
