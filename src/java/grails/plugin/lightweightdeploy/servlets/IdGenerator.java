package grails.plugin.lightweightdeploy.servlets;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random IDs that are "user friendly"
 */
public class IdGenerator {

    /**
     * Generates a new random ID.
     *
     * @return random ID string.
     */
    public String generate() {
        // can't use nextLong as that can return negative numbers
        long r = (long) (ThreadLocalRandom.current().nextDouble() * Long.MAX_VALUE);
        return Long.toString(r, 36);
    }

}
