package betsy.bpmn.cli;

import java.util.Collections;
import java.util.List;

import betsy.bpmn.engines.AbstractBPMNEngine;
import betsy.bpmn.repositories.BPMNEngineRepository;
import configuration.bpmn.BPMNProcessRepository;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pebl.benchmark.test.Test;

public class BPMNCliParser {

    public static final BPMNCliParameter HELP_ONLY = new BPMNCliParameter() {

        @Override
        public List<AbstractBPMNEngine> getEngines() {
            return Collections.emptyList();
        }

        @Override
        public List<Test> getProcesses() {
            return Collections.emptyList();
        }

        @Override public String getTestFolderName() {
            return "test";
        }

        @Override
        public boolean openResultsInBrowser() {
            return false;
        }

        @Override
        public boolean buildArtifactsOnly() {
            return false;
        }

        @Override
        public boolean showHelp() {
            return true;
        }

        @Override
        public boolean useInstalledEngine() {
            return false;
        }

        @Override
        public boolean useRunningEngine() {
            return false;
        }

        @Override
        public boolean keepEngineRunning() {
            return false;
        }

        @Override
        public boolean saveTimeouts() {return false;
        }
    };
    public static final String HELP = "help";
    public static final String BUILD_ONLY = "build-only";
    public static final String OPEN_RESULTS_IN_BROWSER = "open-results-in-browser";
    public static final String USE_CUSTOM_TEST_FOLDER = "use-custom-test-folder";
    private static final String USE_INSTALLED_ENGINE = "use-installed-engine";
    private static final String USE_RUNNING_ENGINE = "use-running-engine";
    private static final String KEEP_ENGINE_RUNNING = "keep-engine-running";
    private static final String SAVE_TIMEOUTS = "save-timeouts";

    private final String[] args;

    public BPMNCliParser(String... args) {
        this.args = args;
    }

    public BPMNCliParameter parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(getOptions(), args);

            for (Option option : cmd.getOptions()) {
                System.out.println(option.toString());
            }

            if (cmd.hasOption(HELP)) {
                return HELP_ONLY;
            }

            return new BPMNCliParameter() {

                @Override
                public List<AbstractBPMNEngine> getEngines() {
                    return new BPMNEngineParser(cmd.getArgs()).parse();
                }

                @Override
                public List<Test> getProcesses() {
                    return new BPMNProcessParser(cmd.getArgs()).parse();
                }

                @Override public String getTestFolderName() {
                    String optionValue = cmd.getOptionValue(USE_CUSTOM_TEST_FOLDER);
                    if (optionValue != null) {
                        return optionValue;
                    } else {
                        return "test";
                    }
                }

                @Override
                public boolean openResultsInBrowser() {
                    return cmd.hasOption(OPEN_RESULTS_IN_BROWSER);
                }

                @Override
                public boolean buildArtifactsOnly() {
                    return cmd.hasOption(BUILD_ONLY);
                }

                @Override
                public boolean showHelp() {
                    return cmd.hasOption(HELP);
                }

                @Override
                public boolean useInstalledEngine() {
                    return cmd.hasOption(USE_INSTALLED_ENGINE);
                }

                @Override
                public boolean useRunningEngine() {
                    return cmd.hasOption(USE_RUNNING_ENGINE);
                }

                @Override
                public boolean keepEngineRunning() {
                    return cmd.hasOption(KEEP_ENGINE_RUNNING);
                }

                @Override
                public boolean saveTimeouts() {return cmd.hasOption(SAVE_TIMEOUTS);
                }
            };
        } catch (ParseException e) {
            return HELP_ONLY;
        }
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption("o", OPEN_RESULTS_IN_BROWSER, false, "Opens results in default browser");
        options.addOption("b", BUILD_ONLY, false, "Builds only the artifacts. Does nothing else.");
        options.addOption("h", HELP, false, "Print usage information.");
        options.addOption("f", USE_CUSTOM_TEST_FOLDER, true, "Use custom test folder");
        options.addOption("i", USE_INSTALLED_ENGINE, false, "Use already installed engine.");
        options.addOption("k", KEEP_ENGINE_RUNNING, false, "Keep the engine running. No engine shutdown!");
        options.addOption("r", USE_RUNNING_ENGINE, false, "Use already running engine.");
        options.addOption("s", SAVE_TIMEOUTS, false, "Save the during the execution measured timeouts");
        return options;
    }

    public void printUsage() {
        String firstLine = "betsy bpmn [OPTIONS] <ENGINES> <PROCESSES>";
        String header = "\nOptions:\n";
        String footer = "\nGROUPS for <ENGINES> and <PROCESSES> are in CAPITAL LETTERS.\n" +
                "<ENGINES>: " + new BPMNEngineRepository().getNames() + "\n\n\n" +
                "<PROCESSES>: " + new BPMNProcessRepository().getNames() + "\n\n\n" +
                "Please report issues at https://github.com/uniba-dsg/betsy/issues";
        new HelpFormatter().printHelp(firstLine,
                header,
                getOptions(),
                footer);
    }

}
