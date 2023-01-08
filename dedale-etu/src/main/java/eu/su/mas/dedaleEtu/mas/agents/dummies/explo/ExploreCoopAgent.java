package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;

/**
 * <pre>
 * ExploreCoop agent.
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *   - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *
 * It stops when all nodes have been visited.
 *
 *
 *  </pre>
 *
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;




	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time.
	 * 			1) set the agent attributes
	 *	 		2) add the behaviours
	 *
	 */
	protected void setup(){

		super.setup();

		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		List<String> list_agentNames=new ArrayList<String>();

		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}


		List<Behaviour> lb=new ArrayList<Behaviour>();

		/************************************************
		 *
		 * ADD the behaviours of the Dummy Moving Agent
		 *
		 ************************************************/

		lb.add(new ExploCoopBehaviour(this,this.myMap,list_agentNames));
		//add the behaviours to comunicate the path to the other agents
		lb.add(new SendPathBehaviour(this,this.myMap,list_agentNames));




		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */


		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}



}



// new class to send the path to the other agents
public class SendPathBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567689731496787661L;
	private boolean finished = false;
	private MapRepresentation myMap;
	private List<String> list_agentNames;

	public SendPathBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> list_agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=list_agentNames;
	}

	@Override
	public void action() {
		//send the path to the other agents
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("PATH");
		msg.setLanguage("English");
		msg.setOntology("PATH");
		msg.setContent(this.myMap.getPath().toString());
		for (String agentName : list_agentNames) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		}
		this.myAgent.send(msg);
		finished = true;
	}

	@Override
	public boolean done() {
		
		return finished;
	}

}