package fr.ensimag.deca;

import java.io.File;
import org.apache.log4j.Logger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);

    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        AtomicBoolean error = new AtomicBoolean(false);
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {
            // Colors : \033[48;5;207m
            System.out.println(
                    "\n" +
                            "\033[48;5;55m\033[38;5;0m\033[5m╔══════════════════════════════════════════════════════════════════════════════╗\033[0m\n" +
                            "\033[48;5;93m\033[38;5;0m\033[5m║\033[0m\033[48;5;93m                                                                              \033[38;5;0m\033[5m║\033[48;5;0m\n" +
                            "\033[48;5;93m\033[38;5;0m\033[5m║\033[0m\033[48;5;93m\033[38;5;226m                 \033[48;5;0m ____            _      _      ____ _ \033[48;5;93m                       \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;92m\033[38;5;0m\033[5m║\033[0m\033[48;5;92m\033[38;5;226m                \033[48;5;0m |  _ \\ _ __ ___ (_) ___| |_   / ___| | \033[48;5;92m                      \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;92m\033[38;5;0m\033[5m║\033[0m\033[48;5;92m\033[38;5;226m                \033[48;5;0m | |_) | '__/ _ \\| |/ _ \\ __| | |  _| | \033[48;5;92m                      \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;91m\033[38;5;0m\033[5m║\033[0m\033[48;5;91m\033[38;5;226m                \033[48;5;0m |  __/| | | (_) | |  __/ |_  | |_| | |___ \033[48;5;91m                   \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;164m\033[38;5;0m\033[5m║\033[0m\033[48;5;164m\033[38;5;226m                \033[48;5;0m |_|   |_|  \\___// |\\___|\\__|  \\____|_____| \033[48;5;164m                  \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;165m\033[38;5;0m\033[5m║\033[0m\033[48;5;165m\033[38;5;226m     \033[48;5;0m  ____         __      ___|__/          _              _____ _  _ \033[48;5;165m       \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;207m\033[38;5;0m\033[5m║\033[0m\033[48;5;207m\033[38;5;226m    \033[48;5;0m  / ___|_ __   / /_    | ____|__ _ _   _(_)_ __   ___  |___ /| ║ | \033[48;5;207m       \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;207m\033[38;5;0m\033[5m║\033[0m\033[48;5;207m\033[38;5;226m    \033[48;5;0m | |  _| '__| | '_ \\   |  _| / _` | | | | | '_ \\ / _ \\   |_ \\| ║ |_ \033[48;5;207m      \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;206m\033[38;5;0m\033[5m║\033[0m\033[48;5;206m\033[38;5;226m    \033[48;5;0m | |_| | |    | (_) |  | |__| (_| | |_| | | |_) |  __/  ___) |__   _| \033[48;5;206m    \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;205m\033[38;5;0m\033[5m║\033[0m\033[48;5;205m\033[38;5;226m     \033[48;5;0m \\____|_|     \\___( ) |_____\\__, |\\__,_|_| .__/ \\___| |____/   |_| \033[48;5;205m      \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;204m\033[38;5;0m\033[5m║\033[0m\033[48;5;204m\033[38;5;226m                      \033[48;5;0m |/ \033[48;5;204m         \033[48;5;0m |_| \033[48;5;204m     \033[48;5;0m |_| \033[48;5;204m                            \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;203m\033[38;5;0m\033[5m║\033[0m\033[48;5;203m                                                                              \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;202m\033[38;5;0m\033[5m║\033[0m\033[48;5;202m\033[38;5;0m             BORNARD Mael, FOKOU DJONTU Manuella, GAUTIER Mattéo,             \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;214m\033[38;5;0m\033[5m║\033[0m\033[48;5;214m\033[38;5;0m                          JEULIN Matteo, MELLOT Arthur                        \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;220m\033[38;5;0m\033[5m║\033[0m\033[48;5;220m                                                                              \033[38;5;0m\033[5m║\033[0m\n" +
                            "\033[48;5;215m\033[38;5;0m\033[5m╚══════════════════════════════════════════════════════════════════════════════╝\033[0m\n" +
                            "\033[0m\n");
            System.exit(0);
        }
        if (options.getSourceFiles().isEmpty()) {
            options.displayUsage();
            System.exit(0);
        }
        if (options.getParallel()) {
            // A FAIRE : instancier DecacCompiler pour chaque fichier à
            // compiler, et lancer l'exécution des méthodes compile() de chaque
            // instance en parallèle. Il est conseillé d'utiliser
            // java.util.concurrent de la bibliothèque standard Java.
            // throw new UnsupportedOperationException("Parallel build not yet implemented");

            // Actual implementation
            ExecutorService executor = Executors.newFixedThreadPool(options.getSourceFiles().size());
            for (File source : options.getSourceFiles()) {
                executor.execute(() -> {
                    DecacCompiler compiler = new DecacCompiler(options, source);
                    if (compiler.compile()) {
                        error.set(true);
                    }
                });
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }


        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error.set(true);
                }
            }
        }
        System.exit(error.get() ? 1 : 0);
    }
}
