package controller.action.ui;

import controller.action.ActionType;
import controller.action.GCAction;
import data.PlayerInfo;
import data.states.AdvancedData;
import data.values.GameStates;
import data.values.Side;


public class MakeGoalieAction extends GCAction {

    private final Side side;
    private final int player;

    public MakeGoalieAction(Side side, int player) {
        super(ActionType.UI);
        this.player = player;
        this.side = side;
    }

    @Override
    public void perform(AdvancedData data) {
        // Set all players goalie prop to zero
        for (PlayerInfo pi : data.getTeam(this.side).player){
            pi.isGoalie = 0;
        }

        // Set the one being goalie
        data.getTeam(this.side).player[this.player].isGoalie = 1;
    }

    @Override
    public boolean isLegal(AdvancedData data) {
        //GoalKeeper can only be changed during a stoppage of the game
        //A stoppage is defined as the game not being in play (INITIAL, READY, SET, FINISHED)
        //or being in the procedure of a game interruption (free kick, corner kick, goal kick, throw in, penalty kick)
        boolean in_legal_game_state = data.gameState == GameStates.INITIAL
                || data.gameState == GameStates.READY
                || data.gameState == GameStates.SET
                || data.gameState == GameStates.FINISHED;
        boolean in_game_interruption = data.gameState == GameStates.PLAYING
                && data.secGameState.isGameInterruption();
        return in_legal_game_state || in_game_interruption;
    }
}
