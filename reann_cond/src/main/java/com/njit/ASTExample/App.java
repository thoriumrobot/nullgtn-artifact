package com.njit.ASTExample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;

public class App {
    static String modDir = "/usr/src/app/nullgtn-artifact/reann_cond_pairs/";
    static File rootDir = new File("/usr/src/app/nullgtn-artifact/minstripped/");
    static String saveDir = "/usr/src/app/nullgtn-artifact/50_20/";

    /*
    Loop over the input classes, parse AST, convert to graph and save to file.
    main() also tries to prune each AST based on several considerations.
    */
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
                if (file.isDirectory()) {
                    processJavaFiles(file, subdir + "/" + file.getName());
                } else if (file.getName().endsWith(".java")
                        && file.toString().contains("/src/main/")) {
                    CompilationUnit compilationUnit = parseJavaFile(file);

                    if (compilationUnit != null) {
                        BaseNames findnames = new BaseNames();
                        findnames.convert(compilationUnit);

                        if (findnames.totCount > 300) {
                            ExpandNames grownames = new ExpandNames(findnames.nameList);
                            grownames.convert(compilationUnit);

                            while (!grownames.nameList.equals(grownames.nameList_old)) {
                                grownames = new ExpandNames(grownames.nameList);
                                grownames.convert(compilationUnit);
                            }

                            // Sample or partition the AST nodes if totCount exceeds 8000
                            if (grownames.totCount > 8000) {
                                grownames.nameList = sampleNodes(grownames.nameList, 8000);
                            }

                            ASTToGraphConverter converter =
                                    new ASTToGraphConverter(grownames.nameList);
                            converter.convert(compilationUnit);

                            BufferedWriter writer =
                                    new BufferedWriter(new FileWriter(modDir + "temp_output.json"));
                            JSONObject graphJson = converter.toJson();
                            writer.write(graphJson.toString(4) + "\n");
                            writer.close();

                            // cluster class
                            ProcessBuilder processBuilder =
                                    new ProcessBuilder("python", modDir + "predkmm.py");
                            Process process = processBuilder.start();
                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(process.getInputStream()));
                            StringBuilder output = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                output.append(line).append("\n");
                            }
                            int exitCode = process.waitFor();
                            String cluster;

                            if (exitCode == 0) {
                                cluster = "data" + String.valueOf(output.toString().charAt(0));
                            } else {
                                cluster = "data1";
                            }

                            // predict the nodes
                            processBuilder =
                                    new ProcessBuilder(
                                            "python", modDir + "GTN_comb/predict.py", cluster,
                                                    modDir);
                            process = processBuilder.start();
                            reader =
                                    new BufferedReader(
                                            new InputStreamReader(process.getInputStream()));
                            output = new StringBuilder();
                            line = "";
                            while ((line = reader.readLine()) != null) {
                                output.append(line).append("\n");
                            }
                            exitCode = process.waitFor();

                            if (exitCode == 0) {
                                // reannotate the file
                                // compilationUnit = parseJavaFile(file);
                                ReannotateClass rc =
                                        new ReannotateClass(output.toString(), grownames.nameList);

                                rc.convert(compilationUnit);

                                // save the file
                                String newFile = rc.toString();
                                String dirpath = saveDir.endsWith("/") ? saveDir + subdir : saveDir + "/" + subdir;
                                File directory = new File(dirpath);
                                if (!directory.exists()) {
                                    directory.mkdirs();
                                }
                                String filePath = dirpath + file.getName();
                                Files.write(
                                        Paths.get(filePath),
                                        newFile.getBytes(StandardCharsets.UTF_8));
                            }

                            File delete_file = new File(modDir + "temp_output.json");
                            delete_file.delete();
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<File> getJavaFiles(String directoryPath) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
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

public static Map<String, Set<Integer>> sampleNodes(Map<String, Set<Integer>> nameList, int maxNodes) {
    Map<String, Set<Integer>> sampledNameList = new HashMap<>();
    int nodeCount = 0;

    // Prioritize important nodes (e.g., based on identifiers) if needed
    List<Map.Entry<String, Set<Integer>>> entries = new ArrayList<>(nameList.entrySet());
    entries.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));

