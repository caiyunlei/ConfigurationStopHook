package com.cyl.idea.plugin.panels;

import com.cyl.idea.plugin.settings.TasksSettings;
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jetbrains.annotations.NotNull;

public class BeforeTerminalTasksPanel extends JPanel {
    private final JCheckBox myActivateToolWindowBeforeRunCheckBox;
    private final JBList<BeforeRunTask<?>> myList;
    private final CollectionListModel<BeforeRunTask<?>> myModel;
    private final List<BeforeRunTask<?>> originalTasks = new SmartList<>();
    private final JPanel myPanel;
    private final Set<BeforeRunTask<?>> clonedTasks = new THashSet<>();
    private RunConfiguration myRunConfiguration;
    private final TasksSettings tasksSettings;

    public BeforeTerminalTasksPanel(RunnerAndConfigurationSettings settings) {
        tasksSettings = TasksSettings.getInstance();
        myModel = new CollectionListModel<>();
        myList = new JBList<>(myModel);
        myList.getEmptyText().setText(ExecutionBundle.message("before.launch.panel.empty"));
        myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myList.setCellRenderer(new BeforeTerminalTasksPanel.MyListCellRenderer());
        myList.setVisibleRowCount(10);

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

        myActivateToolWindowBeforeRunCheckBox = new JCheckBox(ExecutionBundle.message("configuration.activate.toolwindow.before.run"));

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, JBUIScale.scale(5), JBUIScale.scale(5)));
        checkboxPanel.add(myActivateToolWindowBeforeRunCheckBox);
        add(checkboxPanel, BorderLayout.SOUTH);
        if (settings != null) {
            doReset(settings);
        }
    }

    private void saveTasks() {
        List<BeforeRunTask<?>> items = myModel.getItems();
        tasksSettings.updateTasks(myRunConfiguration, items);
    }

    void doReset(@NotNull RunnerAndConfigurationSettings settings) {
        clonedTasks.clear();

        myRunConfiguration = settings.getConfiguration();

        originalTasks.clear();
        originalTasks.addAll(tasksSettings.getBeforeTerminalTasks(myRunConfiguration));
        myModel.replaceAll(originalTasks);
        myActivateToolWindowBeforeRunCheckBox.setSelected(settings.isActivateToolWindowBeforeRun());
        myActivateToolWindowBeforeRunCheckBox.setEnabled(!isUnknown());
        myPanel.setVisible(checkBeforeRunTasksAbility(false));
    }

    private boolean checkBeforeRunTasksAbility(boolean checkOnlyAddAction) {
        if (isUnknown()) {
            return false;
        }

        Set<Key> activeProviderKeys = getActiveProviderKeys();
        for (final BeforeRunTaskProvider<BeforeRunTask> provider : getBeforeRunTaskProviders()) {
            if (provider.createTask(myRunConfiguration) != null) {
                if (!checkOnlyAddAction) {
                    return true;
                } else if (!provider.isSingleton() || !activeProviderKeys.contains(provider.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isUnknown() {
        return myRunConfiguration instanceof UnknownRunConfiguration;
    }

    private void doAddAction(@NotNull AnActionButton button) {
        if (isUnknown()) {
            return;
        }

        Set<Key> activeProviderKeys = getActiveProviderKeys();
        ListPopup listPopup = null;
        Project project = myRunConfiguration.getProject();
        for (final BeforeRunTaskProvider<BeforeRunTask> provider : getBeforeRunTaskProviders()) {
            if (provider.createTask(myRunConfiguration) == null || activeProviderKeys.contains(provider.getId()) && provider.isSingleton()) {
                continue;
            }

            List<RunConfiguration> configurations = ContainerUtil.map(RunManagerImpl.getInstanceImpl(project).getAllSettings(),
                    RunnerAndConfigurationSettings::getConfiguration);
            RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
            listPopup = ConfigurationSelectionUtil.createPopup(project, runManager, configurations, (selectedConfigs, selectedTarget) -> {
                RunConfiguration selectedConfig = ContainerUtil.getFirstItem(selectedConfigs);
                RunnerAndConfigurationSettings selectedSettings = selectedConfig == null ? null : runManager.getSettings(selectedConfig);

                if (selectedSettings != null) {
                    RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask task =
                            (RunConfigurationBeforeRunProvider.RunConfigurableBeforeRunTask) provider.createTask(myRunConfiguration);

                    task.setSettingsWithTarget(selectedSettings, selectedTarget);
                    addTask(task);
                }
            });
        }

        listPopup.show(Objects.requireNonNull(button.getPreferredPopupPoint()));
    }

    @NotNull
    private List<BeforeRunTaskProvider<BeforeRunTask>> getBeforeRunTaskProviders() {
        List<BeforeRunTaskProvider<BeforeRunTask>> extensionList =
                BeforeRunTaskProvider.EXTENSION_POINT_NAME.getExtensionList(myRunConfiguration.getProject());
        return extensionList.stream().filter(anotherConfiguration()).collect(Collectors.toList());
    }

    @NotNull
    private Predicate<BeforeRunTaskProvider<BeforeRunTask>> anotherConfiguration() {
        return RunConfigurationBeforeRunProvider.class::isInstance;
    }

    public void addTask(@NotNull BeforeRunTask task) {
        myModel.add(task);
    }

    @NotNull
    private Set<Key> getActiveProviderKeys() {
        Set<Key> result = new THashSet<>();
        for (BeforeRunTask task : myModel.getItems()) {
            result.add(task.getProviderId());
        }
        return result;
    }

    private class MyListCellRenderer extends JBList.StripedListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof BeforeRunTask) {
                BeforeRunTask task = (BeforeRunTask) value;
                @SuppressWarnings("unchecked")
                BeforeRunTaskProvider<BeforeRunTask> provider = BeforeRunTaskProvider.getProvider(myRunConfiguration.getProject(), task.getProviderId());
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
