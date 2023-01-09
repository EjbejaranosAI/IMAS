package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.io.ObjectInputStream;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareTreasuresLocBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SharePath;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 *
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.
 * This (non optimal) behaviour is done until all nodes are explored.
 *
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	private boolean explored = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;

	private List<Couple<String, List<Couple<Observation, Integer>>>> treasures = new ArrayList<>();

    private static final int TICK_TIME = 100;
    private int blocked_counter = 0;
    private int random_tmp_steps = 0;

    private static final int BUFFER_SIZE = 8;
    private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

	/**
	 *
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames, List<Couple<String, List<Couple<Observation, Integer>>>> treasures) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.treasures=treasures;
	}

    private void tmpRandomMovement(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
        String next_node = moveToNextNodeRandom(lobs);

        // Update buffer only if the agent moved and if the new node is not in the buffer
        if (next_node != null && !this.nodeBuffer.contains(next_node)){

            if (this.nodeBuffer.size() == this.BUFFER_SIZE){
                this.nodeBuffer.remove(0);
            }

            this.nodeBuffer.add(next_node);
        }
        this.random_tmp_steps -= 1;
    }

    private String moveToNextNodeRandom(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
        //Random move from the current position
        Random r= new Random();
        int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
        String next_node = lobs.get(moveId).getLeft();
        String goal_node = next_node; // select the initial random by default if the following checks fail

        if (!this.nodeBuffer.contains(next_node)){
            // System.out.println("Selected node : " + next_node);
            goal_node = next_node;
        } else {
            for (int i = 1; i < lobs.size(); i++) {
                next_node = lobs.get(i).getLeft();
                if (!this.nodeBuffer.contains(next_node)){
                    // System.out.println("Selected node : " + i + " " + next_node);
                    goal_node = next_node;
                    break;
                }
            }
        }

        Boolean moved = ((AbstractDedaleAgent)this.myAgent).moveTo(goal_node);
        Integer i = 1;
        while (!moved && i < lobs.size()) {
            goal_node = lobs.get(i).getLeft();
            moved = ((AbstractDedaleAgent)this.myAgent).moveTo(goal_node);
            i = i+1;
            // If it enters this loop it means that the agent is blocked. Clearing the buffer will help him move more freely.
            this.nodeBuffer.clear();
        }

        if (!moved) {
            return null;
            
        }

        // System.out.println("All nodes in the buffer: " + next_node);
        return goal_node;
    }

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, TICK_TIME,this.myMap,list_agentNames));
			this.myAgent.addBehaviour(new ShareTreasuresLocBehaviour(this.myAgent, TICK_TIME, this.treasures,list_agentNames));
			this.myAgent.addBehaviour(new SharePath(this.myAgent, this.myMap));
		}
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
            
            if (this.random_tmp_steps > 0){
                tmpRandomMovement(lobs);
                return;
            }
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(100);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			if (!lobs.get(0).getRight().isEmpty()){
                Observation treasure_observed = lobs.get(0).getRight().get(0).getLeft();

                System.out.println(this.myAgent.getLocalName()+" - I try to open the safe : " + treasure_observed + ", " +((AbstractDedaleAgent) this.myAgent).openLock(treasure_observed));

				this.treasures.add(lobs.get(0));
				// System.out.println(this.myAgent.getLocalName()+ ":    "+this.treasures);
			}

			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> CurrentNode=iter.next();
				String nodeId = CurrentNode.getLeft();

				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode() || explored){
				//Explo finished
				explored=true;
				// System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
                nextNode = moveToNextNodeRandom(lobs);
                if (nextNode != null && !this.nodeBuffer.contains(nextNode)){

                    if (this.nodeBuffer.size() == this.BUFFER_SIZE){
                        this.nodeBuffer.remove(0);
                    }

                    this.nodeBuffer.add(nextNode);
                }
                // TODO: Add this behaviour back somehow without stopping
				// this.myAgent.addBehaviour(new ExploreCoopAgent.HelloPath((AbstractDedaleAgent) this.myAgent, this.myMap, this.treasures));
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                     
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					// return until the node that have other open nodes
					// System.out.println(this.myAgent.getLocalName()+"-- nextNode: "+nextNode);
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}

				//5) At each time step, the agent check if he received a graph from a teammate.
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				MessageTemplate msgTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("SHARE-TOPO"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
				if (msgReceived!=null) {
					String mclass;
					try {
						mclass = msgReceived.getContentObject().getClass().getName();

					} catch (UnreadableException e) {
						throw new RuntimeException(e);
					}

					if (mclass == "dataStructures.serializableGraph.SerializableSimpleGraph") {
						SerializableSimpleGraph<String, MapAttribute> sgreceived = null;
						try {

							sgreceived = (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

//						System.out.println("Mensajeeee! Recibido: " + sgreceived);
						this.myMap.mergeMap(sgreceived);
					}
				}

				MessageTemplate msgTreasure=MessageTemplate.and(
						MessageTemplate.MatchProtocol("SHARE-TOPO"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgTreasureReceived=this.myAgent.receive(msgTreasure);
//				System.out.println("msgReceived: " + msgReceived);

				if (msgTreasureReceived!=null) {
					String msgclass;
					try {
						msgclass = msgTreasureReceived.getContentObject().getClass().getName();

					} catch (UnreadableException e) {
						throw new RuntimeException(e);
					}

					if (msgclass == "java.util.ArrayList") {

						List<Couple<String, List<Couple<Observation, Integer>>>> tlreceived = null;
						try {
							tlreceived = (List<Couple<String, List<Couple<Observation, Integer>>>>) msgTreasureReceived.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						//System.out.println("---------------------->Mensajeeee Treasure! Recibido: " + tlreceived);
						this.treasures.addAll(tlreceived);
						Set<Couple<String, List<Couple<Observation, Integer>>>> set = new HashSet<>(this.treasures);
						this.treasures.clear();
						this.treasures.addAll((Collection<? extends Couple<String, List<Couple<Observation, Integer>>>>) set);
						// System.out.println(this.myAgent.getLocalName()+ "----- Merged treasures list: " + this.treasures);
					}
				}

				if (!explored && !((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)){
                    this.blocked_counter += 1;
                    // System.out.println("Blocked during: " + this.blocked_counter);
                    if (this.blocked_counter > 5){
                        System.out.println("I am too sad and blocked");
                        this.blocked_counter = 0;
                        this.random_tmp_steps = 10;

                        // After 5 cycles blocked, Do random walk during 10 steps                    
                    }
                } else {
                    this.blocked_counter = 0;
                }
			}

		}
	}

	@Override
	public boolean done() {
	return finished;
	}

}
