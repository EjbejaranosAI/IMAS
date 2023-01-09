package eu.su.mas.dedaleEtu.mas.agents.dummies;


import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import def.HashCodeUtil;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.Arrays;
import java.io.Serializable;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Main class for the CollectorAgent
 */
public class CollectorAgent extends AbstractDedaleAgent{

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
		lb.add(new CollectorBehaviour(this));

		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}


	protected void takeDown(){

	}


	class CollectorBehaviour extends TickerBehaviour{
		private static final long serialVersionUID = 9088209402507795289L;
		private static final int BUFFER_SIZE = 8;
        private static final int TICK_TIME = 100;

        private String current_position;

        private List<String> nodeBuffer = new ArrayList<>(BUFFER_SIZE);
        private List<String> potentialTreasures = new ArrayList<>();
        private HashMap<String, Integer> treasureQuant = new HashMap<String, Integer>();
        private HashMap<String, String> treasureType = new HashMap<String, String>();

        private boolean on_mission = false;
        private boolean backing_up = false;
        private int backoff_wait = 0;
        private int mission_step = 0;
        private String conflict_node = null;
        private int conflict_counter = 0; // Count how many iterations we spend blocked. If reached limit, leave mission.
        private List<String> conflict_path = new ArrayList<>();

        private boolean stop_for_help = false;
        private int stop_patiente = 0;

        private List<String> mission_path = new ArrayList<>(Arrays.asList("-116657", "-116656", "-116655", "-116654", "-116653", "-116652", "-116071", "-121367", "-121366", "-121365", "-121364", "-121363", "-121362", "-121361", "-121360", "-121359", "-121358", "-117834"));


		public CollectorBehaviour (final AbstractDedaleAgent myagent) {
			super(myagent, TICK_TIME);
            // TODO: remove this. Debugging purposes
            if (this.myAgent.getLocalName().toString().contains("2")){
                this.mission_path = new ArrayList<>(Arrays.asList("-117834", "-121358", "-121359", "-121360", "-121361", "-121362", "-121363", "-121364", "-121365", "-121366", "-121367", "-116071", "-116652", "-116653", "-116654", "-116655", "-116656", "-116657"));
            }
		}

        private List<String> getRemainingPath(){
            List<String> remaining = new ArrayList<String>();
            for (int i = this.mission_step; i < this.mission_path.size(); i++) {
               remaining.add(this.mission_path.get(i));
            }
            return remaining;
        }

        private void updatePotentialTreasures(){
            List<String> treasures = new ArrayList<>();
            for (HashMap.Entry<String, String> node : this.treasureType.entrySet()) {
                if (((AbstractDedaleAgent) this.myAgent).getMyTreasureType().toString() == node.getValue() &&
                    this.treasureQuant.get(node.getKey()) > 0
                    ){
                    treasures.add(node.getKey());
                }
            }
            this.potentialTreasures = treasures;
        }

        private void receiveMission() {
            MessageTemplate msgTemplate=MessageTemplate.and(
                    MessageTemplate.MatchProtocol("SHARE-PATH"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

            if (msgReceived!=null) {
			    List<String> mission;
				try {
					mission = (List<String>) msgReceived.getContentObject();
                    System.out.println(this.myAgent.getLocalName() + " - Got the mission: " + mission);

                    this.mission_path = mission;
                    this.stop_for_help = false;
                    this.stop_patiente = 0;
                    this.on_mission = true;

				} catch (UnreadableException e) {
					e.printStackTrace();
				}
            }
        }

        private boolean GetStopMessage() {
            MessageTemplate msgTemplate=MessageTemplate.and(
                    MessageTemplate.MatchProtocol("STOP"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

            if (msgReceived!=null) {
                this.stop_for_help = true;
                this.stop_patiente = 4;
                return true;
            }
            return false;
        }

        private void requestExplorerHelp(){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setSender(this.myAgent.getAID());
			List<String> receivers = new ArrayList<>(Arrays.asList("Explo1", "Explo2", "Explo3"));
            for (String agentName : receivers) {
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
			msg.setContent("NeedHelp");
            msg.setProtocol("HELLO");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

        }

        private void sendTreasureRequest(String current_node){
			List<String> receivers = new ArrayList<>(Arrays.asList("Explo1", "Explo2", "Explo3"));
            List<String> request_nodes = new ArrayList<>(this.potentialTreasures);
            request_nodes.add(0, current_node);
            System.out.println(request_nodes);

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setSender(this.myAgent.getAID());
            for (String agentName : receivers) {
                msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            }

            try {
				msg.setContentObject((Serializable) request_nodes);
			} catch (IOException e) {
				e.printStackTrace();
			}
            msg.setProtocol("SHARE-POINTS");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }

        private void sendBlockingInfo(){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setSender(this.myAgent.getAID());
			List<String> receivers = new ArrayList<>(Arrays.asList("Tanker1", "Tanker2", "Collect1", "Collect2", "Collect3", "Collect4", "Explo1", "Explo2", "Explo3"));
            receivers.remove(this.myAgent.getLocalName());
            for (String agentName : receivers) {
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
            try {
				msg.setContentObject((Serializable) getRemainingPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
            msg.setConversationId("Blocked");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }

        private int getBlockingInfo(){
			MessageTemplate msgTemplate=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
            int backup = 0; // 0 for no action, 1 for backup, -1 for winning

			if (msgReceived!=null) {
				String msg_id = (String) msgReceived.getConversationId();
                if (msg_id == "Blocked") {
                    List<String> agentPath;
					try {
						agentPath = (List<String>) msgReceived.getContentObject();
                        this.conflict_path = agentPath;
                        int remaining_len = getRemainingPath().size();
                        // The agent with more nodes to follow should back off
                        if (remaining_len > agentPath.size()){
                            backup = 1;
                        // If tied, just compare their names strings
                        } else if (remaining_len == agentPath.size() && this.myAgent.getLocalName().compareTo(msgReceived.getSender().getLocalName()) < 0) {
                            backup = 1;
                        } else {
                            backup = -1;
                        }
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
                }
            }
            return backup;
        }

        private void solveBlockedPath(){
            sendBlockingInfo();
            int backup = getBlockingInfo();
            if (backup == 1){
                this.backing_up = true;
                // this.conflict_counter = 0; 
            } else if (backup == -1) {
                // this.conflict_counter += 0; 
            } else {
                this.conflict_counter += 1;
            }

            if (this.conflict_counter == 20){
                // if after 10 retries still blocked, leave mission
                this.on_mission = false;
                this.conflict_counter = 0; 
                System.out.println("Im tired of this shit..");
            }
            return;
        }

        private String backOff(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
            // System.out.println(this.myAgent.getLocalName() + " ------ current mission step : " + this.mission_step + " next node to follow: " + this.mission_path.get(this.mission_step) + " current node: " + lobs.get(0));

            // Check if there is any node that does not match the conflict path
            for (int i = 1; i < lobs.size(); i++) {
                String node = lobs.get(i).getLeft();
                if (!this.conflict_path.contains(node) && node != this.conflict_node && (((AbstractDedaleAgent)this.myAgent).moveTo(node))){
                    // Solve conflict!
                    this.conflict_counter = 0;
                    this.backing_up = false;
                    this.conflict_node = null;
                    if (this.mission_step > 1){
                        this.mission_step -= 1;
                    } else {
                        // Add nodes if we are in the beginning of the mission path
                        this.mission_path.add(0, lobs.get(0).getLeft());
                    }
                    this.backoff_wait = 3; // Wait two cycles to let the other agent pass
                    System.out.println("Som uns cracks! Ho hem solucionat anem cap al node " + node + ". Next step will be to go back at: " + this.mission_path.get(this.mission_step));
                    return node;
                }
            }
            // System.out.println("I couldnt find any escape");

            // If all available nodes conflict: go back
            String prev_node = null;
            Boolean moved = false;

            // If no more mission path to backtrace, just move back where you can
            if (this.mission_step == 1){
                for (int j = 1; j < lobs.size(); j++) {
                    String node = lobs.get(j).getLeft();
			        if (((AbstractDedaleAgent)this.myAgent).moveTo(node)){
                        prev_node = node;
                        moved = true;
                        break;
                    }
                }
            } else {
                prev_node = this.mission_path.get(this.mission_step-2);
                // System.out.println(this.myAgent.getLocalName() + " ------ current mission step : " + this.mission_step + " trying to go back one step: " + this.mission_path.get(this.mission_step-1) + " current node: " + lobs.get(0));
			    moved = ((AbstractDedaleAgent)this.myAgent).moveTo(prev_node);
                // System.out.println("moved to " + prev_node + "? " + moved);
            }

            if (moved) {
                // System.out.println("I could move back! " + prev_node);
                this.conflict_node = lobs.get(0).getLeft(); // Setting the node we moved from as the current conflict node
                if (this.mission_step == 1){
                    // We reached end of path. Time to add extra nodes to the mission
                    this.mission_path.add(0, prev_node);
                } else {
                    System.out.println("Decreased the mission step!");
                    this.mission_step -= 1;
                }
            }
            return prev_node;
        }

        private String moveToNode(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
            String next_node = this.mission_path.get(this.mission_step);
            boolean valid = false;
            for (int i = 0; i < lobs.size(); i++) {
                if (lobs.get(i).getLeft().equals(next_node)){
                    valid = true;
                    break;
                }
            }

            // Just in case due random tick behaviours the agent gets out of bounds, instead of dying, go back to random movement.
            if (!valid){
                System.out.println(this.myAgent.getLocalName() + " - The following node from the mission is not valid!! Aborting");
                this.on_mission = false;
                this.mission_step = 0;
                this.mission_path = null;
                return null;
            }

			Boolean moved = ((AbstractDedaleAgent)this.myAgent).moveTo(next_node);
            if (!moved) {
                solveBlockedPath();
                return null;
            }
            this.mission_step += 1;
            if (this.mission_step == mission_path.size()){
                System.out.println(this.myAgent.getLocalName() + " -- Finished path: final node was a treasure.");
                this.on_mission = false;
                this.mission_step = 0;
                this.mission_path = null;
            }

            return next_node;
        }

        private String moveToNextNodeRandom(List<Couple<String,List<Couple<Observation,Integer>>>> lobs){
            //Random move from the current position
            Random r= new Random();
            int moveId=1+r.nextInt(lobs.size()-1); //removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
            String next_node = lobs.get(moveId).getLeft();
            String goal_node = next_node; // select the initial random by default if the following checks fail

            if (!this.nodeBuffer.contains(next_node)){
				// System.out.println("Selected node : " + next_node);
                goal_node = next_node;
            } else {
                for (int i = 1; i < lobs.size(); i++) {
                    next_node = lobs.get(i).getLeft();
                    if (!this.nodeBuffer.contains(next_node)){
						// System.out.println("Selected node : " + i + " " + next_node);
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

			// System.out.println("All nodes in the buffer: " + next_node);
            return goal_node;
        }

        private void shareTreasureInfo(){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setSender(this.myAgent.getAID());
			List<String> receivers = new ArrayList<>(Arrays.asList("Tanker1", "Tanker2", "Collect1", "Collect2", "Collect3", "Collect4"));
            receivers.remove(this.myAgent.getLocalName());
            for (String agentName : receivers) {
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
            try {
                msg.setContentObject((Serializable) this.treasureType);
                msg.setConversationId("Type");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
            try {
                msg.setContentObject((Serializable) this.treasureQuant);
                msg.setConversationId("Quant");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }

        private void mergeTreasureInfo(){
			MessageTemplate msgTemplate=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

			if (msgReceived!=null) {
                Boolean updated = false;
				String msg_id = (String) msgReceived.getConversationId();
                if (msg_id == "Type") {
                    HashMap<String, String> treasureType;
					try {
						treasureType = (HashMap<String, String>) msgReceived.getContentObject();

                        for (HashMap.Entry<String, String> set : treasureType.entrySet()) {
                            if (!this.treasureType.containsKey(set.getKey())) {
                                this.treasureType.put(set.getKey(), set.getValue());
                                updated = true;
                            }
                        }

					} catch (UnreadableException e) {
						e.printStackTrace();
					}

                } else if (msg_id == "Quant") {
                    HashMap<String, Integer> treasureQuant;

					try {
						treasureQuant = (HashMap<String, Integer>) msgReceived.getContentObject();

                        for (HashMap.Entry<String, Integer> set : treasureQuant.entrySet()) {

                            if (!this.treasureQuant.containsKey(set.getKey())) {
                                this.treasureQuant.put(set.getKey(), set.getValue());
                                updated = true;

                            } else if (this.treasureQuant.get(set.getKey()) > set.getValue()) {
                                this.treasureQuant.put(set.getKey(), set.getValue());
                                updated = true;
                            }
                        }

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
                }
                if (updated){
                    System.out.println(this.myAgent.getLocalName() + " merged treasure list from " + msgReceived.getSender().getLocalName());
                }
            }
        }

		@Override
		public void onTick() {
            // Cooldown wait for the backoff
            if (this.backoff_wait > 0){
                this.backoff_wait -= 1;
                return;
            }
            if (this.stop_patiente > 0){
                this.stop_patiente -= 1;
            }else if (this.stop_patiente == 0){
                this.stop_for_help = false;
            }
			//Example to retrieve the current position
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            this.current_position = myPosition;

            if (this.stop_for_help){
                System.out.println(this.myAgent.getLocalName() + "Stopped waiting at " + myPosition);
            }


			if (myPosition!=""){
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				// System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
				// System.out.println(this.myAgent.getLocalName()+" -- at: "+myPosition);
				
				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
                if (this.mission_path != null){
                    // System.out.println(this.myAgent.getLocalName() + " ------ current mission step : " + this.mission_step + " next node to follow: " + this.mission_path.get(this.mission_step) + " current node: " + lobs.get(0));
                }

				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:

                        // System.out.println("Treasure history: " + this.treasureType + ", " + this.treasureQuant);

						// System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());

                        // Try to unlock only if is the agent type of treasure
                        if (o.getLeft() == ((AbstractDedaleAgent) this.myAgent).getMyTreasureType()){
                            Boolean unlock = ((AbstractDedaleAgent) this.myAgent).openLock(o.getLeft());
                            if (unlock) {
                                System.out.println(this.myAgent.getLocalName()+" - The agent unlocked : " + myPosition);
                            }
                        }

                        int grabbed = ((AbstractDedaleAgent) this.myAgent).pick();
                        if (grabbed > 0) {
                            System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+ grabbed);
                            System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						    b=true;
                        }

                        // Add treasure to list if new. Do it after picking to avoid having outdated info
                        if (!this.treasureQuant.containsKey(myPosition)) {
                            this.treasureQuant.put(myPosition, o.getRight() - grabbed);
                            this.treasureType.put(myPosition, o.getLeft().toString());
                        } else { 
                            // Update quantity if it has been modified
                            if (this.treasureQuant.get(myPosition) != o.getRight() - grabbed) {
                                this.treasureQuant.put(myPosition, o.getRight() - grabbed);
                            }
                        }
						break;
					default:
						break;
					}
				}

				//If the agent picked (part of) the treasure
				if (b){
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println("State of the observations after picking "+lobs2);
				}

				//Trying to store everything in the tankers
                List<Couple<Observation, Integer>> backpack_before = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace();
                Boolean contacted = ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tanker1") || ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tanker2");
                List<Couple<Observation, Integer>> backpack_after = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace();
                Boolean delivered = false;
                for (int i = 0; i < backpack_after.size(); i++) {
                    if (backpack_after.get(i).getRight() > backpack_before.get(i).getRight()){
                        delivered = true;
                    }
                }
                if (delivered && contacted) {
				    System.out.println(this.myAgent.getLocalName()+" - The agent delivered his treasure. Backpack at " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace() + " of capacity");
                }

				//Random move from the current position
                String next_node = null;
                if (!this.stop_for_help){
                    if (this.backing_up){
                        next_node = backOff(lobs);
                    } else if (this.on_mission && !this.backing_up){
                        next_node = moveToNode(lobs);
                    } else {
                        next_node = moveToNextNodeRandom(lobs);
                    }
                }

                if (next_node != null){
                    // This means we moved
                    this.conflict_counter = 0;
                    this.current_position = next_node;
                    // System.out.println(this.myAgent.getLocalName() + " - Moved to position: " + this.current_position);
                }

                // Update buffer only if the agent moved and if the new node is not in the buffer
				if (next_node != null && !this.nodeBuffer.contains(next_node)){

					if (this.nodeBuffer.size() == this.BUFFER_SIZE){
						this.nodeBuffer.remove(0);
					}

					this.nodeBuffer.add(next_node);
				}


				// System.out.println(this.myAgent.getLocalName()+" - nodebuffer: " + this.nodeBuffer);

                // Before iteration ends, share and merge TreasureInfo with nearby agents
                shareTreasureInfo();
                mergeTreasureInfo();

                updatePotentialTreasures();
                // Ask for help to explorers if they are nearby and there is info to be sent
                if (!this.on_mission){
                    if (!this.potentialTreasures.isEmpty()){
                        requestExplorerHelp();
                        if (GetStopMessage()){
                            sendTreasureRequest(this.current_position);
                        }
                    }
                    receiveMission();
                }
			}
		}
	}
}
