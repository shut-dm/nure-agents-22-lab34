package shut.nure.agents.lab34.jade;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static shut.nure.agents.lab34.jade.WumpusConstants.*;

/**
 * Speleologist agent.
 */
public class SpeleologistAgent extends Agent {

    private static final Pattern PERCEPT_PATTERN =  Pattern.compile("Percept\\(\\[(.*)\\], (.+)\\)");

    private AID envID;

    private AID navigatorID;

    private ActionParser actionParser = new RegExpActionParser();

    private PerceptFormatter perceptFormatter = new RandomDictPerceptFormatter();

    @Override
    protected void setup() {
        // Add a TickerBehaviour that schedules a request to navigator and environment agents to buy cave tour every 5 sec
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                if (envID == null) {
                    System.out.println("Trying to find cave.");
                    // check if  there are already responses from environments
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                            MessageTemplate.MatchProtocol("environment")
                    );
                    ACLMessage msg = myAgent.receive(mt);
                    if (msg != null) {
                        envID = msg.getSender();
                        System.out.println("Picked up environment agent: " + envID);
                    } else {
                        // if no response then send requests to them
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("cave-tour-selling");
                        template.addServices(sd);
                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            System.out.println("Found the following environments:");
                            AID[] envs = new AID[result.length];
                            for (int i = 0; i < result.length; ++i) {
                                envs[i] = result[i].getName();
                                System.out.println(envs[i].getName());
                            }
                            int envIdx = ThreadLocalRandom.current().nextInt(0, envs.length);
                            AID id = envs[envIdx];
                            System.out.println("Trying to pick up environment: " + envs[envIdx]);

                            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                            cfp.addReceiver(id);
                            cfp.setContent(actionToString(EnvActionType.RENT_CAVE));
                            myAgent.send(cfp);
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                }
                if (navigatorID == null) {
                    System.out.println("Trying to find navigator.");
                    // check if  there are already responses from navigators
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                            MessageTemplate.MatchProtocol("navigation")
                    );
                    ACLMessage msg = myAgent.receive(mt);
                    if (msg != null && "OK".equals(msg.getContent())) {
                        navigatorID = msg.getSender();
                        System.out.println("Picked up navigator agent: " + navigatorID);
                    } else {
                        // if no response then send requests to them
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("cave-navigation");
                        template.addServices(sd);
                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            System.out.println("Found the following navigators:");
                            AID[] navs = new AID[result.length];
                            for (int i = 0; i < result.length; ++i) {
                                navs[i] = result[i].getName();
                                System.out.println(navs[i].getName());
                            }
                            int navIdx = ThreadLocalRandom.current().nextInt(0, navs.length);
                            AID id = navs[navIdx];
                            System.out.println("Trying to pick up navigator: " + id);

                            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                            cfp.setContent(MAKE_DEAL_CMD);
                            cfp.addReceiver(id);
                            myAgent.send(cfp);
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                }

                if (envID != null && navigatorID != null) {
                    // Start cave tour
                    myAgent.addBehaviour(new RequestPerformer());
                    stop();
                }
            }
        } );
    }

    @Override
    protected void takeDown() {
        // no op
        System.out.println("Speleologist agent stopped.");
    }

    private class RequestPerformer extends CyclicBehaviour {

        private NavigationStep step = NavigationStep.ENV_REQUEST;

        private EnvActionType lastAction = null;

        private Set<EnvPerceptType> lastPercepts = null;

        @Override
        public void action() {
            ACLMessage msg;
            switch (step) {
                case ENV_REQUEST:
                    ACLMessage envPersepReq = new ACLMessage(ACLMessage.REQUEST);
                    envPersepReq.addReceiver(envID);
                    myAgent.send(envPersepReq);
                    step = NavigationStep.ENV_RESPONSE;
                    block();
                    break;
                case ENV_RESPONSE:
                    msg = myAgent.receive(
                            MessageTemplate.and(
                                    MessageTemplate.MatchSender(envID),
                                    MessageTemplate.or(
                                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                                    )
                            )
                    );
                    if (msg != null) {
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            try {
                                lastPercepts = stringToPercepts(msg.getContent());
                                step = NavigationStep.NAV_REQUEST;
                            } catch (RuntimeException e) {
                                System.out.println("Unsupported environment response: " + msg.getContent());
                                step = NavigationStep.ENV_REQUEST;
                            }
                        } else {
                            step = NavigationStep.ENV_REQUEST;
                        }
                    } else {
                        block();
                    }
                    break;
                case NAV_REQUEST:
                    ACLMessage navReq = new ACLMessage(ACLMessage.REQUEST);
                    navReq.addReceiver(navigatorID);
                    navReq.setContent(perceptToNaturalString(lastPercepts));
                    myAgent.send(navReq);
                    step = NavigationStep.NAV_RESPONSE;
                    block();
                    break;
                case NAV_RESPONSE:
                    msg = myAgent.receive(
                            MessageTemplate.MatchSender(navigatorID)
                    );
                    if (msg != null) {
                        if (msg.getPerformative() == ACLMessage.CONFIRM) {
                            lastAction = naturalStringToAction(msg.getContent());
                            step = NavigationStep.ENV_ACTION;
                        } else {
                            step = NavigationStep.NAV_REQUEST;
                        }
                    } else {
                        block();
                    }
                    break;
                case ENV_ACTION:
                    ACLMessage envActReq = new ACLMessage(ACLMessage.CFP);
                    envActReq.addReceiver(envID);
                    envActReq.setContent(actionToString(lastAction));
                    myAgent.send(envActReq);
                    step = NavigationStep.ENV_ACTION_RESULT;
                    block();
                    break;
                case ENV_ACTION_RESULT:
                    msg = myAgent.receive(
                            MessageTemplate.and(
                                    MessageTemplate.MatchSender(envID),
                                    MessageTemplate.or(
                                            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                                            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
                                    )
                            )
                    );
                    if (msg != null) {
                        if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                                && lastAction == EnvActionType.CLIMB
                                && "true".equals(msg.getContent())) {
                            ACLMessage envCfp = new ACLMessage(ACLMessage.CFP);
                            envCfp.addReceiver(envID);
                            envCfp.setContent(actionToString(EnvActionType.FREE_CAVE));
                            myAgent.send(envCfp);

                            ACLMessage navCfp = new ACLMessage(ACLMessage.CFP);
                            navCfp.setContent(BREAK_DEAL_CMD);
                            navCfp.addReceiver(navigatorID);
                            myAgent.send(navCfp);
                            System.out.println("Speleologist agent finishing work...");
                            myAgent.doDelete();
                        }
                        step = NavigationStep.ENV_REQUEST; // regardless of action result
                    } else {
                        block();
                    }
                    break;
                default:
                    assert false : "unsupported state";
            }
        }
    }

    private String perceptToNaturalString(Set<EnvPerceptType> perceptTypes) {
        return perceptFormatter.format(perceptTypes);
    }

    private Set<EnvPerceptType> stringToPercepts(String perceptStr) {
        System.out.println("Speleologist received percepts from environment: " + perceptStr);
        Set<EnvPerceptType> result = EnumSet.noneOf(EnvPerceptType.class);
        final Matcher matcher = PERCEPT_PATTERN.matcher(perceptStr);
        if (matcher.matches()) {
            final Instant perceptTime = Instant.parse(matcher.group(2));
            result = Arrays.stream(matcher.group(1).split(","))
                    .map(String::trim)
                    .filter(((Predicate<String>) String::isEmpty).negate())
                    .map(EnvPerceptType::findByName)
                    .collect(Collectors.toSet());
        } else {
            assert false : "Environment agents uses unknown perception format!";
        }
        
        return result;
    }

    private String actionToString(EnvActionType actionType) {
        return "Action(" + actionType.getName() + ")";
    }

    private EnvActionType naturalStringToAction(String action) {
        return actionParser.parse(action);
    }

    private enum NavigationStep {
        ENV_REQUEST,
        ENV_RESPONSE,
        NAV_REQUEST,
        NAV_RESPONSE,
        ENV_ACTION,
        ENV_ACTION_RESULT
    }
    
}
