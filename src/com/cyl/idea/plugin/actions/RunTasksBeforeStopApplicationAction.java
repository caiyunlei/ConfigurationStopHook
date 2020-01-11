package com.cyl.idea.plugin.actions;

import com.cyl.idea.plugin.settings.TasksSettings;
import com.intellij.execution.*;
import com.intellij.execution.actions.StopAction;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RunTasksBeforeStopApplicationAction extends StopAction {
    protected static final Logger LOG = Logger.getInstance(RunTasksBeforeStopApplicationAction.class);


    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project myProject = e.getProject();
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = RunManager.getInstance(myProject).getSelectedConfiguration();
        ExecutionEnvironment environment = createEnvironmentBuilder(myProject, runnerAndConfigurationSettings).build();
        RunConfiguration runConfiguration = (RunConfiguration) environment.getRunProfile();
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        List<BeforeRunTask<?>> beforeRunTasks = TasksSettings.getBeforeTerminalTasks(runConfiguration);

        for (BeforeRunTask<?> beforeRunTask : beforeRunTasks) {
            if (beforeRunTask instanceof RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask) {
                RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask runBeforeRun =
                        (RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask) beforeRunTask;
                RunnerAndConfigurationSettings settings = runBeforeRun.getSettings();
                ExecutionUtil.runConfiguration(settings, executor);
            }
        }

        super.actionPerformed(e);
    }

    @NotNull
    private static ExecutionEnvironmentBuilder createEnvironmentBuilder(Project project,@Nullable RunnerAndConfigurationSettings configuration) {
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        ExecutionEnvironmentBuilder builder = new ExecutionEnvironmentBuilder(project, executor);

        ProgramRunner<?> runner = ProgramRunnerUtil.getRunner(executor.getId(), configuration);
        if (runner == null && configuration != null) {
            LOG.error("Cannot find runner for " + configuration.getName());
        }
        else if (runner != null) {
            builder.runnerAndSettings(runner, configuration);
        }
        return builder;
    }
}
