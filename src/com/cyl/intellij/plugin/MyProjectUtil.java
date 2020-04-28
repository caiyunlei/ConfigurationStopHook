package com.cyl.intellij.plugin;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;

public class MyProjectUtil {
  private MyProjectUtil() {
  }

  public static Project getCurrentProject() {
    Project[] openProjects = ProjectUtil.getOpenProjects();
    if (openProjects.length > 0) {
      return openProjects[0];
    } else {
      return null;
    }
  }
}
