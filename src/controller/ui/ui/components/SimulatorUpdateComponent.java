package controller.ui.ui.components;

import controller.action.ActionBoard;
import controller.ui.ui.components.AbstractComponent;
import data.PlayerInfo;
import data.states.AdvancedData;
import data.values.Penalties;
import data.values.Side;

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
                            actionAccepted(values[0]);
                        }
                        else { actionRejected(values[0]); }
                        break;
                    case "FINISH":
                        if(ActionBoard.finish.isLegal(data)) {
                            ActionBoard.finish.actionPerformed(null);
                            actionAccepted(values[0]);
                        }
                        else { actionRejected(values[0]); }
                        break;
                    default:
                        break;
                }
                break;
            case "PENALTY":
                team = Integer.parseInt(values[2]);
                robot_number = Integer.parseInt(values[3]);
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
                }
                switch (values[4]) {
                    case "BALL_MANIPULATION":
                        if (ActionBoard.ballManipulation.isLegal(data)) {
                            PlayerInfo pi = data.team[side].player[robot_number];
                            ActionBoard.ballManipulation.performOn(data, pi, side,robot_number);
                            data.isServingPenalty[side][robot_number] = true;
                            actionAccepted(values[0]);
                        }
                        else { actionRejected(values[0]); }
                        break;
                    case "PICKUP":
                    case "INCAPABLE":
                        if (ActionBoard.pickUpHL.isLegal(data)) {
                            PlayerInfo pi = data.team[side].player[robot_number];
                            ActionBoard.pickUpHL.performOn(data, pi, side,robot_number);
                            data.isServingPenalty[side][robot_number] = true;
                            actionAccepted(values[0]);
                        }
                        else { actionRejected(values[0]); }
                        break;
                    case "PHYSICAL_CONTACT":
                        if (ActionBoard.teammatePushing.isLegal(data)) {
                            PlayerInfo pi = data.team[side].player[robot_number];
                            ActionBoard.teammatePushing.performOn(data, pi, side,robot_number);
                            data.isServingPenalty[side][robot_number] = true;
                            actionAccepted(values[0]);
                        }
                        else { actionRejected(values[0]); }
                        break;
                    default:
                        break;
                }
                break;
            case "SCORE":
                team = Integer.parseInt(values[2]);
                if ((int)data.team[0].teamNumber == team) {
                    side = 0;
                }
                else if ((int)data.team[1].teamNumber == team) {
                    side = 1;
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
                }
                //only perform the action if the team is actually on the right side
                if (side == 1) {
                        data.changeSide();
                }
                actionAccepted(values[0]);
                break;
            }
            default:
                actionInvalid(values[0]);
                break;
        }
    }

    private void actionAccepted(String id) {
        returnCommandQueue.add(id + ":OK\n");
    }

    private void actionRejected(String id) {
        returnCommandQueue.add(id + ":ILLEGAL\n");
    }

    private void actionInvalid(String id) {
        returnCommandQueue.add(id + ":INVALID\n");
    }

}