    for (Map.Entry<String, Set<Integer>> entry : entries) {
        Set<Integer> currentSet = entry.getValue();
        if (nodeCount + currentSet.size() <= maxNodes) {
            sampledNameList.put(entry.getKey(), currentSet);
            nodeCount += currentSet.size();
        } else {
            Set<Integer> sampledSet = new HashSet<>();
            Iterator<Integer> iterator = currentSet.iterator();
            while (iterator.hasNext() && nodeCount < maxNodes) {
                sampledSet.add(iterator.next());
                nodeCount++;
            }
            sampledNameList.put(entry.getKey(), sampledSet);
            break;
        }
    }


    public static Map<String, Set<Integer>> sampleNodes(Map<String, Set<Integer>> nameList, int maxNodes) {
        Map<String, Set<Integer>> sampledNameList = new HashMap<>();
        int nodeCount = 0;
    
        // Prioritize important nodes (e.g., based on identifiers) if needed
        List<Map.Entry<String, Set<Integer>>> entries = new ArrayList<>(nameList.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));
    
        for (Map.Entry<String, Set<Integer>> entry : entries) {
            Set<Integer> currentSet = entry.getValue();
            if (nodeCount + currentSet.size() <= maxNodes) {
                sampledNameList.put(entry.getKey(), currentSet);
                nodeCount += currentSet.size();
            } else {
                Set<Integer> sampledSet = new HashSet<>();
                Iterator<Integer> iterator = currentSet.iterator();
                while (iterator.hasNext() && nodeCount < maxNodes) {
                    sampledSet.add(iterator.next());
                    nodeCount++;
                }
                sampledNameList.put(entry.getKey(), sampledSet);
                break;
            }
        }
    
        // Ensure the nameList is complete for the chosen nodes
        ensureCompleteNameList(sampledNameList, nameList);
    
        return sampledNameList;
    }
    
    private static void ensureCompleteNameList(Map<String, Set<Integer>> sampledNameList, Map<String, Set<Integer>> originalNameList) {
        for (String key : sampledNameList.keySet()) {
            if (!sampledNameList.get(key).equals(originalNameList.get(key))) {
                sampledNameList.put(key, originalNameList.get(key));
            }
        }
    }
    public static Map<String, Set<Integer>> sampleNodes(Map<String, Set<Integer>> nameList, int maxNodes) {
        Map<String, Set<Integer>> sampledNameList = new HashMap<>();
        int nodeCount = 0;
    
        // Prioritize important nodes (e.g., based on identifiers) if needed
        List<Map.Entry<String, Set<Integer>>> entries = new ArrayList<>(nameList.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));
    
        for (Map.Entry<String, Set<Integer>> entry : entries) {
            Set<Integer> currentSet = entry.getValue();
            if (nodeCount + currentSet.size() <= maxNodes) {
                sampledNameList.put(entry.getKey(), currentSet);
                nodeCount += currentSet.size();
            } else {
                Set<Integer> sampledSet = new HashSet<>();
                Iterator<Integer> iterator = currentSet.iterator();
                while (iterator.hasNext() && nodeCount < maxNodes) {
                    sampledSet.add(iterator.next());
                    nodeCount++;
                }
                sampledNameList.put(entry.getKey(), sampledSet);
                break;
            }
        }
    
        // Ensure the nameList is complete for the chosen nodes
        ensureCompleteNameList(sampledNameList, nameList);
    
        return sampledNameList;
    }
    
    private static void ensureCompleteNameList(Map<String, Set<Integer>> sampledNameList, Map<String, Set<Integer>> originalNameList) {
        for (String key : sampledNameList.keySet()) {
            if (!sampledNameList.get(key).equals(originalNameList.get(key))) {
                sampledNameList.put(key, originalNameList.get(key));
            }
        }
    }

    // Ensure the nameList is complete for the chosen nodes
    ensureCompleteNameList(sampledNameList, nameList);

    return sampledNameList;
}

private static void ensureCompleteNameList(Map<String, Set<Integer>> sampledNameList, Map<String, Set<Integer>> originalNameList) {
    for (String key : sampledNameList.keySet()) {
        if (!sampledNameList.get(key).equals(originalNameList.get(key))) {
            sampledNameList.put(key, originalNameList.get(key));
        }
    }
}
    }
}

