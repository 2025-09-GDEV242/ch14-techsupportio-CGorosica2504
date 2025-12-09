import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * The responder class represents a response generator object.
 * It is used to generate an automatic response, based on specified input.
 * Input is presented to the responder as a set of words, and based on those
 * words the responder will generate a String that represents the response.
 *
 * Internally, the reponder uses a HashMap to associate words with response
 * strings and a list of default responses. If any of the input words is found
 * in the HashMap, the corresponding response is returned. If none of the input
 * words is recognized, one of the default responses is randomly chosen.
 * 
 * @author David J. Barnes and Michael Kölling.
 * @author Christian Gorosica
 * @version 2025.12.08
 */
public class Responder
{
    // Used to map key words to responses.
    private HashMap<String, String> responseMap;
    // Default responses to use if we don't recognise a word.
    private ArrayList<String> defaultResponses;
    // The name of the file containing the default responses.
    private static final String FILE_OF_DEFAULT_RESPONSES = "default.txt";
    private Random randomGenerator;

    /**
     * Construct a Responder
     */
    public Responder()
    {
        responseMap = new HashMap<>();
        defaultResponses = new ArrayList<>();
        fillResponseMap();
        fillDefaultResponses();
        randomGenerator = new Random();
}

    /**
     * Generate a response from a given set of input words.
     * 
     * @param words  A set of words entered by the user
     * @return       A string that should be displayed as the response
     */
    public String generateResponse(HashSet<String> words)
    {
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            String word = it.next();
            String response = responseMap.get(word);
            if(response != null) {
                return response;
            }
        }
        // If we get here, none of the words from the input line was recognized.
        // In this case we pick one of our default responses (what we say when
        // we cannot think of anything else to say...)
        return pickDefaultResponse();
    }

    /**
     * Enter all the known keywords and their associated responses
     * into our response map.
     */
    private void fillResponseMap()
    {
        responseMap.put("crash", 
                        "Well, it never crashes on our system. It must have something\n" +
                        "to do with your system. Tell me more about your configuration.");
        responseMap.put("crashes", 
                        "Well, it never crashes on our system. It must have something\n" +
                        "to do with your system. Tell me more about your configuration.");
        responseMap.put("slow", 
                        "I think this has to do with your hardware. Upgrading your processor\n" +
                        "should solve all performance problems. Have you got a problem with\n" +
                        "our software?");
        responseMap.put("performance", 
                        "Performance was quite adequate in all our tests. Are you running\n" +
                        "any other processes in the background?");
        responseMap.put("bug", 
                        "Well, you know, all software has some bugs. But our software engineers\n" +
                        "are working very hard to fix them. Can you describe the problem a bit\n" +
                        "further?");
        responseMap.put("buggy", 
                        "Well, you know, all software has some bugs. But our software engineers\n" +
                        "are working very hard to fix them. Can you describe the problem a bit\n" +
                        "further?");
        responseMap.put("windows", 
                        "This is a known bug to do with the Windows operating system. Please\n" +
                        "report it to Microsoft. There is nothing we can do about this.");
        responseMap.put("macintosh", 
                        "This is a known bug to do with the Mac operating system. Please\n" +
                        "report it to Apple. There is nothing we can do about this.");
        responseMap.put("expensive", 
                        "The cost of our product is quite competitive. Have you looked around\n" +
                        "and really compared our features?");
        responseMap.put("installation", 
                        "The installation is really quite straight forward. We have tons of\n" +
                        "wizards that do all the work for you. Have you read the installation\n" +
                        "instructions?");
        responseMap.put("memory", 
                        "If you read the system requirements carefully, you will see that the\n" +
                        "specified memory requirements are 1.5 giga byte. You really should\n" +
                        "upgrade your memory. Anything else you want to know?");
        responseMap.put("linux", 
                        "We take Linux support very seriously. But there are some problems.\n" +
                        "Most have to do with incompatible glibc versions. Can you be a bit\n" +
                        "more precise?");
        responseMap.put("bluej", 
                        "Ahhh, BlueJ, yes. We tried to buy out those guys long ago, but\n" +
                        "they simply won't sell... Stubborn people they are. Nothing we can\n" +
                        "do about it, I'm afraid.");
    }

    /**
     * Build up a list of default responses from which we can pick if we don't know what else to say.
     * 
     * Each non-empty sequence of lines is treated as one response, with lines concatenated using spaces.
     * A singl blank line indicates the end of a response.
     * 
     * IF two or more consecutive blank lines are encountered, an ImproperResponseFormatExemption
     * is raised. When this occurs, all loaded responses are cleared and a single fallback response
     * is added.
     * 
     * If the file cannot be opened or read, warning messages are printed and any responses 
     * successfully loaded prior to the failiure remain in place.
     * 
     * If no responses are available after processing, a final fallback response is inserted.
     */
    private void fillDefaultResponses() {
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(FILE_OF_DEFAULT_RESPONSES);
        
        String currentResponse = "";
        int blankLinesCount = 0;
        
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
             
            String line = reader.readLine();
            
            //Reading the file line by line
            while (line !=null) {
                
                //Checking if the line is blank
                if (line.trim().isEmpty()) {
                    blankLinesCount++;
                    
                    //Two consecutive blank lines indicate a formatting error.
                    if (blankLinesCount >= 2) {
                        throw new ImproperResponseFormatException(
                            "Two or more consecutive blank lines detected"
                            );
                    }
                    
                    //End of a response. We store it and reset.
                    if (!currentResponse.isEmpty()) {
                        defaultResponses.add(currentResponse);
                        currentResponse = "";
                    }
                    
                } else {
                    
                    //Resets the blank lines counter on a non-blank input.
                    blankLinesCount = 0;
                    
                    //Build the current response. We concatenate lines with a space.
                    if (currentResponse.isEmpty()) {
                        currentResponse = line;
                    } else {
                        currentResponse = currentResponse + " " + line;
                    }
                }
                
                line = reader.readLine();
            }
            
            //Storing the last response if the file didn't end with a blank line.
            if (!currentResponse.isEmpty()) {
                defaultResponses.add(currentResponse);
            }
            
        }   
            catch (ImproperResponseFormatException e) {
                
                //Formatting error: Clears all responses and loads a fallback response.
                System.err.println("[WARNING] IMPROPER FORMAT detected: " + e.getMessage() + " in " + FILE_OF_DEFAULT_RESPONSES);
                System.err.println("[NOTICE] Default responses were cleared due to formatting error.\n");
                defaultResponses.clear();
            }
            
            catch (FileNotFoundException e) {
                
                //File does not exist or cannot be opened.
                System.err.println("[WARNING] Unable to open " + FILE_OF_DEFAULT_RESPONSES);
            }
            
            catch (IOException e) {
                
                //Geenral I/O failure.
                System.err.println("[WARNING] A problem was encountered reading " + FILE_OF_DEFAULT_RESPONSES);
            }
            
            //Makes sure we have at least one response.
            if (defaultResponses.size() == 0) {
                defaultResponses.add("Can you elaborate on that?");
            }
        }

    /**
     * Randomly select and return one of the default responses.
     * @return     A random default response
     */
    private String pickDefaultResponse()
    {
        // Pick a random number for the index in the default response list.
        // The number will be between 0 (inclusive) and the size of the list (exclusive).
        int index = randomGenerator.nextInt(defaultResponses.size());
        return defaultResponses.get(index);
    }
}
