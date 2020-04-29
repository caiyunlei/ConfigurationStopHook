package com.cyl.intellij.plugin;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;

import java.util.List;

public class MyRunConfigUtil {
  public static RunnerAndConfigurationSettings getRunConfigurationByName(String name,
          Project project) {

    final RunManager runManager = RunManager.getInstance(project);
    List<RunnerAndConfigurationSettings> settingsList = runManager.getAllSettings();
    return settingsList.stream()
        .filter(runnerAndConfigurationSettings -> runnerAndConfigurationSettings.getName().equals(name))
        .findFirst()
        .orElse(null);
  }
}
