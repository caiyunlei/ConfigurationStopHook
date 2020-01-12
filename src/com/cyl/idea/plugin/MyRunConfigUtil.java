package com.cyl.idea.plugin;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.RunManagerImpl;
import java.util.List;

public class MyRunConfigUtil {
  public static RunnerAndConfigurationSettings getRunConfigurationByName(String name) {
    List<RunnerAndConfigurationSettings> settingsList = getAllRunConfigs();
    final RunnerAndConfigurationSettings settings =
        settingsList.stream().filter(runnerAndConfigurationSettings -> runnerAndConfigurationSettings.getName().equals(name)).findFirst().orElse(null);
    return settings;
  }

  public static List<RunnerAndConfigurationSettings> getAllRunConfigs() {
    final RunManagerImpl runManager =
        (RunManagerImpl) RunManagerImpl.getInstance(MyProjectUtil.getCurrentProject());
    return runManager.getAllSettings();
  }
}
