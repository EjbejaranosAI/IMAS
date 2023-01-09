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

		System.out.println("++++++++++++++++The  agent "+this.getLocalName()+ " is started++++++++++++++++++++++++");


	}

	/**************************************
	 *
	 *
	 * 				BEHAVIOUR
	 *
	 *
	 **************************************/

	public static class HelloPath extends TickerBehaviour {
		/**
		 * When an agent choose to move
		 *
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished = false;
		private final MapRepresentation myMap;
		private final List<Couple<String, List<Couple<Observation, Integer>>>> treasures;
		private ArrayList<String> CoallParticipant;

		private ArrayList<String> DoneReceivers;


		public HelloPath (final AbstractDedaleAgent myagent, MapRepresentation myMap, List<Couple<String, List<Couple<Observation, Integer>>>> treasures) {
			super(myagent,600);
			this.myMap=myMap;
			this.myAgent=myagent;
			this.treasures=treasures;
			this.CoallParticipant=new ArrayList<>(Arrays.asList("Tanker")); // add collector
			this.DoneReceivers= new ArrayList<>(Arrays.asList("Tanker1", "Tanker2","Collector1","Collector2"));

		}



		@Override
		public void onTick() {
			//Example to retrieve the current position
			Set<Couple<String, List<Couple<Observation, Integer>>>> set = new HashSet<>(this.treasures);
			this.treasures.clear();
			this.treasures.addAll((Collection<? extends Couple<String, List<Couple<Observation, Integer>>>>) set);


			// Pruebas de path, desde explorer position
//			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
//			System.out.println(this.myAgent.getLocalName()+" HolaTothom");
//			System.out.println("Treasures List: "+this.treasures);
//			System.out.println("Current position: "+myPosition);
//
//			ArrayList<List> newPath=new ArrayList<>();
//			newPath.add(this.myMap.getShortestPath(myPosition,this.treasures.get(0).getLeft()));
//			for (Integer i = 0; i < (this.treasures.size()-1)/2; i++ ) {
//				newPath.add(this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
//				System.out.println("Path " + i +": "+ this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
//			}
//
//			System.out.println("NewPath: "+newPath);


			if(!this.CoallParticipant.isEmpty()){
				// Notify to stop, waiting the responses
				// TODO Make DoneReceiver dynamic list
				SendStringMessage(DoneReceivers,this.myAgent.getLocalName()+":Exploration finished, route plan done!");

				// Listening to messages until list is finished, (waiting for agents names)
				String NameReceived = ReceiveStringMessage();

				if (NameReceived != null) {
					ArrayList<String> confirmationReceiver= new ArrayList<>(Arrays.asList(NameReceived));
					if (NameReceived.contains("Tanker")){
						if (CoallParticipant.remove("Tanker")){ //Removing the agent from the coalitions list
							System.out.println("--------------------------Tanker included in the coalition");
						} else {
							// Notify negation to enter the coalition
							SendStringMessage(confirmationReceiver,"No place in this coalition for you!");
						}
					} else if (NameReceived.contains("Collector")) {
						if (CoallParticipant.remove("Collector")){ //Removing the agent from the coalitions list
							System.out.println("Collector included in the coalition");
						} else {
							// Notify negation to enter the coalition
							SendStringMessage(confirmationReceiver,"No place in this coalition for you!");
						}
					}
				}
			} else{
				// Go to calculate the treasures path
				this.myAgent.addBehaviour(new sendShortestPath((AbstractDedaleAgent) this.myAgent, this.myMap, this.treasures));
				stop();
			}

		}

		/**********************************************************
		 * 	           SENDING AND RECEIVING MESSAGES
		 *    @param Receivers
		 *    @param message
		 **********************************************************/
		private void SendStringMessage(ArrayList<String> Receivers, String message) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TOPO");
			msg.setSender(this.myAgent.getAID());
			for (String agentName : Receivers) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			msg.setContent(message);
			System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContent()+" - To: "+ Receivers);
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}

		/*********************************************************
		 * 	           RECEIVING MESSAGES
		 * @return message
		 *******************************************************/
		private String ReceiveStringMessage() {
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgStringReceived=this.myAgent.receive(msgTemplate);

			if (msgStringReceived!=null) {
				String message = msgStringReceived.getContent();
				System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgStringReceived.getSender().getName());
				return message;
			}
			return null;
		}

		private void CoalitionAcceptance(String msgagentName) {
			ACLMessage Rmsg = new ACLMessage(ACLMessage.INFORM);
			Rmsg.setProtocol("SHARE-TOPO");
			Rmsg.setSender(this.myAgent.getAID());
			ArrayList<String> receivers = new ArrayList<>(Arrays.asList(msgagentName));
			for (String agentName : receivers) {
				Rmsg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			Rmsg.setContent("No place in this coalition for you!");
			System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ Rmsg.getContent()+" - To: "+ msgagentName);
			((AbstractDedaleAgent)this.myAgent).sendMessage(Rmsg);
		}

