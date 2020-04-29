package com.cyl.intellij.plugin.panels;

import com.cyl.intellij.plugin.settings.TasksSettings;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.compound.ConfigurationSelectionUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.UnknownRunConfiguration;
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeforeTerminalTasksPanel extends JPanel {
    private final JCheckBox myActivateToolWindowBeforeRunCheckBox;
    private final CollectionListModel<RunnerAndConfigurationSettings> myModel;
    private final List<RunnerAndConfigurationSettings> originalTasks = new SmartList<>();
    private final JPanel myPanel;
    private RunConfiguration myRunConfiguration;
    private final TasksSettings tasksSettings;
    private Project project;

    public BeforeTerminalTasksPanel(RunnerAndConfigurationSettings settings, Project project) {
        this.project = project;
        tasksSettings = TasksSettings.getInstance(this.project);

        myModel = new CollectionListModel<>();
        myModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                saveTasks();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                saveTasks();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
            }
        });

        JBList<RunnerAndConfigurationSettings> myList = new JBList<>(myModel);
        myList.getEmptyText().setText(ExecutionBundle.message("before.launch.panel.empty"));
        myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myList.setCellRenderer(new BeforeTerminalTasksPanel.MyListCellRenderer());
        myList.setVisibleRowCount(10);

        ToolbarDecorator myDecorator = ToolbarDecorator.createDecorator(myList);
        if (!SystemInfo.isMac) {
            myDecorator.setAsUsualTopToolbar();
        }
        myDecorator.setAddAction(this::doAddAction);
        myDecorator.setAddActionUpdater(e -> true);

        myPanel = myDecorator.createPanel();
        myDecorator.getActionsPanel().setCustomShortcuts(CommonActionsPanel.Buttons.EDIT,
                CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.EDIT),
                CommonShortcuts.DOUBLE_CLICK_1);

        setLayout(new BorderLayout());
        add(myPanel, BorderLayout.CENTER);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, JBUIScale.scale(5), JBUIScale.scale(5)));
        myActivateToolWindowBeforeRunCheckBox = new JCheckBox(ExecutionBundle.message("configuration.activate.toolwindow.before.run"));
        checkboxPanel.add(myActivateToolWindowBeforeRunCheckBox);
        add(checkboxPanel, BorderLayout.SOUTH);

        if (settings != null) {
            doReset(settings);
        }
    }

    private void saveTasks() {
        tasksSettings.updateTasks(myRunConfiguration, myModel.getItems());
    }

    void doReset(@NotNull RunnerAndConfigurationSettings settings) {
        myRunConfiguration = settings.getConfiguration();

        originalTasks.clear();
        originalTasks.addAll(tasksSettings.getBeforeTerminalTasks(myRunConfiguration));
        myModel.replaceAll(originalTasks);
        myActivateToolWindowBeforeRunCheckBox.setSelected(settings.isActivateToolWindowBeforeRun());
        myActivateToolWindowBeforeRunCheckBox.setEnabled(!isUnknown());
        myPanel.setVisible(true);
    }

    private boolean isUnknown() {
        return myRunConfiguration instanceof UnknownRunConfiguration;
    }

    private void doAddAction(@NotNull AnActionButton button) {
        if (isUnknown()) {
            return;
        }

        ListPopup listPopup = null;
        Project project = myRunConfiguration.getProject();
        for (final BeforeRunTaskProvider<BeforeRunTask> provider : getBeforeRunTaskProviders()) {
            if (provider.createTask(myRunConfiguration) == null || provider.isSingleton()) {
                continue;
            }

            List<RunConfiguration> configurations = ContainerUtil.map(RunManagerImpl.getInstanceImpl(project).getAllSettings(),
                    RunnerAndConfigurationSettings::getConfiguration);
            RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
            listPopup = ConfigurationSelectionUtil.createPopup(project, runManager, configurations, (selectedConfigs, selectedTarget) -> {
                RunConfiguration selectedConfig = ContainerUtil.getFirstItem(selectedConfigs);
                RunnerAndConfigurationSettings selectedSettings = selectedConfig == null ? null : runManager.getSettings(selectedConfig);
                if (selectedSettings != null) {
                    myModel.add(selectedSettings);
                }
            });
        }

        listPopup.show(Objects.requireNonNull(button.getPreferredPopupPoint()));
    }

    @NotNull
    private List<BeforeRunTaskProvider<BeforeRunTask>> getBeforeRunTaskProviders() {
        //todo:change to get runSettings
        List<BeforeRunTaskProvider<BeforeRunTask>> extensionList =
                BeforeRunTaskProvider.EXTENSION_POINT_NAME.getExtensionList(myRunConfiguration.getProject());
        return extensionList.stream().filter(anotherConfiguration()).collect(Collectors.toList());
    }

    @NotNull
    private Predicate<BeforeRunTaskProvider<BeforeRunTask>> anotherConfiguration() {
        return RunConfigurationBeforeRunProvider.class::isInstance;
    }

    private class MyListCellRenderer extends JBList.StripedListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof BeforeRunTask) {
                BeforeRunTask task = (BeforeRunTask) value;
                @SuppressWarnings("unchecked")
                BeforeRunTaskProvider<BeforeRunTask> provider =
                    BeforeRunTaskProvider.getProvider(myRunConfiguration.getProject(),
                        task.getProviderId());
                if (provider != null) {
                    Icon icon = provider.getTaskIcon(task);
                    setIcon(icon != null ? icon : provider.getIcon());
                    setText(provider.getDescription(task));
                }
            }
            return this;
        }
    }
}
