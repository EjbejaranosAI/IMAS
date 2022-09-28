package urv.imas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.util.Logger;

public class SequentialAgent extends Agent {
    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    @Override
    protected void setup() {
        SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        sequentialBehaviour.addSubBehaviour(new WakerBehaviour(this, 500) {
            @Override
            protected void onWake() {
                myLogger.log(Logger.INFO, "First sub-behaviour");
            }
        });
        sequentialBehaviour.addSubBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                myLogger.log(Logger.INFO, "Second sub-behaviour");
            }
        });
        addBehaviour(sequentialBehaviour);
    }
}
