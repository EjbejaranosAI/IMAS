package urv.imas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class FilteringAgent extends Agent {

    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    public class FilteringBehaviour extends SimpleBehaviour {
        private boolean finished = false;
        MessageTemplate messageTemplate = null;
        Agent agent;

        public FilteringBehaviour(Agent agent) {
            MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            MessageTemplate languageFilter = MessageTemplate.MatchLanguage("English");
            messageTemplate = MessageTemplate.and(performativeFilter, languageFilter);
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage aclMessage = agent.blockingReceive(messageTemplate);
            if (aclMessage != null) {
                myLogger.log(Logger.INFO, "Message matching template received: " + aclMessage.getContent());
                finished = true;
            }
        }

        @Override
        public boolean done() {
            return finished;
        }
    }

    @Override
    protected void setup() {
        addBehaviour(new FilteringBehaviour(this));
    }
}
