package online.githuboy.idea.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author suchu
 * @since 2019/7/6 20:12
 */
public class Test {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile(".*(libjrebel|jrebel32\\.dll|jrebel64\\.dll).*");
        String javaAgent = "agentpath:C:\\Users\\githuboy\\.IntelliJIdea2018.3\\system\\plugins-sandbox\\plugins\\jr-ide-idea\\lib\\jrebel6\\lib\\libjrebel64.so";
        Matcher matcher = pattern.matcher(javaAgent);
        System.out.println(matcher.matches());
    }
}
