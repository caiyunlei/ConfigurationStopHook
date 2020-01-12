package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.MyProjectUtil;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "BeforeTerminalTasksConfiguration", storages = @Storage("BeforeTerminalTasks.xml"))
public class TasksSettings implements PersistentStateComponent<TasksSettings> {
    private Map<RunConfiguration, List<BeforeRunTask<?>>> beforeTerminalTasks = new HashMap<>();
    private boolean modified;

    public TasksSettings() {
    }

    public static TasksSettings getInstance() {
        Project project = MyProjectUtil.getCurrentProject();
        return ServiceManager.getService(project, TasksSettings.class);
    }

    public Map<RunConfiguration, List<BeforeRunTask<?>>> getBeforeTerminalTasks() {
        return beforeTerminalTasks;
    }

    public List<BeforeRunTask<?>> getBeforeTerminalTasks(RunConfiguration settings) {
        return beforeTerminalTasks.getOrDefault(settings, Lists.newArrayList());
    }

    public void updateTasks(RunConfiguration settings, List<BeforeRunTask<?>> tasks) {
        modified = true;
        beforeTerminalTasks.put(settings, tasks);
    }

    @Nullable
    @Override
    public TasksSettings getState() {
        System.out.println("get state");
        return this;
    }

    @Override
    public void loadState(@NotNull TasksSettings state) {
        modified = false;
        beforeTerminalTasks = state.getBeforeTerminalTasks();
    }

    public boolean modified() {
        return modified;
    }
}
