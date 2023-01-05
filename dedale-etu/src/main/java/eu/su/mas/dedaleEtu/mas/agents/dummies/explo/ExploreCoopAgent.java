package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;


import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


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
		List<Couple<String, List<Couple<Observation, Integer>>>> treasures = new ArrayList<>();

		/************************************************
		 *
		 * ADD the behaviours of the Dummy Moving Agent
		 *
		 ************************************************/

		lb.add(new ExploCoopBehaviour(this,this.myMap,list_agentNames, treasures));
//		lb.add(new HelloPrint(this));
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */


		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");


	}

	/**************************************
	 *
	 *
	 * 				BEHAVIOUR
	 *
	 *
	 **************************************/

	public static class HelloPath extends SimpleBehaviour {
		/**
		 * When an agent choose to move
		 *
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished = false;
		private final MapRepresentation myMap;
		private final List<Couple<String, List<Couple<Observation, Integer>>>> treasures;

		public HelloPath (final AbstractDedaleAgent myagent, MapRepresentation myMap, List<Couple<String, List<Couple<Observation, Integer>>>> treasures) {
			super(myagent);
			this.myMap=myMap;
			this.myAgent=myagent;
			this.treasures=treasures;
		}



		@Override
		public void action() {
			//Example to retrieve the current position
			Set<Couple<String, List<Couple<Observation, Integer>>>> set = new HashSet<>(this.treasures);
			this.treasures.clear();
			this.treasures.addAll((Collection<? extends Couple<String, List<Couple<Observation, Integer>>>>) set);

			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			System.out.println(this.myAgent.getLocalName()+" HolaTothom");
			System.out.println("Treasures List: "+this.treasures);
			System.out.println("Current position: "+myPosition);

			ArrayList<List> newPath=new ArrayList<>();
			newPath.add(this.myMap.getShortestPath(myPosition,this.treasures.get(0).getLeft()));
			for (Integer i = 0; i < (this.treasures.size()-1)/2; i++ ) {
				newPath.add(this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
				System.out.println("Path " + i +": "+ this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
			}

			System.out.println("NewPath: "+newPath);


////			 Send exploration finished message
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
//		System.out.println("Senders name:  "+ this.myAgent.getAID());
		ArrayList<String> receivers = new ArrayList<>(Arrays.asList("Tanker1", "Tanker2"));
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
//			System.out.println("Recievers name:  "+ agentName + AID.ISLOCALNAME);
		}

			msg.setContent("Exploration finished, route plan done!");
			System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContent());
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

			this.myAgent.addBehaviour(new sendShortestPath((AbstractDedaleAgent) this.myAgent, this.myMap, this.treasures));
			finished = true;
		}

		@Override
		public boolean done() {
			System.out.println("Entering to DONE!!!!!!");
			return finished;
		}

	}

	private static class sendShortestPath extends TickerBehaviour {
		/**
		 * When an agent choose to move
		 *
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private final MapRepresentation myMap;

		private final List<Couple<String, List<Couple<Observation, Integer>>>> treasures;

		public sendShortestPath (final AbstractDedaleAgent myagent, MapRepresentation myMap, List<Couple<String, List<Couple<Observation, Integer>>>> treasures) {
			super(myagent,600);
			this.myMap = myMap;
			this.treasures = treasures;
		}

		@Override
		public void onTick() {
			//			 Receive exploration finished message
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
//			System.out.println("msgReceived: " + msgReceived);

			if (msgReceived!=null) {
				String message = (String) msgReceived.getContent();
				System.out.println(this.myAgent.getLocalName() + " received the message --> " + message);
				if (message.contains("_")){
					ArrayList<List> newPath=new ArrayList<>();
					newPath.add(this.myMap.getShortestPath(message,this.treasures.get(0).getLeft()));
					for (Integer i = 0; i < (this.treasures.size()-1)/2; i++ ) {
						newPath.add(this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
						System.out.println("Path " + i +": "+ this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
					}

					System.out.println("NewPath: "+newPath);


					////			 Send exploration finished message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("SHARE-TOPO");
					msg.setSender(this.myAgent.getAID());
					ArrayList<String> receivers = new ArrayList<>(Arrays.asList("Tanker1", "Tanker2"));
					for (String agentName : receivers) {
						msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
					}

					try {
						msg.setContentObject((Serializable) newPath);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					try {
						System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContentObject());
					} catch (UnreadableException e) {
						throw new RuntimeException(e);
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
		}
	}
}