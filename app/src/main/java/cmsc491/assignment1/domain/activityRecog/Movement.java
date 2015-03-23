package cmsc491.assignment1.domain.activityRecog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * Movement model object.
 */
public class Movement {

    public enum Type {WALKING, SLEEPING, SITTING};

    private Type movementType;
    private DateTime startTime, endTime;

    public static final String APP_TAG = "MovementRecog";

    private static final String SITTING = "Sitting";
    private static final String WALKING = "Walking";
    private static final String SLEEPING = "Sleeping";

    public Movement(Type movementType, DateTime startTime, DateTime endTime){
        this.movementType = movementType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Type stringToMovementType(String movementType){
        if(SITTING.equals(movementType))
            return Type.SITTING;
        else if(SLEEPING.equals(movementType))
            return Type.SLEEPING;
        else if(WALKING.equals(movementType))
            return Type.WALKING;

        return null;
    }

    // TODO: Move activities to resources file.
    public static String getTypeString(Type movement){
        String type = "";
        switch(movement) {
            case SITTING:
                type = "Sitting";
                break;
            case WALKING:
                type = "Walking";
                break;
            case SLEEPING:
                type = "Sleeping";
                break;
        }
        return type;
    }

    public String getTypeString(){
        return getTypeString(movementType);
    }

    public static DateTimeFormatter getDateTimeFormatter(){
        DateTimeFormatter format = new DateTimeFormatterBuilder()
                .appendClockhourOfHalfday(1)
                .appendLiteral(':')
                .appendMinuteOfHour(2)
                .appendLiteral(' ')
                .appendHalfdayOfDayText()
                .toFormatter();
        return format;
    }

    public String getIntervalString(){
        DateTimeFormatter format = getDateTimeFormatter();
        return String.format("%s - %s", startTime.toString(format), endTime.toString(format));
    }

    public String toString(){
        return String.format("%s %s\n", getTypeString(movementType), getIntervalString());
    }

}
