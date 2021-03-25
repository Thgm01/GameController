package data.hl;

/**
 * This class sets attributes given by the humanoid-league rules.
 *
 * @author Michel-Zen
 */
public class HLSimulation extends HL
{
    public HLSimulation()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Simulation";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl_sim";
        /** How many robots are in a team. */
        teamSize = 4;
        /** How many robots of each team may play at one time. */
        robotsPlaying = 2;
    }
}
