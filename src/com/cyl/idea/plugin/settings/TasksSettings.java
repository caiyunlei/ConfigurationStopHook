package com.cyl.idea.plugin.settings;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.RunConfiguration;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TasksSettings {
    private static Map<RunConfiguration, List<BeforeRunTask<?>>> terminalTasksSettings = new ConcurrentHashMap<>();

    public static List<BeforeRunTask<?>> getBeforeTerminalTasks(RunConfiguration settings) {
        return terminalTasksSettings.getOrDefault(settings, Lists.newArrayList());
    }

    public static void updateTasks(RunConfiguration settings, List<BeforeRunTask<?>> tasks) {
        terminalTasksSettings.put(settings, tasks);
    }
}
