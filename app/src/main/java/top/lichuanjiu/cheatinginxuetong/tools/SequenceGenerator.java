package top.lichuanjiu.cheatinginxuetong.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceGenerator {
    private static final String SEQUENCE_FILE = "sequence.txt";
    private static final AtomicLong sequence = new AtomicLong(loadSequenceFromFile());

    public static long getNextSequence() {
        long nextSequence;
        do {
            nextSequence = sequence.incrementAndGet();
        } while (nextSequence < 0);
        saveSequenceToFile(nextSequence);
        return nextSequence;
    }

    private static long loadSequenceFromFile() {
        try {
            if (Files.exists(Paths.get(SEQUENCE_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(SEQUENCE_FILE)));
                return Long.parseLong(content.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void saveSequenceToFile(long sequence) {
        try (FileWriter writer = new FileWriter(new File(SEQUENCE_FILE))) {
            writer.write(Long.toString(sequence));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
