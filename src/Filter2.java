import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter2 {
    private static void exec() {
        Path inputPath = Paths.get("doc/output1.txt");
        Path outputPath = Paths.get("doc/output2.txt");

        if (!inputPath.toFile().canRead()) {
            printExceptionMessage(inputPath.toFile(), true);
            return;
        }
        if (!outputPath.toFile().exists()) {
            try {
                outputPath.toFile().createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (!outputPath.toFile().canWrite()) {
            printExceptionMessage(outputPath.toFile(), false);
            return;
        }

        Pattern inLineP = Pattern.compile("^([0-9]+) \\-??[0-9]+ .+");
        String splits[] = new String[100];
        int splitCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(new File("doc/output1.txt")));
                PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            Iterator<String> it = br.lines().iterator();

            while (it.hasNext()) {
                String line = it.next();

                if (line.equals("EOS")) {
                    for (int i = 0; i < splitCount - 1; i++) {
                        String src[] = splits[i].split(" ");
                        String dst[] = splits[Integer.valueOf(src[1])].split(" ");
                        pw.printf("%s %s\n", src[2], dst[2]);
                    }
                    splitCount = 0;
                    pw.printf(line);
                    pw.println();
                    continue;
                }

                Matcher matchInLine = inLineP.matcher(line);
                if (matchInLine.find()) {
                    int num = Integer.valueOf(matchInLine.group(1));
                    splits[num] = line;
                    splitCount = num + 1;
                } else {
                    System.err.println("error");
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("指定したパスにファイルが存在しません。");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.out.println("文字コードがサポートされていません。");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPrefix(boolean isInputFile) {
        if (isInputFile) {
            return "入力ファイル：";
        } else {
            return "出力ファイル：";
        }
    }

    private static void printExceptionMessage(File file, boolean isInputFile) {
        String prefix = getPrefix(isInputFile);
        StringBuilder builder = new StringBuilder();
        if (!file.exists()) {
            builder.append("%1$s指定したパスにファイルが存在しません。%n");
        } else if (!file.isFile()) {
            builder.append("%1$s指定したパスにあるものはファイルではありません。%n");
            if (file.isDirectory()) {
                builder.append("%1$sディレクトリです。%n");
            } else {
                builder.append("%1$sディレクトリでもありません。%n");
            }
        } else {
            builder.append("%1$s指定したパスにあるファイルは読み込みできません。%n");
        }
        builder.append("%2$s%n");
        System.out.printf(builder.toString(), prefix, file.getPath());
    }

    public static void main(String[] args) {
        Filter2.exec();
    }
}
