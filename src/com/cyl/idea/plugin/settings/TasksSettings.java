package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.MyProjectUtil;
import com.cyl.idea.plugin.MyRunConfigUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "BeforeTerminalTasksConfiguration", storages = @Storage("BeforeTerminalTasks.xml"))
public class TasksSettings implements PersistentStateComponent<TasksSettings> {
    public Map<String, List<String>> runConfigIdBeforeRunTaskIdMap = new HashMap<>();
    private Map<RunConfiguration,
        List<RunnerAndConfigurationSettings>> beforeTerminalTasks = new HashMap<>();

    public Map<String, List<String>> getRunConfigIdBeforeRunTaskIdMap() {
        return runConfigIdBeforeRunTaskIdMap;
    }

    public TasksSettings() {
    }

    public static TasksSettings getInstance() {
        Project project = MyProjectUtil.getCurrentProject();
        return ServiceManager.getService(project, TasksSettings.class);
    }

    public List<RunnerAndConfigurationSettings> getBeforeTerminalTasks(RunConfiguration settings) {
        List<String> taskNames = runConfigIdBeforeRunTaskIdMap.get(settings.getName());
        if (taskNames != null) {
            return taskNames.stream().map(MyRunConfigUtil::getRunConfigurationByName).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void updateTasks(RunConfiguration settings,
        List<RunnerAndConfigurationSettings> tasks) {
        List<String> taskNames = extraTaskNames(tasks);
        runConfigIdBeforeRunTaskIdMap.put(settings.getName(), taskNames);
    }

    private List<String> extraTaskNames(List<RunnerAndConfigurationSettings> tasks) {
        return tasks.stream()
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
        runConfigIdBeforeRunTaskIdMap = state.getRunConfigIdBeforeRunTaskIdMap();
    }
}
