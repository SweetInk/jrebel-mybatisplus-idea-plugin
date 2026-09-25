package online.githuboy.idea.plugin.runner;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Patch the Java command line before JRebel running/debugging
 *
 * @author suchu
 * @since 2019/7/6 13:00
 */
public class PreRunPatcher extends JavaProgramPatcher {
    private static final Logger LOG = Logger.getInstance(PreRunPatcher.class);

    private static final Pattern JREBEL_NATIVE_AGENT_PAT =
            Pattern.compile(".*(libjrebel|jrebel32\\.dll|jrebel64\\.dll).*");

    private static final String JREBEL_EXTERNAL_PLUGIN_PROP = "rebel.plugins";
    private static final String JREBEL_MP_VERSION = "1.0.7";
    private static final String JREBEL_MP_NAME = "jr-mybatisplus";
    private static final String JREBEL_MP_SUFFIX = ".jar";

    private static final String JREBEL_MP_PLUGIN_ID = "jr-mp-ide-idea";

    @Override
    public void patchJavaParameters(
            Executor executor,
            RunProfile configuration,
            JavaParameters javaParameters) {

        try {
            String executorId = executor.getId();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "PreRunPatcher invoked, executor={}, configuration={}",
                        executorId,
                        configuration == null
                                ? "null"
                                : configuration.getClass().getName()
                );
            }
            if (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)
                    || DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
                if (isJRebelRunner()) {
                    patch(javaParameters);
                }
            } else if (hasJRebelArgs(javaParameters)) {
                patch(javaParameters);
            }
        } catch (Throwable e) {
            LOG.error("Failed to patch JRebel command line", e);
        }
    }

    /**
     * Add jr-mybatisplus runtime plugin to -Drebel.plugins
     */
    private void patch(JavaParameters javaParameters) {
        IdeaPluginDescriptor currentPlugin = getCurrentPlugin();
        if (currentPlugin == null) {
            LOG.error("Cannot find plugin: " + JREBEL_MP_PLUGIN_ID);
            return;
        }
        String pluginPath = currentPlugin.getPath().getAbsolutePath();
        String jrebelMpPlugin = new File(new File(pluginPath, "lib"), getJrebelMpFileName()).getAbsolutePath();
        File pluginFile = new File(jrebelMpPlugin);
        if (!pluginFile.isFile()) {
            LOG.error("JRebel MyBatisPlus runtime plugin not found: " + pluginFile.getAbsolutePath());
            return;
        }
        String plugins = javaParameters
                .getVMParametersList()
                .getPropertyValue(JREBEL_EXTERNAL_PLUGIN_PROP);
        if (plugins != null && plugins.contains(jrebelMpPlugin)) {
            LOG.debug("JRebel MyBatisPlus plugin already configured: " + jrebelMpPlugin);
            return;
        }

        if (plugins == null || plugins.isEmpty()) {
            plugins = jrebelMpPlugin;
        } else {
            plugins = plugins + "," + jrebelMpPlugin;
        }

        javaParameters.getVMParametersList()
                .addProperty(JREBEL_EXTERNAL_PLUGIN_PROP, plugins);
        LOG.info("Added JRebel MyBatisPlus plugin: " + jrebelMpPlugin);
    }

    /**
     * Get the jr-mp-ide-idea plugin descriptor
     */
    private IdeaPluginDescriptor getCurrentPlugin() {
        return PluginManager.getPlugin(
                PluginId.getId(JREBEL_MP_PLUGIN_ID)
        );
    }

    /**
     * Check whether Java parameters already contain JRebel arguments.
     */
    private boolean hasJRebelArgs(JavaParameters javaParameters) {
        String[] args = javaParameters
                .getVMParametersList()
                .getArray();
        for (String str : args) {
            if (str.startsWith("-javaagent:")
                    && (str.endsWith("jrebel.jar")
                    || str.endsWith("jrebel-bootstrap.jar"))) {
                return true;
            }
            if (str.startsWith("-agentpath:")
                    && JREBEL_NATIVE_AGENT_PAT.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the current call comes from JRebel runner/debug runner.
     */
    private boolean isJRebelRunner() {
        int maxStackDeep = 10;
        StackTraceElement[] stackTrace =
                Thread.currentThread().getStackTrace();
        int stackFrameIndex = 0;
        for (StackTraceElement element : stackTrace) {
            if (stackFrameIndex > maxStackDeep) {
                return false;
            }
            String className = element.getClassName();
            if ("com.zeroturnaround.javarebel.idea.plugin.runner.JRebelRunner"
                    .equals(className)
                    || "com.zeroturnaround.javarebel.idea.plugin.runner.JRebelDebugRunner"
                    .equals(className)) {
                return true;
            }
            stackFrameIndex++;
        }
        return false;
    }

    private String getJrebelMpFileName() {
        return String.format(
                "%s-%s%s",
                JREBEL_MP_NAME,
                JREBEL_MP_VERSION,
                JREBEL_MP_SUFFIX
        );
    }
}
