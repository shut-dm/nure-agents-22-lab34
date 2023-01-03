package shut.nure.agents.lab34.jade;

import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import shut.nure.agents.lab34.agent.impl.SimpleAgent;
import shut.nure.agents.lab34.wumpusworld.WumpusCave;
import shut.nure.agents.lab34.wumpusworld.WumpusEnvironment;
import shut.nure.agents.lab34.wumpusworld.WumpusPercept;

/**
 * Wumpus world environment agent.
 */
public class EnvironmentAgent extends Agent {

    private static final String SERVICE_NAME = "Wumpus-tour-trading";
    private ReentrantLock cavesNumberLock = new ReentrantLock();
    private int freeCavesNumber = 10;
    private ConcurrentMap<AID, WumpusEnvironment> envs = new ConcurrentHashMap<>();

    @Override
    protected void setup() {
        registerInDF();

        // Add the behaviour serving cave requests from speleologist agents
        addBehaviour(new SpeleologistRequestsServer());
    }

    private void registerInDF() {
        // Register the cave-tour-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("cave-tour-selling");
        sd.setName(SERVICE_NAME);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        // no op
        System.out.println("Environment agent stopped.");
    }

    private boolean acquireCave(AID aid) {
        return acquireCave(aid, ThreadLocalRandom.current().nextInt(0, 2));
    }

    private boolean acquireCave(AID aid, int caveType) {
        cavesNumberLock.lock();
        try {
            if (freeCavesNumber > 0) {
                freeCavesNumber--;
                final WumpusCave cave = createCave(caveType);
                final WumpusEnvironment env = new WumpusEnvironment(cave);
                env.addAgent(new SimpleAgent<>());
                envs.put(aid, env);
                System.out.println("Cave rent by speleologist: " + aid);
                System.out.println(cave.toString());
                return true;
            }
        } finally {
            cavesNumberLock.unlock();
        }
        return false;
    }

    private boolean freeCave(AID aid) {
        cavesNumberLock.lock();
        try {
            if (envs.containsKey(aid)) {
                final WumpusEnvironment env = envs.remove(aid);
                freeCavesNumber++;
                System.out.println("Cave vacated by speleologist: " + aid);
                System.out.println(SERVICE_NAME + " earned enough money, so we are closing today...");
                doDelete();
                return true;
            }
        } finally {
            cavesNumberLock.unlock();
        }
        return false;
    }

    private WumpusCave createCave(int caveType) {
        WumpusCave cave;
        switch (caveType) {
            case 0:
                // from Figure 7.2 A typical wumpus world.
                cave = new WumpusCave(4, 4, ""
                        + ". . . P "
                        + "W G P . "
                        + ". . . . "
                        + "S . P . ");
                break;
            case 1:
                cave = new WumpusCave(4, 4, ""
                        + ". W . G "
                        + ". P . P "
                        + ". . . . "
                        + "S . . . ");
                break;
            default:
                throw new IllegalArgumentException("Unsupported cave type");
        }
        return cave;
    }

    private class SpeleologistRequestsServer extends CyclicBehaviour {
        
        private Pattern ACTION_PATTERN = Pattern.compile("Action\\((.+)\\)");

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
                        try {
                            final Matcher matcher = ACTION_PATTERN.matcher(msg.getContent());
                            if (matcher.matches()) {
                                EnvActionType actionType = EnvActionType.findByName(matcher.group(1));
                                myAgent.addBehaviour(new SpeleologistActionPerformer(msg, actionType));
                            } else {
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                                reply.setContent("unsupported-action-format");
                            }
                        } catch (IllegalArgumentException e) {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                            reply.setContent("unsupported-action");
                        }
                        break;
                    case ACLMessage.REQUEST:
                        try {
                            myAgent.addBehaviour(new SpeleologistRequestPerformer(msg));
                        } catch (IllegalArgumentException e) {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                            reply.setContent("unsupported-request");
                        }
                        break;
                    default:
                        assert false : "Unsupported operation";
                }


                ACLMessage reply = msg.createReply();

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class SpeleologistActionPerformer extends Behaviour {

        private final ACLMessage actionMessage;

        private final AID speleologistID;

        private final EnvActionType actionType;

        public SpeleologistActionPerformer(ACLMessage actionMessage, EnvActionType actionType) {
            this.actionMessage = actionMessage;
            this.actionType = actionType;
            this.speleologistID = actionMessage.getSender();
        }

        @Override
        public void action() {
            ACLMessage reply = actionMessage.createReply();
            switch (actionType) {
                case RENT_CAVE:
                    if (envs.containsKey(speleologistID)) {
                        // already in cave
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("already-in-cave");
                    } else if (acquireCave(speleologistID)) {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setProtocol("environment");
                        reply.setContent("OK");
                    } else {
                        // no caves left
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("no-free-caves");
                    }
                    myAgent.send(reply);
                    break;
                case FREE_CAVE:
                    if (freeCave(speleologistID)) {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("OK");
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("not-in-cave");
                    }
                    myAgent.send(reply);
                    break;
                // Wumpus actions
                case CLIMB:
                case GRAB:
                case FORWARD:
                case TURN_LEFT:
                case TURN_RIGHT:
                case SHOOT:
                    final WumpusEnvironment env = envs.get(speleologistID);
                    if (env == null) {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("not-in-cave");
                    } else {
                        System.out.println("Environment received action from speleologist: " + actionType.getName());
                        final shut.nure.agents.lab34.agent.Agent<?, ?> agent = getWumpusAgent(env);
                        env.execute(agent, actionType.getWumpusAction());
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent(actionType == EnvActionType.CLIMB ? Boolean.toString(env.isGoalGrabbed()) : "OK");
                    }
                    myAgent.send(reply);
                    break;
                default:
                    assert false : "Unsupported action";
            }
        }

        @Override
        public boolean done() {
            // no op
            return true;
        }
    }

    private static shut.nure.agents.lab34.agent.Agent<?, ?> getWumpusAgent(WumpusEnvironment env) {
        return env.getAgents().iterator().next();
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
            final WumpusEnvironment env = envs.get(speleologistID);
            if (env == null) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("not-in-cave");
            } else {
                final shut.nure.agents.lab34.agent.Agent<?, ?> agent = getWumpusAgent(env);
                final WumpusPercept percept = env.getPerceptSeenBy(agent);
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(perceptToString(percept));
            }
            myAgent.send(reply);
        }

        @Override
        public boolean done() {
            // no op
            return true;
        }

        private String perceptToString(WumpusPercept percept) {
            EnumSet<EnvPerceptType> envPercept = EnvPerceptType.fromWumpusPercept(percept);
            return "Percept([" + envPercept.stream()
                    .map(EnvPerceptType::getName)
                    .collect(Collectors.joining(",")) +
            "], " + Instant.now() + ")";
        }
    }
}
