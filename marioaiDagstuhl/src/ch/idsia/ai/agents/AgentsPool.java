package ch.idsia.ai.agents;

import wox.serial.Easy;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 9, 2009
 * Time: 8:28:06 PM
 * Package: ch.idsia.ai.agents
 */

public final class AgentsPool
{
    private static Agent currentAgent = null;

    public static void addAgent(Agent agent) {
        agentsHashMap.put(agent.getName(), agent);
    }

    public static void addAgent(String agentWOXName) throws IllegalFormatException
    {
        addAgent(load(agentWOXName));
    }

    public static Agent load (String name) {
        Agent agent;
        try {
            agent = (Agent) Class.forName (name).newInstance ();
        }
        catch (ClassNotFoundException e) {
            System.out.println (name + " is not a class name; trying to load a wox definition with that name.");
            agent = (Agent) Easy.load (name);
        }
        catch (Exception e) {
            e.printStackTrace ();
            agent = null;
            System.exit (1);
        }
        return agent;
    }

    public static Collection<Agent> getAgentsCollection()
    {
        return agentsHashMap.values();
    }

    public static Set<String> getAgentsNames()
    {
        return AgentsPool.agentsHashMap.keySet();
    }

    public static Agent getAgentByName(String agentName)
    {
        // There is only one case possible;
        Agent ret = AgentsPool.agentsHashMap.get(agentName);
        if (ret == null)
            ret = AgentsPool.agentsHashMap.get(agentName.split(":")[0]);
        return ret;
    }

    public static Agent getCurrentAgent()
    {
        if (currentAgent == null)
            currentAgent = (Agent) getAgentsCollection().toArray()[0];
        return currentAgent;
    }

    public static void setCurrentAgent(Agent agent) {
        currentAgent = agent;
    }

    public static void setCurrentAgent(String agentWOXName)
    {
        setCurrentAgent(AgentsPool.load(agentWOXName));
    }

    static HashMap<String, Agent> agentsHashMap = new LinkedHashMap<String, Agent>();
}
