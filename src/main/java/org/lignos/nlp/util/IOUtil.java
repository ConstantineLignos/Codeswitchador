package org.lignos.nlp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by lignos on 2/23/15.
 */
public class IOUtil {

    public static PrintWriter openPrintWriterOrExit(String path) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(path));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open output file: " + path);
            System.exit(1);
        }
        return writer;
    }

    public static Scanner openScannerOrExit(String path) {
        Scanner reader = null;
        try {
            reader = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open input file: " + path);
            System.exit(1);
        }
        return reader;
    }
}
