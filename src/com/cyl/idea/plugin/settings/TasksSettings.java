package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.MyProjectUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "BeforeTerminalTasksConfiguration", storages = @Storage("BeforeTerminalTasks.xml"))
public class TasksSettings implements PersistentStateComponent<TasksSettings> {
    public Map<String, List<String>> runConfigIdBeforeRunTaskIdMap = new HashMap<>();
    private Map<RunConfiguration,
        List<RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask>> beforeTerminalTasks = new HashMap<>();

    public Map<String, List<String>> getRunConfigIdBeforeRunTaskIdMap() {
        return runConfigIdBeforeRunTaskIdMap;
    }

    public TasksSettings() {
    }

    public static TasksSettings getInstance() {
        Project project = MyProjectUtil.getCurrentProject();
        return ServiceManager.getService(project, TasksSettings.class);
    }

    public List<RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask> getBeforeTerminalTasks(RunConfiguration settings) {
        return beforeTerminalTasks.getOrDefault(settings, Lists.newArrayList());
    }

    public void updateTasks(RunConfiguration settings,
        List<RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask> tasks) {
        List<String> taskNames = extraTaskNames(tasks);
        runConfigIdBeforeRunTaskIdMap.put(settings.getName(), taskNames);
        beforeTerminalTasks.put(settings, tasks);
    }

    private List<String> extraTaskNames(List<RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask> tasks) {
        return tasks.stream()
            .map(beforeRunTask -> beforeRunTask.getSettingsWithTarget().getFirst().getName())
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public TasksSettings getState() {
        System.out.println("get state");
        return this;
    }

    @Override
    public void loadState(@NotNull TasksSettings state) {
        runConfigIdBeforeRunTaskIdMap = state.getRunConfigIdBeforeRunTaskIdMap();
    }
}
