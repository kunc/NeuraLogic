package cz.cvut.fel.ida.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SourceFiles extends Sources {
    private static final Logger LOG = Logger.getLogger(SourceFiles.class.getName());

    public File template;
    public File trainExamples;
    public File valExamples;
    public File testExamples;
    public File trainQueries;
    public File valQueries;
    public File testQueries;

    transient private List<Path> subTemplates;
    transient Path mergedTemplatePath;

    public SourceFiles(String foldId, Settings settings) {
        super(foldId, settings);
    }

    public SourceFiles(Settings settings) {
        super(settings);
    }

    public Boolean validate(Settings settings, StringBuilder problems) {
        Boolean valid = isValid(settings, problems);
        return valid;
    }

    @Override
    public void infer(Settings settings) {
        if (checkForSubstring(trainExamples, settings.queryExampleSeparator, 2)) {
            LOG.info("Queries within train examples file detected via separator " + settings.queryExampleSeparator);
            this.train.QueriesProvided = true;
        }
        if (checkForSubstring(valExamples, settings.queryExampleSeparator, 2)) {
            LOG.info("Queries within validation examples file detected via separator " + settings.queryExampleSeparator);
            this.val.QueriesProvided = true;
        }
        if (checkForSubstring(testExamples, settings.queryExampleSeparator, 2)) {
            LOG.info("Queries within test examples file detected via separator " + settings.queryExampleSeparator);
            this.test.QueriesProvided = true;
        }

        super.infer(settings);
    }

    public Boolean isValid(Settings settings, StringBuilder problems) {
        Boolean validate = super.validate(settings, problems);
        //TODO is complete?
        return validate;
    }

    public SourceFiles(Settings settings, CommandLine cmd) {
        super(settings);
        String sourcePath = cmd.getOptionValue("sourcesDir", settings.sourcePath);

        if (cmd.hasOption("sourcesFile")) {
            String sources_ = cmd.getOptionValue("sourcesFile");
            loadFromJson(sources_);
        }

        if (cmd.hasOption("foldPrefix")) {
            String foldPrefix = cmd.getOptionValue("foldPrefix", settings.foldsPrefix);
            if (foldPrefix.contains(File.separator)) {
                LOG.severe("Invalid folds prefix name, it must not contain file separators: " + foldPrefix);
                throw new IllegalArgumentException(foldPrefix);
            }
        }

        File[] foldSubdirs = getFoldSubdirs(settings, cmd, sourcePath);
        if (foldSubdirs != null && foldSubdirs.length > 0) {
            setupFromDir(settings, cmd, Paths.get(sourcePath).toAbsolutePath().toFile());
            crawlFolds(settings, cmd, sourcePath);
        } else {
            setupFromDir(settings, cmd, Paths.get(sourcePath).toAbsolutePath().toFile());
        }
    }

    private File[] getFoldSubdirs(Settings settings, CommandLine cmd, String sourcePath) {
        File dir = new File(sourcePath);
        File[] foldDirs = dir.listFiles((dir1, name) -> name.startsWith(cmd.getOptionValue("foldPrefix", settings.foldsPrefix)));
        return foldDirs;
    }

    private void crawlFolds(Settings settings, CommandLine cmd, String path) {
        File[] foldDirs = getFoldSubdirs(settings, cmd, path);
        this.folds = new ArrayList<>();
        int i = 0;
        if (foldDirs != null) {
            Arrays.sort(foldDirs);
            for (File foldDir : foldDirs) {
                SourceFiles sFold = new SourceFiles(foldDir.getName(), settings);
                sFold.parent = this;
                sFold.setupFromDir(settings, cmd, foldDir);
                this.folds.add(sFold);
                // recursive setup call on this fold sources object
                sFold.crawlFolds(settings, cmd, Paths.get(path, foldDir.toString()).toString());
            }
        }
    }

    /**
     * TODO do not create the parse trees here, process them separately later in the respective builders (load only Readers)
     *
     * @param settings
     * @param cmd
     * @param foldDir
     * @return
     */
    private SourceFiles setupFromDir(Settings settings, CommandLine cmd, File foldDir) {
        LOG.info("Setting up input sourceFiles from directory: " + foldDir);
        try {
            if (this.template != null) {
                settings.templateFile = this.template.getPath();
            }
            String templatePath = cmd.getOptionValue("template", settings.templateFile);
            File template_;

            File templ = sanitizePath(settings, cmd, foldDir, sanitizeTempl(templatePath));
            mergedTemplatePath = Paths.get(templ + settings.mergedTemplatesSuffix + this.foldId);

            if (templatePath.contains(",")) {
                LOG.warning("There are multiple templates, will try to merge them first");
                subTemplates = new ArrayList<>();
                if (templatePath.startsWith("["))
                    templatePath = templatePath.substring(1, templatePath.length() - 2);
                String[] split = templatePath.split(",");
                mergedTemplatePath.toFile().delete();   //delete previously existing
                template_ = mergeTemplates(settings, cmd, foldDir, split);
            } else {
                template_ = getTemplate(settings, cmd, foldDir, templatePath);
            }

            if (template_ != null) {
                String fileType = recognizeFileType(template_.toString(), template_, settings);

                if (fileType.equals("text/x-java")) {
                    binaryTemplateStream = new FileInputStream(template_.toString());
                } else {
                    setupTemplate(template_, foldDir);
                }
            }

        } catch (IOException e) {
            LOG.info("There is no learning template");
        }

        try {
            if (this.trainExamples != null) {
                settings.trainExamplesFile = trainExamples.getPath();
            }
            String trainExamplesPath = cmd.getOptionValue("trainExamples", settings.trainExamplesFile);
            File trainExamples_ = null;
            if (trainExamplesPath.startsWith("\\.") || settings.sourcePathProvided) {
                trainExamples_ = Paths.get(foldDir.toString(), trainExamplesPath).toFile();
            } else {
                trainExamples_ = Paths.get(trainExamplesPath).toFile();
            }
            if (!trainExamples_.exists()) {
                LOG.finer("Could not find trainExamples file in " + trainExamples_ + ", will try to use " + Paths.get(foldDir.toString(), settings.trainExamplesFile2) + " file for the same purpose");
                trainExamples_ = Paths.get(foldDir.toString(), settings.trainExamplesFile2).toFile();
            }

            this.train.ExamplesReader = new FileReader(trainExamples_);
            this.trainExamples = trainExamples_;
            recognizeFileType(this.trainExamples.getAbsolutePath(), trainExamples_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no train examples");
        }

        try {
            if (this.valExamples != null) {
                settings.valExamplesFile = valExamples.getPath();
            }
            String valExamplesPath = cmd.getOptionValue("valExamples", settings.valExamplesFile);
            File valExamples_ = null;
            if (valExamplesPath.startsWith("\\.") || settings.sourcePathProvided) {
                valExamples_ = Paths.get(foldDir.toString(), valExamplesPath).toFile();
            } else {
                valExamples_ = Paths.get(valExamplesPath).toFile();
            }
            this.val.ExamplesReader = new FileReader(valExamples_);
            this.valExamples = valExamples_;
            recognizeFileType(this.valExamples.getAbsolutePath(), valExamples_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no separate validation examples found.");
        }

        try {
            if (this.testExamples != null) {
                settings.testExamplesFile = testExamples.getPath();
            }
            String testExamplesPath = cmd.getOptionValue("testExamples", settings.testExamplesFile);
            File testExamples_ = null;
            if (testExamplesPath.startsWith("\\.") || settings.sourcePathProvided) {
                testExamples_ = Paths.get(foldDir.toString(), testExamplesPath).toFile();
            } else {
                testExamples_ = Paths.get(testExamplesPath).toFile();
            }
            this.test.ExamplesReader = new FileReader(testExamples_);
            this.testExamples = testExamples_;
            recognizeFileType(this.testExamples.getAbsolutePath(), testExamples_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no separate test examples found.");
        }

        try {
            if (this.trainQueries != null) {
                settings.trainQueriesFile = trainQueries.getPath();
            }
            String trainQueriesPath = cmd.getOptionValue("trainQueries", settings.trainQueriesFile);
            File trainQueries_ = null;
            if (trainQueriesPath.startsWith("\\.") || settings.sourcePathProvided) {
                trainQueries_ = Paths.get(foldDir.toString(), trainQueriesPath).toFile();
            } else {
                trainQueries_ = Paths.get(trainQueriesPath).toFile();
            }
            if (!trainQueries_.exists()) {
                LOG.finer("Could not find trainQueries file in " + trainQueries_ + ", will try to use " + Paths.get(foldDir.toString(), settings.trainQueriesFile2) + " file for the same purpose");
                trainQueries_ = Paths.get(foldDir.toString(), settings.trainQueriesFile2).toFile();
            }

            this.train.QueriesReader = new FileReader(trainQueries_);
            this.trainQueries = trainQueries_;

            recognizeFileType(this.trainQueries.getAbsolutePath(), trainQueries_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no separate train queries found.");
        }

        try {
            if (this.valQueries != null) {
                settings.valQueriesFile = valQueries.getPath();
            }
            String valQueriesPath = cmd.getOptionValue("valQueries", settings.valQueriesFile);
            File valQueries_;
            if (valQueriesPath.startsWith("\\.") || settings.sourcePathProvided) {
                valQueries_ = Paths.get(foldDir.toString(), valQueriesPath).toFile();
            } else {
                valQueries_ = Paths.get(valQueriesPath).toFile();
            }
            this.val.QueriesReader = new FileReader(valQueries_);
            this.valQueries = valQueries_;
            recognizeFileType(this.valQueries.getAbsolutePath(), valQueries_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no separate validation queries found.");
        }

        try {
            if (this.testQueries != null) {
                settings.testQueriesFile = testQueries.getPath();
            }
            String testQueriesPath = cmd.getOptionValue("testQueries", settings.testQueriesFile);
            File testQueries_;
            if (testQueriesPath.startsWith("\\.") || settings.sourcePathProvided) {
                testQueries_ = Paths.get(foldDir.toString(), testQueriesPath).toFile();
            } else {
                testQueries_ = Paths.get(testQueriesPath).toFile();
            }
            this.test.QueriesReader = new FileReader(testQueries_);
            this.testQueries = testQueries_;
            recognizeFileType(this.testQueries.getAbsolutePath(), testQueries_, settings);

        } catch (FileNotFoundException e) {
            LOG.info("There are no separate test queries found.");
        }

        return this;
    }

    public static String sanitizeTempl(String name) {
        String sane = name.replaceAll("[,:;'\\[\\]]", "_");
        return sane;
    }

    private void setupTemplate(File template_, File foldDir) throws IOException {
        AtomicBoolean changed = new AtomicBoolean(false);
        Path templPath = template_.toPath();
        String content = checkTemplate4Imports(templPath, changed, foldDir);
        if (changed.get()) {
            Files.write(mergedTemplatePath, content.getBytes());
            template_ = mergedTemplatePath.toFile();
        }
        this.setTemplateReader(new FileReader(template_));
        this.template = template_;
    }

    private String checkTemplate4Imports(Path toImport, AtomicBoolean changed, File foldDir) throws FileNotFoundException {
        try {
            if (toImport.toString().startsWith(".")) { //todo next repair relative path finding if subtemplate with import statement came from different folder
                toImport = Paths.get(foldDir + "/" + toImport);
            }
            List<String> strings = Files.readAllLines(toImport);
            for (int i = 0; i < strings.size(); i++) {
                String line = strings.get(i);
                if (line.startsWith("import ")) {
                    changed.set(true);
                    line = line.substring(7);
                    line = line.replaceAll(" ", "");
                    if (line.contains(",")) {
                        String[] split = line.split(",");
                        StringBuilder sb = new StringBuilder();
                        for (String templ : split) {
                            sb.append(checkTemplate4Imports(Paths.get(templ), changed, foldDir));
                        }
                        strings.set(i, sb.toString());
                    } else {
                        String imported = checkTemplate4Imports(Paths.get(line), changed, foldDir);
                        strings.set(i, imported);
                    }
                }
            }
            return String.join("\n", strings);
        } catch (IOException e) {
            LOG.severe("There is no subtemplate found at the specified path! : " + toImport);
            throw new FileNotFoundException();
        }
    }

    private File mergeTemplates(Settings settings, CommandLine cmd, File foldDir, String[] split) throws FileNotFoundException {
        File templ = mergedTemplatePath.toFile();
        for (String path : split) {
            File setupTemplate = getTemplate(settings, cmd, foldDir, path);
            if (setupTemplate != null) {
                subTemplates.add(Paths.get(setupTemplate.getPath()));
            }
        }
        for (Path subTemplate : subTemplates) {
            try {
                List<String> strings = Files.readAllLines(subTemplate);
                if (templ.exists())
                    Files.write(templ.toPath(), strings, StandardOpenOption.APPEND);
                else
                    Files.write(templ.toPath(), strings, StandardOpenOption.CREATE);
            } catch (IOException e) {
                LOG.severe("There is no subtemplate found at the specified path! : " + subTemplate);
                throw new FileNotFoundException();
            }
        }
        return templ;
    }

    private File getTemplate(Settings settings, CommandLine cmd, File foldDir, String templatePath) throws FileNotFoundException {
        File template_ = sanitizePath(settings, cmd, foldDir, templatePath);

        if (template_.exists()) {
            if (parent != null && parent.getTemplateReader() != null) {
                LOG.warning("Inconsistent setting - there are templates both in parent folder and fold folder (don't know which one to use)");
            }
            return template_;
        } else {
            // if no template is provided in current folder, take the one from the parent folder
            if (parent != null) {
                this.setTemplateReader(parent.getTemplateReader());
                this.template = ((SourceFiles) parent).template;
                return null;
            } else {
                LOG.finer("Could not find template file in " + template_ + ", will try to use " + foldDir.toString() + "/" + settings.templateFile2 + " file for the same purpose");
                template_ = Paths.get(foldDir.toString(), settings.templateFile2).toFile();
                if (template_.exists()) {
                    return template_;
                } else {
                    LOG.severe("There is no template found at the specified path! : " + templatePath);
                    throw new FileNotFoundException();
                }
            }
        }
    }

    private File sanitizePath(Settings settings, CommandLine cmd, File foldDir, String templatePath) {
        File template_;
        if (templatePath.startsWith(".") || (settings.sourcePathProvided && !cmd.hasOption("template"))) {
            template_ = Paths.get(foldDir.toString(), templatePath).toFile();
        } else {
            template_ = Paths.get(templatePath).toFile();
        }
        return template_;
    }

    private String recognizeFileType(String path, File sourceType, Settings settings) {
        String contentType = identifyFileTypeUsingFilesProbeContentType(path);
        switch (contentType) {
            case "text/plain":
                settings.plaintextInput = true;
                LOG.fine("Input " + sourceType + " file type identified as plain text");
                break;
            case "text/x-microdvd":
                settings.plaintextInput = true;
                LOG.fine("Input " + sourceType + " file type identified as plain text (text/x-microdvd)");
                break;
            case "application/xml":
                LOG.fine("Input " + sourceType + " file type identified as xml");
                break;
            case "application/json":
                LOG.fine("Input " + sourceType + " file type identified as json");
                break;
            case "text/x-java":
                LOG.fine("Input " + sourceType + " file type identified as binary/java");
                break;
            default:
                LOG.warning("File type of input " + sourceType + " not recognized!");
        }
        return contentType;
    }

    public boolean checkForSubstring(File file, String substring, int numberOfLines) {

        if (file == null) {
            return false;
        }
        //are queries hidden in the example file? Quick check
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null && i++ < numberOfLines) {
                if (line.contains(substring))
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void importFromCSV(String inPath) {

    }

    public SourceFiles loadFromJson(String inPath) {
        InstanceCreator<SourceFiles> creator = new InstanceCreator<SourceFiles>() {
            public SourceFiles createInstance(Type type) {
                return SourceFiles.this;
            }
        };
        Gson gson = new GsonBuilder().registerTypeAdapter(SourceFiles.class, creator).create();
        try {
            String json = new String(Files.readAllBytes(Paths.get(inPath)));
            SourceFiles sources = gson.fromJson(json, SourceFiles.class);
            return sources;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SourceFiles loadFromJson(Settings settings, String inPath) {
        SourceFiles sourceFiles = null;
        if (new File(inPath).exists())
            sourceFiles = new SourceFiles(settings).loadFromJson(inPath);
        else if (Paths.get(inPath, "sources.json").toFile().exists())
            sourceFiles = new SourceFiles(settings).loadFromJson(Paths.get(inPath, "sources.json").toString());
        if (sourceFiles == null) {
            LOG.warning("No SourceFiles have been found at: " + inPath);
        }
        return sourceFiles;
    }

    public void exportToCSV(String outPath) {

    }

    public static String identifyFileTypeUsingFilesProbeContentType(final String fileName) {
        String fileType = null;
        final File file = new File(fileName);
        try {
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException ioException) {
            LOG.severe("ERROR: Unable to determine file type for " + fileName + " due to IOException " + ioException);
        }
        if (fileType == null) {
            LOG.severe("ERROR: Unable to determine file type (for unknown reason, probably opened by other process?): " + fileName + " defaulting to text/plain");
            fileType = "text/plain";
        }
        return fileType;
    }

    @Override
    public Reader getTemplateReader() {
        if (template == null){
            return null;
        }
        try {
            if (super.getTemplateReader() == null || !super.getTemplateReader().ready()) {
                this.setTemplateReader(new FileReader(template));
            }
        } catch (Exception e) {
            try {
                this.setTemplateReader(new FileReader(template));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return super.getTemplateReader();
    }
}