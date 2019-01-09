import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter1 {
    private static void exec() {
        Path inputPath = Paths.get("doc/input.txt");
        Path outputPath = Paths.get("doc/output1.txt");

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

        CharsetDecoder decoder = Charset.forName("EUC-JP").newDecoder().onUnmappableCharacter(CodingErrorAction.IGNORE)
                .onMalformedInput(CodingErrorAction.IGNORE);

        Pattern headerP = Pattern.compile("^\\* ([0-9]+) (\\-??[0-9]+)[D|O]");
        boolean isFirst = true;

        try (Reader r = Channels.newReader(FileChannel.open(inputPath), decoder, -1);
                BufferedReader br = new BufferedReader(r);
                PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            Iterator<String> it = br.lines().iterator();
            while (it.hasNext()) {
                String line = it.next();

                if (line.equals("EOS")) {
                    pw.println();
                    pw.printf(line);
                    continue;
                }

                Matcher matchHeader = headerP.matcher(line);
                if (matchHeader.find()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        pw.println();
                    }
                    pw.printf("%s %s ", matchHeader.group(1), matchHeader.group(2));
                } else {
                    pw.printf("%s", line.split("\t")[0]);
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
        Filter1.exec();
    }
}
