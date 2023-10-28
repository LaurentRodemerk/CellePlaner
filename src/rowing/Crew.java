package rowing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Crew {

    private String name;
    private Boat boat = null;
    private List<Athlete> athletes;
    private Athlete cox = null;

    public Crew() {
    }

    public Crew(String name, Athlete... athletes) {

        this.name = name;

        this.athletes = new ArrayList<>();
        Collections.addAll(this.athletes, athletes);
    }

    public Crew(String name, String boat, Athlete cox, Athlete... athletes) {

        this.name = name;
        this.cox = cox;

        this.athletes = new ArrayList<>();
        Collections.addAll(this.athletes, athletes);

        setBoat(boat, cox);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAthletes(Athlete... athletes) {

        this.athletes.clear();
        Collections.addAll(this.athletes, athletes);
    }

    public List<Athlete> getAthletes() {
        return athletes;
    }

    public void addCrewMember(Athlete athlete) {
        if (athletes.contains(athlete)) athletes.add(athlete);
    }

    public void removeCrewMember(Athlete athlete) {
        athletes.remove(athlete);
    }

    public Boat getBoat() {
        return boat;
    }

    public void setBoat(String boat) {
        Athlete[] athletes = new Athlete[this.athletes.size()];

        for (int i = 0; i < this.athletes.size(); i++) {
            athletes[i] = this.athletes.get(i);
        }

        this.boat = new Boat(boat, cox, athletes);
    }

    public void setBoat(String boat, Athlete cox) {
        Athlete[] athletes = new Athlete[this.athletes.size()];

        for (int i = 0; i < this.athletes.size(); i++) {
            athletes[i] = this.athletes.get(i);
        }

        this.boat = new Boat(boat, cox, athletes);
    }
}
