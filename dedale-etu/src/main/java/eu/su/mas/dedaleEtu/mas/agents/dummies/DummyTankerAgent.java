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

import jade.lang.acl.UnreadableException;
import jade.core.AID;


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


class TankerBehaviour extends TickerBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean accepted = false;

	private static final int BUFFER_SIZE = 8;

	private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

	public TankerBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent, 600);
	}

	private String chooseNextNode(List<Couple<String, List<Couple<Observation, Integer>>>> lobs) {
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

        return goal_node;
	}

	@Override
	public void onTick() {
		System.out.println("Entering to TankerBehaviour!!!!!!");
		String out = String.valueOf(false);

		// Listening to confirmation message of agent name being accepted
		String msgconf = ReceiveStringMessage("SHARE-CONFI");

		if (msgconf != null) {
//			String msgconfir = msgconf.getContent();
//			System.out.println(this.myAgent.getLocalName() + " received the message --> " + msgconfir);

			if (msgconf.contains("Accepted member of coalition")) {
				String[] SpltMsg = msgconf.split(":", 2);
				String AgName = SpltMsg[0];
				String Conf = SpltMsg[1];
				accepted = true;
				String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
				System.out.println("Position:" + myPosition);

				//	Send position to the explorer
				ArrayList<String> receivers = new ArrayList<>(Arrays.asList(AgName));
				SendStringMessage(receivers, this.myAgent.getLocalName() + ":node53146546:" + myPosition,"SHARE-POS");

				ACLMessage msgReceived = null;
				while (msgReceived == null) {
					// Receive path from explorer

					MessageTemplate msgTemplate = MessageTemplate.and(
							MessageTemplate.MatchProtocol("SHARE-PATH"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					msgReceived = this.myAgent.receive(msgTemplate);
	//					System.out.println("TankerBehaviour msgReceived: " + msgReceived);
					ArrayList<List> paths;
					if (msgReceived != null) {
	//					System.out.println("TankerBehaviour msgReceived INDISE: " + msgReceived);
						try {
							paths = (ArrayList<List>) msgReceived.getContentObject();
							System.out.println(this.myAgent.getLocalName() + " received the message --> " + paths);
							for (List path : paths) {
								out = String.valueOf(true);
								for (Object p : path) {
									((AbstractDedaleAgent) this.myAgent).moveTo((String) p);
									System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
									try {
										this.myAgent.doWait(1000);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
	//							if (out == String.valueOf(true)){
	//								stop();
	//								System.out.println("Out of Tanker behaiour!");
	//							}
								stop();
								System.out.println("Out of Tanker behaviour!");
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
	//							e.printStackTrace();
							String ms = msgReceived.getContent();
							System.out.println(this.myAgent.getLocalName() + " received the message --> " + ms);
						}
					}
				}
			} else if (msgconf.contains("No place in this coalition for you!")) {
				System.out.println(this.myAgent.getLocalName() + " going back to random walk");
				for (int i = 0; i <= 3; i++) {
					List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
	//						//The move action (if any) should be the last action of your behaviour
	//						Random r= new Random();
	//						int moveId=1+r.nextInt(lobs.size()-1);
					//Random move from the current position
					String next_node = chooseNextNode(lobs);
                    if (next_node != null && !this.nodeBuffer.contains(next_node)){

                        if (this.nodeBuffer.size() == this.BUFFER_SIZE){
                            this.nodeBuffer.remove(0);
                        }

                        this.nodeBuffer.add(next_node);
                    }

					//The move action (if any) should be the last action of your behaviour
					// ((AbstractDedaleAgent) this.myAgent).moveTo(next_node);
	//						System.out.println(this.myAgent.getLocalName()+" - nodebuffer: " + this.nodeBuffer);
	//						System.out.println(this.myAgent.getLocalName()+" ---- Moving to:  "+lobs.get(moveId).getLeft());
	//						((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
				}
				this.myAgent.addBehaviour(new RandomTankerBehaviour((AbstractDedaleAgent) this.myAgent));
				stop();
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


class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private static final int BUFFER_SIZE = 8;

	private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 60);
	}

	private String chooseNextNode(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
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

        return goal_node;
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
                SendStringMessage(typereceiver,this.myAgent.getLocalName(),"SHARE-NAME");

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
                // System.out.println(this.myAgent.getLocalName() + " - " + next_node);
				//The move action (if any) should be the last action of your behaviour
				// ((AbstractDedaleAgent)this.myAgent).moveTo(next_node);
				if (next_node != null && !this.nodeBuffer.contains(next_node)){

					if (this.nodeBuffer.size() == this.BUFFER_SIZE){
						this.nodeBuffer.remove(0);
					}

					this.nodeBuffer.add(next_node);
				}
			}
		}
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
