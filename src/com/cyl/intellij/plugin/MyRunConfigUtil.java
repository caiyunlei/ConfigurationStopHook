package com.cyl.intellij.plugin;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.impl.SingleConfigurationConfigurable;
import com.intellij.openapi.project.Project;

import java.util.List;

public class MyRunConfigUtil {

  public static RunnerAndConfigurationSettings getRunConfigurationByName(String name, Project project) {
    final RunManager runManager = RunManager.getInstance(project);
    List<RunnerAndConfigurationSettings> settingsList = runManager.getAllSettings();
    return settingsList.stream()
        .filter(runnerAndConfigurationSettings -> runnerAndConfigurationSettings.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public static String getUserObjectName(Object userObject) {
      if (userObject instanceof ConfigurationType) {
          return ((ConfigurationType) userObject).getDisplayName();
      } else if (userObject instanceof ConfigurationFactory) {
          return ((ConfigurationFactory) userObject).getName();
      } else if (userObject instanceof SingleConfigurationConfigurable) {
          return ((SingleConfigurationConfigurable) userObject).getNameText();
      } else if (userObject instanceof RunnerAndConfigurationSettingsImpl) {
          return ((RunnerAndConfigurationSettingsImpl) userObject).getName();
      } else {
          return userObject.toString();
      }
  }
}
