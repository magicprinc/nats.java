// Copyright 2015-2022 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client;

import io.nats.ConsoleOutput;
import io.nats.NatsRunnerUtils;
import io.nats.NatsServerRunner;
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NatsTestServer extends NatsServerRunner {
    static {
        NatsServerRunner.setDefaultOutputSupplier(ConsoleOutput::new);
        quiet();
    }

    public static void quiet() {
        NatsServerRunner.setDefaultOutputLevel(Level.WARNING);
    }

    public static void verbose() {
        NatsServerRunner.setDefaultOutputLevel(Level.ALL);
    }

    public NatsTestServer() throws IOException {
        super();
    }

    public NatsTestServer(boolean debug) throws IOException {
        super(debug);
    }

    public NatsTestServer(boolean debug, boolean jetstream) throws IOException {
        super(debug, jetstream);
    }

    public NatsTestServer(int port, boolean debug) throws IOException {
        super(port, debug);
    }

    public NatsTestServer(int port, boolean debug, boolean jetstream) throws IOException {
        super(port, debug, jetstream);
    }

    public NatsTestServer(String configFilePath, boolean debug) throws IOException {
        super(configFilePath, debug);
    }

    public NatsTestServer(String configFilePath, boolean debug, boolean jetstream) throws IOException {
        super(configFilePath, debug, jetstream);
    }

    public NatsTestServer(String configFilePath, String[] configInserts, int port, boolean debug) throws IOException {
        super(configFilePath, configInserts, port, debug);
    }

    public NatsTestServer(String configFilePath, int port, boolean debug) throws IOException {
        super(configFilePath, port, debug);
    }

    public NatsTestServer(String[] customArgs, boolean debug) throws IOException {
        super(customArgs, debug);
    }

    public NatsTestServer(String[] customArgs, int port, boolean debug) throws IOException {
        super(customArgs, port, debug);
    }

    public NatsTestServer(int port, boolean debug, boolean jetstream, String configFilePath, String[] configInserts, String[] customArgs) throws IOException {
        super(port, debug, jetstream, configFilePath, configInserts, customArgs);
    }

    public NatsTestServer(Builder b) throws IOException {
        super(b);
    }

    public static int nextPort() throws IOException {
        return NatsRunnerUtils.nextPort();
    }

    public String getLocalhostUri(String schema) {
        return NatsRunnerUtils.getLocalhostUri(schema, getPort());
    }

    public String getNatsLocalhostUri() {
        return NatsRunnerUtils.getNatsLocalhostUri(getPort());
    }

    public static String getNatsLocalhostUri(int port) {
        return NatsRunnerUtils.getNatsLocalhostUri(port);
    }

    public static String getLocalhostUri(String schema, int port) {
        return NatsRunnerUtils.getLocalhostUri(schema, port);
    }

    /// force shutdown (NatsServerRunner#shutdown on Windows 10 create dozens of zombie-processes)
    @Override
    public void shutdown(boolean wait) throws InterruptedException {
        try {
            Field processField = NatsServerRunner.class.getDeclaredField("process");
            processField.setAccessible(true);
            Process process = (Process) processField.get(this);
            if (process != null) {
                try {
                    process.destroy();
                    System.out.println("%%% Shut down [" + process + "]");
                    if (wait) {
                        process.waitFor(2, TimeUnit.MINUTES);
                    }
                } finally {
                    process.destroyForcibly();
                    if (process.exitValue() != 0){
                        new Exception("shutdown location: exitValue="+process.exitValue()).printStackTrace();
                    }
                }
                processField.set(this, null);
            }
        } catch (Throwable e) {
            System.err.println("Can't override shutdown(wait) ⇒ fallback to NatsServerRunner");
            e.printStackTrace();
            super.shutdown(wait);
        }
    }

    /// find duplicate ports
    /// taskkill /F /IM nats-server.exe
    public static void main(String[] args) {
        File folder = new File(System.getProperty("java.io.tmpdir"));
        assert folder.isDirectory();

        // Map to store port -> set of filenames containing that port
        val portToFiles = new HashMap<String,Set<String>>();

        File[] files = folder.listFiles((dir, name) -> name.matches("nats_java_test\\d+\\.conf"));
        if (files == null) {
            System.out.println("No matching files found.");
            return;
        }

        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("port:")) {
                        // Extract port number after "port:"
                        String port = line.substring(5).trim();
                        portToFiles.putIfAbsent(port, new HashSet<>());
                        portToFiles.get(port).add(file.getName());
                        break; // assuming only one port line per file
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            }
        }

        // Print duplicate ports with file names
        boolean duplicatesFound = false;
        for (Map.Entry<String, Set<String>> entry : portToFiles.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicatesFound = true;
                System.out.println("Duplicate port " + entry.getKey() + " found in files: " + entry.getValue());
            }
        }

        if (!duplicatesFound) {
            System.out.println("No duplicate ports found.");
        }
    }
}
