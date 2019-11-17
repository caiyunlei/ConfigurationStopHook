package com.cyl.idea.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.execution.actions.StopAction;
import org.jetbrains.annotations.NotNull;

public class RunTasksBeforeStopApplicationAction extends StopAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);
    }
}
