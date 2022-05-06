package itmo.vkr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Collector {

    private final String generatedPath = "E:\\ITMO\\ВКР\\Code\\crud-generator\\generator-xtend-model\\target\\generated-sources\\xtend\\generated";

    private final String finalPath = "E:\\ITMO\\ВКР\\Code\\crud-generator\\main-service\\src\\main\\java\\generated";

    public boolean isGenerated() {
        return isFileExist(generatedPath);
    }

    private boolean isFileExist(String path) {
        return new File(path).exists();
    }

    public void collectSources(){
        moveRepositories();
    }


    public void moveRepositories() {
        try {
            if (isGenerated() || isFileExist(finalPath))
                Files.move(Path.of(generatedPath), Path.of(finalPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void moveRepositories() {
//        try (DirectoryStream<Path> files = Files.newDirectoryStream(Path.of(xtendDir.getAbsolutePath()))) {
//            for (Path srcFile : files) {
//                System.out.println(srcFile.getFileName());
//                if (xtendDir.exists() && new File(destinationDir).exists()) {
//                    if (srcFile.getFileName().toString().endsWith("Repository.java"))
//                        Files.move(srcFile, Path.of(repoDestPath + srcFile.getFileName()));
//                    else if (srcFile.getFileName().toString().endsWith("Service.java"))
//                        Files.move(srcFile, Path.of(serviceDestPath + srcFile.getFileName()));
//                    else if (srcFile.getFileName().toString().endsWith("Controller.java"))
//                        Files.move(srcFile, Path.of(controllerDestPath + srcFile.getFileName()));
//                    else Files.move(srcFile, Path.of(entityDestPath + srcFile.getFileName()));
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
