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

    public Movement(Type movementType, DateTime startTime, DateTime endTime){
        this.movementType = movementType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // TODO: Move activities to resources file.
    public String getTypeString(){
        String type = "";
        switch(movementType) {
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

    public String getIntervalString(){
        DateTimeFormatter format = new DateTimeFormatterBuilder()
                .appendClockhourOfHalfday(1)
                .appendLiteral(':')
                .appendMinuteOfHour(2)
                .appendLiteral(' ')
                .appendHalfdayOfDayText()
                .toFormatter();

        return String.format("%s - %s", startTime.toString(format), endTime.toString(format));
    }

    public String toString(){
        return String.format("%s %s\n", getTypeString(), getIntervalString());
    }

}
