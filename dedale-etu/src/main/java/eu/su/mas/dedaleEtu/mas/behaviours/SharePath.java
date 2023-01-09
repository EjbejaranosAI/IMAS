package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;


import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.TickerBehaviour;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.core.Agent;


public class SharePath extends TickerBehaviour {
    /**
     * When an agent choose to move
     *
     */
    private static final long serialVersionUID = 9088209402507795289L;

    private MapRepresentation myMap;

    ArrayList<String> last_sender;


    public SharePath (Agent myagent, MapRepresentation myMap) {
        super(myagent,100);
        this.myMap=myMap;
        this.myAgent=myagent;

    }

    @Override
    public void onTick() {

        //Handshake
        ArrayList<String> greet = ReceiveStringMessage("HELLO");
        if (greet != null){
            this.last_sender = new ArrayList<>(Arrays.asList(greet.get(1)));
            SendStringMessage(this.last_sender,"I'm Here","STOP");
        }
        //Listen to a path request
        ArrayList<String> points = (ArrayList<String>) ReceiveObjectMessage("SHARE-POINTS",ACLMessage.REQUEST);
        if (points != null){
            List<String> TreasuresPath =new ArrayList<>();
            List<String> temporalPath = new ArrayList<>();
            Integer minSize = 500;
            for (Integer i = 0; i < points.size()-1; i++ ) {
                try {
                    temporalPath = this.myMap.getShortestPath(points.get(0), points.get(i+1));
                    if(minSize >= temporalPath.size()){
                        TreasuresPath = temporalPath;
                        minSize = temporalPath.size();
                    }
                } catch(Exception e){
                    System.out.println("No solution for this goal");
                }
            }

            //Send path to agent
            if (!TreasuresPath.isEmpty()){
                SendObjectMessage(this.last_sender, TreasuresPath,ACLMessage.INFORM);
            }
        }

    }

    /**********************************************************
     * 	           SENDING AND RECEIVING MESSAGES
     *    @param Receivers
     *    @param message
     **********************************************************/
    private void SendStringMessage(ArrayList<String> Receivers, String message, String protocol) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(protocol);
        msg.setSender(this.myAgent.getAID());
        for (String agentName : Receivers) {
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        msg.setContent(message);
        System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContent()+" - To: "+ Receivers);
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

    /*********************************************************
     * 	           RECEIVING MESSAGES
     * @return message
     *******************************************************/


    private ArrayList<String> ReceiveStringMessage(String protocol) {
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol(protocol),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

        if (msgReceived!=null) {
            String message = msgReceived.getContent();
            System.out.println(this.myAgent.getLocalName() + " received the message --> " + message+" by: "+msgReceived.getSender().getName());
            ArrayList<String> res =new ArrayList<>(Arrays.asList(message,msgReceived.getSender().getLocalName()));
            return res;
        }
        return null;
    }

    /*********************************************************
     * 	           RECEIVING OBJECTS
     * @return message
     *******************************************************/
    private Object ReceiveObjectMessage(String protocol, Object performative) {
        // Receive path from explorer
        MessageTemplate msgTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(protocol),
                MessageTemplate.MatchPerformative((Integer) performative));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        //					System.out.println("TankerBehaviour msgReceived: " + msgReceived);
        ArrayList<List> paths = null;
        if (msgReceived != null) {
            //					System.out.println("TankerBehaviour msgReceived INDISE: " + msgReceived);
            try {
                paths = (ArrayList<List>) msgReceived.getContentObject();
                System.out.println(this.myAgent.getLocalName() + " received the message --> " + paths);
            } catch (UnreadableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return paths;
        }
        return null;
    }

    /*********************************************************
     * 	           SENDING OBJECTS
     * @return message
     *******************************************************/
    private void SendObjectMessage(ArrayList<String> Receivers, Object message, Object performative) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-PATH");
        msg.setPerformative((Integer) performative);
        msg.setSender(this.myAgent.getAID());
        for (String agentName : Receivers) {
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        try {
            msg.setContentObject((Serializable) message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println(this.myAgent.getLocalName()+" sent the message --> "+ msg.getContentObject());
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }
}
