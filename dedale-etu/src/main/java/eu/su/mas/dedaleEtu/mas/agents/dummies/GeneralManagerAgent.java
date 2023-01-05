package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.TankerCoorAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;


/**
 * General Manager agent. It keeps track of all dummy collectors and their positions.
 *
 */
public class GeneralManagerAgent extends AbstractDedaleAgent {

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


        //lb.add(new TrackCollectors(this));
        //lb.add(new SendCollector(this));
        //lb.add(new PetitionTankerCoor(this));
        //lb.add(new GetInfoExplorers(this));


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
 * 				BEHAVIOUR
 *
 **************************************/


/**************************************
* 		Managae Explorers behaviours
 **************************************/
class GetInfoExplorers extends SimpleBehaviour {
    /**
     * When an agent choose to migrate all its components should be serializable
     *
     */
    private static final long serialVersionUID = 9088209402507795289L;

    private boolean finished = false;

    public GetInfoExplorers (final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    @Override
    public void action() {
        // Get the current position and observations of the agent
        String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
        List<String> explorerList = new ArrayList<String>();

        // Process the observations to determine the number of explorers in the map
        if(lobs!=null){
            for(Couple<String,List<Couple<Observation,Integer>>> o:lobs){
                if(o.getRight().size()>0){
                    if(o.getRight().get(0).getLeft().getName().equals("Explorer")){
                        explorerList.add(o.getLeft());
                    }
                }
            }
        }
        // Print the number of explorers in the map
        System.out.println("There are "+explorerList.size()+" explorers in the map");
    }

    @Override
    public boolean done() {
        return finished;
    }
}

/**************************************
 * 		Manage Tankers behaviours
 **************************************/
class TrackCoordinators extends SimpleBehaviour {
    /**
     * When an agent choose to migrate all its components should be serializable
     *
     */
    private static final long serialVersionUID = 9088209402507795289L;

    private boolean finished = false;

    public TrackCoordinators (final AbstractDedaleAgent myagent) {
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

/**************************************
 * 		Manage message behaviours
 **************************************/
class SendInstructions extends SimpleBehaviour {
    /**
     * When an agent choose to migrate all its components should be serializable
     *
     */
    private static final long serialVersionUID = 9088209402507795289L;

    private boolean finished = false;

    public SendInstructions (final AbstractDedaleAgent myagent) {
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
/**************************************
 * 		Manage receive states behaviours
 **************************************/
class ReceiveStats extends SimpleBehaviour {
    /**
     * When an agent choose to migrate all its components should be serializable
     *
     */
    private static final long serialVersionUID = 9088209402507795289L;

    private boolean finished = false;

    public ReceiveStats (final AbstractDedaleAgent myagent) {
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