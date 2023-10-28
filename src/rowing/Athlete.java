package rowing;

public class Athlete {

    private String name;
    private String nickname;

    public Athlete() {
    }

    public Athlete(final String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public boolean equals(Athlete other) {
        return name.equals(other.getName());
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}