package com.cyl.intellij.plugin.actions;

import com.cyl.intellij.plugin.settings.TasksSettings;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.StopAction;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeforeTerminalTasksAction extends StopAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project myProject = e.getProject();

        RunnerAndConfigurationSettings stopSettings = RunManager.getInstance(myProject).getSelectedConfiguration();
        RunConfiguration runConfiguration = stopSettings.getConfiguration();
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();

        List<RunnerAndConfigurationSettings> beforeRunConfigurations = TasksSettings.getInstance().getBeforeTerminalTasks(runConfiguration);
        for (RunnerAndConfigurationSettings runnerAndConfigurationSettings : beforeRunConfigurations) {
            if (runnerAndConfigurationSettings != null) {
                ExecutionUtil.runConfiguration(runnerAndConfigurationSettings, executor);
            }
        }

        super.actionPerformed(e);
    }
}