//		@Override
//		public boolean done() {
//			System.out.println("Entering to DONE!!!!!!");
//			return finished;
//		}

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
			System.out.println("Entered to shortestPath");

			// Send message confirming an agent as part of the coallition
			ArrayList<String> receiversC = new ArrayList<>(Arrays.asList("Tanker1", "Tanker2"));
			SendStringMessage(receiversC, this.myAgent.getLocalName()+":Accepted member of coallition");

			// Receive position of an agent in the coalition
			String message = ReceiveStringMessage();

			if (message!=null) {
				System.out.println("Busca la palabra node:"+message.contains("node"));
				if (message.contains("node")){
					String[] SpltMsg = message.split(":", 3);
					System.out.println("splt var"+SpltMsg);
					String AgName = SpltMsg[0];
					String Pos = SpltMsg[2];
					ArrayList<List> TreasuresPath =new ArrayList<>();
					TreasuresPath.add(this.myMap.getShortestPath(Pos,this.treasures.get(0).getLeft()));
					for (Integer i = 0; i < (this.treasures.size()-1)/2; i++ ) {
						TreasuresPath.add(this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
//						System.out.println("Path " + i +": "+ this.myMap.getShortestPath(this.treasures.get(i).getLeft(),this.treasures.get(i+1).getLeft()));
					}
					System.out.println("Path to treasures in the list: "+ TreasuresPath);

					//Send path to the coalition agent
					ArrayList<String> pathReceiver = new ArrayList<>(Arrays.asList(AgName));
					SendObjectMessage(pathReceiver, TreasuresPath);
//					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//					msg.setProtocol("SHARE-TOPO");
//					msg.setSender(this.myAgent.getAID());
//
//					for (String agentName : receivers) {
//						msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
//					}
//
//					try {
//						msg.setContentObject((Serializable) TreasuresPath);
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//					try {
//						System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContentObject());
//					} catch (UnreadableException e) {
//						throw new RuntimeException(e);
//					}
//					System.out.println("Sent Message Explo path: "+ msg);
//					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

				}
			}
		}

		private void SendObjectMessage(ArrayList<String> Receivers, Object message) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TOPO");
			msg.setSender(this.myAgent.getAID());
			for (String agentName : Receivers) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			try {
				msg.setContentObject((Serializable) message);
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

		private void SendStringMessage(ArrayList<String> Receivers, String message) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TOPO");
			msg.setSender(this.myAgent.getAID());
			for (String agentName : Receivers) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			msg.setContent(message);
			System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContent()+" - To: "+ Receivers);
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}

		private String ReceiveStringMessage() {
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgStringReceived=this.myAgent.receive(msgTemplate);

			if (msgStringReceived!=null) {
				String message = msgStringReceived.getContent();
				System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgStringReceived.getSender().getName());
				return message;
			}
			return null;
		}
	}
}