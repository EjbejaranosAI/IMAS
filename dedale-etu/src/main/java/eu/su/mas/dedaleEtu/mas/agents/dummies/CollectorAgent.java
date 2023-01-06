package eu.su.mas.dedaleEtu.mas.agents.dummies;


import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

/**
 * Main class for the CollectorAgent
 */
public class CollectorAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;

	// private List<>



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
		lb.add(new CollectorBehaviour(this));

		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}


	protected void takeDown(){

	}


	class CollectorBehaviour extends TickerBehaviour{
		private static final long serialVersionUID = 9088209402507795289L;
		private static final int BUFFER_SIZE = 8;

        private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);

		public CollectorBehaviour (final AbstractDedaleAgent myagent) {
			super(myagent, 600);
		}

        private String chooseNextNode(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
            //Random move from the current position
            Random r= new Random();
            int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
            String next_node = lobs.get(moveId).getLeft();

            if (!this.nodeBuffer.contains(next_node)){
				// System.out.println("Selected node : " + next_node);
                return next_node;
            } else {
                for (int i = 1; i < lobs.size(); i++) {
                    next_node = lobs.get(i).getLeft();
                    if (!this.nodeBuffer.contains(next_node)){
						// System.out.println("Selected node : " + i + " " + next_node);
                        return next_node;
                    }
                }
            }
			// System.out.println("All nodes in the buffer: " + next_node);
			moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
			next_node = lobs.get(moveId).getLeft();
            return next_node;  // Even if all nodes are visited, it will eventually use one.
        }

		@Override
		public void onTick() {
			//Example to retrieve the current position
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();


			if (myPosition!=""){
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				// System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
				
				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();

				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:

						// System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
						// System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());

                        Boolean g_unlock = ((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD);
                        Boolean d_unlock = ((AbstractDedaleAgent) this.myAgent).openLock(Observation.DIAMOND);
                        if (g_unlock || d_unlock) {
						    System.out.println(this.myAgent.getLocalName()+" - The agent unlocked : " + myPosition);
                        }
                        int grabbed = ((AbstractDedaleAgent) this.myAgent).pick();
                        if (grabbed > 0) {
                            System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+ grabbed);
                            System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						    b=true;
                        }
						break;
					default:
						break;
					}
				}

				//If the agent picked (part of) the treasure
				if (b){
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println("State of the observations after picking "+lobs2);
				}

				//Trying to store everything in the tankers
                Boolean delivered = ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tanker1") || ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tanker2");
                if (delivered) {
				    System.out.println(this.myAgent.getLocalName()+" - The agent delivered his treasure. Backpack at " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace() + " of capacity");
                }

				//Random move from the current position
                String next_node = chooseNextNode(lobs);


				//The move action (if any) should be the last action of your behaviour
				Boolean moved = ((AbstractDedaleAgent)this.myAgent).moveTo(next_node);
                // Update buffer only if the agent moved and if the new node is not in the buffer
				if (!this.nodeBuffer.contains(next_node) && moved){

					if (this.nodeBuffer.size() == this.BUFFER_SIZE){
						this.nodeBuffer.remove(0);
					}

					this.nodeBuffer.add(next_node);
				}

				// System.out.println(this.myAgent.getLocalName()+" - nodebuffer: " + this.nodeBuffer);

                // Some messaging tests
		        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                // msg.setProtocol("SHARE-TOPO");
                msg.setSender(this.myAgent.getAID());
			    msg.addReceiver(new AID("Tanker1",AID.ISLOCALNAME));
                msg.setContent("Panardo");

		        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}

		}

	}
}