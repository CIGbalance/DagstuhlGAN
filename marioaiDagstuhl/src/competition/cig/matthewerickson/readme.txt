This is my submission to the Mario AI Competition 2009.


+++ How to use the agent +++

The file 'matthewerickson.xml' is a WOX file for the agent. The classes needed to run this agent are located in the directory 'matthewerickson'.

I have tested the agent using the MainRun class with a seed of 0 producing a competition score of 12129.767163085939. MainRun was modified to load my agent by adding the following code to createAgentsPool() (and removing the existing agent):

AgentsPool.addAgent(AgentsPool.load("matthewerickson.xml"));


+++ About the Agent +++

The agent was evolved using genetic programming and some simple hard coded detectors. Each program is a tree which evaluates to a collection of four values. These values tell the agent if it should go left or right, press jump or not, press speed or not and finally to press down or not.

The nodes in the tree can be things like addition, subtraction, if-then etc. Some nodes are detectors, e.g. closest enemy, next pit.

A population of 500 was used. Each generation was created by 90% crossbreeding, 9% cloning and 1% mutation.

There is lots of room for improvement. E.g. there is no detector for blocks yet.


+++ Disclaimer +++

This code was written in a hurry. I apologise if it's a bit messy :)