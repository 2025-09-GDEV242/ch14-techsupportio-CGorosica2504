
/**
 * This is an exception handling that indicates that a response file is not formatted
 * correctly.
 *
 * @author Christian Gorosica
 * @version 2025.12.08
 */
public class ImproperResponseFormatException extends Exception
{
    public ImproperResponseFormatException(String message) {
        super(message);
    }
}