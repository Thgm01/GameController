package controller;

public class SystemClock {

    private static SystemClock instance;
    private long currentTime = 0;
    private static boolean systemTime = true;


    /**
     * Returns the instance of the singleton. If the Clock wasn't initialized once before, a new instance will
     * be created and returned (lazy instantiation)
     *
     * @return  The instance of the Clock
     */
    public static SystemClock getInstance()
    {
        if (instance == null) {
            instance = new SystemClock();
        }
        return instance;
    }

    /**
     * Sets the mode of the GameController to not use the real time but a provided time
     */
    public static void setSimulatedTime() {
        systemTime = false;
    }


    public long getCurrentTimeMillis() {
        if (systemTime) { return System.currentTimeMillis(); }
        else { return (long) (currentTime); }
    }

    public synchronized void setTime(long time) {
        this.currentTime = time;
    }
}
