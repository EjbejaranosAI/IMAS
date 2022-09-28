package urv.imas;

import jade.core.Agent;
import jade.util.Logger;

public class HelloWorldAgent extends Agent {

    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    @Override
    protected void setup() {
        myLogger.log(Logger.INFO, "Agent " + getAID().getLocalName() + " is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            myLogger.log(Logger.INFO, args[0].toString());
        }

        doDelete();
    }

    @Override
    protected void takeDown() {
        myLogger.log(Logger.INFO, "Agent " + getAID().getLocalName() + " terminating.");
        super.takeDown();
    }
}
