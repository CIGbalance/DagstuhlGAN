/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.mario.engine;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.RandomAgent;

/**
 *
 * @author thehedgeify
 */
public class GlobalMetricOptions {
    public static int rolloutDepth = 1;
    public static int numRollouts = 0;
    public static Agent roller = new RandomAgent();
}
