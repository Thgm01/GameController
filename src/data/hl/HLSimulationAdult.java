package data.hl;

/**
 * This class sets attributes given by the humanoid-league rules.
 *
 * @author Michel-Zen
 */
public class HLSimulationAdult extends HLSim
{
    public HLSimulationAdult()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Simulation AdultSize";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl_sim_adult";
        /** How many robots are in a team. */
        teamSize = 2;
        /** How many robots of each team may play at one time. */
        robotsPlaying = 2;
    }
}
