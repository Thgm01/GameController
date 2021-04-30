package controller.action.ui;

import common.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.states.AdvancedData;
import data.values.SecondaryGameStates;


/**
 * @author Maike Paetzel
 *
 * This action performs aborts a currently ongoing Game Interruption.
 * This is usually used when the ball is touched by a player from the team the
 * game interruption was awarded to pefore the ball was in play.
 */
public class AbortGameInterruption extends GCAction {
    /**
     * Which side has the advantage with this game interruption
     */
    private int side;

    public AbortGameInterruption(int side) {
        super(ActionType.UI);
        this.side = side;
    }


    @Override
    public void perform(AdvancedData data)
    {
        SecondaryGameStates secGameState = data.secGameState;
        data.secGameState = data.previousSecGameState;
        data.previousSecGameState = secGameState;
        data.secGameStateInfo.reset();
        Log.setNextMessage("Aborting: " + secGameState.toString() + " " + data.team[side].teamColor.toString());

        data.gameClock.clearSecondaryClock();
        ActionBoard.clockPause.perform(data);

    }

    @Override
    public boolean isLegal(AdvancedData data) {
        byte[] bytes = data.secGameStateInfo.toByteArray();
        byte team = bytes[0];
        boolean isGoodTeam = team == data.team[side].teamNumber;
        return data.secGameState.isGameInterruption() && isGoodTeam;
    }
}
