package controller.action.ui;

import common.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.Rules;
import data.states.AdvancedData;
import data.values.GameStates;
import data.values.SecondaryGameStates;

/**
 * @author Ludovic Hofer
 *
 * This class allows to represents the action for all the game interruptions which behaves similarly such as
 * - Penalty Kick
 * - Direct Free Kick
 * - Indirect Free Kick
 * - Corner Kick
 * - Goal Kick
 * - Throw-in
 */
public class GameInterruption extends GCAction {

    /**
     * Which side has the advantage with this game interruption
     */
    private int side;

    /**
     * Secondary Game state associated with the action
     */
    private SecondaryGameStates secGameState;

    public GameInterruption(int side, SecondaryGameStates secGameState) {
        super(ActionType.UI);
        this.side = side;
        this.secGameState = secGameState;
    }

    /**
     * @return true if the game_interruption is in progress
     */
    public boolean isActive(AdvancedData data) {
        return data.secGameState == secGameState && data.secGameStateInfo.toByteArray()[0] == data.team[side].teamNumber;

    }

    @Override
    public void perform(AdvancedData data)
    {

        // If action has not been started, change secondary game state and initialize the status
        if (!isActive(data)) {
            data.previousSecGameState = data.secGameState;
            data.secGameState = secGameState;
            data.secGameStateInfo.setFreeKickData(data.team[side].teamNumber, (byte) 0);

            Log.setNextMessage(secGameState.toString() + " " + data.team[side].teamColor.toString());
            ActionBoard.clockPause.perform(data);
        } else {
            // GameInterruption is already happening, increment subMode
            byte team = data.secGameStateInfo.toByteArray()[0];
            byte subMode = data.secGameStateInfo.toByteArray()[1];

            switch(subMode) {
                case 0:
                    data.gameClock.setSecondaryClock(Rules.league.game_interruption_preparation_time);
                    data.secGameStateInfo.setFreeKickData(team, (byte) 1);
                    break;
                case 1:
                    data.gameClock.clearSecondaryClock();
                    data.secGameStateInfo.setFreeKickData(team, (byte) 2);
                    break;
                case 2:
                    data.secGameState = data.previousSecGameState;
                    data.previousSecGameState = secGameState;
                    data.secGameStateInfo.reset();
                    Log.setNextMessage("End " + secGameState.toString() + data.team[side].teamColor.toString());
                    data.whenCurrentSecondaryGameStateEnded = data.getTime();
                    ActionBoard.clockPause.perform(data);
                    break;
            }
        }
    }

    @Override
    public boolean isLegal(AdvancedData data)
    {

        if (data.testmode) return true;
        if (isActive(data) && data.secGameStateInfo.toByteArray()[1] != 1) return true;
        boolean validGameState = data.gameState == GameStates.PLAYING;
        boolean validSecGameState = data.secGameState == SecondaryGameStates.NORMAL
                    || data.secGameState == SecondaryGameStates.OVERTIME
                    || isActive(data);
        boolean validTimingOfReady = (data.secGameStateInfo.toByteArray()[1] == 1 //current sub mode is preparation phase
                    && data.getSecondaryTime (3) != null //a secondary game clock exists
                    && data.getSecondaryTime(3) <= (Rules.league.game_interruption_preparation_time - Rules.league.game_interruption_minimal_ready_time)) //the minimal time guaranteed to the opponent has passed
                    || !data.secGameState.isGameInterruption() // if there is no game interruption the action is legal
                    || (data.secGameStateInfo.toByteArray()[1] != 1); //in any other sub mode the action is legal

        return validGameState && validSecGameState && validTimingOfReady;
    }

    public SecondaryGameStates getSecGameState() {
        return secGameState;
    }
}
