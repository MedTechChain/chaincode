package nl.medtechchain.chaincode.subprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SubprocessCall {
    public static String execute(String command) throws IOException {

        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null)
            output.append(line);

        try {
            process.waitFor();
        } catch (InterruptedException ignored) {
        }

        if (process.exitValue() != 0) {
            String msg = String.format("Error executing command '%s': Process exited with error status code %d: %s", command, process.exitValue(), output);
            throw new IOException(msg);
        }

        return output.toString();
    }
}
