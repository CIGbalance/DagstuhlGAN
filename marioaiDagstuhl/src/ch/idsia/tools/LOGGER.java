package ch.idsia.tools;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 7, 2009
 * Time: 8:59:20 PM
 * Package: ch.idsia.tools
 */
// TODO: Warning message: bilk yourself is easier that the system.
public class LOGGER
{
    private static int count = 0;

    public static void save(String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(history);
            bw.close();
            System.out.println("\n\nlog file saved to " + fileName);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }

    public enum VERBOSE_MODE {ALL, INFO, WARNING, ERROR, TOTAL_SILENCE}
    static TextArea textAreaConsole = null;
    private static VERBOSE_MODE verbose_mode = VERBOSE_MODE.TOTAL_SILENCE;
    public static void setVerboseMode(VERBOSE_MODE verboseMode)
    {
        LOGGER.verbose_mode = verboseMode;
    }

    public static void setTextAreaConsole(TextArea tac)
    {
        textAreaConsole = tac;
    }

    private static String history = "console:\n";

    public static void println(String record, VERBOSE_MODE vm)
    {
        LOGGER.print(record + "\n", vm);
    }

    private static DecimalFormat df = new DecimalFormat("000");


    public static void print(String record, VERBOSE_MODE vm) {
        try
        {
            // upperbounded by maximum size of the string : 6826363
            addRecord(record, vm);
        }
        catch (OutOfMemoryError e)
        {
            System.err.println("OutOfMemory Exception while logging. Application data is not corrupted.");
            save(prepareDumpName());
            history = "console:\n";
        }
    }

    private static String prepareDumpName() {
        return "LOGGERDump" + df.format(count++) + ".txt";
    }

    private static void addRecord(String record, VERBOSE_MODE vm)
    {
        if (verbose_mode == VERBOSE_MODE.TOTAL_SILENCE)
            return; // Not recommended to use this mode. Nothing would be stored in files as well!

        if (vm.compareTo(verbose_mode) >= 0)
        {
            if (vm.compareTo(VERBOSE_MODE.WARNING) >= 0)
                System.err.print(record);
            else
                System.out.print(record);
        }

        String r = "\n[:" + vm + ":] " + record;
        history += r ;
        if (history.length() > 1048576) // 1024 * 1024, 1 MByte.
        {
            save(prepareDumpName());
            history = "console:\n";
        }
        if (textAreaConsole != null)
            textAreaConsole.setText(history);

    }
    public static String getHistory() { return history; }
}
