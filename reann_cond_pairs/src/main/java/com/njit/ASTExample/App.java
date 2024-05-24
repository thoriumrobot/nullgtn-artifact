import java.util.stream.Collectors;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;

public class App {
    static String modDir = "/usr/src/app/nullgtn-artifact/reann_cond_pairs/";
    static File rootDir = new File("/usr/src/app/nullgtn-artifact/minstripped/");
    static String saveDir = "/usr/src/app/nullgtn-artifact/50_20/";

    public static void main(String[] args) {
        modDir = args[0] + "reann_cond_pairs/";
        rootDir = new File(args[1]);
        saveDir = args[2];

        processJavaFiles(rootDir, "");
    }

    public static void processJavaFiles(File rootDir, String subdir) {
        try {
            File[] files = rootDir.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (file.isDirectory() && file.toString().contains("/src/main")) {
                    HashMap<String, Integer> fileCount = new HashMap<>();
                    HashMap<String, ASTToGraphConverter> converters = new HashMap<>();
                    HashMap<String, HashMap<Integer, Double>> scores = new HashMap<>();

                    List<File[]> javaFilePairs = getJavaFilePairs(file.toString());

                    List<CompilationUnit[]> batchCompilationUnits = new ArrayList<>();
                    List<File[]> batchPairs = new ArrayList<>();
                    int currentBatchSize = 0;

                    for (File[] pair : javaFilePairs) {
                        CompilationUnit[] compilationUnits = new CompilationUnit[2];

                        for (int i = 0; i < 2; i++) {
                            if (converters.containsKey(pair[i].getAbsolutePath())) {
                                continue;
                            }
                            compilationUnits[i] = parseJavaFile(pair[i]);
                        }

                        BaseNames[] findnames = new BaseNames[2];
                        int totCount = 0;

                        for (int i = 0; i < 2; i++) {
                            if (compilationUnits[i] != null) {
                                findnames[i] = new BaseNames();
                                findnames[i].convert(compilationUnits[i]);
                                totCount += findnames[i].totCount + findnames[i].nameList.size();
                            }
                        }

                        if (currentBatchSize + totCount > 8000) {
                            processBatch(batchCompilationUnits, batchPairs, fileCount, converters, scores);
                            batchCompilationUnits.clear();
                            batchPairs.clear();
                            currentBatchSize = 0;
                        }

                        batchCompilationUnits.add(compilationUnits);
                        batchPairs.add(pair);
                        currentBatchSize += totCount;
                    }

                    if (!batchCompilationUnits.isEmpty()) {
                        processBatch(batchCompilationUnits, batchPairs, fileCount, converters, scores);
                    }

                    reannotateFiles(fileCount, converters, rootDir, subdir);
                } else if (file.isDirectory()) {
                    processJavaFiles(file, subdir + "/" + file.getName());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processBatch(List<CompilationUnit[]> batchCompilationUnits, List<File[]> batchPairs, HashMap<String, Integer> fileCount, HashMap<String, ASTToGraphConverter> converters, HashMap<String, HashMap<Integer, Double>> scores) throws IOException, InterruptedException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(modDir + "temp_output.json", true));

        for (int j = 0; j < batchPairs.size(); j++) {
            CompilationUnit[] compilationUnits = batchCompilationUnits.get(j);
            File[] pair = batchPairs.get(j);

            for (int i = 0; i < 2; i++) {
                if (converters.containsKey(pair[i].getAbsolutePath())) {
                    continue;
                }

                ExpandNames growNames = new ExpandNames(new BaseNames().nameList);
                growNames.convert(compilationUnits[i]);

                while (!growNames.nameList.equals(growNames.nameList_old)) {
                    growNames = new ExpandNames(growNames.nameList);
                    growNames.convert(compilationUnits[i]);
                }

                converters.put(pair[i].getAbsolutePath(), new ASTToGraphConverter(growNames.nameList));
                converters.get(pair[i].getAbsolutePath()).convert(compilationUnits[i]);
            }

            for (int i = 0; i < 2; i++) {
                JSONObject graphJson = converters.get(pair[i].getAbsolutePath()).toJson();
                writer.write(graphJson.toString(4) + "\\n");

                BufferedWriter writer_cluster = new BufferedWriter(new FileWriter(modDir + "temp_output_" + i + ".json"));
                writer_cluster.write(graphJson.toString(4) + "\\n");
                writer_cluster.close();
            }
        }

        writer.close();

        for (int i = 0; i < 2; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder("python", modDir + "predkmm.py", String.valueOf(i), modDir);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\\n");
            }
            int exitCode = process.waitFor();

            String cluster = (exitCode == 0) ? String.valueOf(output.toString().charAt(0)) : "1";

            processBuilder = new ProcessBuilder("python", modDir + "GTN_comb/predict.py", cluster, modDir);
            process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            exitCode = process.waitFor();

            if (exitCode == 0) {
                String filePath = batchPairs.get(i)[0].getAbsolutePath();
                fileCount.putIfAbsent(filePath, 0);
                fileCount.put(filePath, fileCount.get(filePath) + 1);

                scores.putIfAbsent(filePath, new HashMap<>());
                String outputLine;
                int lineCount = 0;

                while ((outputLine = reader.readLine()) != null) {
                    int rnno, fidx;
                    if (lineCount < converters.get(batchPairs.get(0)[0].getAbsolutePath()).rlvCount) {
                        fidx = 0;
                        rnno = lineCount;
                    } else {
                        fidx = 1;
                        rnno = lineCount - converters.get(batchPairs.get(0)[0].getAbsolutePath()).rlvCount;
                    }

                    scores.get(batchPairs.get(fidx)[0].getAbsolutePath()).putIfAbsent(rnno, 0.0);
                    scores.get(batchPairs.get(fidx)[0].getAbsolutePath()).put(rnno, scores.get(batchPairs.get(fidx)[0].getAbsolutePath()).get(rnno) + Double.parseDouble(outputLine));

                    lineCount++;
                }
            }
        }
        new File(modDir + "temp_output.json").delete();
    }

