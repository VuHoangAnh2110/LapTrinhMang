package server.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe logging utility with file output and console output
 * Supports different log levels and automatic log rotation
 */
public class Logger {
    public enum Level {
        DEBUG, INFO, WARN, ERROR, FATAL
    }
    
    private static Logger instance;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private BlockingQueue<LogEntry> logQueue;
    private Thread loggerThread;
    private boolean isRunning;
    
    // Configuration
    private Level currentLevel = Level.INFO;
    private boolean enableConsoleOutput = true;
    private boolean enableFileOutput = true;
    private String logDirectory = "logs";
    private String logFilePrefix = "server";
    private int maxLogFileSize = 10 * 1024 * 1024; // 10MB
    private int maxLogFiles = 10;
    
    // File handling
    private PrintWriter fileWriter;
    private String currentLogFile;
    
    private Logger() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.isRunning = true;

        // Create log directory
        createLogDirectory();

        // Start logger thread
        this.loggerThread = new Thread(this::processLogs, "Logger-Thread");
        this.loggerThread.setDaemon(true);
        this.loggerThread.start();

        // Đảm bảo flush và đóng file khi JVM tắt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            flushPendingLogs();  // ghi hết log trong queue
            shutdown();          // đóng writer
        }));
    }
    
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    // Main logging methods
    public static void logSync(String message) {
        getInstance().logSync(Level.INFO, message);
    }
    
    public static void debugSync(String message) {
        getInstance().logSync(Level.DEBUG, message);
    }
    
    public static void infoSync(String message) {
        getInstance().logSync(Level.INFO, message);
    }
    
    public static void warnSync(String message) {
        getInstance().logSync(Level.WARN, message);
    }
    
    public static void errorSync(String message) {
        getInstance().logSync(Level.ERROR, message);
    }
    
    public static void errorSync(String message, Throwable throwable) {
        getInstance().logSync(Level.ERROR, message + " - " + getStackTrace(throwable));
    }
    
    public static void fatalSync(String message) {
        getInstance().logSync(Level.FATAL, message);
    }
    
    private void logSync(Level level, String message) {
        if (level.ordinal() >= currentLevel.ordinal()) {
            LogEntry entry = new LogEntry(level, message, System.currentTimeMillis());
            
            try {
                logQueue.offer(entry);
            } catch (Exception e) {
                // Fallback to direct console output if queue fails
                System.err.println("Logger queue failed: " + e.getMessage());
                outputToConsole(entry);
            }
        }
    }

    public static void flushLogs() {
        getInstance().flushPendingLogs();
    }

    private void flushPendingLogs() {
        // Process all remaining entries in queue
        while (!logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.poll(); // Non-blocking
                if (entry != null) {
                    if (enableConsoleOutput) {
                        outputToConsole(entry);
                    }
                    
                    if (enableFileOutput) {
                        outputToFile(entry);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error flushing log: " + e.getMessage());
            }
        }
        
        // Force file flush
        if (fileWriter != null) {
            fileWriter.flush();
        }
    }
    
    private void processLogs() {
        while (isRunning || !logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.take();
                
                if (enableConsoleOutput) {
                    outputToConsole(entry);
                }
                
                if (enableFileOutput) {
                    outputToFile(entry);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing log entry: " + e.getMessage());
            }
        }
    }
    
    private void outputToConsole(LogEntry entry) {
        String formattedMessage = formatLogEntry(entry);
        
        if (entry.level == Level.ERROR || entry.level == Level.FATAL) {
            System.err.println(formattedMessage);
        } else {
            System.out.println(formattedMessage);
        }
    }
    
    private void outputToFile(LogEntry entry) {
        try {
            ensureFileWriter();
            
            if (fileWriter != null) {
                fileWriter.println(formatLogEntry(entry));
                fileWriter.flush();
                
                // Check if log rotation is needed
                checkLogRotation();
            }
            
        } catch (Exception e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    private String formatLogEntry(LogEntry entry) {
        return String.format("[%s] [%s] %s", 
            DATE_FORMAT.format(new Date(entry.timestamp)),
            entry.level.name(),
            entry.message
        );
    }
    
    private void ensureFileWriter() {
        String expectedLogFile = getLogFileName();
        
        if (!expectedLogFile.equals(currentLogFile) || fileWriter == null) {
            closeFileWriter();
            openFileWriter(expectedLogFile);
        }
    }
    
    private void openFileWriter(String logFile) {
        try {
            File file = new File(logFile);
            FileWriter fw = new FileWriter(file, true); // Append mode
            fileWriter = new PrintWriter(fw, true); // Auto-flush
            currentLogFile = logFile;
            
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
            enableFileOutput = false;
        }
    }
    
    private void closeFileWriter() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
        currentLogFile = null;
    }
    
    private String getLogFileName() {
        String date = FILE_DATE_FORMAT.format(new Date());
        return logDirectory + File.separator + logFilePrefix + "_" + date + ".log";
    }
    
    private void createLogDirectory() {
        File dir = new File(logDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Failed to create log directory: " + logDirectory);
                enableFileOutput = false;
            }
        }
    }
    
    private void checkLogRotation() {
        if (currentLogFile == null) return;
        
        File currentFile = new File(currentLogFile);
        if (currentFile.length() > maxLogFileSize) {
            rotateLogFiles();
        }
    }
    
    private void rotateLogFiles() {
        closeFileWriter();
        
        try {
            // Delete oldest log files if we exceed the limit
            File logDir = new File(logDirectory);
            File[] logFiles = logDir.listFiles((dir, name) -> 
                name.startsWith(logFilePrefix) && name.endsWith(".log"));
            
            if (logFiles != null && logFiles.length >= maxLogFiles) {
                // Sort by last modified time and delete oldest
                java.util.Arrays.sort(logFiles, 
                    (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                
                for (int i = 0; i < logFiles.length - maxLogFiles + 1; i++) {
                    if (!logFiles[i].delete()) {
                        System.err.println("Failed to delete old log file: " + logFiles[i].getName());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during log rotation: " + e.getMessage());
        }
    }
    
    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    // Configuration methods
    public void setLevel(Level level) {
        this.currentLevel = level;
    }
    
    public void setConsoleOutput(boolean enabled) {
        this.enableConsoleOutput = enabled;
    }
    
    public void setFileOutput(boolean enabled) {
        this.enableFileOutput = enabled;
    }
    
    public void setLogDirectory(String directory) {
        this.logDirectory = directory;
        createLogDirectory();
    }
    
    public void setMaxLogFileSize(int sizeInBytes) {
        this.maxLogFileSize = sizeInBytes;
    }
    
    public void setMaxLogFiles(int maxFiles) {
        this.maxLogFiles = maxFiles;
    }
    
    // Shutdown method
    public void shutdown() {
        isRunning = false;
        
        if (loggerThread != null) {
            loggerThread.interrupt();
            try {
                loggerThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        closeFileWriter();
    }
    
    // Statistics
    public int getQueueSize() {
        return logQueue.size();
    }
    
    public Level getCurrentLevel() {
        return currentLevel;
    }
    
    public boolean isFileOutputEnabled() {
        return enableFileOutput;
    }
    
    public boolean isConsoleOutputEnabled() {
        return enableConsoleOutput;
    }
    
    // Inner class for log entries
    private static class LogEntry {
        final Level level;
        final String message;
        final long timestamp;
        
        LogEntry(Level level, String message, long timestamp) {
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}