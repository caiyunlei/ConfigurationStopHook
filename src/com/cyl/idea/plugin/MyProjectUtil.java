package com.cyl.idea.plugin;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;

public class MyProjectUtil {
  private MyProjectUtil() {
  }

  public static Project getCurrentProject() {
    return ProjectUtil.getOpenProjects()[0];
  }
}
