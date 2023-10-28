package rowing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Heat implements Cloneable{

    public static final int DISTANCE_250 = 250;
    public static final int DISTANCE_350 = 350;
    public static final int DISTANCE_500 = 500;
    public static final int DISTANCE_1000 = 1000;
    public static final int DISTANCE_1500 = 1500;
    public static final int DISTANCE_2000 = 2000;
    public static final int DISTANCE_3000 = 3000;
    public static final int DISTANCE_6000 = 6000;

    private static int count = -1;

    private final int id;
    private Date time;
    private int distance;
    private int lanes;
    private final List<Boat> boats;


    public Heat() {
        count++;
        id = count;
        this.boats = new ArrayList<Boat>();
        setLanes();
    }

    public Heat(int distance, Boat... boats) {
        count++;
        id = count;

        this.setDistance(distance);
        this.boats = new ArrayList<Boat>();

        for (Boat b : boats) {
            addBoat(b);
        }

    }

    public Heat(Date time, int distance, Boat... boats) {
        count++;
        id = count;

        this.setTime(time);
        this.setDistance(distance);
        this.boats = new ArrayList<Boat>();

        for (Boat b : boats) {
            addBoat(b);
        }

    }


    public int getID() {
        return id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getLanes() {
        return lanes;
    }

    private void setLanes() {
        lanes = boats != null ? boats.size() : 0;
    }

    public void addBoat(Boat boat) {
        boats.add(boat);
        setLanes();
    }

    public void removeBoat(Boat boat) {
        boats.remove(boat);
        setLanes();
    }

    public List<Boat> getBoats() {
        return boats;
    }

    public static int getHeats() {
        return count;
    }

    public boolean equals(Heat other) {
        if (other == null) return false;

        return this.id == other.id;
    }


    public String toInfoString() {

        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < boats.size(); i++) {
            tmp.append(boats.get(i).toString());

            if (!(i + 1 >= boats.size())) {
                tmp.append(" - ");
            }

        }
        String t;
        if (time != null) {
            String pattern = "HH:mm";
            SimpleDateFormat sdFormat = new SimpleDateFormat(pattern);

            t = sdFormat.format(time) + " :";
        } else {
            t = "";
        }

        return String.format("%3d.\t[%sm]\t%s\t%s", id, distance, t, tmp.toString());
    }

    public String toCSVString(int heatNumber, char separator) {


        StringBuilder str = new StringBuilder();
        String time = "";

        if (this.time != null) {
            String pattern = "HH:mm";
            SimpleDateFormat sdFormat = new SimpleDateFormat(pattern);

            time = sdFormat.format(this.time);
        } else {
            time = "";
        }

        for (Boat boat : boats) {
            str.append(boat.toString()).append(separator);
        }

        str = new StringBuilder(str.substring(0, str.length() - 1));

        return String.format("%d%c%s%c%dm%c%s", heatNumber, separator, time, separator, distance, separator, str.toString());
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < boats.size(); i++) {
            tmp.append(boats.get(i).toString());

            if (!(i + 1 >= boats.size())) {
                tmp.append(" - ");
            }

        }

        return String.format("[%sm]\t %s", distance, tmp.toString());
    }

}
