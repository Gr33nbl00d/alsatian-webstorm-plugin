package de.greenlood.alsatian.webstorm.plugin;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.*;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import de.greenlood.alsatian.webstorm.plugin.runconfig.AlsatianRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianRunProgramRunner extends GenericProgramRunner {

    public static final String RUNNER_ID = "Alsatian";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {

        return (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)) && profile instanceof AlsatianRunConfiguration;
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
        if (executionResult == null) {
            return null;
        }
        else {
            RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
            RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
            RerunTestsNotification.showRerunNotification(environment.getContentToReuse(), executionResult.getExecutionConsole());
            RerunTestsAction.register(descriptor);
            return descriptor;
        }
    }
}