    private static void reannotateFiles(HashMap<String, Integer> fileCount, HashMap<String, ASTToGraphConverter> converters, File rootDir, String subdir) throws IOException {
        for (Map.Entry<String, Integer> entry : fileCount.entrySet()) {
            File newFile = new File(entry.getKey());
            ASTToGraphConverter fileConverter = converters.get(entry.getKey());
            CompilationUnit fileRoot = (CompilationUnit) fileConverter.storedRoot;

            HashMap<Integer, Double> fileScores = scores.get(entry.getKey());

            for (Map.Entry<Integer, Double> nodeEntry : fileScores.entrySet()) {
                Node node;
                if (!fileConverter.rlvNodes.isEmpty() && nodeEntry.getKey() < fileConverter.rlvNodes.size()) {
                    node = fileConverter.rlvNodes.get(nodeEntry.getKey());
                } else {
                    continue;
                }

                if (((node instanceof MethodDeclaration) && nodeEntry.getValue() > fileCount.get(entry.getKey()) * 0.7341) || ((node instanceof FieldDeclaration) && nodeEntry.getValue() > fileCount.get(entry.getKey()) * 0.8449)) {
                    ((NodeWithAnnotations<?>) node).addAnnotation("Nullable");
                }
            }

            Path rootPath = rootDir.toPath();
            Path rootSub = rootPath.relativize(newFile.toPath());

            String dirPath = saveDir + subdir + "/" + rootSub.getParent() + "/";
            File directory = new File(dirPath);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filePath = dirPath + newFile.getName();
            Files.write(Paths.get(filePath), fileRoot.toString().getBytes(StandardCharsets.UTF_8));
        }
        NullableParameterModifier.processProject(new File(saveDir + subdir + "/"));
        NullableProcessorByName.nPBM(saveDir + subdir + "/");
    }

    private static CompilationUnit parseJavaFile(File file) throws IOException {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        return parseResult.getResult().orElse(null);
    }

    private static List<File[]> getJavaFilePairs(String dirPath) {
        File dir = new File(dirPath);
        List<File[]> filePairs = new ArrayList<>();
        File[] javaFiles = dir.listFiles((d, name) -> name.endsWith(".java"));

        if (javaFiles != null) {
            for (int i = 0; i < javaFiles.length; i++) {
                for (int j = i + 1; j < javaFiles.length; j++) {
                    filePairs.add(new File[]{javaFiles[i], javaFiles[j]});
                }
            }
        }

        return filePairs;
    }
}
