package eu.su.mas.dedaleEtu.mas.agents;


import java.util.*;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

import eu.su.mas.dedaleEtu.princ.Globals;
/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents.
 * <br/>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 *
 * @author hc
 *
 */
public class TankerAgent extends AbstractDedaleAgent{

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

		System.out.println("************************The  agent "+this.getLocalName()+ " is started********************");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
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
		super(myagent, Globals.TICK_TIME);
	}

	private String chooseNextNode(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
        //Random move from the current position
        Random r= new Random();
        int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
        String next_node = lobs.get(moveId).getLeft();
        String goal_node = next_node; // select the initial random by default if the following checks fail

        if (!this.nodeBuffer.contains(next_node)){
            goal_node = next_node;
        } else {
            for (int i = 1; i < lobs.size(); i++) {
                next_node = lobs.get(i).getLeft();
                if (!this.nodeBuffer.contains(next_node)){
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

        //Example to retrieve the current position
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

        if (myPosition!=""){
            //List of observable from the agent's current position
            List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
            //Random move from the current position
            String next_node = chooseNextNode(lobs);
            if (next_node != null && !this.nodeBuffer.contains(next_node)){

                if (this.nodeBuffer.size() == this.BUFFER_SIZE){
                    this.nodeBuffer.remove(0);
                }

                this.nodeBuffer.add(next_node);
            }
        }
	}
}
