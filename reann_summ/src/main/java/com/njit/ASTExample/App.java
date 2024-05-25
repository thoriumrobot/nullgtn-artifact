package com.njit.ASTExample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class App {
    static String modDir = "/usr/src/app/nullgtn-artifact/reann_summ/";
    static File rootDir = new File("/usr/src/app/nullgtn-artifact/minstripped/");
    static String saveDir = "/usr/src/app/nullgtn-artifact/50_20/";

    /*
    Loop over the input classes, parse AST, convert to graph and save to file.
    main() also tries to prune each AST based on several considerations.
    */
    public static void main(String[] args) {
        modDir = args[0] + "reann_summ/";
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
                    HashMap<String, ASTToGraphConverterSumm> converters = new HashMap<>();
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

                        // Chunking the AST if totCount exceeds 8000
                        if (totCount > 8000) {
                            List<CompilationUnit[]> chunks = chunkAST(compilationUnits, findnames, 8000);
                            for (CompilationUnit[] chunk : chunks) {
                                batchCompilationUnits.add(chunk);
                                batchPairs.add(pair);
                            }
                        } else {
                            batchCompilationUnits.add(compilationUnits);
                            batchPairs.add(pair);
                        }

                        currentBatchSize += totCount;

                        if (currentBatchSize > 8000) {
                            processBatch(batchCompilationUnits, batchPairs, fileCount, converters, scores);
                            batchCompilationUnits.clear();
                            batchPairs.clear();
                            currentBatchSize = 0;
                        }
                    }

                    if (!batchCompilationUnits.isEmpty()) {
                        processBatch(batchCompilationUnits, batchPairs, fileCount, converters, scores);
                    }

                    reannotateFiles(fileCount, converters, rootDir, subdir, scores);
                } else if (file.isDirectory()) {
                    processJavaFiles(file, subdir + "/" + file.getName());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<CompilationUnit[]> chunkAST(CompilationUnit[] compilationUnits, BaseNames[] findnames, int maxNodes) {
        List<CompilationUnit[]> chunks = new ArrayList<>();
        
        for (int i = 0; i < 2; i++) {
            if (compilationUnits[i] == null) continue;
            
            List<Node> nodes = compilationUnits[i].getChildNodes();
            List<Node> currentChunk = new ArrayList<>();
            int currentCount = 0;

            for (Node node : nodes) {
                int nodeCount = countNodes(node);
                if (currentCount + nodeCount > maxNodes) {
                    chunks.add(new CompilationUnit[]{createChunk(currentChunk, findnames[i])});
                    currentChunk.clear();
                    currentCount = 0;
                }
                currentChunk.add(node);
                currentCount += nodeCount;
            }
            
            if (!currentChunk.isEmpty()) {
                chunks.add(new CompilationUnit[]{createChunk(currentChunk, findnames[i])});
            }
        }

        return chunks;
    }

    private static int countNodes(Node node) {
        return node.getChildNodes().size() + 1;
    }

    private static CompilationUnit createChunk(List<Node> nodes, BaseNames findnames) {
        CompilationUnit chunk = new CompilationUnit();
        for (Node node : nodes) {
            if (node instanceof TypeDeclaration) {
                chunk.addType((TypeDeclaration<?>) node.clone());
            } else {
                chunk.addOrphanComment(node.getComment().orElse(null));
                chunk.addOrphanComment(node.clone().getComment().orElse(null));
            }
        }
        findnames.convert(chunk);
        return chunk;
    }

    private static void processBatch(List<CompilationUnit[]> batchCompilationUnits, List<File[]> batchPairs, HashMap<String, Integer> fileCount, HashMap<String, ASTToGraphConverterSumm> converters, HashMap<String, HashMap<Integer, Double>> scores) throws IOException, InterruptedException {
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

                converters.put(pair[i].getAbsolutePath(), new ASTToGraphConverterSumm(growNames.nameList));
                converters.get(pair[i].getAbsolutePath()).convert(compilationUnits[i]);
            }

            for (int i = 0; i < 2; i++) {
                JSONObject graphJson = converters.get(pair[i].getAbsolutePath()).toJson();
                writer.write(graphJson.toString(4) + "\n");

                BufferedWriter writer_cluster = new BufferedWriter(new FileWriter(modDir + "temp_output_" + i + ".json"));
                writer_cluster.write(graphJson.toString(4) + "\n");
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
                output.append(line).append("\n");
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

    private static void reannotateFiles(HashMap<String, Integer> fileCount, HashMap<String, ASTToGraphConverterSumm> converters, File rootDir, String subdir, HashMap<String, HashMap<Integer, Double>> scores) throws IOException {
        for (Map.Entry<String, Integer> entry : fileCount.entrySet()) {
            File newFile = new File(entry.getKey());
            ASTToGraphConverterSumm fileConverter = converters.get(entry.getKey());
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

    private static List<File> getJavaFiles(File dir) {
        List<File> javaFiles = new ArrayList<>();

        File[] files = dir.listFiles();
        if (files == null) {
            return javaFiles;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                javaFiles.addAll(getJavaFiles(file));
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }

        return javaFiles;
    }

    public static List<File[]> getJavaFilePairs(String directoryPath) {
        List<File[]> pairs = new ArrayList<>();
        List<File> javaFiles = getJavaFiles(new File(directoryPath));

        for (int i = 0; i < javaFiles.size(); i++) {
            for (int j = i + 1; j < javaFiles.size(); j++) {
                pairs.add(new File[] {javaFiles.get(i), javaFiles.get(j)});
            }
        }

        return pairs;
    }

    public static CompilationUnit parseJavaFile(File file) {
        JavaParser parser = new JavaParser();
        try {
            ParseResult<CompilationUnit> parseResult = parser.parse(file);
            if (parseResult.isSuccessful()) {
                return parseResult.getResult().orElse(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveJsonToFile(String filePath, JSONArray jsonArray) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
