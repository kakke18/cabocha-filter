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

/* Update:
 * (2016/12/07)演習中にUnmappableCharacterExceptinoが発生していたので、無視するようにプログラムを更新しました。
 * (2015/12/03)久しぶりに更新しました。Java 8に対応しました。Streamを返すBufferedReaderのlines()はJava 8からの対応になりますので、このプログラムはJava 8でないと動きません。（ちなみに、AutoCloseableはJava 7から）
 * Java 8の日本語のJavadocはこちら https://docs.oracle.com/javase/jp/8/docs/api/
 */
public class Task {
    private static void exec() {
        Path inputPath = Paths.get("読み込むファイルへのパス");
        Path outputPath = Paths.get("書き出すファイルへのパス");
        if (!inputPath.toFile().canRead()) {
            printExceptionMessage(inputPath.toFile(), true);
            return;
        }
        if (!outputPath.toFile().canWrite()) {
            printExceptionMessage(outputPath.toFile(), false);
            return;
        }
        CharsetDecoder decoder = Charset.forName("EUC-JP").newDecoder().// 文字コードがEUC-JPであるファイルを読み込みます。
                onUnmappableCharacter(CodingErrorAction.IGNORE).// Unmappable characterを無視します。
                onMalformedInput(CodingErrorAction.IGNORE);// Malformed inputを無視します。
        try (Reader r = Channels.newReader(FileChannel.open(inputPath), decoder, -1);
                BufferedReader br = new BufferedReader(r);
                PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, // 文字コードがUTF-8となるようにファイルを書き出します。
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            Iterator<String> it = br.lines().iterator();
            while (it.hasNext()) {
                String line = it.next();
                System.out.println(line);
                /*
                 * 主な文字化けの原因：
                 * （１）JavaのStringやcharの文字コードはUTF-16BEです。サロゲートペアに注意し必要に応じてコードポイント（UTF-32BE）
                 * を使用してください。 参考：StringのcodePointCountメソッド、Java
                 * 8からのCharSequenceインターフェースのcodePointsメソッド
                 * （２）Javaのソースコードのファイルの文字コード（例：UTF-8）はJVMオプションで次のように指定してください。
                 * -Dfile.encoding=UTF-8
                 * （３）Unicodeには存在するがEUC-JPには存在しない文字や、波線と全角チルダなど、文字コード間で非互換な文字に注意してください。
                 *
                 * 読み込み時の謎のエラーの可能性のある原因：
                 * UTF-8のファイルを読み込む時、ファイルの先頭にBOMがついている可能性があります。読み込み時に問題が発生する場合はBOMを削除してみてください。
                 */
                pw.println("ファイルに書き出します。");
                /*
                 *
                 * 文字列操作TIPS (1)Stringクラスのメソッドを使う方法 rf.
                 * http://docs.oracle.com/javase/jp/8/api/java/lang/String.html
                 *
                 * ⅰ.split() splitメソッドは、String型の引数であるトークンによって、そのStringを分割し、String配列を返すメソッドです。
                 * String[] array = line.split("トークン"); 例えば、次のように書くと、arrayという配列の外延が{"モンキー", "D",
                 * "ルフィー"}になります。 String str = "モンキー・D・ルフィー"; String[] array = str.split("・");
                 * なお、StringTokenizerクラスで同様の操作ができますが、そちらは現在非推奨になっています。
                 *
                 * ⅱ.endsWith()/startsWith()
                 * endsWithメソッドは、String型の引数によって、そのStringが終わっているかを判定し、booleanを返すメソッドです。 boolean
                 * denden_6 = "denden_6".endsWith("6");//"denden_6"が6で終わっているので、trueです。 boolean
                 * denden6_lab =
                 * "denden6_lab".endsWith("6");//"denden6_lab"が6で終わっていないので、falseです。
                 *
                 * ⅲ.substring()
                 * substringメソッドは、Int型の引数によって指定される、そのStringのインデックス番号の範囲を取り出して、Stringを返すメソッドです。
                 * String saying = "てかLINEやってる？笑"; //"て"がインデックス番号0、 //"か"がインデックス番号1、
                 * //"L"がインデックス番号2、以下同様。 String result = saying.substring(2, 6);//"LINE"になります。
                 *
                 * ⅳ.他にもStringクラスにはequals, length, charAt, indexOfなどハンディーなメソッドがあるのでぜひ調べましょう。
                 *
                 * (2)正規表現(Regex, Regular Expression)を使う方法 rf.
                 * http://docs.oracle.com/javase/jp/8/api/java/util/regex/Pattern.html rf.
                 * http://docs.oracle.com/javase/jp/8/api/java/util/regex/Matcher.html
                 * 詳しくは、個々で調べましょう。使い方の具体例と注意事項だけ書いておきます。 Pattern p = Pattern.compile("正規表現");
                 * Matcher m = p.matcher(line); if (m.find()) {//もし、lineが正規表現にマッチしたら
                 *
                 * } Javaの正規表現のエスケープシーケンスは\記号が２つ必要です。例えば、改行文字\nを正規表現で使いたい場合は、\\nと書く必要があります。
                 *
                 */
            }
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
        Task.exec();
    }
}
