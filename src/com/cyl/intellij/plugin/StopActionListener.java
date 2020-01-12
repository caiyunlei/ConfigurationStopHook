package com.cyl.intellij.plugin;

import com.cyl.intellij.plugin.actions.BeforeTerminalTasksAction;
import com.intellij.application.Topics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import org.jetbrains.annotations.NotNull;

public class StopActionListener implements AnActionListener, Disposable {
    private static final String STOP_ACTION_ID = "Stop";

    public StopActionListener() {
        Topics.subscribe(AnActionListener.TOPIC, this, this);
    }

    @Override
    public void afterActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        if (isStopAction(action)) {
            BeforeTerminalTasksAction.runTasks(event.getProject());
        }
    }

    private boolean isStopAction(AnAction action) {
        String myIdeaActionID = ActionManager.getInstance().getId(action);
        return myIdeaActionID.equals(STOP_ACTION_ID);
    }

    @Override
    public void dispose() {
    }
}
