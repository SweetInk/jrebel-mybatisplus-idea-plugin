package online.githuboy.idea.plugin.runner;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Patch the Java command line before JRebel running/debugging
 *
 * @author suchu
 * @since 2019/7/6 13:00
 */
public class PreRunPatcher extends JavaProgramPatcher {
    private final static Pattern JREBEL_NATIVE_AGENT_PATTER = Pattern.compile(".*(libjrebel|jrebel32\\.dll|jrebel64\\.dll).*");
    private final static String JREBEL_EXTERNAL_PLUGIN_PROP = "rebel.plugins";
    private final static String JREBEL_MP_VERSION = "1.0.7";
    private final static String JREBEL_MP_NAME = "jr-mybatisplus";
    private final static String JREBEL_MP_SUFFIX = ".jar";
    private final static String JREBEL_MP_PLUGIN_ID = "jr-mp-ide-idea";
//    private static final ConsoleLog log = ConsoleLog.getInstance();

    private void dumpJavaParameters(StringBuilder stringBuilder, JavaParameters javaParameters) {
        String[] args = javaParameters.getVMParametersList().getArray();
        stringBuilder.append("Vm Params:\n");
        for (String arg : args) {
            stringBuilder.append(arg).append("\n");
        }
        @NotNull String[] programArgs = javaParameters.getProgramParametersList().getArray();
        stringBuilder.append("Program Params:\n");
        for (String arg : programArgs) {
            stringBuilder.append(arg).append("\n");
        }

    }

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("Dump Run/Debug Configuration info ----- start\n");
        builder.append("Current Executor Id:" + executor.getId() + "\n");
        if (DefaultRunExecutor.EXECUTOR_ID.equals(executor.getId()) || DefaultDebugExecutor.EXECUTOR_ID.equals(executor.getId())) {
            if (isJRebelRunner()) {
                patch(javaParameters);
            }
        } else {
            if (hasJRebelArgs(javaParameters)) {
                patch(javaParameters);
            }
        }
        dumpJavaParameters(builder, javaParameters);
        dumpStackTrace(builder);
        builder.append("Dump Run/Debug Configuration info ----- end\n");
//        log.info(builder.toString(), "");
        System.out.println("patchers");
    }

    /**
     * Patch the program parameter
     *
     * @param javaParameters JavaParameters
     */
    private void patch(JavaParameters javaParameters) {
        IdeaPluginDescriptor currentPlugin = getCurrentPlugin();
        if (null == currentPlugin) {
            return;
        }
        String pluginPath = currentPlugin.getPath().getAbsolutePath();

        String jrebelMpPlugin = pluginPath + File.separator + "lib" + File.separator + getJrebelMpFileName();
        String plugins = javaParameters.getVMParametersList().getPropertyValue(JREBEL_EXTERNAL_PLUGIN_PROP);
        if (!StringUtils.isEmpty(plugins)) {
            plugins += "," + jrebelMpPlugin;
        } else {
            plugins = jrebelMpPlugin;
        }
        javaParameters.getVMParametersList().addProperty(JREBEL_EXTERNAL_PLUGIN_PROP, plugins);
    }

    /**
     * Get the `jr-mp-ide-idea` plugin descriptor
     *
     * @return IdeaPluginDescriptor
     */
    private IdeaPluginDescriptor getCurrentPlugin() {
        return PluginManager.getPlugin(PluginId.getId(JREBEL_MP_PLUGIN_ID));
    }

    /**
     * check the program args contains the `JRebel` arguments
     *
     * @param javaParameters JavaParameters
     * @return true - contains, false - not have jrebel args
     */
    private boolean hasJRebelArgs(JavaParameters javaParameters) {
        String[] args = javaParameters.getVMParametersList().getArray();
        for (String str : args) {
            if (str.startsWith("-javaagent:") && (str.endsWith("jrebel.jar") || str.endsWith("jrebel-bootstrap.jar"))) {
                return true;
            }
            if (str.startsWith("-agentpath")) {
                if (JREBEL_NATIVE_AGENT_PATTER.matcher(str).matches()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Check the Java program runner is JRebelRunner/Debugger
     *
     * @return
     */
    private boolean isJRebelRunner() {
        int maxStackDeep = 10;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackFrameIndex = 0;
        for (StackTraceElement element : stackTrace) {
            if (stackFrameIndex > maxStackDeep) return false;
            String clzName = element.getClassName();
            if ("com.zeroturnaround.javarebel.idea.plugin.runner.JRebelRunner".equals(clzName) || "com.zeroturnaround.javarebel.idea.plugin.runner.JRebelDebugRunner".equals(clzName)) {
                return true;
            }
            stackFrameIndex++;
        }
        return false;
    }

    private void dumpStackTrace(StringBuilder stringBuilder) {
        stringBuilder.append("StackTrace:\n");
        int maxStackDeep = 10;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackFrameIndex = 0;
        for (StackTraceElement element : stackTrace) {
            if (stackFrameIndex > maxStackDeep) break;
            if (stackFrameIndex != 0)
                stringBuilder.append("\t");
            stringBuilder.append(element.getClassName() + "\n");
            stackFrameIndex++;
        }
    }

    private String getJrebelMpFileName() {
        return String.format("%s-%s%s", JREBEL_MP_NAME, JREBEL_MP_VERSION, JREBEL_MP_SUFFIX);
    }
}
