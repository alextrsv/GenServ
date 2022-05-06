package ru.jpoint.xtend.demo;

import java.io.*;

public class GeneratedSourceEditor {


    public static void main(String[] args) throws IOException {
        String fileName = "E:\\ITMO\\ВКР\\Code\\crud-generator\\generator\\src\\main\\resources\\Orders.java";

        String contents = readUsingBufferedReader(fileName);


        String annotationImport = "import org.eclipse.xtend.lib.annotations.EqualsHashCode\n" +
                "import org.eclipse.xtend.lib.annotations.ToString\n" +
                "import javax.persistence.JoinColumn\n" +
                "import ru.jpoint.xtend.demo.Entity\n" +
                "import org.eclipse.xtend.lib.annotations.Accessors\n\n\n";
        String annotation = "@Entity\n";

        StringBuilder builder = new StringBuilder(contents);

        builder.insert(builder.indexOf("import javax.xml.bind.annotation.XmlAccessType;"), "//");
        builder.insert(builder.indexOf("import javax.xml.bind.annotation.XmlAccessorType;"), "//");
        builder.insert(builder.indexOf("import javax.xml.bind.annotation.XmlElement;"), "//");
        builder.insert(builder.indexOf("import javax.xml.bind.annotation.XmlType;"), "//");

        while (builder.indexOf("@XmlElement(required = true)") != -1)
            builder.delete(builder.indexOf("@XmlElement(required = true)"),
                    builder.indexOf("@XmlElement(required = true)") + "@XmlElement(required = true)".length());


        builder.delete(builder.indexOf("/**"), builder.indexOf("public class"));
        builder.delete(builder.indexOf("/**"), builder.length());
        builder.insert(builder.length(), "}\n}\n");

        builder.insert(builder.indexOf("public class"), annotationImport);
        builder.insert(builder.indexOf("public class"), annotation);


        String result = builder.toString();
        System.out.println("===================================================");
        System.out.println(result);

        saveFile(fileName, builder.toString());



    }

    public static void saveFile(String fileName, String content){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName)))
        {
            bw.write(content);
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }

    private static String readUsingBufferedReader(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(fileName));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
