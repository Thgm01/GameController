package data.hl;

/**
 * This class sets attributes given by the humanoid-league rules.
 *
 * @author Michel-Zen
 */
public class HLDropIn extends HL
{
    public HLDropIn()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Drop In";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl_dropin";
        /** If true, the drop-in player competition is active */
        dropInPlayerMode = true;

        robotsPlaying = 5;

        teamSize = 7;
    }
}
