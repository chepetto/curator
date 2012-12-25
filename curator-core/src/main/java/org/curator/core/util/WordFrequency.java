package org.curator.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class WordFrequency {

    private static Map<String, Integer> map = new HashMap<String, Integer>(2000000);

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(new FileInputStream("/home/damoeb/uni/master/wiki/smap"));
        Pattern p = Pattern.compile("([^ ]+) (.*)");
        String ow = "";
        Integer maxf = 0;
        Integer of = 0;
        while (s.hasNextLine()) {
            String line = s.nextLine();
            Matcher m = p.matcher(line);
            if (m.find()) {
                String w = m.group(1);
                if (w.matches(".*[\\-–0-9]+.*"))
                    continue;

                Integer f = Integer.parseInt(m.group(2));
                if (ow.equals(w)) {
                    of += f;
                } else {
                    if (of > 9) {
                        map.put(ow, of);

                        if (of > maxf) {
                            System.out.println(of);
                            maxf = of;
                        }
                    }
                    ow = w;
                    of = f;
                }
            }
//      System.out.println(c);
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/map"));
        out.writeObject(map);
        out.close();
    }

}
