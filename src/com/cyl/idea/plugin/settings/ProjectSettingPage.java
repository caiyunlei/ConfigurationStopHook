package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.panels.TasksBeforeStopApplicationPanel;
import com.cyl.idea.plugin.panels.TreeCellRender;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class ProjectSettingPage implements Configurable {
    private JPanel myWholePanel;
    private Tree myConfigurationTree;
    private JPanel myRightPanel;
//    private PropertiesComponent myPropertiesComponent;

    public ProjectSettingPage(PropertiesComponent propertiesComponent) {
        myWholePanel = new JPanel();
        myWholePanel.setLayout(new BorderLayout());

        myRightPanel = new JPanel(new BorderLayout());

        myConfigurationTree = new Tree();
        myConfigurationTree.setShowsRootHandles(true);
        myConfigurationTree.setCellRenderer(new TreeCellRender(getRunManager()));

        JSplitPane splitPane = new JSplitPane(SwingConstants.VERTICAL, myConfigurationTree, myRightPanel);
        splitPane.setDividerLocation(250);

        myWholePanel.add(splitPane);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TasksBeforeStopApplication";
    }

    @Override
    public JComponent createComponent() {
        initTree();
        return myWholePanel;
    }

    private void initTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        myConfigurationTree.setModel(new DefaultTreeModel(root));
        RunManagerImpl runManager = getRunManager();

        //runConfigurable data come from here
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
        myConfigurationTree.expandRow(0);
        myConfigurationTree.setRootVisible(false);
        TreeUtil.expandAll(myConfigurationTree);

        myConfigurationTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            //com/intellij/execution/impl/RunConfigurable.kt
            TreePath selectionPath = myConfigurationTree.getSelectionPath();
            if (selectionPath != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                getSafeUserObject(node);
            }
        });
    }

    @NotNull
    private RunManagerImpl getRunManager() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        //todo:multiple projects
        return (RunManagerImpl) RunManagerImpl.getInstance(projects[0]);
    }

    @Override
    public boolean isModified() {
        return false;
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
            TasksBeforeStopApplicationPanel newRightPanel = new TasksBeforeStopApplicationPanel((RunnerAndConfigurationSettings) userObject);
            updateRightPanel(newRightPanel);
        }
        return userObject;
    }

    private void updateRightPanel(TasksBeforeStopApplicationPanel newRightPanel) {
        myRightPanel.removeAll();
        myRightPanel.add(newRightPanel, BorderLayout.CENTER);
    }
}
