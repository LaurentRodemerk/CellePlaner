package io;

import rowing.Athlete;
import rowing.Boat;
import rowing.Heat;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class HeatReader {


    private static final String[] coxRegEx = {"St. ", "St ", "St.: ", "St: ", "Stm ", "Stm. ", "Stm: ", "Stm.: ",
            "Stf. ", "Stf ", "Stf: ", "Stf.: ", "Str. ", "Str ", "Str.: ", "Str: "};


    public HeatReader() {
    }

    /**
     * Analyzes given String and returns represented heat
     */
    public Heat stringToHeat(String string) throws IllegalComponentStateException {
        /*
         * Interprets a string of special format as a heat.
         * Regex: <Distance>; <Athlet 1> (<Boat 1>); <Athlet 2>, <Athlete 3>, St. <Athlete 4>, (<Boat 3>)
         *
         * Step 1: Distance
         * Step 2: Crew + Boat:
         * Step 2.1: Athletes
         * Step 2.2: Cox
         * Step 2.3: Boat
         */

        // error message
        final String ERROR_MSG = "Illegal Character '%c' (%s - at %d)";


        //automaton states
        final int STATE_DISTANCE = 1;
        final int STATE_INIT_ATHLETES = 2;
        final int STATE_ATHLETES = 3;
        final int STATE_BOAT = 4;

        final int STATE_ERROR = -1;

        int currentState = STATE_DISTANCE;

        Heat heat = new Heat();

        StringBuilder distance = new StringBuilder();
        StringBuilder athletes = new StringBuilder();
        StringBuilder nameOfBoat = new StringBuilder();

        //Analyzing string
        char[] ca = string.toCharArray();

        Boat boat = new Boat();

        for (int charCounter = 0; charCounter < ca.length; charCounter++) {
            char c = ca[charCounter];


            //Step 1: Distance
            if (currentState == STATE_DISTANCE) {
                if (Character.isDigit(c)) {
                    distance.append(c);
                } else {
                    if (isSeparator(c)) {
                        //save distance and go to step 2
                        heat.setDistance(Integer.parseInt(distance.toString()));
                        currentState = STATE_INIT_ATHLETES;
                        continue;
                    } else {
                        //error state
                        throw new IllegalComponentStateException(String.format(ERROR_MSG, c, string, charCounter));
                    }
                }
            }

            //Step 2: Crew and Boat
            //Step 2.1: Athletes

            // Initializing
            if (currentState == STATE_INIT_ATHLETES) {
                if (isSeparator(c)) continue;

                if (Character.isLetter(c) || c == '\"') {
                    if (c != '\"')
                        athletes.append(c);
                    currentState = STATE_ATHLETES;
                    continue;
                } else throw new IllegalComponentStateException(String.format(ERROR_MSG, c, string, charCounter));
            }

            if (currentState == STATE_ATHLETES) {
                if (Character.isLetter(c) || isSeparator(c) || c == '-' || c == '.' || c == ':' || c == '\"' || c == ' ') {
                    if (c != '\"')
                        athletes.append(c);
                } else {
                    //End of Athletes reached
                    if (c == '(') {
                        if (athletes.charAt(0) == ',') {
                            athletes = new StringBuilder(athletes.substring(1));
                        }

                        String[] athleteNames = athletes.toString().split(",");

                        for (int i = 0; i < athleteNames.length; ++i) {

                            if (athleteNames[i].charAt(0) == ' ') {
                                athleteNames[i] = athleteNames[i].substring(1);
                            }
                            if (athleteNames[i].charAt(athleteNames[i].length() - 1) == ' ') {
                                athleteNames[i] = athleteNames[i].substring(0, athleteNames[i].length() - 1);
                            }

                        }

                        //Step 2.2 Cox:
                        final String strCoxIndicator = "St";

                        if (athleteNames[athleteNames.length - 1].contains(strCoxIndicator)) {

                            for (String str : coxRegEx) {
                                athleteNames[athleteNames.length - 1] = athleteNames[athleteNames.length - 1]
                                        .replace(str, "");
                                if (!athleteNames[athleteNames.length - 1].contains(strCoxIndicator)) {
                                    break;
                                }
                            }

                            Athlete cox = new Athlete(athleteNames[athleteNames.length - 1]);
                            boat.setCox(cox);

                            String[] tmp = new String[athleteNames.length - 1];

                            if (athleteNames.length - 2 >= 0) {
                                if (tmp.length >= 0) System.arraycopy(athleteNames, 0, tmp, 0, tmp.length);
                            }

                            athleteNames = tmp;
                        }

                        // adding athletes to boat
                        Athlete[] aAthletes = new Athlete[athleteNames.length];

                        for (int i = 0; i < athleteNames.length; ++i) {
                            aAthletes[i] = new Athlete(athleteNames[i]);
                        }

                        boat.setAthletes(aAthletes);
                        currentState = STATE_BOAT;

                    } else throw new IllegalComponentStateException(String.format(ERROR_MSG, c, string, charCounter));
                }
                continue;
            }

            //Step 2.3: Boat
            if (currentState == STATE_BOAT) {
                if (c != ')' && c != '\"') {
                    nameOfBoat.append(c);
                } else {
                    boat.setName(nameOfBoat.toString());
                    heat.addBoat(boat);

                    //reset boat and crew
                    boat = new Boat();
                    athletes = new StringBuilder();
                    nameOfBoat = new StringBuilder();

                    currentState = STATE_INIT_ATHLETES;
                }
            }

        }

        return heat;
    }

    /**
     * reads given CSV file (UTF-8) and returns its heats
     */
    public List<Heat> readHeats(File file) throws IllegalComponentStateException, IOException {

        if (!file.canRead() || !file.isFile())
            return null;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.readLine(); // 1st line: headlines

        List<Heat> data = reader.lines().map(this::stringToHeat).collect(Collectors.toList());
        reader.close();

        return data;
    }


    private boolean isSeparator(char c) {
        return (c == ';' || c == ',');
    }

}
