package com.cyl.intellij.plugin.settings;

import com.cyl.intellij.plugin.MyRunConfigUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@State(name = "ConfigurationStopHook", storages = @Storage("ConfigurationStopHook.xml"))
public class TasksSettings implements PersistentStateComponent<TasksSettings> {
    public Map<String, List<String>> runConfigIdBeforeRunTaskIdMap = new HashMap<>();

    public Map<String, List<String>> getRunConfigIdBeforeRunTaskIdMap() {
        return runConfigIdBeforeRunTaskIdMap;
    }

    public TasksSettings() {
    }

    public static TasksSettings getInstance(Project project) {
        return ServiceManager.getService(project, TasksSettings.class);
    }

    public List<RunnerAndConfigurationSettings> getBeforeTerminalTasks(RunConfiguration settings) {
        List<String> taskNames = runConfigIdBeforeRunTaskIdMap.get(settings.getName());
        if (taskNames != null) {
            return taskNames.stream()
                    .map(taskName -> MyRunConfigUtil.getRunConfigurationByName(taskName, settings.getProject()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void updateTasks(RunConfiguration settings, List<RunnerAndConfigurationSettings> tasks) {
        List<String> taskNames = extraTaskNames(tasks);
        runConfigIdBeforeRunTaskIdMap.put(settings.getName(), taskNames);
    }

    private List<String> extraTaskNames(List<RunnerAndConfigurationSettings> tasks) {
        return tasks.stream()
                .filter(Objects::nonNull)
            .map(RunnerAndConfigurationSettings::getName)
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public TasksSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TasksSettings state) {
        //todo:optimize
        runConfigIdBeforeRunTaskIdMap = state.getRunConfigIdBeforeRunTaskIdMap();
    }
}
