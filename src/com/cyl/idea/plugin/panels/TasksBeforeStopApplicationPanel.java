package com.cyl.idea.plugin.panels;

import com.cyl.idea.plugin.settings.TasksSettings;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.UnknownRunConfiguration;
import com.intellij.execution.impl.RunConfigurationBeforeRunProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.SmartList;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TasksBeforeStopApplicationPanel extends JPanel {
    private final JCheckBox myShowSettingsBeforeRunCheckBox;
    private final JCheckBox myActivateToolWindowBeforeRunCheckBox;
    private final JBList<BeforeRunTask<?>> myList;
    private final CollectionListModel<BeforeRunTask<?>> myModel;
    private final List<BeforeRunTask<?>> originalTasks = new SmartList<>();
    private final JPanel myPanel;
    private final Set<BeforeRunTask<?>> clonedTasks = new THashSet<>();
    private RunConfiguration myRunConfiguration;

    public TasksBeforeStopApplicationPanel(RunnerAndConfigurationSettings settings) {
        myModel = new CollectionListModel<>();
        myList = new JBList<>(myModel);
        myList.getEmptyText().setText(ExecutionBundle.message("before.launch.panel.empty"));
        myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myList.setCellRenderer(new TasksBeforeStopApplicationPanel.MyListCellRenderer());
        myList.setVisibleRowCount(4);

        myModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                updateText();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateText();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
            }
        });

        ToolbarDecorator myDecorator = ToolbarDecorator.createDecorator(myList);
        if (!SystemInfo.isMac) {
            myDecorator.setAsUsualTopToolbar();
        }

        myDecorator.setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton button) {
            }
        });
        //noinspection Convert2Lambda
        myDecorator.setEditActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(@NotNull AnActionEvent e) {
                return false;
            }
        });
        myDecorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton button) {
                doAddAction(button);
            }
        });

        myDecorator.setAddActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(@NotNull AnActionEvent e) {
                return true;
            }
        });

        myShowSettingsBeforeRunCheckBox = new JCheckBox(ExecutionBundle.message("configuration.edit.before.run"));
        myShowSettingsBeforeRunCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateText();
            }
        });
        myActivateToolWindowBeforeRunCheckBox = new JCheckBox(ExecutionBundle.message("configuration.activate.toolwindow.before.run"));
        myActivateToolWindowBeforeRunCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateText();
            }
        });

        myPanel = myDecorator.createPanel();
        myDecorator.getActionsPanel().setCustomShortcuts(CommonActionsPanel.Buttons.EDIT,
                CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.EDIT),
                CommonShortcuts.DOUBLE_CLICK_1);


        setLayout(new BorderLayout());
        add(myPanel, BorderLayout.CENTER);
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, JBUIScale.scale(5), JBUIScale.scale(5)));
        checkboxPanel.add(myShowSettingsBeforeRunCheckBox);
        checkboxPanel.add(myActivateToolWindowBeforeRunCheckBox);
        add(checkboxPanel, BorderLayout.SOUTH);
        if (settings != null) {
            doReset(settings);
        }
    }

    void doReset(@NotNull RunnerAndConfigurationSettings settings) {
        clonedTasks.clear();

        myRunConfiguration = settings.getConfiguration();

        originalTasks.clear();
        originalTasks.addAll(TasksSettings.getBeforeTerminalTasks(myRunConfiguration));
        myModel.replaceAll(originalTasks);
        myShowSettingsBeforeRunCheckBox.setSelected(settings.isEditBeforeRun());
        myShowSettingsBeforeRunCheckBox.setEnabled(!isUnknown());
        myActivateToolWindowBeforeRunCheckBox.setSelected(settings.isActivateToolWindowBeforeRun());
        myActivateToolWindowBeforeRunCheckBox.setEnabled(!isUnknown());
        myPanel.setVisible(checkBeforeRunTasksAbility(false));
        updateText();
    }

    private void updateText() {
        StringBuilder sb = new StringBuilder();

        if (myShowSettingsBeforeRunCheckBox.isSelected()) {
            sb.append(ExecutionBundle.message("configuration.edit.before.run"));
        }

        List<BeforeRunTask<?>> tasks = myModel.getItems();
        if (!tasks.isEmpty()) {
            LinkedHashMap<BeforeRunTaskProvider<?>, Integer> counter = new LinkedHashMap<>();
            for (BeforeRunTask<?> task : tasks) {
                //noinspection unchecked
                BeforeRunTaskProvider<BeforeRunTask> provider = BeforeRunTaskProvider.getProvider(myRunConfiguration.getProject(), (Key<BeforeRunTask>) task.getProviderId());
                if (provider != null) {
                    Integer count = counter.get(provider);
                    if (count == null) {
                        count = task.getItemsCount();
                    } else {
                        count += task.getItemsCount();
                    }
                    counter.put(provider, count);
                }
            }
            for (Map.Entry<BeforeRunTaskProvider<?>, Integer> entry : counter.entrySet()) {
                BeforeRunTaskProvider provider = entry.getKey();
                String name = provider.getName();
                name = StringUtil.trimStart(name, "Run ");
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(name);
                if (entry.getValue() > 1) {
                    sb.append(" (").append(entry.getValue().intValue()).append(")");
                }
            }
        }

        if (myActivateToolWindowBeforeRunCheckBox.isSelected()) {
            sb.append(sb.length() > 0 ? ", " : "").append(ExecutionBundle.message("configuration.activate.toolwindow.before.run"));
        }
        if (sb.length() > 0) {
            sb.insert(0, ": ");
        }
        sb.insert(0, ExecutionBundle.message("before.launch.panel.title"));
//        myListener.titleChanged(sb.toString());
    }

    @NotNull
    public List<BeforeRunTask<?>> getTasks() {
        List<BeforeRunTask<?>> items = myModel.getItems();
        return items.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(items);
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
        DefaultActionGroup actionGroup = new DefaultActionGroup(null, false);
        for (final BeforeRunTaskProvider<BeforeRunTask> provider : getBeforeRunTaskProviders()) {
            if (provider.createTask(myRunConfiguration) == null || activeProviderKeys.contains(provider.getId()) && provider.isSingleton()) {
                continue;
            }

            actionGroup.add(new AnAction(provider.getName(), null, provider.getIcon()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    BeforeRunTask task = provider.createTask(myRunConfiguration);
                    if (task == null) {
                        return;
                    }

                    provider.configureTask(button.getDataContext(), myRunConfiguration, task)
                            .onSuccess(changed -> {
                                if (!provider.canExecuteTask(myRunConfiguration, task)) {
                                    return;
                                }
                                task.setEnabled(true);

                                addTask(task);
                            });
                }
            });
        }
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(ExecutionBundle.message("add.new.run.configuration.action2.name"), actionGroup,
                SimpleDataContext.getProjectContext(myRunConfiguration.getProject()), false, false, false, null,
                -1, Conditions.alwaysTrue());
        popup.show(Objects.requireNonNull(button.getPreferredPopupPoint()));
    }

    @NotNull
    private List<BeforeRunTaskProvider<BeforeRunTask>> getBeforeRunTaskProviders() {
        List<BeforeRunTaskProvider<BeforeRunTask>> extensionList = BeforeRunTaskProvider.EXTENSION_POINT_NAME.getExtensionList(myRunConfiguration.getProject());
        return extensionList.stream().filter(anotherConfiguration()).collect(Collectors.toList());
    }

    @NotNull
    private Predicate<BeforeRunTaskProvider<BeforeRunTask>> anotherConfiguration() {
        return RunConfigurationBeforeRunProvider.class::isInstance;
    }

    public void addTask(@NotNull BeforeRunTask task) {
        myModel.add(task);
        TasksSettings.addNewTask(myRunConfiguration, task);
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
