package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.TankerCoorAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;


/**
 * Collector coordination agent. It keeps track of all dummy collectors and their positions.
 *
 */
public class CollectorCoorAgent extends AbstractDedaleAgent {

		private static final long serialVersionUID = -2991562876411096907L;


	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time.
	 * 			1) set the agent attributes
	 *	 		2) add the behaviours
	 *
	 */
	protected void setup(){
		super.setup();

		//get the parameters given when creating the agent into the object[]
		final Object[] args = getArguments();
		//use them as parameters for your behaviours

		List<Behaviour> lb=new ArrayList<Behaviour>();
		List<DummyCollectorAgent> collectors=new ArrayList<DummyCollectorAgent>();

		/************************************************
		 *
		 * ADD the behaviours of you agent here
		 *
		 ************************************************/
		lb.add(new TrackCollectors(this));
		lb.add(new SendCollector(this));
		lb.add(new PetitionTankerCoor(this));


		/***
		 * MANDATORY TO ALLOW YOUR AGENT(S) TO BE DEPLOYED CORRECTLY WITH DEDALE
		 */

		addBehaviour(new startMyBehaviours(this,lb));

	}


	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	/**
	 * This method is automatically called before migration.
	 * You can add here all the saving you need
	 */
	protected void beforeMove(){
		super.beforeMove();
	}

	/**
	 * This method is automatically called after migration to reload.
	 * You can add here all the info regarding the state you want your agent to restart from
	 *
	 */
	protected void afterMove(){
		super.afterMove();
	}
}

/**************************************
 *
 *
 * 				BEHAVIOUR
 *
 *
 **************************************/

class TrackCollectors extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public TrackCollectors (final AbstractDedaleAgent myagent) {
		super(myagent);
	}

@Override
	public void action() {
		// empty for now
	}

	@Override
	public boolean done() {
		return finished;
	}
}

class SendCollector extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public SendCollector (final AbstractDedaleAgent myagent) {
		super(myagent);
	}

@Override
	public void action() {
		// empty for now
	}

	@Override
	public boolean done() {
		return finished;
	}

}

class PetitionTankerCoor extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public PetitionTankerCoor (final AbstractDedaleAgent myagent) {
		super(myagent);
	}

@Override
	public void action() {
		// empty for now
	}

	@Override
	public boolean done() {
		return finished;
	}

}

