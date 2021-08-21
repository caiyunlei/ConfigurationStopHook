package com.cyl.intellij.plugin;

import com.cyl.intellij.plugin.settings.TasksSettings;
import com.intellij.application.Topics;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StopActionListener implements AnActionListener, Disposable {
    private static final String STOP_ACTION_ID = "Stop";

    public StopActionListener() {
        Topics.subscribe(AnActionListener.TOPIC, this, this);
    }

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
        if (isStopAction(action)) {
            runTasks(event.getProject());
        }
    }

    private boolean isStopAction(AnAction action) {
        String myIdeaActionID = ActionManager.getInstance().getId(action);
        return STOP_ACTION_ID.equals(myIdeaActionID);
    }

    private static void runTasks(Project p) {
        RunnerAndConfigurationSettings stopSettings = RunManager.getInstance(p).getSelectedConfiguration();
        RunConfiguration runConfiguration = stopSettings.getConfiguration();
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();

        var beforeRunConfigurations = TasksSettings.getInstance(p).getBeforeTerminalTasks(runConfiguration);
        for (var runnerAndConfigurationSettings : beforeRunConfigurations) {
            if (runnerAndConfigurationSettings != null) {
                ExecutionUtil.runConfiguration(runnerAndConfigurationSettings, executor);
            }
        }
    }

    @Override
    public void dispose() {
    }
}
