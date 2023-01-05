package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.
 * @author hc
 *
 */
public class ShareTreasuresLocBehaviour extends TickerBehaviour{

//	private MapRepresentation myMap;
	private List<Couple<String, List<Couple<Observation, Integer>>>> treasures;
	private List<String> receivers;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.
	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param treasures (the list of treasures locations to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareTreasuresLocBehaviour(Agent a, long period, List<Couple<String, List<Couple<Observation, Integer>>>> treasures, List<String> receivers) {
		super(a, period);
		this.treasures=treasures;
		this.receivers=receivers;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	protected void onTick() {
//		System.out.println("Tratando de enviar la lista de tesoros");
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents.
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
//		System.out.println("Senders name:  "+ this.myAgent.getAID());
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
//			System.out.println("Recievers name:  "+ agentName + AID.ISLOCALNAME);
		}

		try {
			msg.setContentObject((Serializable) this.treasures);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("Mensajeee! Enviado:  "+ msg);
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);


	}

}