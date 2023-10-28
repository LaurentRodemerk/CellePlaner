package io;

import rowing.Heat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegattaWriter {

    public static char STANDARD_SEPARATOR =';';

    public void write(List<Heat> data, File outputFile, char separator) {


        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), StandardCharsets.UTF_16));

            String CSV_HEADER = "Rennen%cStart%cDistanz%cBoot 1%cBoot 2%cBoot 3%cBoot 4\n";
            out.append(String.format(CSV_HEADER, separator, separator, separator, separator,
                    separator, separator));

            int heatNumber = 0;

            for (Heat h : data) {
                String str = h != null ? h.toCSVString(++heatNumber, separator) + "\n" : "Mittagspause\n";
                out.append(str);
            }
            out.flush();
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
