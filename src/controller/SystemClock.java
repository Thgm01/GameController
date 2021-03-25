package controller;

public class SystemClock {

    private static SystemClock instance;
    private long currentTime = 0;

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


    public long getCurrentTimeMillis() {
        return (long) (currentTime);
    }

    public synchronized void setTime(long time) {
        this.currentTime = time;
    }
}
