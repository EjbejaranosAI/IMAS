package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.CollectorCoorAgent;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;


/**
 * Explore coordination agent. It keeps track of all dummy explorer and their positions.
 *
 */
public class ExplorerCoorAgent extends AbstractDedaleAgent {

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
		List<DummyTankerAgent> explorers=new ArrayList<DummyTankerAgent>();

		/************************************************
		 *
		 * ADD the behaviours of you agent here
		 *
		 ************************************************/
		lb.add(new TrackExplorers(this));
		lb.add(new SendExplorer(this));
		lb.add(new ReceiveMapUpdate(this));
		lb.add(new ReceiveTreasureLocation(this));
		lb.add(new PetitionCollectorCoor(this));


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

class TrackExplorers extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public TrackExplorers (final AbstractDedaleAgent myagent) {
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

class SendExplorer extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public SendExplorer (final AbstractDedaleAgent myagent) {
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

class ReceiveMapUpdate extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public ReceiveMapUpdate (final AbstractDedaleAgent myagent) {
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

class ReceiveTreasureLocation extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public ReceiveTreasureLocation (final AbstractDedaleAgent myagent) {
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

class PetitionCollectorCoor extends SimpleBehaviour {
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	public PetitionCollectorCoor (final AbstractDedaleAgent myagent) {
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


