package controller.ui.ui.components;

import controller.action.ActionBoard;
import controller.action.ui.*;
import controller.ui.ui.components.AbstractComponent;
import data.PlayerInfo;
import data.Rules;
import data.states.AdvancedData;
import data.values.Penalties;
import data.values.SecondaryGameStates;
import data.values.Side;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

public class SimulatorUpdateComponent extends AbstractComponent{

    private BlockingQueue<String> commandQueue;
    private BlockingQueue<String> returnCommandQueue;

    public SimulatorUpdateComponent(BlockingQueue<String> commandQueue, BlockingQueue<String> returnCommandQueue) {
        this.commandQueue = commandQueue;
        this.returnCommandQueue = returnCommandQueue;
    }

    @Override
    public void update(AdvancedData data) {
        if(commandQueue.size() > 0) {
            String latest_command = commandQueue.poll();
            handleCommand(latest_command, data);
        }
    }

    private void handleCommand(String command, AdvancedData data) {
        System.out.println("Latest command is : " + command);
        String[] values = command.split(":");
        int team = -1;
        int robot_number = -1;
        int side = -1;
        switch (values[1]) {
            case "STATE":
                handleStateChanged(data, values);
                break;
            case "PENALTY":
                team = Integer.parseInt(values[2]);
                //Internally robot IDs start at 0, for the AutoRef they start at 1
                robot_number = Integer.parseInt(values[3]) - 1;
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                handleRobotPenalty(data, values, robot_number, side);
                break;
            case "SCORE":
                team = Integer.parseInt(values[2]);
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                if(ActionBoard.goalInc[side].isLegal(data)) {
                    ActionBoard.goalInc[side].perform(data);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "KICKOFF":
                team = Integer.parseInt(values[2]);
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                if(ActionBoard.kickOff[side].isLegal(data)) {
                    ActionBoard.kickOff[side].perform(data);
                    if(side == 1) { data.leftSideKickoff = true; }
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "SIDE_LEFT":{
                team = Integer.parseInt(values[2]);
                if ((int) data.team[0].teamNumber == team) {
                    side = 0;
                } else if ((int) data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                //only perform the action if the team is actually on the right side
                if (side == 1) {
                        data.changeSide();
                }
                actionAccepted(values[0]);
                break;
            }
            case "DIRECT_FREEKICK":
            case "INDIRECT_FREEKICK":
            case "PENALTYKICK":
            case "CORNERKICK":
            case "GOALKICK":
            case "THROWIN":
                team = Integer.parseInt(values[2]);
                if ((int) data.team[0].teamNumber == team) {
                    side = 0;
                } else if ((int) data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                System.out.println("Secondary game state info team is " + data.secGameStateInfo.toByteArray()[0]);
                System.out.println("Secondary game state info substate is " + data.secGameStateInfo.toByteArray()[1]);
                if(values.length == 3 && data.secGameStateInfo.toByteArray()[0] == 0
                        // Initiating a game interruption is only legal if none is ongoing at the moment
                        || values.length == 4 && values[3].equals("READY") && data.secGameStateInfo.toByteArray()[0] != 0 && data.secGameStateInfo.toByteArray()[1] == 0
                        // Going into the READY state is only allowed if a game interruption was initiated before
                        || values.length == 4 && values[3].equals("PREPARE") && data.secGameStateInfo.toByteArray()[0] != 0 && data.secGameStateInfo.toByteArray()[1] == 1
                        // Going into the PREPARE state is only allowed if the game interruption was set to READY before
                        || values.length == 4 && values[3].equals("EXECUTE") && data.secGameStateInfo.toByteArray()[0] != 0 && data.secGameStateInfo.toByteArray()[1] == 2
                        // Going into the EXECUTE state is only allowed if the game interruption was set to PREPARE before
                ) {
                    handleGameInterruption(data, values, side);
                } else if (values.length == 4 && values[3].equals("RETAKE")) {
                    if(ActionBoard.retakeGameInterruptions[side].isLegal(data)) {
                        ActionBoard.retakeGameInterruptions[side].perform(data);
                        actionAccepted(values[0]);
                    }
                    else {
                        actionRejected(values[0]);
                        break;
                    }
                } else if (values.length == 4 && values[3].equals("ABORT")) {
                    if(ActionBoard.abortGameInterruptions[side].isLegal(data)) {
                        ActionBoard.abortGameInterruptions[side].perform(data);
                        actionAccepted(values[0]);
                    }
                    else {
                        actionRejected(values[0]);
                        break;
                    }
                }
                else {
                    actionRejected(values[0]);
                    break;
                }
                break;
            case "CARD": {
                team = Integer.parseInt(values[2]);
                //Internally robot IDs start at 0, for the AutoRef they start at 1
                robot_number = Integer.parseInt(values[3]) - 1;
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
                } else {
                    actionRejected(values[0]);
                    break;
                }
                String card_color = values[4];
                handleRobotShownCard(data, values, robot_number, side, card_color);
                break;
            }
            case "DROPPEDBALL": {
                if (ActionBoard.dropBall.isLegal(data)) {
                    ActionBoard.dropBall.perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            default:
                actionInvalid(values[0]);
                break;
        }
    }

    private void handleRobotShownCard(AdvancedData data, String[] values, int robot_number, int side, String card_color_string) {
        Color card_color = null;
        switch (card_color_string) {
            case "WARN":
                card_color = Color.BLUE;
                break;
            case "YELLOW":
                card_color = Color.YELLOW;
                break;
            case "RED":
                card_color = Color.RED;
                break;
            default:
                actionRejected(values[0]);
                break;
        }
        if (card_color != null) {
            CardIncrease card = new CardIncrease(Side.getFromInt(side), robot_number, card_color);
            if(card.isLegal(data)) {
                card.perform(data);
                actionAccepted(values[0]);
            } else {
                actionRejected(values[0]);
            }

        }

    }

    private void handleGameInterruption(AdvancedData data, String[] values, int side) {
        switch (values[1]) {
            case "DIRECT_FREEKICK": {
                if (ActionBoard.directFreeKick[side].isLegal(data)) {
                    ActionBoard.directFreeKick[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            case "INDIRECT_FREEKICK": {
                if (ActionBoard.indirectFreeKick[side].isLegal(data)) {
                    ActionBoard.indirectFreeKick[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            case "PENALTYKICK": {
                if (ActionBoard.penaltyKick[side].isLegal(data)) {
                    ActionBoard.penaltyKick[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            case "CORNERKICK": {
                if (ActionBoard.cornerKick[side].isLegal(data)) {
                    ActionBoard.cornerKick[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            case "GOALKICK": {
                if (ActionBoard.goalKick[side].isLegal(data)) {
                    ActionBoard.goalKick[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            case "THROWIN": {
                if (ActionBoard.throwIn[side].isLegal(data)) {
                    ActionBoard.throwIn[side].perform(data);
                    actionAccepted(values[0]);
                } else {
                    actionRejected(values[0]);
                }
                break;
            }
            default:
                actionRejected(values[0]);
                break;
        }

    }

    private void handleRobotPenalty(AdvancedData data, String[] values, int robot_number, int side) {
        switch (values[4]) {
            case "BALL_MANIPULATION":
                if (ActionBoard.ballManipulation.isLegal(data)) {
                    PlayerInfo pi = data.team[side].player[robot_number];
                    ActionBoard.ballManipulation.performOn(data, pi, side, robot_number);
                    data.isServingPenalty[side][robot_number] = true;
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "PICKUP":
            case "INCAPABLE":
                if (ActionBoard.pickUpHL.isLegal(data)) {
                    PlayerInfo pi = data.team[side].player[robot_number];
                    ActionBoard.pickUpHL.performOn(data, pi, side, robot_number);
                    data.isServingPenalty[side][robot_number] = true;
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "PHYSICAL_CONTACT":
                if (ActionBoard.hlPushing.isLegal(data)) {
                    PlayerInfo pi = data.team[side].player[robot_number];
                    ActionBoard.hlPushing.performOn(data, pi, side, robot_number);
                    data.isServingPenalty[side][robot_number] = true;
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            default:
                actionRejected(values[0]);
                break;
        }
    }

    private void handleStateChanged(AdvancedData data, String[] values) {
        switch (values[2]) {
            case "READY":
                if(ActionBoard.ready.isLegal(data)) {
                    ActionBoard.ready.actionPerformed(null);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "SET":
                if(ActionBoard.set.isLegal(data)) {
                    ActionBoard.set.actionPerformed(null);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "PLAY":
                if(ActionBoard.play.isLegal(data)) {
                    ActionBoard.play.actionPerformed(null);
                    if (data.secGameState != SecondaryGameStates.PENALTYSHOOT) {
                        data.resetPenaltyTimes();
                        data.resetPenalties();
                    }
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "FINISH":
                if(ActionBoard.finish.isLegal(data)) {
                    ActionBoard.finish.actionPerformed(null);
                    data.resetPenaltyTimes();
                    data.resetPenalties();
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "SECOND-HALF":
                if (ActionBoard.secondHalf.isLegal(data)) {
                    ActionBoard.secondHalf.perform(data);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "OVERTIME-FIRST-HALF":
                if (ActionBoard.firstHalfOvertime.isLegal(data)) {
                    ActionBoard.firstHalfOvertime.perform(data);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "OVERTIME-SECOND-HALF":
                if (ActionBoard.secondHalfOvertime.isLegal(data)) {
                    ActionBoard.secondHalfOvertime.perform(data);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            case "PENALTY-SHOOTOUT":
                if (ActionBoard.penaltyShoot.isLegal(data)) {
                    ActionBoard.penaltyShoot.perform(data);
                    actionAccepted(values[0]);
                }
                else { actionRejected(values[0]); }
                break;
            default:
                actionRejected(values[0]);
                break;
        }
    }

    private void actionAccepted(String id) {
        returnCommandQueue.add(id + ":OK\n");
        System.out.println("Message with ID " + id + " was returned with status OK");
    }

    private void actionRejected(String id) {
        returnCommandQueue.add(id + ":ILLEGAL\n");
        System.out.println("Message with ID " + id + " was returned with status ILLEGAL");
    }

    private void actionInvalid(String id) {
        returnCommandQueue.add(id + ":INVALID\n");
        System.out.println("Message with ID " + id + " was returned with status INVALID");
    }

}
