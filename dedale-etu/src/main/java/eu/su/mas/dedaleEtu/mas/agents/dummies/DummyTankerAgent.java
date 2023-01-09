package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents.
 * <br/>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 *
 * @author hc
 *
 */
public class DummyTankerAgent extends AbstractDedaleAgent{

	/**
	 *
	 */
	private static final long serialVersionUID = -1784844593772918359L;



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time.
	 * 			1) set the agent attributes
	 *	 		2) add the behaviours
	 *
	 */
	protected void setup(){

		super.setup();

		List<Behaviour> lb=new ArrayList<Behaviour>();
//		lb.add(new TankerBehaviour(this));
		lb.add(new RandomTankerBehaviour(this));

		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
}


/**************************************
 *
 *
 * 				BEHAVIOUR
 *
 *
 **************************************/

class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private static final int BUFFER_SIZE = 80;

	private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 600);
	}

	@Override
	public void onTick() {
		String out = String.valueOf(false);
//		Receive exploration finished message
		String StopMessage = ReceiveStringMessage("DONE");

		if (StopMessage!=null) {
			String[] Splt= StopMessage.split(":",2);
			String spltagent = Splt[0];
			StopMessage = Splt[1];
			if (StopMessage.equals("Exploration finished, route plan done!")){

				// Send message agent type to the explorer who asked to stop
				ArrayList<String> typereceiver = new ArrayList<>(Arrays.asList(spltagent));
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				SendStringMessage(typereceiver,this.myAgent.getLocalName()+":"+myPosition,"SHARE-POS");

				// Go to tanker Behaviour
				this.myAgent.addBehaviour(new TankerBehaviour((AbstractDedaleAgent) this.myAgent));
				stop();
				out = String.valueOf(true);
			}
		}

		if (out.equals(String.valueOf(false))){
			//Example to retrieve the current position
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

			if (myPosition!=""){
				//List of observable from the agent's current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				//Random move from the current position
				String next_node = chooseNextNode(lobs);
				//The move action (if any) should be the last action of your behaviour
				((AbstractDedaleAgent)this.myAgent).moveTo(next_node);
			}
		}
	}

	private String chooseNextNode(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
		//Random move from the current position
		Random r= new Random();
		int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
		String next_node = lobs.get(moveId).getLeft();

		if (!this.nodeBuffer.contains(next_node)){
			return next_node;
		} else {
			for (int i = 1; i < lobs.size(); i++) {
				next_node = lobs.get(i).getLeft();
				if (!this.nodeBuffer.contains(next_node)){
					return next_node;
				}
			}
		}
		return next_node;  // Even if all nodes are visited, it will eventually use one.
	}

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

	private String ReceiveStringMessage(String protocol) {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol(protocol),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

		if (msgReceived!=null) {
			String message = msgReceived.getContent();
			System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgReceived.getSender().getName());
			return message;
		}
		return null;
	}
}

class TankerBehaviour extends TickerBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean accepted = false;

	private static final int BUFFER_SIZE = 80;

	private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

	public TankerBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent, 600);
	}

	@Override
	public void onTick() {
		ArrayList path = (ArrayList) ReceiveObjectMessage("SHARE-FIXPOS");
		if (path != null) {
			for (Object p : path) {
				((AbstractDedaleAgent) this.myAgent).moveTo((String) p);
				System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
				try {
					this.myAgent.doWait(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			stop();
			System.out.println("Out of Tanker behaviour!");
		}

	}

	private String chooseNextNode(List<Couple<String, List<Couple<Observation, Integer>>>> lobs) {
		//Random move from the current position
		Random r = new Random();
		int moveId = 1 + r.nextInt(lobs.size() - 1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
		String next_node = lobs.get(moveId).getLeft();

		if (!this.nodeBuffer.contains(next_node)) {
			return next_node;
		} else {
			for (int i = 1; i < lobs.size(); i++) {
				next_node = lobs.get(i).getLeft();
				if (!this.nodeBuffer.contains(next_node)) {
					return next_node;
				}
			}
		}
		return next_node;  // Even if all nodes are visited, it will eventually use one.
	}

	private Object ReceiveObjectMessage(String protocol) {
		// Receive path from explorer
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol(protocol),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
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

	private String ReceiveStringMessage(String protocol) {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol(protocol),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

		if (msgReceived!=null) {
			String message = msgReceived.getContent();
			System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgReceived.getSender().getName());
			return message;
		}
		return null;
	}

//	@Override
//	public boolean done() {
//		System.out.println("Entering to DONE!!!!!!");
//		return finished;
//	}

}


