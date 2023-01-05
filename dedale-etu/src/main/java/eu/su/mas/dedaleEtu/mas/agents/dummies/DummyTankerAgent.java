package eu.su.mas.dedaleEtu.mas.agents.dummies;


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


class TankerBehaviour extends SimpleBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public TankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		System.out.println("Entering to tanker action!!!!!!");

		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println("Position:" + myPosition);

//			 Send position to the explorer
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
//		System.out.println("Senders name:  "+ this.myAgent.getAID());
		ArrayList<String> receivers = new ArrayList<>(Arrays.asList("Explo1", "Explo2"));
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
//			System.out.println("Recievers name:  "+ agentName + AID.ISLOCALNAME);
		}

		msg.setContent(myPosition);
		System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContent());
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

		ACLMessage msgReceived = null;
		while (msgReceived == null) {
			// Receive path from explorer
			MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			msgReceived = this.myAgent.receive(msgTemplate);
			System.out.println("msgReceived: " + msgReceived);
			ArrayList<List<String>> paths = new ArrayList<>();
			if (msgReceived != null) {
				try {
					paths = (ArrayList<List<String>>) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (List path : paths) {
					for(Object p:path){
						((AbstractDedaleAgent) this.myAgent).moveTo((String) p);
						System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
						try {
							this.myAgent.doWait(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}
		}

		// Dummy message with path
//		ArrayList<List<String>> paths = new ArrayList<>();
//		paths.add(Arrays.asList("2_5", "3_5", "3_4", "3_3", "3_2", "3_1", "3_0"));
//		paths.add(Arrays.asList("2_0", "2_1", "1_1"));
//		paths.add(Arrays.asList("2_1", "2_2", "3_2", "3_3", "4_3", "5_3"));



//		for (List path : paths) {
//			for(Object p:path){
//				((AbstractDedaleAgent) this.myAgent).moveTo((String) p);
//				System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
//				try {
//					this.myAgent.doWait(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//		}
		finished = true;
	}

	@Override
	public boolean done() {
		System.out.println("Entering to DONE!!!!!!");
		return finished;
	}

}


class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 600);
	}

	@Override
	public void onTick() {
		//Example to retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=""){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//The move action (if any) should be the last action of your behaviour
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);
			System.out.println(this.myAgent.getLocalName()+" ---- Moving to:  "+lobs.get(moveId).getLeft());
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());

//			 Receive exploration finished message
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
//		System.out.println("msgReceived: " + msgReceived);

			if (msgReceived!=null) {
				String message = (String) msgReceived.getContent();
				System.out.println(this.myAgent.getLocalName() + " received the message --> " + message);
				if (message.equals("Exploration finished, route plan done!")){
					this.myAgent.addBehaviour(new TankerBehaviour((AbstractDedaleAgent) this.myAgent));
					stop();
				}
			}
		}

	}



}
