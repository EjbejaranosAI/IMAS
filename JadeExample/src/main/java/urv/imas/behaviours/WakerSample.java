package urv.imas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.util.Logger;

public class WakerSample extends Agent {
    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    @Override
    protected void setup() {
        addBehaviour(new WakerBehaviour(this, 250) {
            @Override
            protected void onWake() {
                myLogger.log(Logger.INFO, "One shot task after a 250 ms delay");
            }
        });
    }
}
