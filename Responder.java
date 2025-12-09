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
    // The name of the file containing responses with their keys
    private static final String FILE_OF_RESPONSES = "responses.txt";
    
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
     * Populates the responseMap by reading structured key-response pairs from an external
     * file. The file format is:
     * 
     * key1, key2, key3
     * response line 1
     * response line 2
     * 
     * keyA
     * another response block
     * 
     * If two or more consecutive blank lines are encountered, an ImproperResponseFormatExemption
     * is raised. When this occurs, all loaded responses are cleared.
     * 
     * If the file cannot be opened or read, warning messages are printed and any responses 
     * successfully loaded prior to the failiure remain in place.
     */
    private void fillResponseMap() {
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(FILE_OF_RESPONSES);
        
        //Clears existing entries so the map is always rebuilt from the file.
        responseMap.clear();
        
        //Holds the keys for the current block
        String[] responseKeys = null;
        
        
        StringBuilder currentResponse = new StringBuilder();
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
                    
                    //End of a key-response pair. 
                    if (responseKeys != null && currentResponse.length() > 0) {
                        
                        String response = currentResponse.toString().trim();
                        
                        //Store the same response for each key in the block
                        for (String key : responseKeys) {
                            
                            responseMap.put(key.trim(), response);
                        }
                        
                        //Reset for the next block
                        responseKeys = null;
                        currentResponse.setLength(0);
                    }
                    
                } else {
                    
                    //Resets the blank lines counter on a non-blank input.
                    blankLinesCount = 0;
                    
                    //If keys have not been read yet, this line defines them
                    if (responseKeys == null) {
                        
                        //Keys are comma separated
                        responseKeys = line.split(",");
                    } else {
                        
                        //This line belongs to the response text
                        if (currentResponse.length() == 0) {
                            
                            currentResponse.append(line);
                        } else {
                            
                            currentResponse.append(" ").append(line);
                        }
                    }
                }
                
                line = reader.readLine();
            }
            
            //Storing the last block if the file didn't end with a blank line.
            if (responseKeys != null && currentResponse.length() > 0) {
                String response = currentResponse.toString().trim();
                
                for (String key : responseKeys) {
                    responseMap.put(key.trim(), response);
                }
            }
            
        }   
            catch (ImproperResponseFormatException e) {
                
                //Formatting error: Clears all custom responses.
                System.err.println("[WARNING] IMPROPER FORMAT detected: " + e.getMessage() + " in " + FILE_OF_RESPONSES);
                System.err.println("[NOTICE] Custom responses that were read from " + FILE_OF_RESPONSES + " have been cleared due to formatting errors.\n");
                responseMap.clear();
            }
            
            catch (FileNotFoundException e) {
                
                //File does not exist or cannot be opened.
                System.err.println("[WARNING] Unable to open " + FILE_OF_RESPONSES);
            }
            
            catch (IOException e) {
                
                //Geenral I/O failure.
                System.err.println("[WARNING] A problem was encountered reading " + FILE_OF_RESPONSES);
            }
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
        
        StringBuilder currentResponse = new StringBuilder();
        int blankLinesCount = 0;
        
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
             
            String response = reader.readLine();
            
            //Reading the file line by line
            while (response !=null) {
                
                //Checking if the line is blank
                if (response.trim().isEmpty()) {
                    blankLinesCount++;
                    
                    //Two consecutive blank lines indicate a formatting error.
                    if (blankLinesCount >= 2) {
                        throw new ImproperResponseFormatException(
                            "Two or more consecutive blank lines detected"
                            );
                    }
                    
                    //End of a response. We store it and reset.
                    if (currentResponse.length() > 0) {
                        defaultResponses.add(currentResponse.toString());
                        currentResponse.setLength(0);
                    }
                    
                } else {
                    
                    //Resets the blank lines counter on a non-blank input.
                    blankLinesCount = 0;
                    
                    //Build the current response. We concatenate lines with a space.
                    if (currentResponse.length() == 0) {
                        currentResponse.append(response);
                    } else {
                        currentResponse.append(" ").append(response);
                    }
                }
                
                response = reader.readLine();
            }
            
            //Storing the last response if the file didn't end with a blank line.
            if (currentResponse.length() > 0) {
                defaultResponses.add(currentResponse.toString());
            }
            
        }   
            catch (ImproperResponseFormatException e) {
                
                //Formatting error: Clears all responses and loads a fallback response.
                System.err.println("[WARNING] IMPROPER FORMAT detected: " + e.getMessage() + " in " + FILE_OF_DEFAULT_RESPONSES);
                System.err.println("[NOTICE] Default responses that were read from " + FILE_OF_DEFAULT_RESPONSES + " have been cleared due to formatting errors.\n");
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
