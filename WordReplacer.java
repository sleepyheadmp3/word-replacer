import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * Class that performs a global search and replace on a text file using either
 * a hash map, a red-black tree, or a binary search tree.
 * @author Jennifer Fu
 * @version 1.0 December 15, 2024
 */

public class WordReplacer {
    private static MyMap<String, String> ruleMap = new MyHashMap<>();
    private static MyMap<String, String> map;

    /**
     * Main method that parses command line arguments, initializes the data
     * structure of choice, processes and stores word replacement rules in
     * a hash map, and executes word replacements on the input text file.
     * @param args command line arguments in valid format:
     *             <input text file> <word replacements file> <bst|rbt|hash>
     */
    public static void main(String[] args) {
        // calls separate method for parsing command line args
        cmdLineChecker(args);

        // creates map and stores input file within
        if (args[2].equals("bst")) {
            map = new BSTreeMap<>();
            storeInput(args[1], "bst");
        }
        else if (args[2].equals("rbt")) {
            map = new RBTreeMap<>();
            storeInput(args[1], "rbt");
        }
        else {
            map = new MyHashMap<>();
            storeInput(args[1], "hash");
        }

        // reads input file, replaces words
        String output = readInFile(args[0]);

        // formats and prints final output!
        System.out.printf("%s\n", output);
    }

    /**
     * Parses command line args and determines if they are in a valid format;
     * handles errors and exceptions with corresponding message.
     * @param cmdLine the command line arguments as an array.
     */
    private static void cmdLineChecker(String[] cmdLine) {
        // checks number of arguments
        if (cmdLine.length != 3 || cmdLine[0] == null || cmdLine[1] == null ||
            cmdLine[2] == null) {
            System.err.println("Usage: java WordReplacer <input text file>" +
                    " <word replacements file> <bst|rbt|hash>");
            System.exit(1);
        }
        String inputTextFile = cmdLine[0];
        String wordReplacementsFile = cmdLine[1];
        String dataStructure = cmdLine[2];

        // checks text file validity by constructing FileReader
        // https://www.geeksforgeeks.org/java-io-filereader-class/
        try {
            new FileReader(inputTextFile).close();
        }
        // https://www.geeksforgeeks.org/java-io-filenotfoundexception-in-java/
        catch (IOException e) {
            System.err.println("Error: Cannot open file '" + inputTextFile +
                    "' for input.");
            System.exit(1);
        }
        try {
            new FileReader(wordReplacementsFile).close();
        }
        catch (IOException e) {
            System.err.println("Error: Cannot open file '" +
                    wordReplacementsFile + "' for input.");
            System.exit(1);
        }

        // checks data structure argument
        if (!dataStructure.equals("bst") && !dataStructure.equals("rbt") &&
                !dataStructure.equals("hash")) {
            System.err.println("Error: Invalid data structure '" +
                    dataStructure + "' received.");
            System.exit(1);
        }
    }

    /**
     * Reads the word replacement rules from the word replacements file, stores
     * first in hash map and then in data structure of choice. Handles cycles
     * and transitive dependencies during insertion.
     * @param replacementFile the name of the word replacements file.
     * @param dataStructure   the name of the chosen data structure ("hash" /
     *                        "bst" / "rbt").
     */
    private static void storeInput(String replacementFile,
                                   String dataStructure) {
        // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        try (BufferedReader replacementReader =
                     new BufferedReader(new FileReader(replacementFile))) {
            String line, key, value;
            while ((line = replacementReader.readLine()) != null) {
                // https://www.w3schools.com/java/java_ref_string.asp
                key = line.substring(0, line.indexOf("->") - 1);
                value = line.substring(line.indexOf("->") + 3);

                // inserts rules into hash map and respective map
                dependencyInsert(key, value, dataStructure);
            }
            // designates ruleMap as map since already hashed
            if (dataStructure.equals("hash")) { map = ruleMap; }
        }
        catch (IOException e) {
            System.err.println("Error: An I/O error occurred reading '" +
                    replacementFile + "'.");

        }
    }


    /**
     * Inserts replacement rules into a hash map and then into the chosen data
     * structure. Faulty handling and compression of cycles. Updates pointers
     * for all dependent keys in both temporary hash and actual map.
     * @param key           the word to be replaced.
     * @param value         the word to replace the key with.
     * @param dataStructure the name of the data structure ("hash" / "bst" /
     *                      "rbt").
     */
    private static void dependencyInsert(String key, String value,
                                         String dataStructure) {
        String parent = key;
        String child = value;
        // checks for cycle first
        while (child != null) {
            if (child.equals(key)) {
                // cycle found, print error message and exit
                System.err.println("Error: Cycle detected when trying to add" +
                        " replacement rule: " + key + " -> " + value);
                System.exit(1);
            }
            parent = child;
            child = ruleMap.get(child);
        }

        // dependency found, update current rule to point to parent
        String current = key;
        while (current != null && !current.equals(parent)) {
            child = ruleMap.get(current);
            ruleMap.put(current, parent);
            // updates pointers in actual map as well
            if (!dataStructure.equals("hash")) { map.put(current, parent); }
            current = child;
        }
    }

    /**
     * Reads the input text file, replaces words according to ruleMap, formats
     * and returns final modified text.
     * @param inFile the name of the input text file.
     * @return the modified text with correct replacements.
     */
    // reads input file and adds words / tests to see if they're words
    private static String readInFile(String inFile) {
        StringBuilder output = new StringBuilder();
        try (BufferedReader inFileReader =
                new BufferedReader(new FileReader(inFile))) {
            StringBuilder word = new StringBuilder();
            StringBuilder tempLine = new StringBuilder();
            String line;
            while ((line = inFileReader.readLine()) != null) {
                // reads each character in a line and adds to output
                for (int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    if (Character.isLetter(ch)) { word.append(ch); }
                    if (!Character.isLetter(ch) || i == line.length() - 1) {
                        if (!word.isEmpty()) {
                            String newWord;
                            // replaces word with new one from map if found
                            if ((newWord = ruleMap.get(word.toString())) != null) {
                                tempLine.append(newWord);
                            }
                            else { tempLine.append(word); }
                            // resets StringBuilder for word after appending
                            word.setLength(0);
                        }
                    }
                    if (!Character.isLetter(ch)) {
                        tempLine.append(ch);
                    }
                }

                // formats line correctly and appends, resets line
                output.append(tempLine).append("\n");
                tempLine.setLength(0);
            }
        }
        catch (IOException e) {
            System.err.println("Error: An I/O error occurred reading '" +
                    inFile + "'.");
            System.exit(1);
        }
        // trims off extra newline at very end and returns
        return output.toString().trim();
    }
}
