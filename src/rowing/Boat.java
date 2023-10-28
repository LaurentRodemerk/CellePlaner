package rowing;

public class Boat {

    private String name;
    private Athlete cox;
    private Athlete[] athletes;

    public Boat() {
        cox = null;
        name = null;
    }

    public Boat(final String name, Athlete cox, Athlete... athletes) {
        this.name = name;
        this.setCox(cox);
        this.setAthletes(athletes);
    }

    public String getName() {
        return name;
    }

    public Athlete getCox() {
        return cox;
    }

    public void setCox(Athlete cox) {
        this.cox = cox;
    }

    public Athlete[] getAthletes() {
        return athletes;
    }

    public void setAthletes(Athlete[] athletes) {
        this.athletes = athletes;
    }

    public boolean equals(Boat other) {
        return this.getName().equals(other.getName());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {

        StringBuilder tmp = new StringBuilder();

        if (athletes.length > 0) {

            for (int i = 0; i < athletes.length; i++) {
                tmp.append(athletes[i].getName());

                if (!(i + 1 >= athletes.length)) {
                    tmp.append(", ");
                }

            }

            if (cox != null) {
                tmp.append(", St. ").append(cox.getName());
            }

        } else {
            tmp = new StringBuilder("OHNE MANNSCHAFT");
        }

        return String.format("%s (%s)", tmp.toString(), name);
    }

}
