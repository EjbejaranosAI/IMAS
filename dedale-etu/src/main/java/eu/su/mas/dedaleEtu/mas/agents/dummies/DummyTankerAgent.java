package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import java.util.concurrent.TimeUnit;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

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
		lb.add(new TankerBehaviour(this));

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

		// Send position to the explorer
//		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//		msg.setProtocol("SHARE-TOPO");
//		msg.setSender(this.myAgent.getAID());
////		System.out.println("Senders name:  "+ this.myAgent.getAID());
//		for (String agentName : receivers) {
//			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
////			System.out.println("Recievers name:  "+ agentName + AID.ISLOCALNAME);
//		}
//
//		try {
//			msg.setContentObject((Serializable) this.treasures);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
////		System.out.println("Mensajeee! Enviado:  "+ msg);
//		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

		// Receive path from explorer
//		MessageTemplate msgTemplate=MessageTemplate.and(
//				MessageTemplate.MatchProtocol("SHARE-TOPO"),
//				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
//		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
//		ArrayList<List<String>> paths = new ArrayList<>();
//		if (msgReceived!=null) {
//			try {
//				paths = (ArrayList<List<String>>) msgReceived.getContentObject();
//			} catch (UnreadableException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

		// Dummy message with path
		ArrayList<List<String>> paths = new ArrayList<>();
		paths.add(Arrays.asList("2_5", "3_5", "3_4", "3_3", "3_2", "3_1", "3_0"));
		paths.add(Arrays.asList("2_0", "2_1", "1_1"));
		paths.add(Arrays.asList("2_1", "2_2", "3_2", "3_3", "4_3", "5_3"));



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

//		int n_tank = Character.getNumericValue(this.myAgent.getLocalName().charAt(6));
//		System.out.println(n_tank == 1);
//		if(n_tank == 1) {
//			String target  = "9_9";
//			System.out.println("if tanker 1");
//			List<String> path = Arrays.asList("5_6", "6_6", "6_7", "7_7");
//			for (String p : path) {
//				((AbstractDedaleAgent) this.myAgent).moveTo(p);
//				System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
//				try {
//					this.myAgent.doWait(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		} else if (n_tank == 2) {
//			String target  = "5_5";
//			List<String> path2 = Arrays.asList("0_1", "1_1", "1_2", "2_2");
//			for (String p2 : path2) {
//				((AbstractDedaleAgent) this.myAgent).moveTo(p2);
//				System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p2);
//				try {
//					this.myAgent.doWait(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
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
//			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			//Little pause to allow you to follow what is going on
			//try {
			//	System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			//	System.in.read();
			//} catch (IOException e) {
			//	e.printStackTrace();
			//}

			//list of observations associated to the currentPosition
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
//			System.out.println("-----"+ ((AbstractDedaleAgent) this.myAgent).getMyTreasureType()+"-----");

//			List<String> path = Arrays.asList("5_6", "6_6", "6_7");

			//The move action (if any) should be the last action of your behaviour
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);
//			System.out.println(this.myAgent.getLocalName()+" ---- Moving to:  "+lobs.get(moveId).getLeft());
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());

//			if (path!= null) {
//				for (String p : path) {
//					((AbstractDedaleAgent) this.myAgent).moveTo(p);
//					System.out.println(this.myAgent.getLocalName() + " ---- Moving to:  " + p);
//				}
//				path = (null);
//			} else {
//				//Random move from the current position
//				Random r= new Random();
//				int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
//			}
		}

	}

}

