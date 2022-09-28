package urv.imas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.util.Logger;

public class SimpleAgent extends Agent {
    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    @Override
    protected void setup() {
        addBehaviour(new SimpleBehaviour() {
            boolean finished = false;

            @Override
            public void action() {
                myLogger.log(Logger.INFO, "Running just once");
                finished = true;
            }
            @Override
            public boolean done() {
                return finished;
            }
        });
    }
}
