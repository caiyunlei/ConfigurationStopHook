package com.cyl.intellij.plugin.settings;

import com.cyl.intellij.plugin.panels.StopHookTasksPanel;
import com.cyl.intellij.plugin.panels.TreeCellRender;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SettingPage implements Configurable {
    private static final String CONFIG_PAGE_DISPLAY_NAME = "ConfigurationStopHook";
    private JPanel myWholePanel;
    private Tree myRunConfigurationsTree;
    private JPanel myRightPanel;
    private Project project;

    public SettingPage(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return CONFIG_PAGE_DISPLAY_NAME;
    }

    @Override
    public JComponent createComponent() {
        myRunConfigurationsTree = new Tree();
        myRunConfigurationsTree.setShowsRootHandles(true);
        RunManagerImpl runManager = getRunManager();
        myRunConfigurationsTree.setCellRenderer(new TreeCellRender(runManager));

        myRightPanel = new JPanel(new BorderLayout());

        JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, myRunConfigurationsTree, myRightPanel);
        splitPanel.setBackground(JBColor.WHITE);
        splitPanel.setDividerLocation(250);

        myWholePanel = new JPanel();
        myWholePanel.setLayout(new BorderLayout());
        myWholePanel.add(splitPanel);
        initTree();
        return myWholePanel;
    }

    private void initTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        myRunConfigurationsTree.setModel(new DefaultTreeModel(root));

        buildTreeNode(root);

        myRunConfigurationsTree.expandRow(0);
        myRunConfigurationsTree.setRootVisible(false);
        TreeUtil.expandAll(myRunConfigurationsTree);

        myRunConfigurationsTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            //com/intellij/execution/impl/RunConfigurable.kt
            TreePath selectionPath = myRunConfigurationsTree.getSelectionPath();
            if (selectionPath != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                getSafeUserObject(node);
            }
        });
    }

    private void buildTreeNode(DefaultMutableTreeNode root) {
        //runConfigurable data come from here
        RunManagerImpl runManager = getRunManager();

        Map<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> runConfigurations =
                runManager.getConfigurationsGroupedByTypeAndFolder(true);

        for (Map.Entry<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> configurationTypeMapEntry :
                runConfigurations.entrySet()) {
            ConfigurationType type = configurationTypeMapEntry.getKey();
            Map<String, List<RunnerAndConfigurationSettings>> folderMap = configurationTypeMapEntry.getValue();

            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type);
            root.add(typeNode);
            for (Map.Entry<String, List<RunnerAndConfigurationSettings>> entry : folderMap.entrySet()) {
                DefaultMutableTreeNode node = null;
                String folder = entry.getKey();
                if (folder == null) {
                    node = typeNode;
                } else {
                    node = new DefaultMutableTreeNode(folder);
                    typeNode.add(node);
                }

                for (RunnerAndConfigurationSettings settings : entry.getValue()) {
                    node.add(new DefaultMutableTreeNode(settings));
                }
            }
        }
    }

    private RunManagerImpl getRunManager() {
        return (RunManagerImpl) RunManager.getInstance(project);
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
    }

    @Override
    public void reset() {
    }

    private Object getSafeUserObject(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof RunnerAndConfigurationSettingsImpl) {
            StopHookTasksPanel newRightPanel = new StopHookTasksPanel((RunnerAndConfigurationSettings) userObject, project);
            updateRightPanel(newRightPanel);
        }
        return userObject;
    }

    private void updateRightPanel(StopHookTasksPanel newRightPanel) {
        myRightPanel.removeAll();
        myRightPanel.add(newRightPanel, BorderLayout.CENTER);
    }
}
