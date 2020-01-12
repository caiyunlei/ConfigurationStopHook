package com.cyl.idea.plugin.actions;

import com.cyl.idea.plugin.settings.TasksSettings;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.StopAction;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BeforeTerminalTasksAction extends StopAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project myProject = e.getProject();
        RunnerAndConfigurationSettings settings =
            RunManager.getInstance(myProject).getSelectedConfiguration();
        RunConfiguration runConfiguration = settings.getConfiguration();
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        List<RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask> beforeRunTasks =
            TasksSettings.getInstance().getBeforeTerminalTasks(runConfiguration);

        for (RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask beforeRunTask :
            beforeRunTasks) {
            if (beforeRunTask != null) {
                RunnerAndConfigurationSettings tasksSettings = beforeRunTask.getSettings();
                ExecutionUtil.runConfiguration(tasksSettings, executor);
            }
        }

        super.actionPerformed(e);
    }
}
