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
import eu.su.mas.dedaleEtu.princ.Globals;
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
		// lb.add(new PathSharing(this,this.myMap));
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

	public static class PathSharing extends TickerBehaviour {
		/**
		 * When an agent choose to move
		 *
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished = false;
		private final MapRepresentation myMap;

        ArrayList<String> last_sender;


		public PathSharing (final AbstractDedaleAgent myagent, MapRepresentation myMap) {
			super(myagent,Globals.TICK_TIME);
			this.myMap=myMap;
			this.myAgent=myagent;

		}

		@Override
		public void onTick() {

			//Handshake
			ArrayList<String> greet = ReceiveStringMessage("HELLO");
            if (greet != null){
                this.last_sender = new ArrayList<>(Arrays.asList(greet.get(1)));
                SendStringMessage(this.last_sender,"I'm Here","STOP");
            }
			//Listen to a path request
			ArrayList<String> points = (ArrayList<String>) ReceiveObjectMessage("SHARE-POINTS",ACLMessage.REQUEST);
            if (points != null){
                List<String> TreasuresPath =new ArrayList<>();
                List<String> temporalPath = new ArrayList<>();
                Integer minSize = 500;
                for (Integer i = 0; i < points.size()-1; i++ ) {
                    temporalPath =this.myMap.getShortestPath(points.get(0),points.get(i+1));
                    if(minSize >= temporalPath.size()){
                        TreasuresPath = temporalPath;
                        minSize = temporalPath.size();
                    }
                }
                System.out.println("Path to treasures in the list: "+ TreasuresPath);

                //Send path to agent
                SendObjectMessage(this.last_sender, TreasuresPath,ACLMessage.INFORM);
            }

		}

		/**********************************************************
		 * 	           SENDING AND RECEIVING MESSAGES
		 *    @param Receivers
		 *    @param message
		 **********************************************************/
		private void SendStringMessage(ArrayList<String> Receivers, String message, String protocol) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol(protocol);
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


		private ArrayList<String> ReceiveStringMessage(String protocol) {
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol(protocol),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

			if (msgReceived!=null) {
				String message = msgReceived.getContent();
				System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgReceived.getSender().getName());
				ArrayList<String> res =new ArrayList<>(Arrays.asList(message,msgReceived.getSender().getLocalName()));
				return res;
			}
			return null;
		}

		/*********************************************************
		 * 	           RECEIVING OBJECTS
		 * @return message
		 *******************************************************/
		private Object ReceiveObjectMessage(String protocol, Object performative) {
			// Receive path from explorer
			MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol(protocol),
					MessageTemplate.MatchPerformative((Integer) performative));
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			//					System.out.println("TankerBehaviour msgReceived: " + msgReceived);
			ArrayList<List> paths = null;
			if (msgReceived != null) {
				//					System.out.println("TankerBehaviour msgReceived INDISE: " + msgReceived);
				try {
					paths = (ArrayList<List>) msgReceived.getContentObject();
					System.out.println(this.myAgent.getLocalName() + " received the message --> " + paths);
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return paths;
			}
			return null;
		}

		/*********************************************************
		 * 	           SENDING OBJECTS
		 * @return message
		 *******************************************************/
		private void SendObjectMessage(ArrayList<String> Receivers, Object message, Object performative) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-PATH");
			msg.setPerformative((Integer) performative);
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
	}
}
