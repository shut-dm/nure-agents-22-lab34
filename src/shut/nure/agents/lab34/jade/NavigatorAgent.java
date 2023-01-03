package shut.nure.agents.lab34.jade;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import shut.nure.agents.lab34.wumpusworld.EfficientHybridWumpusAgent;
import shut.nure.agents.lab34.wumpusworld.WumpusAction;
import shut.nure.agents.lab34.wumpusworld.WumpusPercept;

import static shut.nure.agents.lab34.jade.WumpusConstants.*;

/**
 * Navigator agent.
 */
public class NavigatorAgent extends Agent {

    private AID speleologistID = null;

    private shut.nure.agents.lab34.agent.Agent<WumpusPercept, WumpusAction> agent = null;

    private ActionFormatter actionFormatter = new RandomDictActionFormatter();

    private PerceptParser perceptParser = new RegExpPerceptParser();

    @Override
    protected void setup() {
        registerInDF();

        // Add the behaviour serving requests from speleologist agents
        addBehaviour(new SpeleologistRequestsServer());
    }

    private void registerInDF() {
        // Register the cave-navigation selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("cave-navigation");
        sd.setName("Wumpus-cave-navigation");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class SpeleologistRequestsServer extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                switch (msg.getPerformative()) {
                    case ACLMessage.CFP:
                        ACLMessage reply = msg.createReply();
                        reply.setProtocol("navigation");
                        if (MAKE_DEAL_CMD.equals(msg.getContent())) {
                            if (speleologistID == null) {
                                speleologistID = msg.getSender();
                                initWumpusAgent();
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setContent("OK");
                            } else {
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                reply.setContent("already-serving-speleologist");
                            }
                        } else if (BREAK_DEAL_CMD.equals(msg.getContent())) {
                            if (Objects.equals(speleologistID, msg.getSender())) {
                                System.out.println("Enough for navigation today..");
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setContent("done");
                                myAgent.send(reply);
                                doDelete();
                                return;
                            } else {
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                reply.setContent("unknown-speleologist");
                            }
                        } else {
                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            reply.setContent("unknown-request");
                        }
                        myAgent.send(reply);
                        break;
                    case ACLMessage.REQUEST:
                        myAgent.addBehaviour(new SpeleologistRequestPerformer(msg));
                        break;
                    default:
                        assert false : "Unsupported operation";
                }
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        // no op
        System.out.println("Navigator agent stopped.");
    }

    private void initWumpusAgent() {
        agent = new EfficientHybridWumpusAgent(
                WumpusConstants.MAP_WIDTH,
                WumpusConstants.MAP_HEIGHT,
                WumpusUtils.createStartPosition()
        );
    }

    private class SpeleologistRequestPerformer extends Behaviour {
        private final ACLMessage requestMessage;

        private final AID speleologistID;

        public SpeleologistRequestPerformer(ACLMessage requestMessage) {
            this.requestMessage = requestMessage;
            this.speleologistID = requestMessage.getSender();
        }

        @Override
        public void action() {
            ACLMessage reply = requestMessage.createReply();
            if (Objects.equals(NavigatorAgent.this.speleologistID, speleologistID)) {
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent(navigate(requestMessage.getContent()));
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("wrong-speleologist-id");
            }
            myAgent.send(reply);
        }

        private String navigate(String requestMsg) {
            System.out.println("Navigator received request: " + requestMsg);
            final Set<EnvPerceptType> percepts = perceptParser.parse(requestMsg);
            final WumpusPercept wumpusPercept = EnvPerceptType.toWumpusPercept(percepts);
            final Optional<WumpusAction> wumpusAction = agent.act(wumpusPercept);
            final EnvActionType action = EnvActionType.findByWumpusAction(wumpusAction.get());
            final String navigationMsg = actionFormatter.format(action);
            System.out.println("Navigator sent response: " + navigationMsg);
            return navigationMsg;
        }

        @Override
        public boolean done() {
            return true;
        }
    }

}
