package com.rusefi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 3/30/2015
 */
public class VariableRegistry extends TreeMap<String, String> {
    public static final VariableRegistry INSTANCE = new VariableRegistry();

    private final Pattern VAR = Pattern.compile("(@@(.*?)@@)");

    public Map<String, Integer> intValues = new HashMap<>();

    private final StringBuilder cNumbericDefinitions = new StringBuilder();
    private final StringBuilder javaNumbericDefinitions = new StringBuilder();

    private VariableRegistry() {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    public String processLine(String line) {
        Matcher m;
        while ((m = VAR.matcher(line)).find()) {
            String key = m.group(2);


            //     key =

            if (!containsKey(key))
                throw new IllegalStateException("No such variable: " + key);
            String s = get(key);
            line = m.replaceFirst(s);
        }
        return line;
    }

    public void register(String var, String value) {
        System.out.println("Registering " + var + " as " + value);
        put(var, value);

        tryToRegisterAsInteger(var, value);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private void tryToRegisterAsInteger(String var, String value) {
        try {
            int intValue = Integer.parseInt(value);
            System.out.println("key [" + var + "] value: " + intValue);
            if (intValues.containsKey(var))
                throw new IllegalStateException("Not allowed to redefine: " + var);
            intValues.put(var, intValue);
            cNumbericDefinitions.append("#define " + var + " " + intValue + "\r\n");
            javaNumbericDefinitions.append("\tpublic static final int " + var + " = " + intValue + ";\r\n");
        } catch (NumberFormatException e) {
            System.out.println("Not an integer: " + value);
        }
    }

    public void register(String var, int i) {
        register(var, Integer.toString(i));
        register(var + "_hex", Integer.toString(i, 16));
    }

    public void writeNumericsToFile(String headerDestinationFolder) throws IOException {
        String fileName = headerDestinationFolder + File.separator + "rusefi_generated.h";
        System.out.println("Writing to " + fileName);
        BufferedWriter cHeader = new BufferedWriter(new FileWriter(fileName));

        cHeader.write(cNumbericDefinitions.toString());
        cHeader.close();
    }

    public String getJavaConstants() {
        return javaNumbericDefinitions.toString();
    }
}